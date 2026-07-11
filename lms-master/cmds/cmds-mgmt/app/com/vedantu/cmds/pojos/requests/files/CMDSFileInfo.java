package com.vedantu.cmds.pojos.requests.files;

import com.vedantu.cmds.pojos.content.question.CMDSResourceInfo;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.content.pojos.SrcType.LinkType;
import com.vedantu.mongo.VedantuRecordState;

public class CMDSFileInfo extends CMDSResourceInfo {

    public String   thumbnail;
    public String   url;
    public LinkType linkType;

    public CMDSFileInfo(String id, String name, EntityType type, String orgId, long timeCreated,
            long lastUpdated, String addedBy, long programsAddedTo, boolean published,
            boolean completed, boolean converted, String globalId, VedantuRecordState recordState,
            LinkType linkType) {

        this(id, name, type, orgId, timeCreated, lastUpdated, addedBy, programsAddedTo, published,
                completed, converted, globalId, recordState, linkType, 0);
    }

    public CMDSFileInfo(String id, String name, EntityType type, String orgId, long timeCreated,
            long lastUpdated, String addedBy, long programsAddedTo, boolean published,
            boolean completed, boolean converted, String globalId, VedantuRecordState recordState,
            LinkType linkType, long size) {

        super(id, name, type, orgId, timeCreated, lastUpdated, addedBy, programsAddedTo, published,
                completed, converted, globalId, recordState, size);
        this.linkType = linkType;

    }

}
