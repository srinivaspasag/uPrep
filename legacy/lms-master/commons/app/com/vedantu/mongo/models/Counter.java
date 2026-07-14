package com.vedantu.mongo.models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "counters", noClassnameStored = true)
@Indexes({ @Index(value = "collection,field", unique = true), })
public class Counter extends VedantuBaseMongoModel {

    public String collection;
    public String field;
    public long   value;

}
