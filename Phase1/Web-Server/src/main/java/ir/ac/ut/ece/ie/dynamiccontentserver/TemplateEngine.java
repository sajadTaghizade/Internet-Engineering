package ir.ac.ut.ece.ie.dynamiccontentserver;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class TemplateEngine {
    public static String render(String templateName, Map<String, String> data) {
        try {
            String path = "/templates/" + templateName;
            InputStream is = TemplateEngine.class.getResourceAsStream(path);
            
            if (is == null) {
                return "<h1>Template Error: " + templateName + " not found in " + path + "</h1>";
            }

            String content = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            for (Map.Entry<String, String> entry : data.entrySet()) {
                content = content.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }

            return content;
            
        } catch (Exception e) {
            e.printStackTrace();
            return "<h1>Internal Template Engine Error: " + e.getMessage() + "</h1>";
        }
    }
}
