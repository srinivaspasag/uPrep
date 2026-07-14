package com.vedantu.commons.pojos;

public class ExternalURLInfo extends FieldInfo {
    public String url;
    public ExternalURLInfo(String appstore, String url) {

        this.url = url;
        this.name = appstore;
    }

 
}
