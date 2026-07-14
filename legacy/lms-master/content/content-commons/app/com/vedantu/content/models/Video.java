package com.vedantu.content.models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.utils.ImageDisplayURLUtil;
import com.vedantu.content.commons.interfaces.IIndexable;
import com.vedantu.content.pojos.SrcType.LinkType;
import com.vedantu.content.pojos.VideoInfo;

@Entity(value = "videos", noClassnameStored = true)
public class Video extends AbstractFileModel implements IIndexable {

    @Indexed
    private String cmdsVideoId;
    public boolean published;

    // Linked options
    public long    duration;
    public String  usage;

    public Video() {

        super();
        contentType = EntityType.VIDEO;
    }

    final public String getCmdsVideoId() {

        return cmdsVideoId;
    }

    final public void setCmdsVideoId(String cmdsVideoId) {

        this.cmdsVideoId = cmdsVideoId;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append(" thumbnail : ").append(thumbnail);
        builder.append(" originalFileName : ").append(originalFileName);
        builder.append(" description : ").append(description);
        builder.append(" cmdsVideoId : ").append(cmdsVideoId);
        builder.append(" published : ").append(published);
        builder.append(" extension : ").append(extension);
        builder.append(" linkType : ").append(linkType);
        builder.append(" url : ").append(url);
        builder.append(" duration : ").append(duration);
        builder.append(" converted : ").append(converted);
        builder.append(" stored : ").append(stored);
        builder.append(" uuid : ").append(uuid);
        builder.append(" usage : ").append(usage);

        return builder.toString();
    }

    @Override
    public ModelBasicInfo toBasicInfo() {

        // String orgId = (contentSrc != null) ? contentSrc.id : StringUtils.EMPTY;
        VideoInfo info = new VideoInfo(_getStringId(), name, duration, thumbnail);

        String url = linkType == LinkType.UPLOADED ? ImageDisplayURLUtil.getEntityVideoURL(
                EntityType.VIDEO, uuid, extension, converted ? FileCategory.CONVERTED
                        : FileCategory.ORIGINAL) : this.url;
        info.url = url;
        info.linkType = linkType;
        return info;
   
    }

    @Override
    public long getExportableSize(){
        return size.getEncrypted()+ size.getThumbnail();
    }
}
