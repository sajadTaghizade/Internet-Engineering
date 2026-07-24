package ir.ac.ut.ece.ie.dynamiccontentserver;

import ir.ac.ut.ece.ie.security.AuthGuard;
import ir.ac.ut.ece.ie.security.JwtUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArticleController implements WebPage {

    private static final Pattern REFERENCES_PATTERN = Pattern.compile("\"references\"\\s*:\\s*\\[(.*?)\\]");
    private final ArticleService articleService;
    private final JwtUtil jwtUtil;

    public ArticleController(ArticleService articleService, JwtUtil jwtUtil) {
        this.articleService = articleService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public HttpResponse renderGet(HashMap<String, String> params) {
        String idParam = params.get("id");
        if (idParam != null) {
            return handleDetail(idParam);
        }

        String query = params.get("q");
        String body = "{\"data\":" + JsonUtils.articlesToJson(articleService.listArticles(query)) + "}";
        return HttpResponse.ok(body);
    }

    @Override
    public HttpResponse renderPost(HashMap<String, String> params) {
        JwtUtil.Claims claims = AuthGuard.requireAuth(params, jwtUtil);
        if (claims == null) {
            return HttpResponse.unauthorized(JsonUtils.error("Authentication required to publish an article"));
        }

        String rawBody = params.get("_rawBody");
        if (rawBody == null || rawBody.isBlank()) {
            return HttpResponse.badRequest(JsonUtils.error("Invalid JSON body"));
        }

        String title = JsonUtils.extractStringField(rawBody, "title");
        String articleAbstract = JsonUtils.extractStringField(rawBody, "abstract");
        String body = JsonUtils.extractStringField(rawBody, "body");

        List<Integer> references;
        try {
            references = extractReferences(rawBody);
        } catch (IllegalArgumentException e) {
            return HttpResponse.badRequest(JsonUtils.error("Field 'references' must be an array of integers"));
        }

        ArticleService.CreateArticleResult result = articleService.createArticle(
                title, articleAbstract, body, references, claims.getUserId());

        switch (result.getStatus()) {
            case SUCCESS:
                return HttpResponse.created("{\"data\":" + JsonUtils.articleToJson(result.getArticle()) + "}");
            case CONFLICT:
                return HttpResponse.conflict(JsonUtils.error(result.getMessage()));
            case VALIDATION_ERROR:
                return HttpResponse.badRequest(JsonUtils.error(result.getMessage()));
            default:
                return HttpResponse.badRequest(JsonUtils.error("Invalid request"));
        }
    }

    private HttpResponse handleDetail(String idParam) {
        int articleId;
        try {
            articleId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            return HttpResponse.notFound(JsonUtils.error("Article not found"));
        }

        ArticleService.ArticleDetails details = articleService.getArticleDetails(articleId);
        if (details == null) {
            return HttpResponse.notFound(JsonUtils.error("Article not found"));
        }

        String articleJson = JsonUtils.articleToJson(details.getArticle());
        String detailJson = articleJson.substring(0, articleJson.length() - 1)
                + ",\"referencedArticles\":"
                + JsonUtils.articlesToJson(details.getReferencedArticles())
                + "}";

        return HttpResponse.ok("{\"data\":" + detailJson + "}");
    }

    private List<Integer> extractReferences(String json) {
        Matcher matcher = REFERENCES_PATTERN.matcher(json);
        if (!matcher.find()) {
            return new ArrayList<>();
        }

        String rawArray = matcher.group(1).trim();
        List<Integer> references = new ArrayList<>();
        if (rawArray.isEmpty()) {
            return references;
        }

        String[] parts = rawArray.split(",");
        for (String part : parts) {
            String token = part.trim();
            if (!token.matches("-?\\d+")) {
                throw new IllegalArgumentException("references must be integers");
            }
            references.add(Integer.parseInt(token));
        }

        return references;
    }
}
