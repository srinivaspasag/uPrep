package com.vedantu.cmds.pojos.requests;

import play.data.validation.Constraints.Required;

public class GetFoldersReq  {

	public String folderId;
	@Required
	public String orgId;
	@Required
	public String userId;
	public int start;
	public int size;
}
