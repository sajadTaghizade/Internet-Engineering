package ir.ac.ut.ece.ie.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "articles")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String abs;

    @Column(columnDefinition = "TEXT")
    private String body;

    private long createdAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "article_references", joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "referenced_article_id")
    private List<Integer> references = new ArrayList<>();

    private int citationCount = 0;

    public Article() {
    }

    public Article(String title, String abs, String body) {
        this.title = title;
        this.abs = abs;
        this.body = body;
        this.createdAt = System.currentTimeMillis();
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAbs() {
        return abs;
    }

    public String getBody() {
        return body;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public List<Integer> getReferences() {
        return references;
    }

    public void setReferences(List<Integer> references) {
        this.references = references;
    }

    public int getCitationCount() {
        return citationCount;
    }

    public void incrementCitationCount() {
        this.citationCount++;
    }

}
