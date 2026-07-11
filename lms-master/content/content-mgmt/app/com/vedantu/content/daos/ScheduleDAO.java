package com.vedantu.content.daos;

import java.util.Iterator;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.content.models.Schedule;
import com.vedantu.ei.exceptions.VedantuException;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuDBResult;
import com.vedantu.mongo.MongoManager.SortOrder;
import com.vedantu.mongo.VedantuRecordState;

public class ScheduleDAO extends VedantuBasicDAO<Schedule, ObjectId>{

    private static final ALogger               LOGGER   = Logger.of(ScheduleDAO.class);
    public static final ScheduleDAO INSTANCE = new ScheduleDAO(Schedule.class);

    public ScheduleDAO(Class<Schedule> entityClass) {
        super(entityClass);
    }

    public boolean addSchedule() {
        return false;
    }

    public VedantuDBResult<Schedule> getScheduleByMonth(String orgId, String sectionId, long month) {
        DBObject getScheduleQuery = new BasicDBObject();
        LOGGER.debug("Querying for " + Schedule.class);
        getScheduleQuery.put("orgId", orgId);
        getScheduleQuery.put("sectionId", sectionId);
        getScheduleQuery.put("month", month);
        getScheduleQuery.put("recordState", VedantuRecordState.ACTIVE.name());
        LOGGER.debug("Query: " + getScheduleQuery.toString());
        VedantuDBResult<Schedule> links = getInfos(getScheduleQuery, null, MongoManager.NO_START,
                MongoManager.NO_LIMIT, MongoManager.getSortQuery("day", SortOrder.ASC.name()));
        return links;
    }

    public Schedule getScheduleByDate(String orgId, String sectionId, long month, long day) {
        DBObject getScheduleQuery = new BasicDBObject();
        LOGGER.debug("Querying for " + Schedule.class);
        getScheduleQuery.put("orgId", orgId);
        getScheduleQuery.put("sectionId", sectionId);
        getScheduleQuery.put("month", month);
        getScheduleQuery.put("day", day);
        getScheduleQuery.put("recordState", VedantuRecordState.ACTIVE.name());
        LOGGER.debug("Query: " + getScheduleQuery.toString());
        VedantuDBResult<Schedule> links = getInfos(getScheduleQuery, null, MongoManager.NO_START,
                MongoManager.NO_LIMIT, null);
        Iterator<Schedule> schedule = links.results.iterator();
        while(schedule.hasNext()){
            return schedule.next();
        }
        return null;
    }

    public boolean removeScheduleByDate(String orgId, String sectionId, long month, long day) {
        Schedule removeScheduleQuery = getScheduleByDate(orgId, sectionId, month, day);
        if(removeScheduleQuery == null) {
            return false;
        }else{
            removeScheduleQuery.recordState = VedantuRecordState.DELETED;
            save(removeScheduleQuery);
            return true;
        }
    }
}
