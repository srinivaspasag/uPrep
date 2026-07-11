package com.lms.pojo.request;

import com.lms.common.vedantu.mongo.VedantuRecordState;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrgCenterInfo extends AbstractOrgStructureInfo {

	public OrgCenterInfo(String id, String name, String code,
						 VedantuRecordState recordState) {
		super(id, name, code, recordState);
	}
}
