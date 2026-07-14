package com.lms.common.vedantu.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SystemInfoRes {

    private String hostname;
    private String ipAddress;
    private String httpPort;
    private String buildVersion;
    private String cwd;
    private String appName;

}