package ir.ac.ut.ece.ie.dynamiccontentserver;

import ir.ac.ut.ece.ie.dto.UserDto;
import ir.ac.ut.ece.ie.model.Article;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JsonUtils {

    private static final String STRING_FIELD_TEMPLATE = "\"%s\"\\s*:\\s*\"((?:\\\\.|[^\\\"])*)\"";

    private JsonUtils() {}

    public static String extractStringField(String json, String fieldName) {
        Pattern fieldPattern = Pattern.compile(String.format(STRING_FIELD_TEMPLATE, Pattern.quote(fieldName)));
        Matcher matcher = fieldPattern.matcher(json);
        if (!matcher.find()) {
            return null;
        }
        return unescapeJsonString(matcher.group(1));
    }

    public static String unescapeJsonString(String value) {
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

    public static String quoteOrNull(String value) {
        return value == null ? "null" : quote(value);
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
                .append("\"references\":").append(intListToJson(article.getReferences())).append(",")
                .append("\"authorId\":").append(article.getAuthorId() == null ? "null" : article.getAuthorId()).append(",")
                .append("\"authorUsername\":").append(quoteOrNull(article.getAuthorUsername()))
                .append("}");
        return json.toString();
    }

    public static String authResponseToJson(String token, UserDto user) {
        return "{\"data\":{"
                + "\"token\":" + quote(token) + ","
                + "\"user\":" + userToJson(user)
                + "}}";
    }

    public static String userToJson(UserDto user) {
        if (user == null) {
            return "null";
        }

        return "{"
                + "\"id\":" + user.getId() + ","
                + "\"username\":" + quote(user.getUsername()) + ","
                + "\"email\":" + quoteOrNull(user.getEmail()) + ","
                + "\"phone\":" + quoteOrNull(user.getPhone()) + ","
                + "\"createdAt\":" + user.getCreatedAt()
                + "}";
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
