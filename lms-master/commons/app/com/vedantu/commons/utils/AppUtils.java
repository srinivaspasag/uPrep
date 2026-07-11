package com.vedantu.commons.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

import play.Play;

public class AppUtils {

    public static String getAppKey() {

        String appSecret = Play.application().configuration().getString("application.secret");

        String hostName = "";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e1) {

        }
        String cacheKey = appSecret + hostName;
        return cacheKey;
    }
}
