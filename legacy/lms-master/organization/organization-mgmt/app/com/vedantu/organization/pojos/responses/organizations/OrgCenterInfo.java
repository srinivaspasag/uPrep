package com.vedantu.organization.pojos.responses.organizations;

import com.vedantu.mongo.VedantuRecordState;

public class OrgCenterInfo extends AbstractOrgStructureInfo {

	public OrgCenterInfo(String id, String name, String code,
			VedantuRecordState recordState) {
		super(id, name, code, recordState);
	}
}
