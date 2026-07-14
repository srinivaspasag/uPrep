package com.lms.content;

import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.FileConversionState;
import com.lms.enums.SrcType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CMDSVideoInfo extends CMDSResourceInfo {

    public String thumbnail;
    public String url;
    public SrcType.LinkType linkType;
    public long duration;
    public List<FileConversionState> conversionStates;

    public CMDSVideoInfo(String id, String name, EntityType type, String orgId, long timeCreated,
                         long lastUpdated, String addedBy, long programsAddedTo, boolean published,
                         boolean completed, boolean converted, String globalId, VedantuRecordState recordState,
                         SrcType.LinkType linkType, long size) {

        super(id, name, type, orgId, timeCreated, lastUpdated, addedBy, programsAddedTo, published,
                completed, converted, globalId, recordState, size);
        this.linkType = linkType;

    }

    public CMDSVideoInfo(String id, String name, EntityType type, String orgId, long timeCreated,
                         long lastUpdated, String addedBy, long programsAddedTo, boolean published,
                         boolean completed, boolean converted, String globalId, VedantuRecordState recordState,
                         SrcType.LinkType linkType, long size, List<FileConversionState> conversionStates) {

        super(id, name, type, orgId, timeCreated, lastUpdated, addedBy, programsAddedTo, published,
                completed, converted, globalId, recordState, size);
        this.linkType = linkType;
        this.conversionStates = conversionStates;
    }

    public CMDSVideoInfo(String id, String name, EntityType type, String orgId, long timeCreated,
                         long lastUpdated, String addedBy, long programsAddedTo, boolean published,
                         boolean completed, boolean converted, String globalId, VedantuRecordState recordState,
                         SrcType.LinkType linkType) {

        this(id, name, type, orgId, timeCreated, lastUpdated, addedBy, programsAddedTo, published,
                completed, converted, globalId, recordState, linkType, 0);
    }

}
