package controllers;

import java.net.InetAddress;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Play;
import play.cache.Cache;
import pojos.SystemInfoRes;
import response.JSONResponse;
import util.AppUtils;
import util.CommandLineUtils;

import com.google.gson.Gson;

public class SysInfo extends AbstractUIController {

    public static void get() {

        SystemInfoRes sysInfo = _getSysInfo();
        renderJSON(new JSONResponse(sysInfo));
    }

    protected static SystemInfoRes _getSysInfo() {

        String json = (String) Cache.get(AppUtils.getAppKey());
        SystemInfoRes systemInfo = null;
        Gson gson = new Gson();
        if (StringUtils.isNotEmpty(json)) {
            systemInfo = gson.fromJson(json, SystemInfoRes.class);
            Logger.log4j.info("retrived from cached");
        } else {
            try {
                systemInfo = new SystemInfoRes();
                systemInfo.cwd = System.getProperty("user.dir");
                systemInfo.buildVersion = CommandLineUtils
                        .getCurrentDeployedVersion(systemInfo.cwd);;
                systemInfo.appName = Play.configuration.getProperty("application.name");
                systemInfo.httpPort = Play.configuration.getProperty("http.port");

                systemInfo.hostname = InetAddress.getLocalHost().getHostName();
                systemInfo.ipAddress = InetAddress.getLocalHost().getHostAddress();
                json = gson.toJson(systemInfo);
                Logger.log4j.info("Current JSON " + json);
                Cache.set(AppUtils.getAppKey(), json, "1h");

            } catch (Exception e) {
                Logger.log4j.error("Error", e);
            }
        }
        return systemInfo;
    }

}
