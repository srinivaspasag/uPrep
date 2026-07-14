package com.lms.managers;

import com.google.gson.Gson;
import com.lms.common.exception.VedantuException;
import com.lms.common.news.NewsActivity;
import com.lms.common.news.NewsUtils;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.Scope;
import com.lms.common.vedantu.event.api.IEventDetails;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.enums.NewsContext;
import com.lms.enums.NotificationReason;
import com.lms.managers.news.NewsFeedSecurityVaildator;
import com.lms.managers.news.UserSecuritySet;
import com.lms.managers.news.generator.INewsFanOutCollector;
import com.lms.models.AbstractBoardEntityTagModel;
import com.lms.models.NewsActivityInfo;
import com.lms.models.NewsFeed;
import com.lms.models.NewsNotification;
import com.lms.news.generators.NewsFanOutCollectorFactory;
import com.lms.pojos.news.NewsActivityRef;
import com.lms.pojos.news.NewsFeedInfo;
import com.lms.repository.NewsActivityInfoRepo;
import com.lms.repository.NewsFeedRepo;
import com.lms.repository.NewsNotificationRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;

@Component
public class NewsActivityGenerator {

    public static final NewsActivityGenerator INSTANCE = new NewsActivityGenerator();
    private static final Logger logger = LoggerFactory.getLogger(NewsActivityGenerator.class);
    @Autowired
    private NewsActivityInfoRepo newsActivityInfoRepo;
    @Autowired
    private NewsFeedRepo newsFeedRepo;
    @Autowired
    private NewsNotificationRepo newsNotificationRepo;

    protected NewsActivityGenerator() {

    }

    public static NewsActivityGenerator getInstance() {

        return INSTANCE;
    }

    public boolean generate(NewsActivity newsActivity, IEventDetails details) throws VedantuException {

        // TODO do not create news if srcEntity is already DELETED

		/*@SuppressWarnings("unchecked")
		VedantuBasicDAO<VedantuBaseMongoModel, ObjectId> srcDAO = EntityTypeDAOFactory.INSTANCE
				.get(newsActivity.src.type);

		if (srcDAO == null) {
			return false;
		}
		VedantuBaseMongoModel baseModel = srcDAO.getById(newsActivity.src.id, VedantuRecordState.ACTIVE);
		if (baseModel == null) {
			// check for delete and return true
			return true;
		}*/
		NewsFeedSecurityVaildator.set(new UserSecuritySet(null, null));
		NewsFeedSecurityVaildator.get().setContextType(NewsContext.EMAIL);
		String context = getContext(newsActivity.src.type, null);
		if (StringUtils.isEmpty(context)) {
			NewsFeedSecurityVaildator.get().setOrgId(context);
		}
		newsActivity.scope = NewsFeedSecurityVaildator.isPublicEvent(newsActivity.eType) ? Scope.PUBLIC
				: (newsActivity.scope != null ? newsActivity.scope : Scope.PRIVATE);
		String activityId = save(newsActivity);
		logger.info("activityId : " + activityId);
		if (StringUtils.isEmpty(activityId)) {
			return false;
		}
		logger.info("Notificaion generation is enabled : " + details.getNotificationEnabled());

		if (!details.getNotificationEnabled()) {
			// if notification is not enabled for this event we dont need to
			// create news activity
			logger.info("No notifications are enabled for this news activity" + newsActivity);
			return true;
		}

		logger.info("activityId : " + activityId);
		Map<NotificationReason, Set<SrcEntity>> fanOutSet = getFanOutEntitySet(newsActivity);
		if (null == fanOutSet || fanOutSet.isEmpty()) {
			logger.info("empty fanOutSet");

			return true;
		}

		if (newsActivity.sendNewsFeed) {
			long fanOutCount = fanOut(newsActivity, activityId, "newsfeed", fanOutSet, null);

            logger.info("activityId : " + activityId + ", fanOutCount : " + fanOutCount);
        } else {
            logger.info("activityId : " + activityId + ", sendNewsFeed:" + newsActivity.sendNewsFeed);
        }
        logger.info("fanOutSet : " + fanOutSet);

        Map<NotificationReason, Set<SrcEntity>> fanOutNotificationSet = new HashMap<NotificationReason, Set<SrcEntity>>();

        final List<NotificationReason> notificationReasons = NotificationReason.getNotificationReasonSet();

        for (NotificationReason notificationReason : notificationReasons) {
            if (fanOutSet.containsKey(notificationReason)) {
                fanOutNotificationSet.put(notificationReason, fanOutSet.get(notificationReason));
            }
        }

        logger.debug("fanOutNotificationSet : " + fanOutNotificationSet);
        Map<SrcEntity, NewsFeedInfo> feedForEmail = new HashMap<SrcEntity, NewsFeedInfo>();
        long fanOutNotificationCount = fanOut(newsActivity, activityId, "newsnotifications", fanOutNotificationSet,
                feedForEmail);

        logger.info("activityId : " + activityId + ", fanOutNotificationCount : " + fanOutNotificationCount);
        logger.info("generating email notification");
        //boolean generated = EmailNotificationGenerator.INSTANCE.generate(feedForEmail);
        //logger.info("email notification generation status is : " + generated);
        return true;
    }

