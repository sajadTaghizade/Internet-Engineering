package ir.ac.ut.ece.ie.dynamiccontentserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.StringTokenizer;

public class DynamicContentServer {

    private Router router = new Router();

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(9092);
        Socket socket;

        while ((socket = serverSocket.accept()) != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String readLine = reader.readLine();

            if (readLine == null) continue;

            String method = readLine.split(" ")[0];
            HashMap<String, String> params = new HashMap<>();
            String pageName = getPageName(readLine, params);

      
            if (pageName.startsWith("/static/")) {
                String resourcePath = pageName.substring(1);
                InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);

                if (is != null) {
                    byte[] content = is.readAllBytes();
                    String header = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/css\r\n" +
                            "Content-Length: " + content.length + "\r\n\r\n";
                    socket.getOutputStream().write(header.getBytes());
                    socket.getOutputStream().write(content);
                    socket.getOutputStream().flush();
                    socket.close();
                    continue;
                } else {
                    socket.getOutputStream().write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
                    socket.close();
                    continue;
                }
            }

          
            int contentLength = 0;
            String line;
            while (!(line = reader.readLine()).isEmpty()) {
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
            }

            if (method.equals("POST") && contentLength > 0) {
                char[] bodyChars = new char[contentLength];
                reader.read(bodyChars);
                String body = new String(bodyChars);

                for (String param : body.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2) {
                        String key = pair[0];
                        String value = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);

                        if (params.containsKey(key)) {
                            params.put(key, params.get(key) + "," + value);
                        } else {
                            params.put(key, value);
                        }
                    }
                }
            }

            WebPage page = router.resolve(pageName);
            String html = (page != null) ? 
                          (method.equals("POST") ? page.renderPost(params) : page.renderGet(params)) : 
                          "<h1>404 Page Not Found</h1>";

            byte[] data = html.getBytes(StandardCharsets.UTF_8);

            String header = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/html; charset=UTF-8\r\n" +
                            "Content-Length: " + data.length + "\r\n\r\n";

            socket.getOutputStream().write(header.getBytes());
            socket.getOutputStream().write(data);
            socket.getOutputStream().flush();
            socket.close();
        }
        serverSocket.close();
    }

    private String getPageName(String readLine, HashMap<String, String> params) throws UnsupportedEncodingException {
        StringTokenizer tokenizer = new StringTokenizer(readLine, " ");
        tokenizer.nextToken();
        String fileName = tokenizer.nextToken().substring(1);

        if (fileName.contains("?")) {
            String[] parts = fileName.split("\\?");
            fileName = parts[0];
            String query = parts[1];

            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2) {
                    String key = pair[0];
                    String value = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);

                    if (params.containsKey(key)) {
                        params.put(key, params.get(key) + "," + value);
                    } else {
                        params.put(key, value);
                    }
                }
            }
        }
        return fileName.isEmpty() ? "/" : "/" + fileName;
    }

    public static void main(String[] args) throws IOException {
        new DynamicContentServer().start();
    }
}
