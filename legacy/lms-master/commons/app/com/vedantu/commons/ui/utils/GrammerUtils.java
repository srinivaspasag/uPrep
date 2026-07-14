package com.vedantu.commons.ui.utils;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class GrammerUtils {

    private static Set<Character> vowels = null;

    static {
        vowels = new HashSet<Character>();
        vowels.add('a');
        vowels.add('e');
        vowels.add('i');
        vowels.add('o');
        vowels.add('u');

    }

    public static String getArticle(String word) {

        if (StringUtils.isNotEmpty(word)) {
            if (vowels.contains(word.charAt(0))) {
                return "an";
            }
        }
        return "a";

    }
}
