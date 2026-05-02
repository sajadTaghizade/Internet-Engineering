package ir.ac.ut.ece.ie.dynamiccontentserver;

import ir.ac.ut.ece.ie.model.Article;

import java.util.List;

public final class JsonUtils {

    private JsonUtils() {}

    public static String escape(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
            }
        }
        return escaped.toString();
    }

    public static String quote(String value) {
        return "\"" + escape(value) + "\"";
    }

    public static String articleToJson(Article article) {
        if (article == null) {
            return "null";
        }

        StringBuilder json = new StringBuilder();
        json.append("{")
                .append("\"id\":").append(article.getId()).append(",")
                .append("\"title\":").append(quote(article.getTitle())).append(",")
                .append("\"abstract\":").append(quote(article.getAbs())).append(",")
                .append("\"body\":").append(quote(article.getBody())).append(",")
                .append("\"createdAt\":").append(article.getCreatedAt()).append(",")
                .append("\"citationCount\":").append(article.getCitationCount()).append(",")
                .append("\"references\":").append(intListToJson(article.getReferences()))
                .append("}");
        return json.toString();
    }

    public static String articlesToJson(List<Article> articles) {
        if (articles == null) {
            return "[]";
        }

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < articles.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append(articleToJson(articles.get(i)));
        }
        json.append("]");
        return json.toString();
    }

    public static String error(String message) {
        return "{\"error\":" + quote(message) + "}";
    }

    public static String message(String message) {
        return "{\"message\":" + quote(message) + "}";
    }

    private static String intListToJson(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return "[]";
        }

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append(values.get(i));
        }
        json.append("]");
        return json.toString();
    }
}
