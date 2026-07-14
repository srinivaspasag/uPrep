package com.vedantu.organization.pojos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.pojos.utils.OrgStructureBasicInfoNameComparator;

public class OrgProgramCenterBasicInfo extends OrgStructureBasicInfo {

	public List<OrgProgramSectionBasicInfo> sections = new ArrayList<OrgProgramSectionBasicInfo>();

	public OrgProgramCenterBasicInfo(String id, VedantuRecordState recordState,
			String name, String code, EntityType type) {

		super(id, recordState, name, code, type);
	}

	private Map<String, OrgProgramSectionBasicInfo> map = new HashMap<String, OrgProgramSectionBasicInfo>();

	public OrgProgramSectionBasicInfo _getOrAddProgramSection(
			OrgStructureBasicInfo o) {

		if (!map.containsKey(o.id)) {

			map.put(o.id, (OrgProgramSectionBasicInfo) o);
			if (!sections.contains(o)) {
				sections.add((OrgProgramSectionBasicInfo) o);
			}
		}
		Collections
				.sort(sections, OrgStructureBasicInfoNameComparator.INSTANCE);
		return map.get(o.id);
	}

	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder();
		builder.append("{sections:");
		builder.append(sections);
		builder.append(", name:");
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
