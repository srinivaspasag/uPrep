package com.lms.common.utils;

import org.apache.http.cookie.Cookie;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SessionExtractorUtils {
    public static Map<String, String> getSessionParams(Cookie cookie) {

        Map<String, String> cookieMap = new HashMap<String, String>();
        if (cookie == null) {
            return cookieMap;
        }
        String[] values = cookie.getValue().substring(cookie.getValue().indexOf('-') + 1).split("%00");

        for (String key : values) {

            System.out.println();
            String[] keys = key.split("%3A");
            if (keys.length == 2) {
                cookieMap.put(keys[0], keys[1]);
            }

        }
        return cookieMap;

    }
}
