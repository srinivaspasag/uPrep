package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class UpdateOrganizationDownloadStatusReq {

    public boolean disableDownload=false;
    @NotBlank(message = "orgId should not be null")
    public String orgId;
}
