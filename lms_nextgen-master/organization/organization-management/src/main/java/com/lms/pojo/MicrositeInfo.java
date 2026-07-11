package com.lms.pojo;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
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
