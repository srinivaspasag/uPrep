package com.vedantu.ext.cmds.utils.commons;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class StringUtils {

    private static final String CANNONICAL_REPLACEMENT_REGEX_PATTERN = "[^a-zA-Z0-9]";
    public static final String  EMPTY                                = "";
    /**
     * Represents a failed index search.
     * @since 2.1
     */
    public static final int INDEX_NOT_FOUND = -1;

    public static boolean isEmpty(String value) {

        return value == null || value.length() <= 0;
    }

    public static boolean isNotEmpty(String value) {

        return !isEmpty(value);
    }

    public static String join(String separator, String... values) {

        StringBuilder sb = new StringBuilder();
        boolean isStart = true;
        for (String value : values) {
            if (!isStart) {
                sb.append(separator);
            }
            sb.append(value);
            isStart = false;
        }
        return sb.toString();
    }
    
    public static String join(String separator, List<String> values) {

        StringBuilder sb = new StringBuilder();
        boolean isStart = true;
        for (String value : values) {
            if (!isStart) {
                sb.append(separator);
            }
            sb.append(value);
            isStart = false;
        }
        return sb.toString();
    }

    /**
     * It will convert name by stripping of spaces but will keep all other characters.
     * 
     * @param name
     * @return
     */
    public static String toCanonicalName(String name) {

        name = name.toLowerCase().replaceAll(CANNONICAL_REPLACEMENT_REGEX_PATTERN, "");

        return name;

    }

    public static String
            joinString(Collection<String> values, String separator, String annotateWith) {

        StringBuilder sb = new StringBuilder();
        boolean start = true;
        for (String s : values) {
            if (isEmpty(s)) {
                continue;
            }
            if (!start) {
                sb.append(separator);
            }

            sb.append(annotateWith);
            sb.append(s);
            sb.append(annotateWith);
            start = false;
        }
        return sb.toString();
    }

    public static String dateString(String datePattern, long millis) {

        // String datePattern = "yyyy-MM-dd";
        Date d = new Date(millis);
        SimpleDateFormat dtf = new SimpleDateFormat(datePattern);
        return dtf.format(d);
    }
    

    public static boolean isValidEmail(String email) {
        EmailValidator validator = new EmailValidator();
        return validator.validate(email);
    }
    /**
     * Strips of spaces and replaces them with "_"
     * @param input
     * @return
     */
    public static String strip(String input) {
        input = input.trim().replace(" ", "_");
        return input.toLowerCase();
        
        
    }

    /**
     * <p>Gets the substring after the last occurrence of a separator.
     * The separator is not returned.</p>
     *
     * <p>A <code>null</code> string input will return <code>null</code>.
     * An empty ("") string input will return the empty string.
     * An empty or <code>null</code> separator will return the empty string if
     * the input string is not <code>null</code>.</p>
     *
     * <p>If nothing is found, the empty string is returned.</p>
     *
     * <pre>
     * StringUtils.substringAfterLast(null, *)      = null
     * StringUtils.substringAfterLast("", *)        = ""
     * StringUtils.substringAfterLast(*, "")        = ""
     * StringUtils.substringAfterLast(*, null)      = ""
     * StringUtils.substringAfterLast("abc", "a")   = "bc"
     * StringUtils.substringAfterLast("abcba", "b") = "a"
     * StringUtils.substringAfterLast("abc", "c")   = ""
     * StringUtils.substringAfterLast("a", "a")     = ""
     * StringUtils.substringAfterLast("a", "z")     = ""
     * </pre>
     *
     * @param str  the String to get a substring from, may be null
     * @param separator  the String to search for, may be null
     * @return the substring after the last occurrence of the separator,
     *  <code>null</code> if null String input
     * @since 2.0
     */
    public static String substringAfterLast(String str, String separator) {
        if (isEmpty(str)) {
            return str;
        }
        if (isEmpty(separator)) {
            return EMPTY;
        }
        int pos = str.lastIndexOf(separator);
        if (pos == INDEX_NOT_FOUND || pos == (str.length() - separator.length())) {
            return EMPTY;
        }
        return str.substring(pos + separator.length());
    }

}
