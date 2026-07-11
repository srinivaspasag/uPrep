package com.vedantu.comm.pojos;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.content.pojos.tests.ResourceInfo;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.user.pojos.UserInfo;

public class RemarkInfo extends ResourceInfo {

	public String	content;
	public UserInfo	addedFor;

	public RemarkInfo(String id, String name, EntityType type,
			long timeCreated, long lastUpdated, String addedBy,
			long programsAddedTo, VedantuRecordState recordState) {
		super(id, name, type, timeCreated, lastUpdated, addedBy,
				programsAddedTo, recordState);
	}

}
