/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pojos;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author ajithreddy
 */
public class WebInfo {

    public String title;
    public String description;
    public String keywords;
    public String url;
    public Set<String> images;

    public WebInfo() {
        images = new HashSet<String>();
    }

    @Override
    public String toString() {
        return "WebInfo{" + "title=" + title + ", description=" + description + ", keywords=" + keywords + ", url=" + url + ", images=" + images + '}';
    }
    public JSONObject toJson(){
        JSONObject json = new JSONObject();
        try {
            json.put("title", title);
            json.put("content", description);
            json.put("keywords", keywords);
            json.put("url", url);
            json.put("thumbnail", images);
        } catch (JSONException ex) {
        }
        return json;
    }
}
