package ir.ac.ut.ece.ie.dynamiccontentserver;

import ir.ac.ut.ece.ie.model.Article;
import ir.ac.ut.ece.ie.repository.ArticleRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;

    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public List<Article> listArticles(String query) {
        if (query == null || query.isBlank()) {
            List<Article> articles = new ArrayList<>(articleRepository.findAll());
            sortArticles(articles);
            return articles;
        }

        String normalizedQuery = query.toLowerCase();
        List<Article> matchingArticles = new ArrayList<>(articleRepository
                .findByTitleContainingIgnoreCaseOrAbsContainingIgnoreCase(query, query));

        List<Article> titleMatches = new ArrayList<>();
        List<Article> abstractMatches = new ArrayList<>();

        for (Article article : matchingArticles) {
            boolean titleMatch = article.getTitle() != null && article.getTitle().toLowerCase().contains(normalizedQuery);
            boolean abstractMatch = article.getAbs() != null && article.getAbs().toLowerCase().contains(normalizedQuery);

            if (titleMatch) {
                titleMatches.add(article);
            } else if (abstractMatch) {
                abstractMatches.add(article);
            }
        }

        sortArticles(titleMatches);
        sortArticles(abstractMatches);

        List<Article> rankedResults = new ArrayList<>(titleMatches.size() + abstractMatches.size());
        rankedResults.addAll(titleMatches);
        rankedResults.addAll(abstractMatches);
        return rankedResults;
    }

    public List<Article> listArticlesByAuthor(int authorId) {
        List<Article> articles = new ArrayList<>(articleRepository.findByAuthorId(authorId));
        sortArticles(articles);
        return articles;
    }

    public ArticleDetails getArticleDetails(int articleId) {
        Article article = articleRepository.findById(articleId).orElse(null);
        if (article == null) {
            return null;
        }

        List<Article> referencedArticles = new ArrayList<>();
        if (article.getReferences() != null) {
            for (Integer referenceId : article.getReferences()) {
                articleRepository.findById(referenceId).ifPresent(referencedArticles::add);
            }
        }

        return new ArticleDetails(article, referencedArticles);
    }

    public CreateArticleResult createArticle(String title, String articleAbstract, String body, List<Integer> referenceIds,
                                              int authorId, String authorUsername) {
        if (title == null || title.isBlank() || articleAbstract == null || articleAbstract.isBlank() || body == null || body.isBlank()) {
            return CreateArticleResult.validationError("Fields 'title', 'abstract', and 'body' are required");
        }

        if (articleRepository.existsByTitle(title)) {
            return CreateArticleResult.conflict("Article with this title already exists");
        }

        Article article = new Article(title, articleAbstract, body, authorId, authorUsername);
        List<Integer> safeReferences = referenceIds == null ? new ArrayList<>() : new ArrayList<>(referenceIds);

        for (Integer referenceId : safeReferences) {
            if (referenceId == null) {
                continue;
            }

            articleRepository.findById(referenceId).ifPresent(referencedArticle -> {
                referencedArticle.incrementCitationCount();
                articleRepository.save(referencedArticle);
            });
        }

        article.setReferences(safeReferences);
        Article savedArticle = articleRepository.save(article);

        return CreateArticleResult.success(savedArticle);
    }

    private void sortArticles(List<Article> articles) {
        articles.sort(Comparator
                .comparingInt(Article::getCitationCount).reversed()
                .thenComparing(Article::getCreatedAt, Comparator.reverseOrder()));
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
