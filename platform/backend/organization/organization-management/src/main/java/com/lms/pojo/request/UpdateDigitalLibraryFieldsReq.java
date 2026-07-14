package com.lms.pojo.request;

import java.util.List;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class UpdateDigitalLibraryFieldsReq {
	@NotBlank(message = "userId should not be null")    
	public String userId;
    public String orgId;
    public List<String> fields;

}
