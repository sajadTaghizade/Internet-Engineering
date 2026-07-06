package ir.ac.ut.ece.ie.dynamiccontentserver;

import java.util.HashMap;

public class RegisterController implements WebPage {

    private final AuthService authService;

    public RegisterController(AuthService authService) {
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
        String email = JsonUtils.extractStringField(rawBody, "email");
        String phone = JsonUtils.extractStringField(rawBody, "phone");

        AuthService.AuthResult result = authService.register(username, password, email, phone);

        switch (result.getStatus()) {
            case SUCCESS:
                return HttpResponse.created(JsonUtils.authResponseToJson(result.getToken(), result.getUser()));
            case CONFLICT:
                return HttpResponse.conflict(JsonUtils.error(result.getMessage()));
            case VALIDATION_ERROR:
                return HttpResponse.badRequest(JsonUtils.error(result.getMessage()));
            default:
                return HttpResponse.badRequest(JsonUtils.error("Invalid request"));
        }
    }
}
