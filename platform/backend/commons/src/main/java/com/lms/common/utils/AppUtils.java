package com.lms.common.utils;


import java.net.InetAddress;
import java.net.UnknownHostException;

public class AppUtils {
    public static String getAppKey() {

        String appSecret = "application.secret";

        String hostName = "";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch ( UnknownHostException e1) {

        }
        String cacheKey = appSecret + hostName;
        return cacheKey;
    }
}
