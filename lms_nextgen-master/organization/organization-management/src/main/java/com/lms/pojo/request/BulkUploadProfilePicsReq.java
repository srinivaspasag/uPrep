package com.lms.pojo.request;

import com.lms.common.vedantu.commons.pojos.requests.AbstractFileUploadReq;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;


@Getter
@Setter
public class BulkUploadProfilePicsReq extends AbstractFileUploadReq {

	public String orgId;

	public BulkUploadProfilePicsReq(MultipartFile body) {
		super(body);

	}


}
