package com.vedantu.cmds.models;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;
import com.vedantu.cmds.mgmt.interfaces.ICMDSModel;
import com.vedantu.cmds.pojos.requests.files.CMDSFileInfo;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.utils.ImageDisplayURLUtil;
import com.vedantu.content.commons.interfaces.IIndexable;
import com.vedantu.content.models.File;
import com.vedantu.mongo.VedantuRecordState;

@Entity(value = "cmdsfiles", noClassnameStored = true)
public class CMDSFile extends File implements IIndexable, ICMDSModel {

    @Indexed
    public String       globalFileId;

    Map<String, String> presetMap;

    public CMDSFile() {

        super();
        published = false;
        scope = Scope.ORG;
        recordState = VedantuRecordState.TEMPORARY;
        contentType = EntityType.CMDSFILE;
    }

    @Override
    public String toString() {

        String currentObjectString = super.toString();

        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotEmpty(currentObjectString)) {
            builder.append(currentObjectString);
        }

        builder.append(" g   info.url = url;lobalFileId : ").append(globalFileId);
        return builder.toString();
    }

    @Override
    public ModelBasicInfo toBasicInfo() {

        String orgId = (contentSrc != null) ? contentSrc.id : StringUtils.EMPTY;

        CMDSFileInfo info = new CMDSFileInfo(_getStringId(), name, EntityType.CMDSFILE, orgId,
                timeCreated, lastUpdated, userId, 0, published, completed, converted, globalFileId,
                recordState, linkType, this.getExportableSize());

        if (StringUtils.isNotEmpty(thumbnail)) {
            info.thumbnail = ImageDisplayURLUtil.getEntityImageURL(EntityType.CMDSFILE, thumbnail);

        }
        // info.url = url;

        info.url = ImageDisplayURLUtil.getEntityFileURL(EntityType.CMDSFILE, this.uuid, extension,
                FileCategory.ORIGINAL);

        return info;
    }

    @Override
    public String getGlobalId() {

        return globalFileId;
    }

    @Override
    public long getExportableSize() {

        if (size != null) {
            return size.getThumbnail() + size.getEncrypted();
        }
        return 0;
    }

}
