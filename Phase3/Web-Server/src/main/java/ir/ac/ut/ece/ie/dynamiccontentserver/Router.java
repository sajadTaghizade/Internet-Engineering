package ir.ac.ut.ece.ie.dynamiccontentserver;

import java.util.HashMap;
import java.util.Map;

public class Router {

    private final Map<String, WebPage> staticRoutes = new HashMap<>();
    private final WebPage articleController = new ArticleController();

    public Router() {
        staticRoutes.put(key("GET", "/api/articles"), articleController);
        staticRoutes.put(key("POST", "/api/articles"), articleController);
    }

    public RouteMatch resolve(String method, String path) {
        WebPage exactMatch = staticRoutes.get(key(method, path));
        if (exactMatch != null) {
            return new RouteMatch(exactMatch, new HashMap<>());
        }

        if ("GET".equalsIgnoreCase(method) && path.startsWith("/api/articles/")) {
            String idPart = path.substring("/api/articles/".length());
            if (!idPart.isEmpty() && !idPart.contains("/")) {
                HashMap<String, String> params = new HashMap<>();
                params.put("id", idPart);
                return new RouteMatch(articleController, params);
            }
        }

        return null;
    }

    private String key(String method, String path) {
        return method.toUpperCase() + " " + path;
    }

    public static class RouteMatch {
        private final WebPage page;
        private final HashMap<String, String> pathParams;

        public RouteMatch(WebPage page, HashMap<String, String> pathParams) {
            this.page = page;
            this.pathParams = pathParams;
        }

        public WebPage getPage() {
            return page;
        }

        public HashMap<String, String> getPathParams() {
            return pathParams;
        }
    }
}
