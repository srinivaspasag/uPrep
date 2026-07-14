package com.lms.common.vedantu.commons.pojos.requests.responces;

import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SystemInfoRes extends VedantuBaseMongoModel {

    public String hostname;
    public String ipAddress;
    public String httpPort;
    public String buildVersion;
    public String cwd;
    public String appName;

}
