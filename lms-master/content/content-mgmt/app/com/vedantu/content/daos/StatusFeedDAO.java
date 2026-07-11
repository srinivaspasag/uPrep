package com.vedantu.content.daos;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuException;
import com.vedantu.content.models.StatusFeed;
import com.vedantu.user.daos.AbstractUserActionDAO;

public class StatusFeedDAO extends AbstractUserActionDAO<StatusFeed, ObjectId> {
	private static final ALogger LOGGER = Logger.of(StatusFeedDAO.class);

	public static final StatusFeedDAO INSTANCE = new StatusFeedDAO();

	public StatusFeedDAO() {
		super(StatusFeed.class);
	}

	public StatusFeed addStatusFeed(StatusFeed feed) throws VedantuException {
		LOGGER.debug("saving status feed : " + feed);
		save(feed);
		return feed;
	}

}
