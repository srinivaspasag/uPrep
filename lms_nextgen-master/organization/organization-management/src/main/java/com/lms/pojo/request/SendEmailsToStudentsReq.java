package com.lms.pojo.request;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class SendEmailsToStudentsReq {
	@NotBlank(message = "subject should not be null")
    public String subject;
	@NotBlank(message = "message should not be null")
    public String message;
	@NotBlank(message = "programId should not be null")
    public String programId;
	@NotBlank(message = "sectionId should not be null")
    public String sectionId;
	@NotBlank(message = "centerId should not be null")
    public String centerId;
	@NotBlank(message = "orgId should not be null")
    public String orgId;
	@NotBlank(message = "userId should not be null")
    public String userId;

}
