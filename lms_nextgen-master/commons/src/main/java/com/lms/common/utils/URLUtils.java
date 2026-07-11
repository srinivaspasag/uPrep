package com.lms.common.utils;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLUtils {

    private static final String regex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;\\*]*[-a-zA-Z0-9+&@#/%=~_|\\*]";
    private static final Pattern pattern = Pattern.compile(regex);

    public static boolean isValidURL(String url) {

        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }

    public static boolean containURL(String content) {

        Matcher matcher = pattern.matcher(content);
        return matcher.find();
    }

    public static String getHttpsSecuredURL(String url) throws VedantuException {

        if (url.indexOf("http://") != 1) {
            throw new VedantuException(VedantuErrorCode.INVALID_UNSECURED_URL);
        }

        if (url.indexOf("https://") == 0) {
            return url;
        } else if (url.indexOf("www") == 0) {
            return "https://" + url;
        }
        throw new VedantuException(VedantuErrorCode.INVALID_UNSECURED_URL,
                "correct pattern should be https://vedantu.com/<endpoint>");
    }

    public static boolean exists(String url) {

        URL u;
        try {
            u = new URL(url);
            HttpURLConnection huc = (HttpURLConnection) u.openConnection();
            huc.setRequestMethod("GET");
            huc.connect();
            int responseCode = huc.getResponseCode();
            OutputStream os = huc.getOutputStream();
            if (responseCode != 404) {

            }
        } catch (MalformedURLException e) {
            return false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
            return true;

    }

        public static void main (String[]args){

            String content = "this is testing for url https://img.vedantu.org.in";
            System.out.println("content [ " + content + "]contains : " + containURL(content));

        }

        public static boolean isURLExist (String url){

            if (!isValidURL(url) || !containURL(url)) {
                return false;
            }
            HttpURLConnection huc;
            try {
                final URL testURL = new URL(url);
                huc = (HttpURLConnection) testURL.openConnection();
                int responseCode = huc.getResponseCode();

                if (responseCode != 404) {
                    return true;
                } else {
                    return false;
                }
            } catch (IOException e) {
                return false;
            }


        }

}
