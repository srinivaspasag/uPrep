package com.lms.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class GetOrgReq {
    public String  userId;
    public boolean checkForTNC;

    @NotBlank(message = "orgId should not be null")
    public String  orgId;
    public boolean getKey;
}
