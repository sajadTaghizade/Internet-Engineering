package ir.ac.ut.ece.ie.dynamiccontentserver;

import ir.ac.ut.ece.ie.security.AuthGuard;
import ir.ac.ut.ece.ie.security.JwtUtil;

import java.util.HashMap;

public class UpdateContactController implements WebPage {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UpdateContactController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public HttpResponse renderGet(HashMap<String, String> params) {
        return HttpResponse.notFound(JsonUtils.error("Not Found"));
    }

    @Override
    public HttpResponse renderPost(HashMap<String, String> params) {
        JwtUtil.Claims claims = AuthGuard.requireAuth(params, jwtUtil);
        if (claims == null) {
            return HttpResponse.unauthorized(JsonUtils.error("Authentication required"));
        }

        String rawBody = params.get("_rawBody");
        if (rawBody == null || rawBody.isBlank()) {
            return HttpResponse.badRequest(JsonUtils.error("Invalid JSON body"));
        }

        String email = JsonUtils.extractStringField(rawBody, "email");
        String phone = JsonUtils.extractStringField(rawBody, "phone");

        UserService.UpdateResult result = userService.updateContact(claims.getUserId(), email, phone);

        switch (result.getStatus()) {
            case SUCCESS:
                return HttpResponse.ok("{\"data\":" + JsonUtils.userToJson(result.getUser()) + "}");
            case CONFLICT:
                return HttpResponse.conflict(JsonUtils.error(result.getMessage()));
            case VALIDATION_ERROR:
                return HttpResponse.badRequest(JsonUtils.error(result.getMessage()));
            default:
                return HttpResponse.unauthorized(JsonUtils.error("Authentication required"));
        }
    }
}
