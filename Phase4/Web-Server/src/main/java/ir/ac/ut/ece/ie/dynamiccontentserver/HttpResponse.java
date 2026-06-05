package ir.ac.ut.ece.ie.dynamiccontentserver;

public class HttpResponse {

    private final int statusCode;
    private final String body;

    public HttpResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    public static HttpResponse ok(String body) {
        return new HttpResponse(200, body);
    }

    public static HttpResponse created(String body) {
        return new HttpResponse(201, body);
    }

    public static HttpResponse badRequest(String body) {
        return new HttpResponse(400, body);
    }

    public static HttpResponse notFound(String body) {
        return new HttpResponse(404, body);
    }

    public static HttpResponse conflict(String body) {
        return new HttpResponse(409, body);
    }
}