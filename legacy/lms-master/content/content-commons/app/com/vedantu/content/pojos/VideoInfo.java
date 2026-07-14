package com.vedantu.content.pojos;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.utils.ImageDisplayURLUtil;
import com.vedantu.content.pojos.SrcType.LinkType;

public class VideoInfo  extends ModelBasicInfo{

    public String id;
    public String name;
    public long duration;
    public String thumbnailURL;
    public String url;
    public LinkType linkType;
    

    public VideoInfo(String id, String name, long duration, String thumbnail) {
      String thumbnailURL = ImageDisplayURLUtil.getEntityThumbnail(EntityType.VIDEO, thumbnail);
      this.id = id;
      this.name = name;
      this.duration = duration;
      this.thumbnailURL = thumbnailURL;
    }
}