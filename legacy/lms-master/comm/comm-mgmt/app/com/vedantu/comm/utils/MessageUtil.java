package com.vedantu.comm.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import play.Logger.ALogger;

import com.vedantu.comm.managers.StatusFeedManager;
import com.vedantu.comm.news.details.UserNewsEntityDetails;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.organization.pojos.OrgMemberBasicInfo;
import com.vedantu.user.pojos.UserInfo;

public class MessageUtil {

    private static final ALogger LOGGER                       = play.Logger.of(MessageUtil.class);

    public static final String   TABLE_NAME_USER_MESSAGE_V2   = "messages.user_message_table_v2";
    public static final String   TABLE_NAME_MESSAGES_V2       = "messages.message_table_v2";
    public static final String   TABLE_NAME_USER_CONVERSATION = "messages.user_conversation_table";
    public static final String   TABLE_NAME_CONVERSATION      = "messages.conversation_table";
    public static final String   TABLE_NAME_USER_MESSAGE      = "messages.user_message_table";
    public static final String   TABLE_NAME_MESSAGES          = "messages.message_table";

    public final static String   IDSeparator                  = ",";                                ;

    public static String fromList(ArrayList<String> list, String separator) {

        if (separator == null) {
            separator = MessageUtil.IDSeparator;
        }
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            buffer.append(list.get(i));
            if (i < (list.size() - 1)) {
                buffer.append(separator);
            }
        }
        return buffer.toString();
    }

    public static String fromList(String[] list, String separator) {

        if (separator == null) {
            separator = MessageUtil.IDSeparator;
        }
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < list.length; i++) {
            buffer.append(list[i]);
            if (i < (list.length - 1)) {
                buffer.append(separator);
            }
        }
        return buffer.toString();
    }

    // public static String toJson(Message message) {
    // JsonMessage jsonMessage = new JsonMessage(message.getSenderId(),
    // message.getRecepientId(),
    // message.getGroupId(), message.getAction(),
    // Message.getTypeInString(message.getType()),
    // message.getMessage(), message.getSentTime());
    //
    // return (new Gson().toJson(jsonMessage));
    // }

    public static SrcEntity populateUserNewsEntityDetails(String orgId, SrcEntity srcEntity) {

        List<SrcEntity> srcEntities = new ArrayList<SrcEntity>();
        srcEntities.add(srcEntity);
        List<SrcEntity> updatedSrcEntities = populateUserNewsEntityDetails(orgId, srcEntities);

        if (CollectionUtils.isNotEmpty(updatedSrcEntities)) {
            srcEntity = updatedSrcEntities.iterator().next();

            LOGGER.info("Decorated entity " + srcEntity);
        }
        return srcEntity;
    }

    public static List<SrcEntity> populateUserNewsEntityDetails(String orgId,
            List<SrcEntity> srcEntities) {

        play.Logger.info(" Collection user information for all users in news set user count "
                + srcEntities.size());

        if (srcEntities == null || srcEntities.isEmpty()) {
            LOGGER.error("srcEntities set is null or empty");
            return null;
        }
        List<String> userIds = new ArrayList<String>();
        List<SrcEntity> userList = new ArrayList<SrcEntity>();
        for (SrcEntity entity : srcEntities) {
            userIds.add(entity.id);
        }
        Map<String, ModelBasicInfo> orgMemberInfoSet = StatusFeedManager.getUserInfoMap(orgId,
                userIds);

        LOGGER.info("Received cursor from mongo for " + orgMemberInfoSet.keySet());
        if (CollectionUtils.isEmpty(orgMemberInfoSet.entrySet())) {
            LOGGER.error("received cursor is null");
            return null;
        }
        UserInfo userInfo = null;
        for (SrcEntity entity : srcEntities) {

            userInfo = (UserInfo) orgMemberInfoSet.get(entity.id);

            if (userInfo == null) {
                LOGGER.debug("No user info found for" + entity.id);
                continue;
            }

            UserNewsEntityDetails userDetails = new UserNewsEntityDetails();
            userDetails.id = (userInfo instanceof OrgMemberBasicInfo) ? ((OrgMemberBasicInfo) userInfo).userId
                    : userInfo.id;

            userDetails.type = EntityType.USER;
            if (StringUtils.isNotEmpty(userInfo.firstName)) {
                userDetails.firstName = userInfo.firstName;
                LOGGER.info(" first name found for user " + userDetails.firstName + " for user id:"
                        + userDetails.id);

            } else {

                LOGGER.debug(" No first name found for user " + userDetails.id);

            }
            if (StringUtils.isNotEmpty(userInfo.lastName)) {
                userDetails.lastName = userInfo.lastName;

            } else {
                userDetails.lastName = StringUtils.EMPTY;
            }

            userDetails.thumbnail = userInfo.thumbnail;
            if (userInfo instanceof OrgMemberBasicInfo) {
                userDetails.profile = ((OrgMemberBasicInfo) userInfo).profile;
            }
            userList.add(userDetails);
        }

        return userList;
    }

}
