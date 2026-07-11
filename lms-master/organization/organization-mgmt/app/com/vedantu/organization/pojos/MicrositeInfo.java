package com.vedantu.organization.pojos;

import java.util.HashMap;
import java.util.Map;

import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.mongo.VedantuRecordState;

public class MicrositeInfo extends ModelBasicInfo {

   
    public String              privacyURL;
    public Map<String, String> config;
    public String              playURL;
    
    public MicrositeInfo(String id, VedantuRecordState recordState) {

        this.id = id;
        this.recordState = recordState;
        this.config=new HashMap<String, String>();

        
    }
    
}
