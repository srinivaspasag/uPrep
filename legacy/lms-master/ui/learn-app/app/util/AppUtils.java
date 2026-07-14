package util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import play.Play;

public class AppUtils {

    public static String getAppKey() {

        String appSecret = Play.configuration.getProperty("application.secret");

        String hostName = "";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e1) {

        }
        String cacheKey = appSecret + hostName;
        return cacheKey;
    }
}
