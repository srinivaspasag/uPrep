package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;

@Setter
@Getter
public class UpdateOrganizationClassroomConnectStatusReq {

    @NotNull(message = "show classroom connect should not be null")
    public boolean showClassroomConnect;
    @NotBlank(message = "orgId should Not be null")
    public String orgId;
}
