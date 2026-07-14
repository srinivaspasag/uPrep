package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class AddMicrositeConfigReq extends AbstractOrgScopeReq {

    public String              privacy;
    public Map<String, String> config;
    public String              templateId;


}
