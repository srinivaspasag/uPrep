package com.vedantu.eventbus.managers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.gson.Gson;
import com.vedantu.comm.enums.NewsContext;
import com.vedantu.comm.enums.NotificationReason;
import com.vedantu.comm.managers.news.NewsFeedSecurityVaildator;
import com.vedantu.comm.managers.news.UserSecuritySet;
import com.vedantu.comm.managers.news.generator.INewsFanOutCollector;
import com.vedantu.comm.pojos.news.NewsActivityRef;
import com.vedantu.comm.pojos.news.NewsFeedInfo;
import com.vedantu.comm.utils.news.NewsUtils;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.hbase.HBaseUtil;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.daos.QuestionDAO;
import com.vedantu.content.managers.CommentManager;
import com.vedantu.content.models.AbstractBoardEntityTagModel;
import com.vedantu.content.models.Comment;
import com.vedantu.content.models.Solution;
import com.vedantu.eventbus.emails.notification.generators.EmailNotificationGenerator;
import com.vedantu.eventbus.news.generators.NewsFanOutCollectorFactory;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;

public class NewsActivityGenerator {

    final static ALogger                      LOGGER   = Logger.of(NewsActivityGenerator.class);
    public static final NewsActivityGenerator INSTANCE = new NewsActivityGenerator();

    protected NewsActivityGenerator() {

    }

    public static NewsActivityGenerator getInstance() {

        return INSTANCE;
    }

    public boolean generate(NewsActivity newsActivity, IEventDetails details) throws VedantuException {

        // TODO do not create news if srcEntity is already DELETED

        @SuppressWarnings("unchecked")
        VedantuBasicDAO<VedantuBaseMongoModel, ObjectId> srcDAO = EntityTypeDAOFactory.INSTANCE
                .get(newsActivity.src.type);

        if (srcDAO == null) {
            return false;
        }
        VedantuBaseMongoModel baseModel = srcDAO.getById(newsActivity.src.id,
                VedantuRecordState.ACTIVE);
        if (baseModel == null) {
            // check for delete and return true
            return true;
        }
        NewsFeedSecurityVaildator.set(new UserSecuritySet(null, null));
        NewsFeedSecurityVaildator.get().setContextType(NewsContext.EMAIL);
        String context = getContext(newsActivity.src.type, baseModel);
        if (StringUtils.isNotEmpty(context)) {
            NewsFeedSecurityVaildator.get().setOrgId(context);
        }
        newsActivity.scope = NewsFeedSecurityVaildator.isPublicEvent(newsActivity.eType) ? Scope.PUBLIC
                : (newsActivity.scope != null ? newsActivity.scope : Scope.PRIVATE);
        String activityId = save(newsActivity);
        LOGGER.info("activityId : " + activityId);
        if (StringUtils.isEmpty(activityId)) {
            return false;
        }
        LOGGER.info("Notificaion generation is enabled : " + details.getNotificationEnabled());

        if (!details.getNotificationEnabled()) {
            // if notification is not enabled for this event we dont need to
            // create news activity
            LOGGER.info("No notifications are enabled for this news activity" + newsActivity);
            return true;
        }

        LOGGER.info("activityId : " + activityId);
        Map<NotificationReason, Set<SrcEntity>> fanOutSet = getFanOutEntitySet(newsActivity);
        if (null == fanOutSet || fanOutSet.isEmpty()) {
            LOGGER.info("empty fanOutSet");

            return true;
        }

        if (newsActivity.sendNewsFeed) {
            long fanOutCount = fanOut(newsActivity, activityId, "newsfeed", fanOutSet, null);

            LOGGER.info("activityId : " + activityId + ", fanOutCount : " + fanOutCount);
        } else {
            LOGGER.info("activityId : " + activityId + ", sendNewsFeed:"
                    + newsActivity.sendNewsFeed);
        }
        LOGGER.info("fanOutSet : " + fanOutSet);

        Map<NotificationReason, Set<SrcEntity>> fanOutNotificationSet = new HashMap<NotificationReason, Set<SrcEntity>>();

        final List<NotificationReason> notificationReasons = NotificationReason
                .getNotificationReasonSet();

        for (NotificationReason notificationReason : notificationReasons) {
            if (fanOutSet.containsKey(notificationReason)) {
                fanOutNotificationSet.put(notificationReason, fanOutSet.get(notificationReason));
            }
        }

        LOGGER.debug("fanOutNotificationSet : " + fanOutNotificationSet);
        Map<SrcEntity, NewsFeedInfo> feedForEmail = new HashMap<SrcEntity, NewsFeedInfo>();
        long fanOutNotificationCount = fanOut(newsActivity, activityId, "notification",
                fanOutNotificationSet, feedForEmail);

        LOGGER.info("activityId : " + activityId + ", fanOutNotificationCount : "
                + fanOutNotificationCount);
        LOGGER.info("generating email notification");
        boolean generated = EmailNotificationGenerator.INSTANCE.generate(feedForEmail);
        LOGGER.info("email notification generation status is : " + generated);
        return true;
    }

