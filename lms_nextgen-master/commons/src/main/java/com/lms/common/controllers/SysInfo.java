package com.lms.common.controllers;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.utils.AppUtils;
import com.lms.common.utils.CommandLineUtils;
import com.lms.common.vedantu.dto.SystemInfoRes;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
public class SysInfo {
    private static final Logger logger = LoggerFactory.getLogger(SysInfo.class);
    @RequestMapping("/sysinfo/get")
    public VedantuResponse getInfo(){

        logger.debug(" Called getInfo");

            String appKey = AppUtils.getAppKey();

            SystemInfoRes systemInfo = new SystemInfoRes();

            if (!appKey.isEmpty()) {
                systemInfo.setAppName(appKey);
                logger.debug("retrived from cached");
            } else {
                try {
                    systemInfo = new SystemInfoRes();
                    systemInfo.setCwd(System.getProperty("user.dir"));
                    systemInfo.setBuildVersion( CommandLineUtils.getCurrentDeployedVersion(systemInfo.getHostname()));
                    systemInfo.setAppName("VEDANTU_APP_NAME");
                    systemInfo.setHttpPort("http.port");

                    systemInfo.setHostname(InetAddress.getLocalHost().getHostName());
                    systemInfo.setIpAddress(InetAddress.getLocalHost().getHostAddress());



                } catch (UnknownHostException e) {
                    logger.error("Error", e);
                    return new VedantuResponse(e);
                } catch (VedantuException e) {

                    return new VedantuResponse(e);
                }
            }
            return new VedantuResponse(systemInfo);
        }


    }

