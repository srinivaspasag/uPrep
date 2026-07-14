package com.lms.common.vedantu.model;

import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;


@Setter
@Getter
@Document(collection = "counters")
@CompoundIndexes({ @CompoundIndex(name = "collection,field", unique = true), })
public class Counter extends VedantuBaseMongoModel {

    public String collection;
    public String field;
    public long   value;

}
