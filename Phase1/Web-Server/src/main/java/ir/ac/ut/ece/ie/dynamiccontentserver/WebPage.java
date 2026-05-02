package ir.ac.ut.ece.ie.dynamiccontentserver;

import java.util.HashMap;

public interface WebPage {

    String renderGet(HashMap<String, String> params);

    String renderPost(HashMap<String, String> params);
}
