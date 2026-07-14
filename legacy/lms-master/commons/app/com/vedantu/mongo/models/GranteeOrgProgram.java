package com.vedantu.mongo.models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "granteeorgprograms", noClassnameStored = true)
@Indexes({ @Index(value = "subscriberOrgId, providerOrgId, programId") })
public class GranteeOrgProgram extends VedantuBaseMongoModel implements IListResponseObj{

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
