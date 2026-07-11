package com.lms.news.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.StringUtils;

import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.UserActionType;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.enums.NotificationReason;
import com.lms.managers.news.generator.INewsFanOutCollector;
import com.lms.models.AbstractBoardEntityTagModel;
import com.lms.models.Comment;
import com.lms.models.Discussion;
import com.lms.models.DiscussionRepo;
import com.lms.models.EntityUserActionMapping;
import com.lms.models.StatusFeed;
import com.lms.repository.CommentRepo;
import com.lms.repository.StatusFeedRepo;

public abstract class AbstractSocialNewsFanOutCollector extends AbstractNewsFanOutCollector
		implements INewsFanOutCollector {
	private static final Logger logger = LoggerFactory.getLogger(AbstractSocialNewsFanOutCollector.class);
    @Autowired
	private MongoTemplate mongoTemplate;
    @Autowired
    private DiscussionRepo discussionRepo;
    @Autowired
    private StatusFeedRepo statusFeedRepo;
    @Autowired
    private CommentRepo commentRepo;
	@Override
	public void getNewsSubscribers(NewsActivity activity, Map<NotificationReason, Set<SrcEntity>> subscribers,
			Set<SrcEntity> newsUpdateToDeduplicate) {

		super.getNewsSubscribers(activity, subscribers, newsUpdateToDeduplicate);
		VedantuBaseMongoModel mongoModel = null; 
		if(activity.src.type == EntityType.COMMENT) {
          Optional<Comment> commentOptional = commentRepo.findById(activity.src.id);	 
          if(commentOptional.isPresent()) {
        	  mongoModel = commentOptional.get();
          }
		}

		if (mongoModel == null) {
			return;
		}
		if (activity.src.type == EntityType.COMMENT) {
			Comment comment = (Comment) mongoModel;

			// B upvotes comment of C which is on base entity of A

			if (comment.root != null && !comment.parent.equals(comment.root)) {
				// TODO discuss other thread commenters
				// populateEntityCommenter(comment.root, activity, subscribers,
				// newsUpdateToDeduplicate);
			}

			if (comment.root != null) {
				logger.debug("Finding root parent for comment" + comment.root);
				VedantuBaseMongoModel rootModel = null;
				if(comment.root.type == EntityType.DISCUSSION) {
					Optional<Discussion> discussionOptional = discussionRepo.findById(comment.root.id);
				    if(discussionOptional.isPresent()) {
				    	rootModel = discussionOptional.get();
				    }
				}else if(comment.root.type == EntityType.STATUSFEED) {
					Optional<StatusFeed> statusFeedOptional = statusFeedRepo.findById(comment.root.id);
				    if(statusFeedOptional.isPresent()) {
				    	rootModel = statusFeedOptional.get();
				    }
				}
				if (rootModel != null) {
					if (rootModel != null && rootModel instanceof AbstractBoardEntityTagModel) {
						logger.debug("Found root for comment" + comment.root);
						AbstractBoardEntityTagModel boardEntityTageModel = (AbstractBoardEntityTagModel) rootModel;

						accumulateFanOutEntity(new SrcEntity(EntityType.USER, boardEntityTageModel.userId),
								NotificationReason.ROOT_OWNER, subscribers, newsUpdateToDeduplicate, activity.actor);

						logger.debug("Found root owner for comment " + boardEntityTageModel.userId);
					}
				}
			}
			// TODO discuss other thread commenters
			// populateEntityCommenter(comment.parent, activity, subscribers,
			// newsUpdateToDeduplicate);
		} else {
			/*if (vedantuDAO instanceof IAttemptable) {
				populateEntityAttempter(activity.src, activity, subscribers, newsUpdateToDeduplicate);
			}*/
		}

		// TODO: for now we are not sending notificationn to users who has upvotes the
		// entity
		// collect all the users who has upvoted this entity

		// collect users who has followed the parent entity
		if (activity.info.actionType == UserActionType.COMMENTED) {
			// collect comment user notification data
			populateEntityCommenter(activity.src, activity, subscribers, newsUpdateToDeduplicate,
					NotificationReason.COMMENTED);
		}
		populateEntityFollower(activity.src, activity, subscribers, newsUpdateToDeduplicate);

	}

	protected void populateEntityFollower(SrcEntity src, NewsActivity activity,
			Map<NotificationReason, Set<SrcEntity>> subscribers, Set<SrcEntity> newsUpdateToDeduplicate) {

		List<String> userIds =getEntityUserActionByIds(src, UserActionType.FOLLOWING,
				0, 0, null, null, null);
		if (CollectionUtils.isNotEmpty(userIds)) {
			for (String userId : userIds) {
				accumulateFanOutEntity(new SrcEntity(EntityType.USER, userId), NotificationReason.FOLLOWING_SOURCE,
						subscribers, newsUpdateToDeduplicate, activity.actor);
			}
		}
	}

	protected void populateEntityCommenter(SrcEntity parent, NewsActivity activity,
			Map<NotificationReason, Set<SrcEntity>> subscribers, Set<SrcEntity> newsUpdateToDeduplicate,
			NotificationReason reason) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("parent.id").is(parent.id);
        criteria.and("parent.type").is(parent.type.name());
		query.addCriteria(criteria);
		List<Comment> comments = mongoTemplate.find(query, Comment.class);
		for (Comment comment : comments) {
			accumulateFanOutEntity(new SrcEntity(EntityType.USER, comment.userId), reason, subscribers,
					newsUpdateToDeduplicate, activity.actor);
		}
	}

	protected void populateEntityAttempter(SrcEntity parent, NewsActivity activity,
			Map<NotificationReason, Set<SrcEntity>> subscribers, Set<SrcEntity> newsUpdateToDeduplicate) {

		AtomicLong totalAttempters = new AtomicLong();
		List<String> userIds = getEntityUserActionByIds(activity.src,
				UserActionType.ATTEMPTED,0, 0, null, null, totalAttempters);
		for (String userId : userIds) {
			accumulateFanOutEntity(new SrcEntity(EntityType.USER, userId), NotificationReason.ATTEMPTED, subscribers,
					newsUpdateToDeduplicate, activity.actor);
		}
	}

	public List<String> getEntityUserActionByIds(SrcEntity target, UserActionType actionType, int start, int size,
			String orderBy, String sortOrder, AtomicLong totalHits) {

		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.and(ConstantsGlobal.TARGET_DOT_ID).is(target.getId());

		criteria.and(ConstantsGlobal.TARGET_DOT_TYPE).is(target.getType().name());
		criteria.and(ConstantsGlobal.ACTION_TYPE).is(actionType.name());
		if (StringUtils.isEmpty(orderBy)) {
			orderBy = ConstantsGlobal.TIME_CREATED;
		}

		List<EntityUserActionMapping> results = mongoTemplate.find(query.addCriteria(criteria),
				EntityUserActionMapping.class);

		if (totalHits != null) {
			totalHits.set(results.stream().count());
		}
		List<String> userIds = new ArrayList<String>();
		for (EntityUserActionMapping e : results) {
			userIds.add(e.userId);
		}
		return userIds;
	}

}
