/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uicom.util;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author anirban
 */
public class InstituteUtil {
    private final static Set<String> SUPPORTED_ACTIVITY_FEEDS_TYPES = new HashSet<String>() {{
            add("IMAGE");
            add("WEB_PAGE");
            add("LINK_VIDEO");
            add("VIDEO");
            add("DOCUMENT");
            add("TEST");
            add("ASSIGNMENT");
            add("FILE");
            add("MODULE");
    }};
    public static boolean isSupportedActivityFeed(String type){
        if(SUPPORTED_ACTIVITY_FEEDS_TYPES.contains(type)){
            return true;
        }
        return false;
    }
}
