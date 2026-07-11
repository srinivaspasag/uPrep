package com.lms.models;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(value = "contentgroups")
public class ContentGroup extends VedantuBaseMongoModel {

    @Transient
    public static final String CONTENTS = "contents";
    @Transient
    public static final String TARGET = "target";

    public List<SrcEntity> contents;
    public SrcEntity target;

    public ContentGroup() {

        super();
        contents = new ArrayList<SrcEntity>();
    }
}
