package com.vedantu.organization.pojos.responses.members;

import com.vedantu.mongo.VedantuRecordState;

public class RemoveOrgMemberMappingRes extends AddOrgMemberMappingRes {

	public RemoveOrgMemberMappingRes(String id, VedantuRecordState recordState,
			boolean done) {
		super(id, recordState, done);
	}

}
