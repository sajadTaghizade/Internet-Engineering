package ir.ac.ut.ece.ie.dynamiccontentserver.pages;

import ir.ac.ut.ece.ie.dynamiccontentserver.TemplateEngine;
import ir.ac.ut.ece.ie.dynamiccontentserver.WebPage;
import ir.ac.ut.ece.ie.model.Article;
import ir.ac.ut.ece.ie.repository.ArticleRepository;

import java.util.*;

public class HomePage implements WebPage {

    @Override
    public String renderGet(HashMap<String, String> params) {

        String q = params.get("q");
        List<Article> articles = new ArrayList<>(ArticleRepository.getInstance().getAll());

        articles.sort((a, b) -> Integer.compare(b.getCitationCount(), a.getCitationCount()));

        List<Article> titleMatches = new ArrayList<>();
        List<Article> abstractMatches = new ArrayList<>();

        if (q != null && !q.isEmpty()) {
            String query = q.toLowerCase();
            for (Article a : articles) {
                if (a.getTitle() != null && a.getTitle().toLowerCase().contains(query)) {
                    titleMatches.add(a);
                } else if (a.getAbs() != null && a.getAbs().toLowerCase().contains(query)) {
                    abstractMatches.add(a);
                }
            }
        }

        StringBuilder listHtml = new StringBuilder();

        if (q != null && !q.isEmpty()) {
            if (titleMatches.isEmpty() && abstractMatches.isEmpty()) {
                listHtml.append("<p>No Articles Found</p>");
            } else {
                for (Article a : titleMatches) {
                    listHtml.append(renderArticleCard(a));
                }
                for (Article a : abstractMatches) {
                    listHtml.append(renderArticleCard(a));
                }
            }
        } else {
            for (Article a : articles) {
                listHtml.append(renderArticleCard(a));
            }
        }

        HashMap<String, String> data = new HashMap<>();
        data.put("articles", listHtml.toString());
        data.put("query", q != null ? escapeHtml(q) : "");

        return TemplateEngine.render("home.html", data);
    }

    @Override
    public String renderPost(HashMap<String, String> params) {
        return renderGet(params);
    }

    private String renderArticleCard(Article a) {
        StringBuilder html = new StringBuilder();
        html.append("<div class='article'>");

        html.append("<div class='article-title'>")
            .append("<a href='/article?id=")
            .append(a.getId())
            .append("'>")
            .append(escapeHtmlOrEmpty(a.getTitle()))
            .append("</a>")
            .append("</div>");

        html.append("<div class='article-abstract'>")
            .append(escapeHtmlOrEmpty(a.getAbs()))
            .append("</div>");

        html.append("<div style='margin-top:10px; font-size:14px; color:#777;'>")
            .append("Citations: ")
            .append(a.getCitationCount())
            .append("</div>");

        html.append("</div>");
        return html.toString();
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String escapeHtmlOrEmpty(String s) {
        return s == null ? "" : escapeHtml(s);
    }
}
