package com.lms.pojos.responce;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetCMDSEntityInfoRes {

    public boolean showToggleSwitch;
    public String webmUrl;
    public String mp4Url;
    public int badRatingCount;
    public int goodRatingCount;
    public int avgRatingCount;
    public long reviewCount;


}
