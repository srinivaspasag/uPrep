package com.vedantu.organization.pojos.responses.members;

import java.util.HashMap;
import java.util.Map;

import com.vedantu.user.pojos.responses.UploadProfilePicRes;

public class BulkUploadOrgMembersProfilePicRes {

	public Map<String, UploadProfilePicRes> status;

	public BulkUploadOrgMembersProfilePicRes() {
		this.status = new HashMap<String, UploadProfilePicRes>();
	}

}
