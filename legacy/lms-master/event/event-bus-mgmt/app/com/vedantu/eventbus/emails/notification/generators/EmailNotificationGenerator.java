package com.vedantu.eventbus.emails.notification.generators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.vedantu.comm.managers.NewsAggregatorHelper;
import com.vedantu.comm.managers.news.NewsFeedSecurityVaildator;
import com.vedantu.comm.pojos.news.NewsFeedInfo;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.MailCategory;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.ImageDisplayURLUtil;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.eventbus.email.details.AbstractEmailNotificationDetails;
import com.vedantu.eventbus.factory.EmailNotificationDetailFactory;
import com.vedantu.events.utils.EventUtil;
import com.vedantu.organization.daos.OrgMemberDAO;
import com.vedantu.organization.models.OrgMember;
import com.vedantu.user.daos.EmailBlacklistDAO;
import com.vedantu.user.daos.UserDAO;
import com.vedantu.user.daos.UserEmailUnsubscriptionDAO;
import com.vedantu.user.pojos.UserEmailInfo;
import com.vedantu.user.pojos.UserExtendedInfo;

public class EmailNotificationGenerator {

    private Set<EventType>                         enabledEmailEventTypeSet;

    public static final EmailNotificationGenerator INSTANCE = new EmailNotificationGenerator();

    private static final ALogger                   LOGGER   = Logger.of(EmailNotificationGenerator.class);

    private EmailNotificationGenerator() {

        super();
        enabledEmailEventTypeSet = new HashSet<EventType>();
        List<String> eTypes = Play.application().configuration()
                .getStringList("email.template.enabled.etypes");
        for (String eType : eTypes) {
            enabledEmailEventTypeSet.add(EventType.getEventTypeByValue(eType));
        }
    }

    public boolean generate(Map<SrcEntity, NewsFeedInfo> feedForEmail) throws VedantuException {

        List<String> userIds = new ArrayList<String>();
        for (Entry<SrcEntity, NewsFeedInfo> entry : feedForEmail.entrySet()) {
            if (entry.getKey().type == EntityType.USER) {
                userIds.add(entry.getKey().id);
            }
        }
        if (CollectionUtils.isEmpty(userIds)) {
            return true;
        }

        String orgId = null;
        if (NewsFeedSecurityVaildator.get() != null) {
            orgId = NewsFeedSecurityVaildator.get().getOrgId();
        }

        Map<String, UserEmailInfo> userInfos = collectUserEmailInfo(userIds, orgId);
        for (Entry<SrcEntity, NewsFeedInfo> entry : feedForEmail.entrySet()) {
            if (entry.getKey().type != EntityType.USER) {
                continue;
            }

            UserEmailInfo userInfo = userInfos.get(entry.getKey().id);
            // generate email event only if the user exist
            if (userInfo == null) {
                LOGGER.error("no user found entity: " + entry.getKey());
                continue;
            }
            if (!userInfo.isEmailVerified) {
                LOGGER.error("not verifiedEmail,  userInfo : " + userInfo);
                continue;
            }

            List<NewsFeedInfo> feedInfos = NewsAggregatorHelper.populateDetails(
                    Arrays.asList(entry.getValue()), entry.getKey().id);
            if (CollectionUtils.isEmpty(feedInfos)) {
                LOGGER.error("no feed is populated for entity : " + entry.getKey()
                        + ", newsFeedInfo: " + entry.getValue());
                continue;
            }
            NewsFeedInfo feedInfo = feedInfos.get(0);
            if (!enabledEmailEventTypeSet.contains(feedInfo.eType)) {
                LOGGER.error("email sending is not enabled for eventType:" + feedInfo.eType);
                continue;
            }
            AbstractEmailNotificationDetails details = EmailNotificationDetailFactory.INSTANCE
                    .getEmailNotificationDetails(entry.getValue().eType);
            if (details == null) {
                LOGGER.error("no email notification details found for eventType:"
                        + entry.getValue().eType);
                return false;
            }
            details.user = userInfo;

            details.addRecepient(userInfo.getFullName(), userInfo.email);
            try {
                details.details = feedInfo.toJSON();
            } catch (JSONException e) {
                LOGGER.error(e.getMessage(), e);
            }
            EventUtil.generateEvent(EventType.SEND_EMAIL, null, feedInfos.get(0).actor.id, details,
                    feedInfos.get(0).src);
        }
        return false;
    }

    // TODO Unify with other approaches

    public static Map<String, UserEmailInfo>
            collectUserEmailInfo(List<String> userIds, String orgId) throws VedantuException {

        return collectUserEmailInfo(userIds, orgId, false);
    }

    public static Map<String, UserEmailInfo> collectUserEmailInfo(List<String> userIds,
            String orgId, boolean getAllUserEmailInfos) throws VedantuException {

        LOGGER.debug("Populating userIds " + userIds + "for orgId" + orgId);
        Map<String, OrgMember> orgMemberMap = new HashMap<String, OrgMember>();
        if (StringUtils.isNotEmpty(orgId)) {
            orgMemberMap = OrgMemberDAO.INSTANCE.getMemberMapByUserId(orgId, userIds);
        }

        Map<String, UserExtendedInfo> userInfos = UserDAO.INSTANCE
                .toExtendedInfosMap(UserDAO.INSTANCE.getByIds(ObjectIdUtils.toObjectIds(userIds,
                        true)));
        Map<String, UserEmailInfo> userEmailInfos = new HashMap<String, UserEmailInfo>();
        for (String userId : userInfos.keySet()) {

            UserEmailInfo userEmailInfo = new UserEmailInfo();
            UserExtendedInfo userExtendedInfo = userInfos.get(userId);

            userEmailInfo.fromUserExtendedInfo(userExtendedInfo);
            if (orgMemberMap.containsKey(userId)) {
                OrgMember orgMember = orgMemberMap.get(userId);
                userEmailInfo.firstName = orgMember.firstName;
                userEmailInfo.lastName = orgMember.lastName;
                userEmailInfo.gender = orgMember.gender;
                userEmailInfo.thumbnail = ImageDisplayURLUtil.getEntityThumbnail(EntityType.USER,
                        orgMember.thumbnail);
                userEmailInfo.setCategory(MailCategory.NOTIFICATION);
            }
            if (getAllUserEmailInfos) {
                userEmailInfos.put(userId, userEmailInfo);
            } else if (!EmailBlacklistDAO.INSTANCE.isBlacklisted(userEmailInfo.email)
                    && UserEmailUnsubscriptionDAO.INSTANCE.isEmailAllowed(userId,
                            userEmailInfo.email, MailCategory.NOTIFICATION)) {
                userEmailInfos.put(userId, userEmailInfo);
            }

        }
        return userEmailInfos;
    }

}
