package com.vedantu.cmds.managers;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.daos.NotificationDAO;
import com.vedantu.cmds.models.Notification;
import com.vedantu.cmds.pojos.requests.notifications.GetRegIdsReq;
import com.vedantu.cmds.pojos.requests.notifications.NotificationRegIDReq;
import com.vedantu.cmds.pojos.responses.notifications.GetRegIdsRes;
import com.vedantu.cmds.pojos.responses.notifications.NotificationRegIDRes;
import com.vedantu.commons.VedantuException;
import com.vedantu.user.managers.AbstractVedantuEventManager;

public class NotificationManager extends AbstractVedantuEventManager {

    public static NotificationManager INSTANCE = new NotificationManager();

    private final static ALogger    LOGGER   = Logger.of(NotificationManager.class);

    public NotificationManager() {

    }

    public NotificationRegIDRes registerById(NotificationRegIDReq request)
            throws VedantuException {
        Notification notificationGCMID = new Notification();
        notificationGCMID.regId = request.regId;
        notificationGCMID.deviceId = request.deviceId;
        notificationGCMID.userId = request.userId;
        notificationGCMID.orgId = request.orgId;
        notificationGCMID.programName = request.programName;
        Notification noti = NotificationDAO.INSTANCE.registerById(notificationGCMID);
        NotificationRegIDRes notificationRegIdRes =  new NotificationRegIDRes();
        if(noti!=null)
        {
        	notificationRegIdRes.id = noti._getStringId();
        	LOGGER.debug("user registered by registration id with id: " + notificationRegIdRes.id);
        }
        return notificationRegIdRes;
    }

    public GetRegIdsRes getRegIds(GetRegIdsReq request) throws VedantuException {
        GetRegIdsRes getRegIdsRes =  new GetRegIdsRes();
        List<Notification> getRegIds;
        if(CollectionUtils.isEmpty(request.programNames))
            getRegIds = NotificationDAO.INSTANCE.getRegIds(request.orgId);
        else
            getRegIds = NotificationDAO.INSTANCE.getRegIdsByPrograms(request.orgId,request.programNames);
        getRegIdsRes.totalHits = getRegIds.size();
        if(getRegIdsRes.totalHits == 0)
            return getRegIdsRes;
        for (Notification regId : getRegIds) {
            getRegIdsRes.list.add(regId.toBasicInfo());
        }
        return getRegIdsRes;
    }
}