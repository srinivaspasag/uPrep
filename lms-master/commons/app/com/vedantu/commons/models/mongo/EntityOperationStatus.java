package com.vedantu.commons.models.mongo;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.OperationType;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "entityoperationstatus", noClassnameStored = true)
@Indexes(@Index(value = "type,id"))
public class EntityOperationStatus extends VedantuBaseMongoModel {

    public EntityType    type;
    public String        id;

    public int           numOfSteps;
    public int           numOfStepsCompleted;
    public String        errorCode;
    public String        message;
    public OperationType oType;

    public EntityOperationStatus() {

        numOfSteps = 0;
        numOfStepsCompleted = 0;

    }
}
