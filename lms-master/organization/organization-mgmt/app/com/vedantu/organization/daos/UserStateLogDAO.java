package com.vedantu.organization.daos;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.vedantu.commons.pojos.Interval;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.organization.models.UserStateLog;

public class UserStateLogDAO extends VedantuBasicDAO<UserStateLog, ObjectId> {

    private static final ALogger        LOGGER   = Logger.of(UserStateLogDAO.class);

    public static final UserStateLogDAO INSTANCE = new UserStateLogDAO();

    private UserStateLogDAO() {

        super(UserStateLog.class);
    }

    public boolean recordChange(String orgId, String userId,
            String setByUserId,  
            Interval interval) {

        UserStateLog userStateLog = new UserStateLog();
        userStateLog.orgId = orgId;
        userStateLog.userId = userId;
        userStateLog.setByUserId = setByUserId;
        userStateLog.interval = interval;
        Key<UserStateLog> result = save(userStateLog);
        if (result != null) {
            return true;
        }
        return false;
    }

    
    public List<Interval> getMemberActivationPeriods(String orgId, String userId, Interval queryInterval)
    {
        LOGGER.debug("getMemberActivationPeriods orgId: " + orgId + ", userId: " + userId);
        Query<UserStateLog> query = getQuery();
        //((y1<x2)&&(x1<y2)), where (y1,y2) is period to be passed 
        query.field("orgId").equal(orgId);
        query.field("userId").equal(userId);
        long startOfQueryInterval = queryInterval.getFrom();
        long endOfQueryInterval = queryInterval.getTill();
        query.field("interval.till").greaterThan(startOfQueryInterval);
        query.field("interval.from").lessThan(endOfQueryInterval);
        
        List<UserStateLog> userStateLogs = query.asList();
        List<Interval> intervals = new ArrayList<Interval>();
        
        for(UserStateLog userStateLog : userStateLogs)
        {
            Interval interval = new Interval();
            interval.setFrom(userStateLog.interval.getFrom());
            interval.setTill(userStateLog.interval.getTill());
            
            if(userStateLog.interval.getFrom() < startOfQueryInterval)
            {
                interval.setFrom(startOfQueryInterval);
            }
            
            if(userStateLog.interval.getTill() > endOfQueryInterval)
            {
                interval.setTill(endOfQueryInterval);
            }
            intervals.add(interval);
        }
        return intervals;
    }
}
