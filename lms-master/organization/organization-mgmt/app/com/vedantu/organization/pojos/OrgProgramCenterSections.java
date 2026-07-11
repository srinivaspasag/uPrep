package com.vedantu.organization.pojos;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class OrgProgramCenterSections {

	public String centerId;
	public List<String> sectionIds;

	public OrgProgramCenterSections() {
		this(null);
	}

	public OrgProgramCenterSections(String centerId) {
		this.centerId = centerId;
		sectionIds = new ArrayList<String>();
	}

	public boolean hasSection(String sectionId) {
		return null != sectionIds && StringUtils.isNotEmpty(sectionId)
				&& sectionIds.contains(sectionId);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof OrgProgramCenterSections)) {
			return false;
		}
		OrgProgramCenterSections t = (OrgProgramCenterSections) o;
		return null != t && StringUtils.equals(centerId, t.centerId);
	}

	@Override
	public int hashCode() {
		return StringUtils.defaultIfEmpty(centerId, StringUtils.EMPTY)
				.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OrgProgramCenterSections [centerId=");
		builder.append(centerId);
		builder.append(", sectionIds=");
		builder.append(sectionIds);
		builder.append("]");
		return builder.toString();
	}

}
