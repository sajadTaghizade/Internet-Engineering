package ir.ac.ut.ece.ie.repository;

import ir.ac.ut.ece.ie.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Integer> {

    List<Article> findByTitleContainingIgnoreCaseOrAbsContainingIgnoreCase(String title, String abs);

    boolean existsByTitle(String title);

    List<Article> findByAuthor_Id(int authorId);
}
