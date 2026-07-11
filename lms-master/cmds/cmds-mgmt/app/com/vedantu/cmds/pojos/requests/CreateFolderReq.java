package com.vedantu.cmds.pojos.requests;

import play.data.validation.Constraints.Required;


public class CreateFolderReq {
	@Required
	public String userId;
	@Required
	public String orgId; 
	@Required
	public String name;
	public String parentFolderId;

	
}
