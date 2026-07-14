package com.vedantu.commons.constants;

import play.Logger;

public class Configurations {

    public static final String APP_HOST             = "app.host";
    public static final String APPLEARN_HOST        = "applearn.host";
    public static final String APP_PORT             = "http.port";
    public static final String APP_PROTOCOL         = "app.protocol";
    public static final String MIME_TYPES_FILE_PATH = "mime.types.file.path";
    public static final String     AMAZON_S3_BUCKET_ALLOWEDORIGINS = "amazon.s3.bucket.allowedorigins";
    public static final String     AMAZON_S3_SECRET_KEY            = "amazon.s3.secretKey";
    public static final String     AMAZON_S3_ACCESS_KEY            = "amazon.s3.accessKey";

    public static String getAppLearnHost(String appId){
        try{
            if(appId.equalsIgnoreCase("web-app")){
                return "applearn.host";
            }else if(appId.equalsIgnoreCase("learn-app")){
                return "learnapp.host";
            }else{
                return APPLEARN_HOST;
            }
        }catch(Exception e){
            Logger.info("************          Exception occured while configuring host            ************");
            return APPLEARN_HOST;
        }
    }
}
