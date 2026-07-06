package ir.ac.ut.ece.ie.dynamiccontentserver;

import java.util.HashMap;

public class LoginController implements WebPage {

    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public HttpResponse renderGet(HashMap<String, String> params) {
        return HttpResponse.notFound(JsonUtils.error("Not Found"));
    }

    @Override
    public HttpResponse renderPost(HashMap<String, String> params) {
        String rawBody = params.get("_rawBody");
        if (rawBody == null || rawBody.isBlank()) {
            return HttpResponse.badRequest(JsonUtils.error("Invalid JSON body"));
        }

        String username = JsonUtils.extractStringField(rawBody, "username");
        String password = JsonUtils.extractStringField(rawBody, "password");

        AuthService.AuthResult result = authService.login(username, password);

        switch (result.getStatus()) {
            case SUCCESS:
                return HttpResponse.ok(JsonUtils.authResponseToJson(result.getToken(), result.getUser()));
            case INVALID_CREDENTIALS:
                return HttpResponse.unauthorized(JsonUtils.error(result.getMessage()));
            case VALIDATION_ERROR:
                return HttpResponse.badRequest(JsonUtils.error(result.getMessage()));
            default:
                return HttpResponse.badRequest(JsonUtils.error("Invalid request"));
        }
    }
}
