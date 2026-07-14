package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.common.vedantu.entity.storage.MediaType;
import com.lms.common.vedantu.enums.EntityType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class SignUploadFileReq extends AbstractAuthCheckReq {

	@NotBlank(message = "orgId should not be empty")
	public String orgId;
	@NotBlank(message = "fileName should not be empty")
	public String fileName;
	@NotNull(message = "type should not be empty")
	public EntityType type;
	@NotNull(message = "mediaType should not be empty")
	public MediaType mediaType;

	public String url;

}
