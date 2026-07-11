package com.vedantu.organization.pojos.responses.members;

import com.vedantu.mongo.VedantuRecordState;

public class UpdateOrgMemberMappingRes extends AddOrgMemberMappingRes {

	public UpdateOrgMemberMappingRes(String id, VedantuRecordState recordState,
			boolean done) {
		super(id, recordState, done);
	}

}
