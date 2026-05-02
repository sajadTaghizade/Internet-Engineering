package ir.ac.ut.ece.ie.model;
import java.util.List;
import java.util.ArrayList;

public class Article {

    private static int counter = 1;

    private int id;
    private String title;
    private String abs;
    private String body;
    private long createdAt;
    private List<Integer> references = new ArrayList<>();
    private int citationCount = 0;


    public Article(String title, String abs, String body) {
        this.id = counter++;
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
    public List<Integer> getReferences() { return references; }
    public void setReferences(List<Integer> references) { this.references = references; }
    public int getCitationCount() { return citationCount; }
    public void incrementCitationCount() { this.citationCount++; }

}
