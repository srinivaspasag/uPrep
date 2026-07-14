package com.vedantu.content.models;

import java.util.ArrayList;
import java.util.List;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Transient;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "contentgroups", noClassnameStored = true)
public class ContentGroup extends VedantuBaseMongoModel {
  
    @Transient
    public static final String CONTENTS = "contents";
    @Transient
    public static final String TARGET = "target";

    public List<SrcEntity> contents;
    public SrcEntity       target;
 
    public ContentGroup() {

        super();
        contents = new ArrayList<SrcEntity>();
    }
}
