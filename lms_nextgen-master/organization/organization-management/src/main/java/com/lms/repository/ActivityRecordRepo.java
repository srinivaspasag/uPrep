package com.lms.repository;

import com.lms.common.vedantu.enums.DeviceType;
import com.lms.models.device.mgmt.ActivityRecord;
import com.lms.pojo.responce.device.mgmt.DeviceStatusRes;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRecordRepo extends MongoRepository<ActivityRecord, String>
{
        List<ActivityRecord> findAllByCallingUserIdAndUserIdAndOrgIdAndDeviceIdAndDeviceTypeAndTimeCreatedGreaterThan(String callinguserid, String userid, String orgid, String deviceid, String deviceType,long logintime );
}
