package com.lms.common.vedantu.mongo;

import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.OperationType;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "entityoperationstatus")
@CompoundIndexes(@CompoundIndex(name = "type,id"))
public class EntityOperationStatus extends VedantuBaseMongoModel {

	public EntityType type;
	public String id;

	public int numOfSteps;
	public int numOfStepsCompleted;
	public String errorCode;
	public String message;
	public OperationType oType;

	public EntityOperationStatus() {

		numOfSteps = 0;
		numOfStepsCompleted = 0;

	}
}
