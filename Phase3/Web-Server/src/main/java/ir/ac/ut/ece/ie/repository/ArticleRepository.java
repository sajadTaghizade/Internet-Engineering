package ir.ac.ut.ece.ie.repository;

import ir.ac.ut.ece.ie.model.Article;

import java.util.ArrayList;
import java.util.List;

public class ArticleRepository {

    private static final ArticleRepository instance = new ArticleRepository();
    private final List<Article> articles = new ArrayList<>();

    private ArticleRepository() {}

    public static ArticleRepository getInstance() {
        return instance;
    }

    public void add(Article article) {
        articles.add(article);
    }

    public List<Article> getAll() {
        return articles;
    }

    public Article getById(int id) {
        for (Article a : articles) {
            if (a.getId() == id) {
                return a;
            }
        }
        return null;
    }

    public Article getByTitle(String title) {
        for (Article a : articles) {
            if (a.getTitle().equals(title)) {
                return a;
            }
        }
        return null;
    }
}
