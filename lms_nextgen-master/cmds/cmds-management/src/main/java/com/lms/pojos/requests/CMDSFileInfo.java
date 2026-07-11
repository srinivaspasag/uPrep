package com.lms.pojos.requests;

import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.content.CMDSResourceInfo;
import com.lms.enums.SrcType.LinkType;

public class CMDSFileInfo extends CMDSResourceInfo {

    public String thumbnail;
    public String url;
    public LinkType linkType;

    public CMDSFileInfo(String id, String name, EntityType type, String orgId, long timeCreated, long lastUpdated,
                        String addedBy, long programsAddedTo, boolean published, boolean completed, boolean converted,
                        String globalId, VedantuRecordState recordState, LinkType linkType) {

        this(id, name, type, orgId, timeCreated, lastUpdated, addedBy, programsAddedTo, published, completed, converted,
                globalId, recordState, linkType, 0);
    }

    public CMDSFileInfo(String id, String name, EntityType type, String orgId, long timeCreated, long lastUpdated,
                        String addedBy, long programsAddedTo, boolean published, boolean completed, boolean converted,
                        String globalId, VedantuRecordState recordState, LinkType linkType, long size) {

        super(id, name, type, orgId, timeCreated, lastUpdated, addedBy, programsAddedTo, published, completed,
                converted, globalId, recordState, size);
        this.linkType = linkType;

    }

}
