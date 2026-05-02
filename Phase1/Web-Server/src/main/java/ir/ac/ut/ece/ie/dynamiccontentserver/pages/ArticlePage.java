package ir.ac.ut.ece.ie.dynamiccontentserver.pages;

import ir.ac.ut.ece.ie.dynamiccontentserver.TemplateEngine;
import ir.ac.ut.ece.ie.dynamiccontentserver.WebPage;
import ir.ac.ut.ece.ie.model.Article;
import ir.ac.ut.ece.ie.repository.ArticleRepository;

import java.util.HashMap;
import java.util.List;

public class ArticlePage implements WebPage {

    @Override
    public String renderGet(HashMap<String, String> params) {

        String idStr = params.get("id");
        if (idStr == null) {
            return TemplateEngine.render("message.html", messageData(
                    "No article selected", "/", "Back to Home"));
        }

        int id = Integer.parseInt(idStr);
        Article article = ArticleRepository.getInstance().getById(id);

        if (article == null) {
            return TemplateEngine.render("message.html", messageData(
                    "Article not found", "/", "Back to Home"));
        }

        StringBuilder refsHtml = new StringBuilder();
        List<Integer> refs = article.getReferences();

        if (refs != null && !refs.isEmpty()) {

            refsHtml.append("<ul>");

            for (Integer r : refs) {

                Article ref = ArticleRepository.getInstance().getById(r);

                if (ref != null) {
                    refsHtml.append("<li>")
                            .append("<a href='/article?id=")
                            .append(r)
                            .append("'>")
                            .append(escapeHtml(ref.getTitle()))
                            .append("</a>")
                            .append("</li>");
                }
            }

            refsHtml.append("</ul>");

        } else {
            refsHtml.append("<p>No references.</p>");
        }

        HashMap<String, String> data = new HashMap<>();
        data.put("title", escapeHtml(article.getTitle()));
        data.put("abstract", escapeHtml(article.getAbs()));
        data.put("body", escapeHtml(article.getBody()).replace("\n", "<br>"));
        data.put("citations", String.valueOf(article.getCitationCount()));
        data.put("references", refsHtml.toString());

        return TemplateEngine.render("article.html", data);
    }

    @Override
    public String renderPost(HashMap<String, String> params) {
        return renderGet(params);
    }

    
    private HashMap<String, String> messageData(String msg, String link, String button) {
        HashMap<String, String> data = new HashMap<>();
        data.put("message", msg);
        data.put("link", link);
        data.put("button", button);
        return data;
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
