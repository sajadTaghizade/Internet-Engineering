package ir.ac.ut.ece.ie.dynamiccontentserver;
import ir.ac.ut.ece.ie.dynamiccontentserver.pages.ArticlePage;
import ir.ac.ut.ece.ie.dynamiccontentserver.pages.AddArticlePage;
import ir.ac.ut.ece.ie.dynamiccontentserver.pages.HomePage;

import java.util.HashMap;
import java.util.Map;

public class Router {

    private Map<String, WebPage> routes = new HashMap<>();

    public Router() {
        routes.put("/", new HomePage());
        routes.put("/article", new ArticlePage());
        routes.put("/add", new AddArticlePage());


    }

    public WebPage resolve(String path) {
        return routes.get(path);
    }
}
