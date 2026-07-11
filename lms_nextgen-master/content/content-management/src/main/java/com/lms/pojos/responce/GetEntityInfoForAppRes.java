package com.lms.pojos.responce;

import com.lms.common.vedantu.enums.UserRatingType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetEntityInfoForAppRes {

    public boolean showToggleSwitch;
    public String webmUrl;
    public String mp4Url;
    public UserRatingType rating;
    public String feedback;
}
