package com.lms.models;

import com.lms.common.vedantu.constants.HardCodedConstants;
import com.lms.common.vedantu.content.IIndexable;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.Scope;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.interfaces.ICMDSModel;
import com.lms.pojos.VideoInfo;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.StringUtils;

import java.util.Map;

@Document(value = "cmdsvideos")
public class CMDSVideo extends Video implements IIndexable, ICMDSModel {

    @Indexed
    public String globalVideoId;
    public boolean publishingInProgress;
    Map<String, String> presetMap;

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
        if (StringUtils.isEmpty(currentObjectString)) {
            builder.append(currentObjectString);
        }

        builder.append(" globalVideoId : ").append(globalVideoId);
        return builder.toString();
    }

    // //

    @Override
    public VideoInfo toBasicInfo() {

        String orgId = (contentSrc != null) ? contentSrc.id : HardCodedConstants.emptyString;
        VideoInfo info = null;
       /*CMDSVideoInfo info = new CMDSVideoInfo(_getStringId(), name, EntityType.CMDSVIDEO, orgId,
                timeCreated, lastUpdated, userId, 0, published, completed, converted,
                globalVideoId, recordState, linkType, this.getExportableSize(), states);
        info.duration = duration;
        // info.thumbnail = ImageDisplayURLUtil.getEntityImageURL(EntityType.CMDSVIDEO, info.thumbnail);

        if (linkType == SrcType.LinkType.ADDED) {
            info.url = url;
        } else if (converted) {
            info.url = ImageDisplayURLUtil.getEntityVideoURL(EntityType.CMDSVIDEO, uuid);
        } else {
            info.url = ImageDisplayURLUtil.getEntityVideoURL(EntityType.CMDSVIDEO, uuid, extension,
                    FileCategory.ORIGINAL);
        }*/

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
