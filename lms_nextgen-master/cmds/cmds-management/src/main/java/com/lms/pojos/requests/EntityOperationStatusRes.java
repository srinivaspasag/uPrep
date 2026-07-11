package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.enums.EntityType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntityOperationStatusRes implements IListResponseObj {
	public int numOfSteps;
	public int numCompletedSteps;
	public EntityType type;
	public String id;
	public String errorCode;
	public String message;
	public String jobId;

	public EntityOperationStatusRes() {
		super();
	}

}