	private String save(NewsActivity newsActivity) {
        String rowId = NewsUtils.getRowId(newsActivity.actor, newsActivity.time);
        logger.info("rowId : " + rowId);
        NewsActivityInfo newsActivityInfo = new NewsActivityInfo();
        newsActivityInfo.setActor(newsActivity.actor);
        newsActivityInfo.setComments(newsActivity.comments);
        newsActivityInfo.setEType(newsActivity.eType);
        newsActivityInfo.setInfo(newsActivity.info);
        newsActivityInfo.setInvolved(newsActivity.involved);
        newsActivityInfo.setOrgId(newsActivity.orgId);
        newsActivityInfo.setScope(newsActivity.scope);
        newsActivityInfo.setSendNewsFeed(newsActivity.sendNewsFeed);
        newsActivityInfo.setSharedWith(newsActivity.sharedWith);
        newsActivityInfo.setSrc(newsActivity.src);
        newsActivityInfo.setSrcOwner(newsActivity.srcOwner);
        newsActivityInfo.setTime(newsActivity.time);
        newsActivityInfoRepo.save(newsActivityInfo);
        return rowId;
    }

	private long fanOut(final NewsActivity newsActivity, final String activityId, final String inboxName,
			final Map<NotificationReason, Set<SrcEntity>> fanOutSet, Map<SrcEntity, NewsFeedInfo> feedsForEmail) {

        if (null == fanOutSet || fanOutSet.isEmpty()) {
            logger.info("no fanOutEntitySet for inboxName");

            // System.out.println("no fanOutEntitySet");
            return 0;
        }

        List<NewsFeed> newsFeeds = new ArrayList<>();
        List<NewsNotification> notifications = new ArrayList<>();
        for (Map.Entry<NotificationReason, Set<SrcEntity>> entry : fanOutSet.entrySet()) {
            NotificationReason reason = entry.getKey();
            NewsActivityRef newsActivityRef = new NewsActivityRef(activityId, reason);

            for (SrcEntity fanOutSrcEntity : entry.getValue()) {

                String rowId = NewsUtils.getRowId(fanOutSrcEntity, newsActivity.time);
                if (inboxName.equalsIgnoreCase("newsfeed")) {
                    NewsFeed newsFeed = new NewsFeed(newsActivityRef.aid, newsActivityRef.why);
                    newsFeed.actor = fanOutSrcEntity;
                    newsFeeds.add(newsFeed);
                } else if (inboxName.equalsIgnoreCase("newsnotifications")) {
                    NewsNotification newsNotification = new NewsNotification(newsActivityRef.aid, newsActivityRef.why);
                    newsNotification.actor = fanOutSrcEntity;
                    notifications.add(newsNotification);
                }
                logger.debug(" Fanout News entity" + fanOutSrcEntity + " data " + newsActivity);
                if (feedsForEmail != null) {
                    NewsFeedInfo feedInfo = new NewsFeedInfo(newsActivity);
                    feedInfo.why = entry.getKey();
                    feedInfo.newsFeedId = rowId;
                    feedsForEmail.put(fanOutSrcEntity, feedInfo);
                }
            }

        }
        try {
            if (inboxName.equalsIgnoreCase("newsfeed")) {
                newsFeedRepo.saveAll(newsFeeds);
                return newsFeeds.size();
            } else if (inboxName.equalsIgnoreCase("newsnotifications")) {
                newsNotificationRepo.saveAll(notifications);
                return notifications.size();
            }
            logger.info("Added activityId : " + activityId + " to notification inbox ");
        } catch (Exception e) {
            logger.error("could not add activityId : " + activityId, e);

            return 0L;
        }

        return 0L;
    }

	private Map<NotificationReason, Set<SrcEntity>> getFanOutEntitySet(NewsActivity newsActivity) {

		Map<NotificationReason, Set<SrcEntity>> newsUpdateTo = new HashMap<NotificationReason, Set<SrcEntity>>();
		Set<SrcEntity> newsUpdateToDeduplicate = new HashSet<SrcEntity>();

		logger.info("news activity is : " + newsActivity);
		INewsFanOutCollector eventSpecificNewsGenerator = NewsFanOutCollectorFactory.INSTANCE.get(newsActivity.eType);
		if (eventSpecificNewsGenerator != null) {
			eventSpecificNewsGenerator.getNewsSubscribers(newsActivity, newsUpdateTo, newsUpdateToDeduplicate);

		}
		logger.info("newsUpdateToDeduplicate : " + newsUpdateToDeduplicate.size());
		logger.info("newsUpdateTo : " + newsUpdateTo);

		return newsUpdateTo;
	}

	private String toJSONString(NewsActivity newsActivity) {

		return new Gson().toJson(newsActivity);
	}

	private String toJSONString(NewsActivityRef newsActivityRef) {

		return new Gson().toJson(newsActivityRef);
	}

	private String getContext(EntityType entityType, VedantuBaseMongoModel baseModel) {

		if (baseModel instanceof AbstractBoardEntityTagModel) {
			switch (entityType) {
			case COMMENT:
				//baseModel = CommentManager.getRoot((Comment) baseModel);
				break;
			case SOLUTION:
				//baseModel = QuestionDAO.INSTANCE.getById(((Solution) baseModel).qId);
				break;
			default:
				break;

			}
			if (((AbstractBoardEntityTagModel) baseModel).contentSrc != null) {
				return ((AbstractBoardEntityTagModel) baseModel).contentSrc.id;
			}
		}
		return null;

	}
}
