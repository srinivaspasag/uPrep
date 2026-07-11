package com.lms.models;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.pojo.MicrositeInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Document(value = "microsites")
@CompoundIndexes({ @CompoundIndex(name = "orgId", unique = true), @CompoundIndex(name = "orgId,templateId") })
@Setter
@Getter
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
