package com.lms.pojos.requests;

import com.lms.common.vedantu.enums.EntityType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.File;

@Getter
@Setter
public class UploadCMDSContentFileReq {

	public String orgId;
	@NotNull(message = "file should not be empty")
	public File file;

	public EntityType entityType;

	@NotBlank(message = "key should not be empty")
	public String key;

}
