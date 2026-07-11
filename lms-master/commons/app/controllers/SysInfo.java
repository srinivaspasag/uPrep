package controllers;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Logger.ALogger;
import play.Play;
import play.cache.Cache;
import play.mvc.Result;

import com.google.gson.Gson;
import com.vedantu.commons.JSONResponse;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.response.SystemInfoRes;
import com.vedantu.commons.utils.AppUtils;
import com.vedantu.commons.utils.CommandLineUtils;

/**
 * 
 * @author vikram
 * 
 */
public class SysInfo extends AbstractVedantuController {

    private static final ALogger LOGGER = Logger.of(SysInfo.class);

    public static Result getInfo() {

        LOGGER.debug(" Called getInfo");

        String json = (String) Cache.get(AppUtils.getAppKey());
        SystemInfoRes systemInfo = null;
        Gson gson = new Gson();
        if (StringUtils.isNotEmpty(json)) {
            systemInfo = gson.fromJson(json, SystemInfoRes.class);
            LOGGER.debug("retrived from cached");
        } else {
            try {
                systemInfo = new SystemInfoRes();
                systemInfo.cwd = System.getProperty("user.dir");
                systemInfo.buildVersion = CommandLineUtils
                        .getCurrentDeployedVersion(systemInfo.cwd);;
                systemInfo.appName = Play.application().configuration()
                        .getString("VEDANTU_APP_NAME");
                systemInfo.httpPort = Play.application().configuration().getString("http.port");

                systemInfo.hostname = InetAddress.getLocalHost().getHostName();
                systemInfo.ipAddress = InetAddress.getLocalHost().getHostAddress();
                json = gson.toJson(systemInfo);
                LOGGER.debug("Current JSON " + json);
                Cache.set(AppUtils.getAppKey(), json, 3600);

            } catch (UnknownHostException e) {
                LOGGER.error("Error", e);
                return ok((new JSONResponse(new VedantuException(VedantuErrorCode.SERVICE_ERROR, e)))
                        .toObjectNode());
            } catch (VedantuException e) {

                return ok((new JSONResponse(e)).toObjectNode());
            }
        }
        return ok(getResultResponse(systemInfo).toObjectNode());
    }
}
