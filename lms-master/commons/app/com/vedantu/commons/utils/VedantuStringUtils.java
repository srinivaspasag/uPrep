package com.vedantu.commons.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import play.Logger;
import play.Logger.ALogger;

public class VedantuStringUtils {

    private static final ALogger LOGGER                               = Logger.of(VedantuStringUtils.class);

    private static final String  CANNONICAL_REPLACEMENT_REGEX_PATTERN = "[^a-zA-Z0-9]";

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

    public static boolean isValidContactNumber(String s)
    {
        // The given argument to compile() method
        // is regular expression. With the help of
        // regular expression we can validate mobile
        // number.
        // 1) Begins with 0 or 91
        // 2) Then contains 7 or 8 or 9.
        // 3) Then contains 9 digits
        Pattern p = Pattern.compile("[6-9][0-9]{9}");

        // Pattern class contains matcher() method
        // to find matching between given number
        // and regular expression
        Matcher m = p.matcher(s);
        return (m.find() && m.group().equals(s));
    }

    public static List<String> toCanonical(List<String> names) {

        List<String> cNames = new ArrayList<String>();
        for (String name : names) {
            cNames.add(toCanonicalName(name));
        }
        return cNames;
    }

    public static boolean isEmpty(String[] array) {

        return null == array || array.length == 0;
    }

    public static boolean isNotEmpty(String[] array) {

        return !isEmpty(array);
    }

    public static Collection<String> toLowerCase(Collection<String> values) {

        Collection<String> newValues = new ArrayList<String>();
        for (String val : values) {
            newValues.add(StringUtils.lowerCase(val));
        }
        return newValues;
    }

    public static final String YYYY_MM_DD = "yyyy-MM-dd";

    public static Date toDate(String dateString) {

        Date date = null;
        try {
            date = DateUtils.parseDateStrictly(dateString, YYYY_MM_DD);
        } catch (ParseException e) {
            LOGGER.error("invalid date format found for dob: " + dateString);
        }
        return date;
    }

    public static boolean isValidDOB(String dateStr) {

        return true;

        //if (StringUtils.length(dateStr) != YYYY_MM_DD.length()) {
        //    LOGGER.error("invalid date format (by length) found for dob: " + dateStr);
        //    return false;
        //}
        //Date date = toDate(dateStr);
        //return null != date;
    }

    private static final String DD_MM_YYYY              = "dd-MM-yyyy";
    private static final String DD_MM_YYYY_NO_SEPARATOR = "ddMMyyyy";

    public static String toSimpleDateFormat(String dateStr, boolean noSeparator) {

        Date date = toDate(dateStr);
        if (null == date) {
            return StringUtils.EMPTY;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                noSeparator ? DD_MM_YYYY_NO_SEPARATOR : DD_MM_YYYY);
        String simpleDate = simpleDateFormat.format(date);
        return simpleDate;
    }

    public static void main(String[] argv) {

        System.out.println("hello");
        for (String d : new String[] { "1-1-1", "1-01-1900", "01-1-1900", "01-01-1900", "1900-1-1",
                "1900-1-01", "1900-01-1", "1900-01-01", "19000101", "1900001001", "ABCDEFGHIJ" }) {
            System.out.println(d + " ----> " + isValidDOB(d) + " ==> "
                    + toSimpleDateFormat(d, false) + " ==> " + toSimpleDateFormat(d, true));
        }
    }
}
