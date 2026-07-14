package com.vedantu.eventbus.news.generators;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.comm.enums.NotificationReason;
import com.vedantu.comm.managers.news.generator.INewsFanOutCollector;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.daos.CommentDAO;
import com.vedantu.content.models.AbstractBoardEntityTagModel;
import com.vedantu.content.models.Comment;
import com.vedantu.mongo.IVedantuModel;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuDBResult;
import com.vedantu.socials.apis.IAttemptable;
import com.vedantu.user.pojos.EntityUserActionDAO;

public abstract class AbstractSocialNewsFanOutCollector extends AbstractNewsFanOutCollector
        implements INewsFanOutCollector {
    private static final ALogger LOGGER = Logger.of(AbstractNewsFanOutCollector.class);

    @Override
    public void getNewsSubscribers(NewsActivity activity,
            Map<NotificationReason, Set<SrcEntity>> subscribers,
            Set<SrcEntity> newsUpdateToDeduplicate) {

        super.getNewsSubscribers(activity, subscribers, newsUpdateToDeduplicate);

        VedantuBasicDAO<VedantuBaseMongoModel, ObjectId> vedantuDAO = EntityTypeDAOFactory.INSTANCE
                .get(activity.src.type);
        if (vedantuDAO == null) {
            return;
        }
        IVedantuModel mongoModel = vedantuDAO.getById(activity.src.id);
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
                LOGGER.debug("Finding root parent for comment"+ comment.root);
                VedantuBasicDAO<?, ?> rootDAO = EntityTypeDAOFactory.INSTANCE
                        .get(comment.root.type);
                if (rootDAO != null) {
                    VedantuBaseMongoModel rootModel = rootDAO.getById(comment.root.id);
                    if (rootModel != null && rootModel instanceof AbstractBoardEntityTagModel) {
                        LOGGER.debug("Found root for comment"+ comment.root);
                        AbstractBoardEntityTagModel boardEntityTageModel = (AbstractBoardEntityTagModel) rootModel;

                        accumulateFanOutEntity(new SrcEntity(EntityType.USER,
                                boardEntityTageModel.userId), NotificationReason.ROOT_OWNER,
                                subscribers, newsUpdateToDeduplicate, activity.actor);

                        LOGGER.debug("Found root owner for comment "+ boardEntityTageModel.userId);
                    }
                }
            }
            // TODO discuss other thread commenters
            // populateEntityCommenter(comment.parent, activity, subscribers,
            // newsUpdateToDeduplicate);
        } else {
            if (vedantuDAO instanceof IAttemptable) {
                populateEntityAttempter(activity.src, activity, subscribers,
                        newsUpdateToDeduplicate);
            }
        }

        // TODO: for now we are not sending notificationn to users who has upvotes the entity
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
            Map<NotificationReason, Set<SrcEntity>> subscribers,
            Set<SrcEntity> newsUpdateToDeduplicate) {

        List<String> userIds = EntityUserActionDAO.INSTANCE.getEntityUserActionByIds(src,
                UserActionType.FOLLOWING, MongoManager.NO_START, MongoManager.NO_LIMIT, null, null,
                null);
        if (CollectionUtils.isNotEmpty(userIds)) {
            for (String userId : userIds) {
                accumulateFanOutEntity(new SrcEntity(EntityType.USER, userId),
                        NotificationReason.FOLLOWING_SOURCE, subscribers, newsUpdateToDeduplicate,
                        activity.actor);
            }
        }
    }

    protected void populateEntityCommenter(SrcEntity parent, NewsActivity activity,
            Map<NotificationReason, Set<SrcEntity>> subscribers,
            Set<SrcEntity> newsUpdateToDeduplicate, NotificationReason reason) {

        DBObject query = new BasicDBObject("parent.id", parent.id);
        query.put("parent.type", parent.type.name());
        VedantuDBResult<Comment> comments = CommentDAO.INSTANCE.getInfos(query, null,
                MongoManager.NO_START, MongoManager.NO_LIMIT, null);
        for (Comment comment : comments.results) {
            accumulateFanOutEntity(new SrcEntity(EntityType.USER, comment.userId), reason,
                    subscribers, newsUpdateToDeduplicate, activity.actor);
        }
    }

    protected void populateEntityAttempter(SrcEntity parent, NewsActivity activity,
            Map<NotificationReason, Set<SrcEntity>> subscribers,
            Set<SrcEntity> newsUpdateToDeduplicate) {

        MutableInt totalAttempters = new MutableInt();
        List<String> userIds = EntityUserActionDAO.INSTANCE.getEntityUserActionByIds(activity.src,
                UserActionType.ATTEMPTED, MongoManager.NO_START, MongoManager.NO_LIMIT, null, null,
                totalAttempters);
        for (String userId : userIds) {
            accumulateFanOutEntity(new SrcEntity(EntityType.USER, userId),
                    NotificationReason.ATTEMPTED, subscribers, newsUpdateToDeduplicate,
                    activity.actor);
        }
    }
}
