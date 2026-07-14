package com.lms.pojo.request;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class GetDigitalLibraryFieldsReq {

	@NotBlank(message = "orgId should not be null")
    public String           orgId;
}
