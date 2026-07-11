package com.vedantu.commons.fs.responses;

import java.util.HashMap;
import java.util.Map;

public class SignUploadFileRes {

    public String              uuid;
    public String              url;
    public String              id;
    public String              contentType;
    public boolean             verificationRequired;
    // public String key;

    /** s3 specific data */
    // public String acl;
    // public String AWSAccessKeyId;
    // public String signature;
    // public String policy;
    public Map<String, String> requestParams;

    public SignUploadFileRes() {

        requestParams = new HashMap<String, String>();
        verificationRequired = true;
    }
}
