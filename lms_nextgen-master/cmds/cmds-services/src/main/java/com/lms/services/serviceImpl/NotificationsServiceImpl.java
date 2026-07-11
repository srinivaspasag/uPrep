package com.lms.services.serviceImpl;

import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.models.Notification;
import com.lms.pojos.requests.GetRegIdsReq;
import com.lms.pojos.requests.NotificationRegIDReq;
import com.lms.pojos.responce.GetRegIdsRes;
import com.lms.pojos.responce.NotificationRegIDRes;
import com.lms.repo.NotificationRepo;
import com.lms.services.NotificationsService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationsServiceImpl implements NotificationsService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationsServiceImpl.class);

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private NotificationRepo notificationRepo;

    @Override
    public VedantuResponse getRegIds(GetRegIdsReq request) {
        GetRegIdsRes getRegIdsRes = new GetRegIdsRes();
        List<Notification> getRegIds;
        if (CollectionUtils.isEmpty(request.programNames))
            getRegIds = getRegIds(request.orgId);
        else
            getRegIds = getRegIdsByPrograms(request.orgId, request.programNames);
        getRegIdsRes.totalHits = getRegIds.size();
        if (getRegIdsRes.totalHits == 0)
            return new VedantuResponse(getRegIdsRes);
        for (Notification regId : getRegIds) {
            getRegIdsRes.list.add(regId.toBasicInfo());
        }
        return new VedantuResponse(getRegIdsRes);
    }

    @Override
    public VedantuResponse registerById(NotificationRegIDReq request) {
        Notification notificationGCMID = new Notification();
        notificationGCMID.regId = request.regId;
        notificationGCMID.deviceId = request.deviceId;
        notificationGCMID.userId = request.userId;
        notificationGCMID.orgId = request.orgId;
        notificationGCMID.programName = request.programName;
        Notification noti = registerById(notificationGCMID);
        NotificationRegIDRes notificationRegIdRes = new NotificationRegIDRes();
        if (noti != null) {
            notificationRegIdRes.id = noti._getStringId();
            logger.debug("user registered by registration id with id: " + notificationRegIdRes.id);
        }
        return new VedantuResponse(notificationRegIdRes);
    }

    public Notification registerById(Notification notification) throws VedantuException {

        notificationRepo.save(notification);
        return notification;
    }

    public List<Notification> getRegIds(String orgId) {

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("orgId").is(orgId);
        query.addCriteria(criteria);
        List<Notification> regIds = mongoTemplate.find(query, Notification.class);
        return regIds;
    }


    public List<Notification> getRegIdsByPrograms(String orgId, List<String> programNames) {

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("orgId").is(orgId);
        criteria.and("programName in").is(programNames);
        query.addCriteria(criteria);
        List<Notification> regIds = mongoTemplate.find(query, Notification.class);
        return regIds;
    }
}
