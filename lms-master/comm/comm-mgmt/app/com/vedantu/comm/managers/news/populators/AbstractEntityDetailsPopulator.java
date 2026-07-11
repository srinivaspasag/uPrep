package com.vedantu.comm.managers.news.populators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.board.managers.BoardManager;
import com.vedantu.board.pojos.BoardBasicInfo;
import com.vedantu.board.pojos.BoardTree;
import com.vedantu.comm.enums.NewsContext;
import com.vedantu.comm.managers.news.NewsFeedSecurityVaildator;
import com.vedantu.comm.managers.news.UserSecuritySet;
import com.vedantu.comm.news.details.AbstractSocialEntity;
import com.vedantu.comm.utils.news.NewsUtils;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.ImageDisplayURLUtil;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.content.managers.AnalyticsManager;
import com.vedantu.content.models.AbstractBoardEntityTagModel;
import com.vedantu.content.models.AbstractContentStatsModel;
import com.vedantu.content.models.AbstractFileModel;
import com.vedantu.mongo.IVedantuModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.socials.apis.IAttemptable;
import com.vedantu.user.pojos.EntityUserActionDAO;

public abstract class AbstractEntityDetailsPopulator implements IPopulator {

    private static final ALogger LOGGER = Logger.of(AbstractEntityDetailsPopulator.class);

    @Override
    public void populate(String orgId, String userId, Set<SrcEntity> newsEntities,
            Map<SrcEntity, SrcEntity> srcEntityDetails, EntityType entityType) {

        Map<String, SrcEntity> entityIds = NewsUtils.getSrcEntityIds(newsEntities);

        @SuppressWarnings("rawtypes")
        VedantuBasicDAO dao = EntityTypeDAOFactory.INSTANCE.get(entityType);

        @SuppressWarnings("unchecked")
        Map<String, IVedantuModel> modelMap = dao.toInfosMap(dao.getByIds(
                ObjectIdUtils.toObjectIds(new ArrayList<String>(entityIds.keySet()), true),
                VedantuRecordState.ACTIVE));

        LOGGER.debug("Collected News entity  " + entityIds.keySet());

     
        Map<String, Boolean> attemptMap = AnalyticsManager.getEntityAttemptsMap(entityIds.keySet(),
                userId);

        LOGGER.debug("upVoteMap");
        Map<String, Boolean> upVoteMap = EntityUserActionDAO.INSTANCE.getEntityUpVoteMap(userId,
                entityIds.keySet());

        for (SrcEntity entity : newsEntities) {
            // this populate method will call the child populator method
            SrcEntity details = populate(orgId, userId, entity, modelMap);
            if (details == null) {
                LOGGER.error("no details found for entity: " + entity);
                continue;
            }
           

            if (details instanceof AbstractSocialEntity) {
                // add other social aspect from here
                ((AbstractSocialEntity) details).voted = upVoteMap.get(details.id) == null ? false
                        : upVoteMap.get(details.id).booleanValue();
                
                if( ((AbstractSocialEntity) details).contentSrc == null && StringUtils.isNotEmpty(orgId)){
                    ((AbstractSocialEntity) details).contentSrc = new SrcEntity(EntityType.ORGANIZATION, orgId);
                }
            }

            UserSecuritySet set = NewsFeedSecurityVaildator.get();
            if (set != null && set.contextType == NewsContext.NEWSFEED) {
                IVedantuModel mongoModel = modelMap.get(entity.id);
                if (mongoModel instanceof AbstractBoardEntityTagModel) {
                    Map<String, BoardBasicInfo> basicInfoMap = BoardManager
                            .getInfosMap(((AbstractBoardEntityTagModel) mongoModel).boardIds);

                    List<BoardTree> boardTree = BoardManager.getTreesByInfosMap(basicInfoMap).list;
                    ((AbstractSocialEntity) details).boardTree = BoardManager
                            .toBoardTreeRes(boardTree);

                }
                if (mongoModel instanceof AbstractContentStatsModel) {
                    ((AbstractSocialEntity) details).upVotes = ((AbstractContentStatsModel) mongoModel).upVotes;
                    ((AbstractSocialEntity) details).followers = ((AbstractContentStatsModel) mongoModel).followers;
                    ((AbstractSocialEntity) details).comments = ((AbstractContentStatsModel) mongoModel).comments;
                    ((AbstractSocialEntity) details).views = ((AbstractContentStatsModel) mongoModel).views;

                }
                if (mongoModel instanceof AbstractFileModel
                        && StringUtils.isNotEmpty(((AbstractFileModel) mongoModel).thumbnail)) {
                    ((AbstractSocialEntity) details).thumbnail = ImageDisplayURLUtil
                            .getEntityThumbnail(entity.type,
                                    ((AbstractFileModel) mongoModel).thumbnail);

                    ((AbstractSocialEntity) details).description = ((AbstractFileModel) mongoModel).description;
                }
             
            }
            if (dao instanceof IAttemptable) {
                LOGGER.debug("Attempt decoration" + details.id + " " + details.type
                        + " attempt " + attemptMap.get(details.id));
                ((AbstractSocialEntity) details).attempted = attemptMap.get(details.id) == null ? false
                        : attemptMap.get(details.id).booleanValue();
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
}
