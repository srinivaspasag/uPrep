package com.lms.repository;

import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.models.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ScheduleRepo  extends MongoRepository<Schedule, String> {
    List<Schedule> findByOrgIdAndSectionIdAndMonthAndRecordState(String orgId, String sectionId, long month, VedantuRecordState active);

    List<Schedule>  findByOrgIdAndSectionIdAndMonthAndRecordStateAndDay(String orgId, String sectionId, long month, VedantuRecordState active, long day);
}
