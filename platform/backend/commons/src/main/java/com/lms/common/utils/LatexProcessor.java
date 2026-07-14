package com.lms.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LatexProcessor {
    final private static Pattern rmHookPattern = Pattern.compile("(\\\\rm[a-zA-Z]{1})");

    public static String addHookToLatex(String text) {
        Matcher matcher = rmHookPattern.matcher(text);
        while (matcher.find()) {
            String matchText = matcher.group();
            text = text.replaceAll("\\\\" + matchText,
                    "\\\\rm " + matchText.substring(3));
        }
        text = text.replace("--", "-").replace("\\hfill", " ");
        // \hfill is not supported in matjax as of now
        return text;
    }

    public static void main1(String[] args) {
        String text = "{\\rmf},{\\rmgf},hellormt{\\rmt}";
        Pattern p = Pattern.compile("(\\\\rm[a-zA-Z]{1})");
        Matcher m = p.matcher(text);
        System.out.println("cText : " + text);
        while (m.find()) {
            String mT = m.group();
            System.out.println("found : " + mT);
            text = text.replaceAll("\\\\" + mT, "\\\\rm " + mT.substring(3));
        }
        System.out.println("nText : " + text);
    }
}