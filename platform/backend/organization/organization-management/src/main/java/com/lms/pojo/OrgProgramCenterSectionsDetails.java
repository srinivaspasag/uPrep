package com.lms.pojo;

import com.lms.common.vedantu.mongo.VedantuRecordState;

import java.util.HashSet;
import java.util.Set;


public class OrgProgramCenterSectionsDetails {

	public String programId;
	public VedantuRecordState recordState;
	public Set<OrgProgramCenterSections> centerSections = new HashSet<OrgProgramCenterSections>();
	public boolean isAdded;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OrgProgramCenterSectionsDetails [programId=");
		builder.append(programId);
		builder.append(", recordState=");
		builder.append(recordState);
		builder.append(", centerSections=");
		builder.append(centerSections);
		builder.append(", isAdded=");
		builder.append(isAdded);
		builder.append("]");
		return builder.toString();
	}

}
