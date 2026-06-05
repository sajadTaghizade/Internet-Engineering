package ir.ac.ut.ece.ie.dynamiccontentserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArticleController implements WebPage {

    private static final String STRING_FIELD_TEMPLATE = "\"%s\"\\s*:\\s*\"((?:\\\\.|[^\\\"])*)\"";
    private static final Pattern REFERENCES_PATTERN = Pattern.compile("\"references\"\\s*:\\s*\\[(.*?)\\]");
    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
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
        String rawBody = params.get("_rawBody");
        if (rawBody == null || rawBody.isBlank()) {
            return HttpResponse.badRequest(JsonUtils.error("Invalid JSON body"));
        }

        String title = extractStringField(rawBody, "title");
        String articleAbstract = extractStringField(rawBody, "abstract");
        String body = extractStringField(rawBody, "body");

        List<Integer> references;
        try {
            references = extractReferences(rawBody);
        } catch (IllegalArgumentException e) {
            return HttpResponse.badRequest(JsonUtils.error("Field 'references' must be an array of integers"));
        }

        ArticleService.CreateArticleResult result =
                articleService.createArticle(title, articleAbstract, body, references);

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

    private String extractStringField(String json, String fieldName) {
        Pattern fieldPattern = Pattern.compile(String.format(STRING_FIELD_TEMPLATE, Pattern.quote(fieldName)));
        Matcher matcher = fieldPattern.matcher(json);
        if (!matcher.find()) {
            return null;
        }
        return unescapeJsonString(matcher.group(1));
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

    private String unescapeJsonString(String value) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c != '\\') {
                result.append(c);
                continue;
            }

            if (i + 1 >= value.length()) {
                break;
            }

            char next = value.charAt(++i);
            switch (next) {
                case '"':
                    result.append('"');
                    break;
                case '\\':
                    result.append('\\');
                    break;
                case '/':
                    result.append('/');
                    break;
                case 'b':
                    result.append('\b');
                    break;
                case 'f':
                    result.append('\f');
                    break;
                case 'n':
                    result.append('\n');
                    break;
                case 'r':
                    result.append('\r');
                    break;
                case 't':
                    result.append('\t');
                    break;
                case 'u':
                    if (i + 4 < value.length()) {
                        String hex = value.substring(i + 1, i + 5);
                        try {
                            result.append((char) Integer.parseInt(hex, 16));
                            i += 4;
                        } catch (NumberFormatException ignored) {
                            result.append("\\u").append(hex);
                            i += 4;
                        }
                    }
                    break;
                default:
                    result.append(next);
                    break;
            }
        }
        return result.toString();
    }
}
