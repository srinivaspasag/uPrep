package com.lms.board.pojos.requests;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class UploadOrgBoardReq extends UploadConsumerBoardReq {
	@NotBlank(message = "orgId should not be empty")
	public String orgId;

	public UploadOrgBoardReq(MultipartFile body) {
		super(body);
		// TODO Auto-generated constructor stub
	}
}
