package com.vedantu.organization.daos.device.mgmt;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.DeviceType;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.organization.models.device.mgmt.ActivityRecord;

public class ActivityRecordDAO extends VedantuBasicDAO<ActivityRecord, ObjectId> {

    private static final ALogger          LOGGER   = Logger.of(ActivityRecordDAO.class);

    public static final ActivityRecordDAO INSTANCE = new ActivityRecordDAO();

    private ActivityRecordDAO() {

        super(ActivityRecord.class);
    }

    public ActivityRecord addActivity(String callingAppId, String callingApp, String callingUserId,
            String userId, String orgId, String deviceId, DeviceType deviceType, String page,
            String action, SrcEntity entity, long time) throws VedantuException {

        ActivityRecord record = new ActivityRecord(callingAppId, callingApp, callingUserId, userId,
                orgId, deviceId, deviceType, page, action, entity);
        if (time != 0) {
            record.timeCreated = time;
        }
        save(record);
        return record;
    }

    public List<ActivityRecord> getActivity(String userId, String orgId, String deviceId,
            int start, int size, MutableLong totalHits) throws VedantuException {

        Query<ActivityRecord> recordQuery = ActivityRecordDAO.INSTANCE.getQuery();
        // this will only allow to see activits doesn by himself; not on the behalf of other users
        recordQuery.filter("callingUserId", userId);
        recordQuery.filter("userId", userId);
        recordQuery.filter("orgId", orgId);
        recordQuery.filter("deviceId", deviceId);
        recordQuery.offset(start).limit(size);
        LOGGER.debug(" Query " + recordQuery);
        return recordQuery.asList();
    }
    
    public boolean activityFound(String userId,String orgId,EntityType entityType,String action,String id){
    	
    	 Query<ActivityRecord> recordQuery = ActivityRecordDAO.INSTANCE.getQuery();
    	 recordQuery.filter("userId",userId );
    	 recordQuery.filter("entity.type",entityType );
    	 recordQuery.filter("action",action);
    	 recordQuery.filter("orgId", orgId);
    	 recordQuery.filter("entity.id", id);
       	 recordQuery.order("-timeCreated");
       	 LOGGER.debug(" Query " + recordQuery);
    	 List<ActivityRecord> activityRecords=recordQuery.asList();
    	 LOGGER.info("activityRecords "+activityRecords.size());
    	 return activityRecords.size()>0?true:false;
    }
    
    public List<ActivityRecord> getActivities(String userId,String orgId,EntityType entityType,String action,String id){
    	
   	 Query<ActivityRecord> recordQuery = ActivityRecordDAO.INSTANCE.getQuery();
   	 recordQuery.filter("userId",userId );
   	 recordQuery.filter("entity.type",entityType );
   	 recordQuery.filter("action",action);
   	 recordQuery.filter("orgId", orgId);
   	 recordQuery.filter("entity.id", id);
   	 recordQuery.order("-timeCreated");
   	 LOGGER.debug(" Query " + recordQuery);
   	 return recordQuery.asList();
   }

    public ActivityRecord getLastActivity(String userId, String orgId, String deviceId,
            long loginTime, String deviceType) throws VedantuException {

        Query<ActivityRecord> recordQuery = ActivityRecordDAO.INSTANCE.getQuery();
        // this will only allow to see activits doesn by himself; not on the behalf of other users
        recordQuery.filter("callingUserId", userId);
        recordQuery.filter("userId", userId);
        recordQuery.filter("orgId", orgId);
        recordQuery.filter("deviceId", deviceId);
        recordQuery.filter("deviceType", deviceType);
        recordQuery.field("timeCreated").greaterThan(loginTime);
        recordQuery.order("-timeCreated");
        recordQuery.offset(0).limit(1);

        LOGGER.debug(" Query " + recordQuery);
        List<ActivityRecord> recordLists = recordQuery.asList();
        if (CollectionUtils.isNotEmpty(recordLists)) {
            return recordQuery.asList().get(0);
        }
        return null;
    }
        
    public List<ActivityRecord> getActivities(String userId,String orgId,int offset, int limit){
    	
      	 Query<ActivityRecord> recordQuery = ActivityRecordDAO.INSTANCE.getQuery();
      	 recordQuery.filter("userId",userId );
      	 recordQuery.filter("orgId", orgId);
      	 recordQuery.order("-timeCreated");
      	 recordQuery.offset(offset).limit(limit);
      	 LOGGER.debug(" Query " + recordQuery);
      	 return recordQuery.asList();
      }

}
