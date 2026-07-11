package com.vedantu.organization.pojos;

import org.apache.commons.lang3.StringUtils;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.mongo.VedantuRecordState;

public class OrgStructureBasicInfo extends ModelBasicInfo {

	public String name;
	public String code;
	public EntityType type;

	public OrgStructureBasicInfo(String id, VedantuRecordState recordState,
			String name, String code, EntityType type) {
		super(id, recordState);
		this.name = name;
		this.code = code;
		this.type = type;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof OrgStructureBasicInfo)) {
			return false;
		}
		OrgStructureBasicInfo t = (OrgStructureBasicInfo) o;
		return null != t && StringUtils.equals(id, t.id) && type == t.type;
	}

	@Override
	public int hashCode() {
		return (id + (null != type ? type.name() : StringUtils.EMPTY))
				.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{name:");
		builder.append(name);
		builder.append(", code:");
		builder.append(code);
		builder.append(", type:");
		builder.append(type);
		builder.append(", id:");
		builder.append(id);
		builder.append(", recordState:");
		builder.append(recordState);
		builder.append("}");
		return builder.toString();
	}

}
