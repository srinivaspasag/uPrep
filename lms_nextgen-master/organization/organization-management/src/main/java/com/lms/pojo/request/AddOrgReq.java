package com.lms.pojo.request;

import com.lms.common.vedantu.commons.pojos.requests.Location;
import com.lms.user.vedantu.user.pojo.UserBasicInfo;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
@Setter
@Getter
public class AddOrgReq extends AbstractAddOrgReq {

   @NotBlank(message = "representative should not be null")
    public UserBasicInfo representative;

    @NotBlank(message = "planId should not be null")
    public String        planId;

    @NotBlank(message = "tncVersion should not be null")
    public String        tncVersion;
    public boolean       isNewUI;
    public String        theme;
    public boolean       showSharedSubjects;


}
