package com.vedantu.content.models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.commons.pojos.ScheduleInfo;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.VedantuBaseMongoModel;
@Entity(value = "moduleschedules", noClassnameStored = true)
@Indexes({ @Index(value = "target.id, target.type,source.id, source.type", unique = false),
           @Index(value = "target.id, target.type,globalSource.id, globalSource.type", unique = false)
         })
public class ModuleSchedules extends VedantuBaseMongoModel{
    public SrcEntity entity;
    public SrcEntity source;
    public SrcEntity globalEntity;
    public SrcEntity globalSource;
    public SrcEntity target;
    public ScheduleInfo schedule;
    //To get startsIn,closesIn and endsIn
    public ModuleScheduleInfo moduleSchedule;
    public String orgId;
    public String userId;

    public ScheduleInfo getSchedule() {

        return schedule;
    }
}
