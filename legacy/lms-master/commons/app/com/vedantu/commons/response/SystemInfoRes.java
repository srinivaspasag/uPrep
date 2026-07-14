package com.vedantu.commons.response;

import com.vedantu.mongo.VedantuBaseMongoModel;

public class SystemInfoRes extends VedantuBaseMongoModel {

    public String hostname;
    public String ipAddress;
    public String httpPort;
    public String buildVersion;
    public String cwd;
    public String appName;
     
}
