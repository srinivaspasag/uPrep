package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
@Setter
@Getter
public class UpdateOrgDepartmentReq extends AddOrgDepartmentReq{
    @NotBlank(message = "departmentId should not be null")
    public String departmentId;
}
