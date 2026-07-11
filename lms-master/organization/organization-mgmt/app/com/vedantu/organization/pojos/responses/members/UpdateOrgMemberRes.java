package com.vedantu.organization.pojos.responses.members;

import com.vedantu.mongo.VedantuRecordState;

public class UpdateOrgMemberRes extends AddOrgMemberRes {

	public UpdateOrgMemberRes(String id, VedantuRecordState recordState,
			String orgId, String userId) {
		super(id, recordState, orgId, userId);
	}

}
