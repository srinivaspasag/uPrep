package com.vedantu.cmds.pojos.requests.documents;

import com.vedantu.cmds.pojos.content.question.CMDSResourceInfo;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.content.pojos.SrcType.LinkType;
import com.vedantu.mongo.VedantuRecordState;

public class CMDSDocumentInfo extends CMDSResourceInfo {

    public String   thumbnail;
    public String   url;
    public LinkType linkType;

    public CMDSDocumentInfo(String id, String name, EntityType type, String orgId,
            long timeCreated, long lastUpdated, String addedBy, long programsAddedTo,
            boolean published, boolean completed, boolean converted, String globalId,
            VedantuRecordState recordState, LinkType linkType) {

        this(id, name, type, orgId, timeCreated, lastUpdated, addedBy, programsAddedTo, published,
                completed, converted, globalId, recordState, linkType, 0);
    }

    public CMDSDocumentInfo(String id, String name, EntityType type, String orgId,
            long timeCreated, long lastUpdated, String addedBy, long programsAddedTo,
            boolean published, boolean completed, boolean converted, String globalId,
            VedantuRecordState recordState, LinkType linkType, long size) {

        super(id, name, type, orgId, timeCreated, lastUpdated, addedBy, programsAddedTo, published,
                completed, converted, globalId, recordState, size);
        this.linkType = linkType;

    }

    @Override
    public String toString() {

        return "CMDSDocumentInfo [thumbnail=" + thumbnail + ", url=" + url + ", linkType="
                + linkType + ", toString()=" + super.toString() + "]";
    }

}
