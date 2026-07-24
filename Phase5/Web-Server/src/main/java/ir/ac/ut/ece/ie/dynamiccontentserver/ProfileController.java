package ir.ac.ut.ece.ie.dynamiccontentserver;

import ir.ac.ut.ece.ie.dto.UserDto;
import ir.ac.ut.ece.ie.security.AuthGuard;
import ir.ac.ut.ece.ie.security.JwtUtil;

import java.util.HashMap;

public class ProfileController implements WebPage {

    private final UserService userService;
    private final ArticleService articleService;
    private final JwtUtil jwtUtil;

    public ProfileController(UserService userService, ArticleService articleService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.articleService = articleService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public HttpResponse renderGet(HashMap<String, String> params) {
        JwtUtil.Claims claims = AuthGuard.requireAuth(params, jwtUtil);
        if (claims == null) {
            return HttpResponse.unauthorized(JsonUtils.error("Authentication required"));
        }

        UserDto user = userService.getById(claims.getUserId());
        if (user == null) {
            return HttpResponse.unauthorized(JsonUtils.error("Authentication required"));
        }

        String userJson = JsonUtils.userToJson(user);
        String body = userJson.substring(0, userJson.length() - 1)
                + ",\"articles\":"
                + JsonUtils.articlesToJson(articleService.listArticlesByAuthor(user.getId()))
                + "}";

        return HttpResponse.ok("{\"data\":" + body + "}");
    }

    @Override
    public HttpResponse renderPost(HashMap<String, String> params) {
        return HttpResponse.notFound(JsonUtils.error("Not Found"));
    }
}
