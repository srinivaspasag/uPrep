package com.vedantu.cmds.pojos.requests.videos;

import java.util.List;

import com.vedantu.cmds.pojos.content.question.CMDSResourceInfo;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.FileConversionState;
import com.vedantu.content.pojos.SrcType.LinkType;
import com.vedantu.mongo.VedantuRecordState;

public class CMDSVideoInfo extends CMDSResourceInfo {

    public String   thumbnail;
    public String   url;
    public LinkType linkType;
    public long     duration;
    public List<FileConversionState>       conversionStates;

    public CMDSVideoInfo(String id, String name, EntityType type, String orgId, long timeCreated,
            long lastUpdated, String addedBy, long programsAddedTo, boolean published,
            boolean completed, boolean converted, String globalId, VedantuRecordState recordState,
            LinkType linkType, long size) {

        super(id, name, type, orgId, timeCreated, lastUpdated, addedBy, programsAddedTo, published,
                completed, converted, globalId, recordState, size);
        this.linkType = linkType;

    }

    public CMDSVideoInfo(String id, String name, EntityType type, String orgId, long timeCreated,
            long lastUpdated, String addedBy, long programsAddedTo, boolean published,
            boolean completed, boolean converted, String globalId, VedantuRecordState recordState,
            LinkType linkType, long size,List<FileConversionState> conversionStates) {

        super(id, name, type, orgId, timeCreated, lastUpdated, addedBy, programsAddedTo, published,
                completed, converted, globalId, recordState, size);
        this.linkType = linkType;
        this.conversionStates = conversionStates;
    }

    public CMDSVideoInfo(String id, String name, EntityType type, String orgId, long timeCreated,
            long lastUpdated, String addedBy, long programsAddedTo, boolean published,
            boolean completed, boolean converted, String globalId, VedantuRecordState recordState,
            LinkType linkType) {

        this(id, name, type, orgId, timeCreated, lastUpdated, addedBy, programsAddedTo, published,
                completed, converted, globalId, recordState, linkType, 0);
    }

}
