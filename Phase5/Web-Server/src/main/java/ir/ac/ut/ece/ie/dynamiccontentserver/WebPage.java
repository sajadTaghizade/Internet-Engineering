package ir.ac.ut.ece.ie.dynamiccontentserver;

import java.util.HashMap;

public interface WebPage {

    HttpResponse renderGet(HashMap<String, String> params);

    HttpResponse renderPost(HashMap<String, String> params);
}
