package ir.ac.ut.ece.ie.dynamiccontentserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.StringTokenizer;

import ir.ac.ut.ece.ie.dynamiccontentserver.ArticleService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "ir.ac.ut.ece.ie.repository")
@EntityScan(basePackages = "ir.ac.ut.ece.ie.model")
public class DynamicContentServer {

    private static final int PORT = 9092;
    public void start(Router router) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                try (Socket socket = serverSocket.accept();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    String requestLine = reader.readLine();
                    if (requestLine == null) {
                        continue;
                    }

                    String method = requestLine.split(" ")[0];
                    if ("OPTIONS".equals(method)) {
                        writeCorsPreflightResponse(socket);
                        continue;
                    }
                    HashMap<String, String> params = new HashMap<>();
                    String pagePath = getPageName(requestLine, params);

                    if (pagePath.startsWith("/static/")) {
                        handleStaticRequest(socket, pagePath);
                        continue;
                    }

                    int contentLength = readContentLength(reader);
                    if ("POST".equals(method) && contentLength > 0) {
                        readRequestBody(reader, contentLength, params);
                    }

                    Router.RouteMatch routeMatch = router.resolve(method, pagePath);
                    if (routeMatch == null) {
                        writeJsonResponse(socket, 404, JsonUtils.error("Not Found"));
                        continue;
                    }

                    params.putAll(routeMatch.getPathParams());

                    WebPage page = routeMatch.getPage();
                    HttpResponse response = "POST".equals(method) ? page.renderPost(params) : page.renderGet(params);

                    writeJsonResponse(socket, response.getStatusCode(), response.getBody());
                } catch (SocketException e) {
                    if (!isBrokenPipe(e)) {
                        throw e;
                    }
                }
            }
        }
    }

    private void handleStaticRequest(Socket socket, String pagePath) throws IOException {
        String resourcePath = pagePath.substring(1);
        try (InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (resourceStream == null) {
                socket.getOutputStream().write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
                return;
            }

            byte[] content = resourceStream.readAllBytes();
            String header = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: text/css\r\n"
                    + "Content-Length: " + content.length + "\r\n\r\n";
            socket.getOutputStream().write(header.getBytes());
            socket.getOutputStream().write(content);
            socket.getOutputStream().flush();
        }
    }

    private int readContentLength(BufferedReader reader) throws IOException {
        int contentLength = 0;
        String headerLine;
        while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
            if (headerLine.toLowerCase().startsWith("content-length:")) {
                String[] parts = headerLine.split(":", 2);
                if (parts.length == 2) {
                    try {
                        contentLength = Integer.parseInt(parts[1].trim());
                    } catch (NumberFormatException ignored) {
                        contentLength = 0;
                    }
                }
            }
        }
        return Math.max(contentLength, 0);
    }

    private void readRequestBody(BufferedReader reader, int contentLength, HashMap<String, String> params)
            throws IOException {
        char[] bodyChars = new char[contentLength];
        int offset = 0;
        while (offset < contentLength) {
            int readCount = reader.read(bodyChars, offset, contentLength - offset);
            if (readCount == -1) {
                break;
            }
            offset += readCount;
        }

        String body = new String(bodyChars, 0, offset);
        params.put("_rawBody", body);

        if (body.isEmpty()) {
            return;
        }

        if (body.startsWith("{")) {
            return;
        }

        for (String formParam : body.split("&")) {
            addParam(params, formParam);
        }
    }

    private String getPageName(String readLine, HashMap<String, String> params) {
        StringTokenizer tokenizer = new StringTokenizer(readLine, " ");
        tokenizer.nextToken();
        String fileName = tokenizer.nextToken().substring(1);

        if (fileName.contains("?")) {
            String[] parts = fileName.split("\\?");
            fileName = parts[0];
            String query = parts[1];

            for (String param : query.split("&")) {
                addParam(params, param);
            }
        }
        return fileName.isEmpty() ? "/" : "/" + fileName;
    }

    private void addParam(HashMap<String, String> params, String encodedParam) {
        String[] pair = encodedParam.split("=", 2);
        if (pair.length != 2) {
            return;
        }

        String key = pair[0];
        if (key.isEmpty()) {
            return;
        }

        String value;
        try {
            value = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ignored) {
            return;
        }

        if (params.containsKey(key)) {
            params.put(key, params.get(key) + "," + value);
        } else {
            params.put(key, value);
        }
    }

    private void writeJsonResponse(Socket socket, int statusCode, String responseBody) throws IOException {
        byte[] data = responseBody.getBytes(StandardCharsets.UTF_8);
        String header = toStatusLine(statusCode) + "\r\n"
                + "Content-Type: application/json; charset=UTF-8\r\n"
                + "Access-Control-Allow-Origin: *\r\n"
                + "Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n"
                + "Access-Control-Allow-Headers: Content-Type\r\n"
                + "Content-Length: " + data.length + "\r\n\r\n";

        socket.getOutputStream().write(header.getBytes());
        socket.getOutputStream().write(data);
        socket.getOutputStream().flush();
    }

    private void writeCorsPreflightResponse(Socket socket) throws IOException {
        String header = "HTTP/1.1 200 OK\r\n"
                + "Access-Control-Allow-Origin: *\r\n"
                + "Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n"
                + "Access-Control-Allow-Headers: Content-Type\r\n"
                + "Content-Length: 0\r\n\r\n";

        socket.getOutputStream().write(header.getBytes(StandardCharsets.UTF_8));
        socket.getOutputStream().flush();
    }

    private boolean isBrokenPipe(SocketException exception) {
        String message = exception.getMessage();
        return message != null && message.toLowerCase().contains("broken pipe");
    }

    private String toStatusLine(int statusCode) {
        switch (statusCode) {
            case 200:
                return "HTTP/1.1 200 OK";
            case 201:
                return "HTTP/1.1 201 Created";
            case 400:
                return "HTTP/1.1 400 Bad Request";
            case 404:
                return "HTTP/1.1 404 Not Found";
            case 409:
                return "HTTP/1.1 409 Conflict";
            default:
                return "HTTP/1.1 500 Internal Server Error";
        }
    }

    public static void main(String[] args) throws IOException {
        ApplicationContext context = SpringApplication.run(DynamicContentServer.class, args);
        ArticleService articleService = context.getBean(ArticleService.class);
        Router router = new Router(articleService);
        new DynamicContentServer().start(router);
    }
}
