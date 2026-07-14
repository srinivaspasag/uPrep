package com.vedantu.cmds.models;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;
import com.vedantu.cmds.mgmt.interfaces.ICMDSModel;
import com.vedantu.cmds.pojos.requests.documents.CMDSDocumentInfo;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.utils.ImageDisplayURLUtil;
import com.vedantu.content.commons.interfaces.IIndexable;
import com.vedantu.content.models.Document;
import com.vedantu.content.pojos.SrcType;
import com.vedantu.mongo.VedantuRecordState;

@Entity(value = "cmdsdocuments", noClassnameStored = true)
public class CMDSDocument extends Document implements IIndexable,ICMDSModel {

    @Indexed
    public String       globalDocId;

    Map<String, String> presetMap;
    public boolean       publishingInProgress;
    public CMDSDocument() {

        super();
        published = false;
        globalDocId = null;
        scope = Scope.ORG;
        converted = false;
        recordState = VedantuRecordState.TEMPORARY;
        contentType = EntityType.CMDSDOCUMENT;
    }

    @Override
    public String toString() {

        String currentObjectString = super.toString();

        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotEmpty(currentObjectString)) {
            builder.append(currentObjectString);
        }

        builder.append(" globalDocId : ").append(globalDocId);
        return builder.toString();
    }

    @Override
    public ModelBasicInfo toBasicInfo() {

        String orgId = (contentSrc != null) ? contentSrc.id : StringUtils.EMPTY;
        CMDSDocumentInfo info = new CMDSDocumentInfo(_getStringId(), name, EntityType.CMDSDOCUMENT,
                orgId, timeCreated, lastUpdated, this.userId, 0, published, completed, converted,
                globalDocId, recordState, linkType,this.getExportableSize());

        info.thumbnail = ImageDisplayURLUtil.getEntityImageURL(EntityType.CMDSDOCUMENT,
                info.thumbnail);

        if (linkType == SrcType.LinkType.ADDED) {
            info.url = url;
        } else if (converted) {
            info.url = ImageDisplayURLUtil.getEntityVideoURL(EntityType.CMDSDOCUMENT, uuid);
        } else {
            info.url = ImageDisplayURLUtil.getEntityVideoURL(EntityType.CMDSDOCUMENT, uuid,
                    extension, FileCategory.ORIGINAL);
        }

        return info;
    }

    @Override
    public String getGlobalId() {

        return globalDocId;
    }


    @Override
    public long getExportableSize() {

        if (size != null) {
            return size.getThumbnail() + size.getConverted();
        }
        return 0;
    }

}
