package com.vedantu.comm.managers.news.populators;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.comm.managers.StatusFeedManager;
import com.vedantu.comm.news.details.UserNewsEntityDetails;
import com.vedantu.comm.utils.news.NewsUtils;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.mongo.IVedantuModel;
import com.vedantu.organization.pojos.OrgMemberBasicInfo;
import com.vedantu.user.pojos.UserInfo;

public class UserNewsPopulator extends AbstractEntityDetailsPopulator {

    public final static UserNewsPopulator INSTANCE = new UserNewsPopulator();
    private final static ALogger          LOGGER   = Logger.of(UserNewsPopulator.class);

    private UserNewsPopulator() {

    }

    @Override
    public void populate(String orgId, String userId, Set<SrcEntity> newsEntities,
            Map<SrcEntity, SrcEntity> srcEntityDetails, EntityType entityType) {

        newsEntities.add(new SrcEntity(EntityType.USER, userId));
        Map<String, SrcEntity> entityIds = NewsUtils.getSrcEntityIds(newsEntities);
        Map<String, ModelBasicInfo> orgMemberInfoSet = StatusFeedManager.getUserInfoMap(orgId,
                entityIds.keySet());
        Map<String, IVedantuModel> modelMap = new HashMap<String, IVedantuModel>();
        for (Entry<String, ModelBasicInfo> entry : orgMemberInfoSet.entrySet()) {
            modelMap.put(entry.getKey(), entry.getValue());
        }

        for (SrcEntity entity : newsEntities) {
            // this populate method will call the child populator method
            SrcEntity details = populate(orgId, userId, entity, modelMap);
            if (details == null) {
                LOGGER.error("no details found for entity: " + entity);
                continue;
            }

            LOGGER.debug(" Entity " + details.id + " is upvoted by user" + userId);

            if (details != null && entity != null) {
                srcEntityDetails.put(entity, details);
            } else {
                LOGGER.error(" status feed news entity is not collected " + details.id + " for "
                        + entity);
            }
        }

    }

    @Override
    public SrcEntity populate(String orgId, String userId, SrcEntity newEntity,
            Map<String, IVedantuModel> modelDetailMap) {

//        LOGGER.debug(" Type of user model " + modelDetailMap.get(newEntity.id).getClass());
        UserInfo userInfo = (UserInfo) modelDetailMap.get(newEntity.id);

        if (userInfo == null) {
            LOGGER.error("no user found for : " + newEntity);
            return null;
        }

        UserNewsEntityDetails userDetails = new UserNewsEntityDetails();

        userDetails.id = newEntity.id;

        userDetails.type = EntityType.USER;

        if (userInfo instanceof OrgMemberBasicInfo) {
            userDetails.profile = ((OrgMemberBasicInfo) userInfo).profile;

        }

        if (StringUtils.isNotEmpty(userInfo.firstName)) {
            userDetails.firstName = userInfo.firstName;
            LOGGER.debug(" first name found for user " + userDetails.firstName + " for user id:"
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
        return userDetails;
    }
}
