package com.lms.models;

import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.pojos.SubjectMetadata;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(value = "schedule")
@Setter
@Getter
public class Schedule extends VedantuBaseMongoModel {
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
