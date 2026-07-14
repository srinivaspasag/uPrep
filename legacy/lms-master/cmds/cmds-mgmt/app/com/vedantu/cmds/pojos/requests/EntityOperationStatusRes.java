package com.vedantu.cmds.pojos.requests;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.responses.IListResponseObj;

public class EntityOperationStatusRes implements IListResponseObj {
	public int			numOfSteps;
	public int			numCompletedSteps;
	public EntityType	type;
	public String		id;
	public String		errorCode;
	public String 		message;
	public String       jobId;

	public EntityOperationStatusRes() {
		super();
	}

}
