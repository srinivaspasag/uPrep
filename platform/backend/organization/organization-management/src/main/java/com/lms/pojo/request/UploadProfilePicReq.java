package com.lms.pojo.request;


import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UploadProfilePicReq extends
		com.lms.user.vedantu.user.requests.UploadProfilePicReq {

	public String orgId;
	public String targetUserId;
	public String targetOrgMemberId;

	public UploadProfilePicReq(MultipartFile body) {
		super(body);

	}


}