    private String save(NewsActivity newsActivity) {

        String rowId = NewsUtils.getRowId(newsActivity.actor, newsActivity.time);
        LOGGER.info("rowId : " + rowId);

        Put newsActivityPut = new Put(Bytes.toBytes(rowId));

        String newsActivityJSON = toJSONString(newsActivity);
        newsActivityPut.add(Bytes.toBytes("act"), Bytes.toBytes("data"),
                Bytes.toBytes(newsActivityJSON));

        HTable newsActivityTable = HBaseUtil.getTable("newsactivity");
        try {
            newsActivityTable.put(newsActivityPut);
        } catch (IOException e) {
            LOGGER.error("could not add rowId : " + rowId + ", newsActivityJSON : "
                    + newsActivityJSON, e);

            return null;
        } finally {
            HBaseUtil.returnTable(newsActivityTable);
        }

        return rowId;
    }

    private long fanOut(final NewsActivity newsActivity, final String activityId,
            final String inboxName, final Map<NotificationReason, Set<SrcEntity>> fanOutSet,
            Map<SrcEntity, NewsFeedInfo> feedsForEmail) {

        if (null == fanOutSet || fanOutSet.isEmpty()) {
            LOGGER.info("no fanOutEntitySet for inboxName");

            // System.out.println("no fanOutEntitySet");
            return 0;
        }

        List<Put> inboxPuts = new ArrayList<Put>();

        for (Map.Entry<NotificationReason, Set<SrcEntity>> entry : fanOutSet.entrySet()) {
            NotificationReason reason = entry.getKey();
            NewsActivityRef newsActivityRef = new NewsActivityRef(activityId, reason);

            for (SrcEntity fanOutSrcEntity : entry.getValue()) {

                String rowId = NewsUtils.getRowId(fanOutSrcEntity, newsActivity.time);

                Put newsFeedInboxPut = new Put(Bytes.toBytes(rowId));

                newsFeedInboxPut.add(Bytes.toBytes("act"), Bytes.toBytes("data"),
                        Bytes.toBytes(toJSONString(newsActivityRef)));
                LOGGER.debug(" Fanout News entity" + fanOutSrcEntity + " data " + newsActivity);
                inboxPuts.add(newsFeedInboxPut);
                if (feedsForEmail != null) {
                    NewsFeedInfo feedInfo = new NewsFeedInfo(newsActivity);
                    feedInfo.why = entry.getKey();
                    feedInfo.newsFeedId = rowId;
                    feedsForEmail.put(fanOutSrcEntity, feedInfo);
                }
            }

        }

        HTable inboxTable = HBaseUtil.getTable(inboxName);

        try {
            inboxTable.put(inboxPuts);
            LOGGER.info("Added activityId : " + activityId + " to notification inbox ");
        } catch (IOException e) {
            LOGGER.error("could not add activityId : " + activityId, e);

            return 0L;
        } finally {
            HBaseUtil.returnTable(inboxTable);
        }

        return inboxPuts.size();
    }

    private Map<NotificationReason, Set<SrcEntity>> getFanOutEntitySet(NewsActivity newsActivity) {

        Map<NotificationReason, Set<SrcEntity>> newsUpdateTo = new HashMap<NotificationReason, Set<SrcEntity>>();
        Set<SrcEntity> newsUpdateToDeduplicate = new HashSet<SrcEntity>();

        LOGGER.info("news activity is : " + newsActivity);
        INewsFanOutCollector eventSpecificNewsGenerator = NewsFanOutCollectorFactory.INSTANCE
                .get(newsActivity.eType);
        if (eventSpecificNewsGenerator != null) {
            eventSpecificNewsGenerator.getNewsSubscribers(newsActivity, newsUpdateTo,
                    newsUpdateToDeduplicate);

        }
        LOGGER.info("newsUpdateToDeduplicate : " + newsUpdateToDeduplicate.size());
        LOGGER.info("newsUpdateTo : " + newsUpdateTo);

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
                baseModel = CommentManager.getRoot((Comment) baseModel);
                break;
            case SOLUTION:
                baseModel = QuestionDAO.INSTANCE.getById(((Solution) baseModel).qId);
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
