package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class UpdateEndTimeMappingReq {
    @NotBlank(message = "orgId should not be null")
    public String orgId;
    @NotBlank(message = "targetUserId should not be null")
    public String targetUserId;
    @NotBlank(message = "sectionId should not be null")
    public String sectionId;
    @NotNull
    public long endTime;

}
