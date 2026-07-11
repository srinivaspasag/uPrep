package com.lms.pojo.responce;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GenerateAppCredentialsRes {
    public String appId;
    public String authToken;
    public String secretKey;

    public GenerateAppCredentialsRes(String appId, String authToken, String secretKey) {

        super();
        this.appId = appId;
        this.authToken = authToken;
        this.secretKey = secretKey;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{appId:").append(appId).append(", authToken:").append(authToken)
                .append(", secretKey:").append(secretKey).append("}");
        return builder.toString();
    }
}
