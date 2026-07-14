package com.vedantu.content.daos;

import java.util.Set;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.enums.CommentType;
import com.vedantu.content.models.Comment;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuDBResult;
import com.vedantu.user.daos.AbstractUserActionDAO;

public class CommentDAO extends AbstractUserActionDAO<Comment, ObjectId> {

	private static final ALogger LOGGER = Logger.of(AnswerDAO.class);

	public static final CommentDAO INSTANCE = new CommentDAO();

	private CommentDAO() {
		super(Comment.class);
	}

	public Comment addComment(SrcEntity base, SrcEntity root, SrcEntity parent,
			Scope scope, String userId, String content, Set<String> tags,
			CommentType type) {

		Comment comment = new Comment(userId, content, parent, type, base,
				root, scope, tags);
		LOGGER.debug("saving comment : " + comment);
		save(comment);
		return comment;
	}

	public Comment getComment(String id) throws VedantuException {
		Comment comment = getById(id);
		if (comment == null) {
			throw new VedantuException(VedantuErrorCode.COMMENT_NOT_FOUND);
		}
		return comment;
	}

	public VedantuDBResult<Comment> getComments(SrcEntity parent, int start,
			int size, String orderBy, String sortOrder) {
		DBObject query = new BasicDBObject(ConstantsGlobal.PARENT,
				parent.toDBObject());
		VedantuDBResult<Comment> comments = getInfos(query, null, start, size,
				MongoManager.getSortQuery(orderBy, sortOrder));
		return comments;
	}
}
