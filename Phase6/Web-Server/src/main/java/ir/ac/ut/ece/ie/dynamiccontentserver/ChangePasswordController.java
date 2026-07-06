package ir.ac.ut.ece.ie.dynamiccontentserver;

import ir.ac.ut.ece.ie.security.AuthGuard;
import ir.ac.ut.ece.ie.security.JwtUtil;

import java.util.HashMap;

public class ChangePasswordController implements WebPage {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public ChangePasswordController(UserService userService, JwtUtil jwtUtil) {
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

        String currentPassword = JsonUtils.extractStringField(rawBody, "currentPassword");
        String newPassword = JsonUtils.extractStringField(rawBody, "newPassword");

        UserService.UpdateResult result = userService.changePassword(claims.getUserId(), currentPassword, newPassword);

        switch (result.getStatus()) {
            case SUCCESS:
                return HttpResponse.ok(JsonUtils.message("Password updated successfully"));
            case VALIDATION_ERROR:
                return HttpResponse.badRequest(JsonUtils.error(result.getMessage()));
            default:
                return HttpResponse.unauthorized(JsonUtils.error("Authentication required"));
        }
    }
}
