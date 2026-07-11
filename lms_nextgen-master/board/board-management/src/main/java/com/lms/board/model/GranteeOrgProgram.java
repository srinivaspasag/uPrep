package com.lms.board.model;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "granteeorgprograms")
@Getter
@Setter
@CompoundIndexes({ @CompoundIndex(name = "subscriberOrgId, providerOrgId, programId") })

public class GranteeOrgProgram extends VedantuBaseMongoModel implements IListResponseObj {

	public String providerOrgId;
	public String programId;
	public String subscriberOrgId;

	public GranteeOrgProgram() {

	}

	public GranteeOrgProgram(String providerOrgId,
							 String subscriberOrgId, String progrmId) {

		this.subscriberOrgId = subscriberOrgId;
		this.providerOrgId = providerOrgId;
		this.programId = progrmId;
	}

	public String getProviderOrgId() {
		return providerOrgId;
	}

	public String getSubscriberOrgId() {
		return subscriberOrgId;
	}

	public String getProgramId() {
		return programId;
	}


	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder();
		builder.append("GranteeOrgProgram [providerOrgId=");
		builder.append(providerOrgId);
		builder.append(", programId=");
		builder.append(programId);
		builder.append(", subscriberOrgId=");
		builder.append(subscriberOrgId);
		builder.append(", ]");
		return builder.toString();
	}

}
