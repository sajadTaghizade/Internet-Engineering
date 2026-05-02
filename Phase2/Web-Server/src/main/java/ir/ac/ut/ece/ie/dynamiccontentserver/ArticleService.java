package ir.ac.ut.ece.ie.dynamiccontentserver;

import ir.ac.ut.ece.ie.model.Article;
import ir.ac.ut.ece.ie.repository.ArticleRepository;

import java.util.ArrayList;
import java.util.List;

public class ArticleService {

    private final ArticleRepository articleRepository;

    public ArticleService() {
        this.articleRepository = ArticleRepository.getInstance();
    }

    public List<Article> listArticles(String query) {
        List<Article> articles = new ArrayList<>(articleRepository.getAll());
        sortArticles(articles);

        if (query == null || query.isBlank()) {
            return articles;
        }

        String normalizedQuery = query.toLowerCase();
        List<Article> titleMatches = new ArrayList<>();
        List<Article> abstractMatches = new ArrayList<>();

        for (Article article : articles) {
            if (article.getTitle() != null && article.getTitle().toLowerCase().contains(normalizedQuery)) {
                titleMatches.add(article);
            } else if (article.getAbs() != null && article.getAbs().toLowerCase().contains(normalizedQuery)) {
                abstractMatches.add(article);
            }
        }

        List<Article> rankedResults = new ArrayList<>();
        rankedResults.addAll(titleMatches);
        rankedResults.addAll(abstractMatches);
        return rankedResults;
    }

    public ArticleDetails getArticleDetails(int articleId) {
        Article article = articleRepository.getById(articleId);
        if (article == null) {
            return null;
        }

        List<Article> referencedArticles = new ArrayList<>();
        if (article.getReferences() != null) {
            for (Integer referenceId : article.getReferences()) {
                Article referencedArticle = articleRepository.getById(referenceId);
                if (referencedArticle != null) {
                    referencedArticles.add(referencedArticle);
                }
            }
        }

        return new ArticleDetails(article, referencedArticles);
    }

    public CreateArticleResult createArticle(String title, String articleAbstract, String body, List<Integer> referenceIds) {
        if (title == null || title.isBlank() || articleAbstract == null || articleAbstract.isBlank() || body == null || body.isBlank()) {
            return CreateArticleResult.validationError("Fields 'title', 'abstract', and 'body' are required");
        }

        if (articleRepository.getByTitle(title) != null) {
            return CreateArticleResult.conflict("Article with this title already exists");
        }

        Article article = new Article(title, articleAbstract, body);
        List<Integer> safeReferences = referenceIds == null ? new ArrayList<>() : new ArrayList<>(referenceIds);

        for (Integer referenceId : safeReferences) {
            Article referencedArticle = articleRepository.getById(referenceId);
            if (referencedArticle != null) {
                referencedArticle.incrementCitationCount();
            }
        }

        article.setReferences(safeReferences);
        articleRepository.add(article);

        return CreateArticleResult.success(article);
    }

    private void sortArticles(List<Article> articles) {
        articles.sort((left, right) -> {
            int byCitations = Integer.compare(right.getCitationCount(), left.getCitationCount());
            if (byCitations != 0) {
                return byCitations;
            }
            return Long.compare(right.getCreatedAt(), left.getCreatedAt());
        });
    }

    public static class ArticleDetails {
        private final Article article;
        private final List<Article> referencedArticles;

        public ArticleDetails(Article article, List<Article> referencedArticles) {
            this.article = article;
            this.referencedArticles = referencedArticles;
        }

        public Article getArticle() {
            return article;
        }

        public List<Article> getReferencedArticles() {
            return referencedArticles;
        }
    }

    public static class CreateArticleResult {
        public enum Status {
            SUCCESS,
            VALIDATION_ERROR,
            CONFLICT
        }

        private final Status status;
        private final String message;
        private final Article article;

        private CreateArticleResult(Status status, String message, Article article) {
            this.status = status;
            this.message = message;
            this.article = article;
        }

        public static CreateArticleResult success(Article article) {
            return new CreateArticleResult(Status.SUCCESS, null, article);
        }

        public static CreateArticleResult validationError(String message) {
            return new CreateArticleResult(Status.VALIDATION_ERROR, message, null);
        }

        public static CreateArticleResult conflict(String message) {
            return new CreateArticleResult(Status.CONFLICT, message, null);
        }

        public Status getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public Article getArticle() {
            return article;
        }
    }
}
