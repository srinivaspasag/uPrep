package com.vedantu.cmds.models;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;
import com.vedantu.cmds.mgmt.interfaces.ICMDSModel;
import com.vedantu.cmds.pojos.requests.videos.CMDSVideoInfo;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.utils.ImageDisplayURLUtil;
import com.vedantu.content.commons.interfaces.IIndexable;
import com.vedantu.content.models.Video;
import com.vedantu.content.pojos.SrcType;
import com.vedantu.mongo.VedantuRecordState;

@Entity(value = "cmdsvideos", noClassnameStored = true)
public class CMDSVideo extends Video implements IIndexable, ICMDSModel {

    @Indexed
    public String       globalVideoId;

    Map<String, String> presetMap;
    public boolean       publishingInProgress;

    public CMDSVideo() {

        super();
        published = false;
        completed = false;
        globalVideoId = null;
        scope = Scope.ORG;
        converted = false;
        recordState = VedantuRecordState.TEMPORARY;
        contentType = EntityType.CMDSVIDEO;
    }

    @Override
    public String toString() {

        String currentObjectString = super.toString();

        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotEmpty(currentObjectString)) {
            builder.append(currentObjectString);
        }

        builder.append(" globalVideoId : ").append(globalVideoId);
        return builder.toString();
    }

    // //

    @Override
    public ModelBasicInfo toBasicInfo() {

        String orgId = (contentSrc != null) ? contentSrc.id : StringUtils.EMPTY;

        CMDSVideoInfo info = new CMDSVideoInfo(_getStringId(), name, EntityType.CMDSVIDEO, orgId,
                timeCreated, lastUpdated, userId, 0, published, completed, converted,
                globalVideoId, recordState, linkType, this.getExportableSize(), states);
        info.duration = duration;
        info.thumbnail = ImageDisplayURLUtil
                .getEntityImageURL(EntityType.CMDSVIDEO, info.thumbnail);

        if (linkType == SrcType.LinkType.ADDED) {
            info.url = url;
        } else if (converted) {
            info.url = ImageDisplayURLUtil.getEntityVideoURL(EntityType.CMDSVIDEO, uuid);
        } else {
            info.url = ImageDisplayURLUtil.getEntityVideoURL(EntityType.CMDSVIDEO, uuid, extension,
                    FileCategory.ORIGINAL);
        }

        return info;
    }

    @Override
    public String getGlobalId() {

        return globalVideoId;
    }

    @Override
    public long getExportableSize() {

        if (size != null) {
            return size.getThumbnail() + size.getConverted();
        }
        return 0;
    }

}
