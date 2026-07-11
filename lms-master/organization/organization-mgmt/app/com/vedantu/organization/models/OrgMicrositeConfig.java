package com.vedantu.organization.models;

import java.util.HashMap;
import java.util.Map;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.organization.pojos.MicrositeInfo;

@Entity(value = "microsites", noClassnameStored = true)
@Indexes({ @Index(value = "orgId", unique = true), @Index(value = "orgId,templateId") })
public class OrgMicrositeConfig extends VedantuBaseMongoModel {

    public String              orgId;
    public String              templateId;
    public Map<String, String> configs;
    public String              privacy;

    public OrgMicrositeConfig(){
        configs= new HashMap<String, String>();
    }
    @Override
    public ModelBasicInfo toBasicInfo() {

        // String orgId = (contentSrc != null) ? contentSrc.id : StringUtils.EMPTY;
        MicrositeInfo info = new MicrositeInfo(orgId, recordState);

        return (ModelBasicInfo) info;
    }

}
