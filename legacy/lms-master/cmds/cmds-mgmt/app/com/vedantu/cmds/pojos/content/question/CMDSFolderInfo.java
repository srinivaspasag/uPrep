package com.vedantu.cmds.pojos.content.question;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.mongo.VedantuRecordState;

public class CMDSFolderInfo extends CMDSResourceInfo{

	public Map<EntityType, Integer> stats;
	public List<CMDSFolderInfo> parents;
	public CMDSFolderInfo(String id, String name, EntityType type,
			String orgId, long timeCreated, long lastUpdated, String addedBy,
			long programsAddedTo, boolean published, boolean completed, boolean converted, String globalId,
			VedantuRecordState recordState) {
		super(id, name, type, orgId, timeCreated, lastUpdated, addedBy,
				programsAddedTo, published, completed, converted, globalId, recordState);
		
		parents = new ArrayList<CMDSFolderInfo>();

	}

}
