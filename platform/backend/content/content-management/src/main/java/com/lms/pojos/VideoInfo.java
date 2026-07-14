package com.lms.pojos;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.enums.SrcType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VideoInfo  extends ModelBasicInfo {

    public String id;
    public String name;
    public long duration;
    public String thumbnailURL;
    public String url;
    public SrcType.LinkType linkType;


    public VideoInfo(String id, String name, long duration, String thumbnail) {
       // String thumbnailURL = ImageDisplayURLUtil.getEntityThumbnail(EntityType.VIDEO, thumbnail);
        this.id = id;
        this.name = name;
        this.duration = duration;
        //this.thumbnailURL = thumbnailURL;
    }
}