package com.vedantu.organization.pojos.responses.organizations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vedantu.commons.pojos.responses.ListResponse;
import com.vedantu.mongo.models.GranteeOrgProgram;


public class GetSharedOrgsRes extends ListResponse<GranteeOrgProgram>{
	
	public Long totalHits;
	public List<OrgBasicInfo> subscriberOrgsInfos = new ArrayList<OrgBasicInfo>();
	public Map<String, String> orgsKeyValue = new HashMap<String,String>();
	
	public GetSharedOrgsRes(){
		
	}
}
