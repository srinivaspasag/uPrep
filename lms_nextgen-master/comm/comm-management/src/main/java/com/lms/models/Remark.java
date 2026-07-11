package com.lms.models;

import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;

@Document(value = "remarks")
@CompoundIndexes(@CompoundIndex(name = "provideeId"))
public class Remark extends VedantuBaseMongoModel {
	@NotBlank
	public String providerId;

	@NotBlank
	public String provideeId;
	@NotBlank
	public String content;

	public String orgId;

	public Remark() {
		super();
	}

	public Remark(String providerId, String provideeId, String content, String orgId) {
		super();
		this.providerId = providerId;
		this.provideeId = provideeId;
		this.content = content;
		this.orgId = orgId;
	}

}
