package com.lms.enums;


public enum LatexType
{
    MATHJAX, UNKNOWN, LATEX;

    public static LatexType valueOfKey(String key) {
        LatexType latexType = UNKNOWN;
        try {
            latexType = valueOf(key.trim().toUpperCase());
        } catch (Exception e) {
        }
        return latexType;
    }
}
