package com.vedantu.cmds.daos;

import java.util.List;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.vedantu.cmds.models.Notification;
import com.vedantu.commons.VedantuException;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;


public class NotificationDAO<T extends VedantuBaseMongoModel, K> extends
		VedantuBasicDAO<Notification, ObjectId> {

	private static final ALogger LOGGER = Logger.of(NotificationDAO.class);

	public static final NotificationDAO INSTANCE = new NotificationDAO();

	private NotificationDAO() {

		super(Notification.class);
	}

	public Notification registerById(Notification notification) throws VedantuException {

		save(notification);
		return notification;
	}

	public List<Notification> getRegIds(String orgId) {

        Query<Notification> query = getQuery();
        query.filter("orgId", orgId);
		List<Notification> regIds = query.asList();
		return regIds;
    }

    public List<Notification> getRegIdsByPrograms(String orgId, List<String> programNames) {

        Query<Notification> query = getQuery();
        query.filter("orgId", orgId);
        query.filter("programName in",programNames);
		List<Notification> regIds = query.asList();
		return regIds;
    }

}

