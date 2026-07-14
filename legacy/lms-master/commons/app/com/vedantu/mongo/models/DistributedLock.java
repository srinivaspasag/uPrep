package com.vedantu.mongo.models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "locks", noClassnameStored = true)
@Indexes({ @Index(value = "key", unique = true) })
public class DistributedLock extends VedantuBaseMongoModel {

    public String  key;
    public boolean acquired;

}
