package com.vedantu.content.models;

import java.util.ArrayList;
import java.util.List;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;
import com.vedantu.commons.pojos.SubjectMetadata;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "schedule", noClassnameStored = true)
public class Schedule extends VedantuBaseMongoModel{
    @Indexed
    public String orgId;
    public String programId;
    public String centerId;
    @Indexed
    public String sectionId;
    @Indexed
    public long month;
    public long day;
    public List<String> boardIds = new ArrayList<String>();
    public List<SubjectMetadata> metadata = new ArrayList<SubjectMetadata>();

    public Schedule() {

    }
}
