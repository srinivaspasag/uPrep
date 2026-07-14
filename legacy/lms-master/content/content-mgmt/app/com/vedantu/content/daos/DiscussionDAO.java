package com.vedantu.content.daos;

import java.util.List;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.models.Discussion;
import com.vedantu.user.daos.AbstractUserActionDAO;

public class DiscussionDAO extends AbstractUserActionDAO<Discussion, ObjectId> {

	private static final ALogger LOGGER = Logger.of(DiscussionDAO.class);

	public static final DiscussionDAO INSTANCE = new DiscussionDAO();

	private DiscussionDAO() {
		super(Discussion.class);
	}

	public Discussion addDiscussion(String userId, String name, String content,
			List<String> brdIds, List<String> targetIds, List<String> tags,
			Scope scope, SrcEntity contentSrc) throws VedantuException {
		Discussion diss = new Discussion(name, content, userId);
		diss.scope = scope;
		diss.contentSrc = contentSrc;
		diss.addBoards(brdIds);
		diss.addTargets(targetIds);
		diss.addTags(tags);
		LOGGER.debug("saving discussion : " + diss);
		save(diss);

		return diss;
	}

	public Discussion getDiscussion(String id) throws VedantuException {
		Discussion diss = getById(id);
		if (diss == null) {
			throw new VedantuException(VedantuErrorCode.DISCUSSION_NOT_FOUND);
		}
		return diss;
	}
}
