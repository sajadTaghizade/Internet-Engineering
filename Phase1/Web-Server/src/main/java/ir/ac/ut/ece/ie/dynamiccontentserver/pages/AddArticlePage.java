package ir.ac.ut.ece.ie.dynamiccontentserver.pages;

import ir.ac.ut.ece.ie.dynamiccontentserver.TemplateEngine;
import ir.ac.ut.ece.ie.dynamiccontentserver.WebPage;
import ir.ac.ut.ece.ie.model.Article;
import ir.ac.ut.ece.ie.repository.ArticleRepository;

import java.util.*;

public class AddArticlePage implements WebPage {

    @Override
    public String renderGet(HashMap<String, String> params) {

        StringBuilder refsHtml = new StringBuilder();

        for (Article a : ArticleRepository.getInstance().getAll()) {

            refsHtml.append("<div style='margin-bottom:6px;'>")
                    .append("<input type='checkbox' name='refs' value='")
                    .append(a.getId())
                    .append("'> ")
                    .append(a.getTitle())
                    .append("</div>");
        }

        HashMap<String, String> data = new HashMap<>();
        data.put("references", refsHtml.toString());

        return TemplateEngine.render("add_article.html", data);
    }

    @Override
    public String renderPost(HashMap<String, String> params) {

        String title = params.get("title");
        String abs = params.get("abs");
        String body = params.get("body");

        if (ArticleRepository.getInstance().exists(title)) {

            HashMap<String, String> data = new HashMap<>();
            data.put("message", "Article with this title already exists");
            data.put("link", "/add");
            data.put("button", "Try Again");

            return TemplateEngine.render("message.html", data);
        }

        Article article = new Article(title, abs, body);

        String refs = params.get("refs");

        if (refs != null && !refs.isEmpty()) {

            List<Integer> refIds = new ArrayList<>();

            for (String p : refs.split(",")) {

                int id = Integer.parseInt(p);
                refIds.add(id);

                Article refArticle =
                        ArticleRepository.getInstance().getById(id);

                if (refArticle != null) {
                    refArticle.incrementCitationCount();
                }
            }

            article.setReferences(refIds);
        }

        ArticleRepository.getInstance().add(article);

        HashMap<String, String> data = new HashMap<>();
        data.put("message", "Article Added Successfully");
        data.put("link", "/");
        data.put("button", "Go to Home");

        return TemplateEngine.render("message.html", data);
    }
}
