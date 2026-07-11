package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsGatewayInfo {
    public String url;
    public String host;
    public String postData;
    public String message;
}
