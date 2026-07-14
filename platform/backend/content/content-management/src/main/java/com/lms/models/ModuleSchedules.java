package com.lms.models;

import com.lms.common.pojos.ScheduleInfo;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "moduleschedules")
@CompoundIndexes({@CompoundIndex(name = "target.id, target.type,source.id, source.type", unique = false),
        @CompoundIndex(name = "target.id, target.type,globalSource.id, globalSource.type", unique = false)
})
public class ModuleSchedules extends VedantuBaseMongoModel {
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
