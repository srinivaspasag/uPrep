package com.vedantu.comm.managers.news.populators;

import java.util.Map;
import java.util.Set;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.comm.news.details.QuestionNewsEntityDetails;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.models.Question;
import com.vedantu.mongo.IVedantuModel;

public class QuestionNewsPopulator extends AbstractEntityDetailsPopulator {

    public final static QuestionNewsPopulator INSTANCE = new QuestionNewsPopulator();
    private final static ALogger              LOGGER   = Logger.of(QuestionNewsPopulator.class);

    @Override
    public void populate(String orgId, String userId, Set<SrcEntity> newsEntities,
            Map<SrcEntity, SrcEntity> srcEntityDetails, EntityType entityType) {

        super.populate(orgId, userId, newsEntities, srcEntityDetails, entityType);
    }

    @Override
    public SrcEntity populate(String orgId, String userId, SrcEntity newsEntity,
            Map<String, IVedantuModel> modelDetailMap) {

        LOGGER.debug(" Populating question " + newsEntity.id);
        Question question = (Question) modelDetailMap.get(newsEntity.id);
        if (question == null) {
            LOGGER.error("no question found for : " + newsEntity);
            return null;
        }
        QuestionNewsEntityDetails details = new QuestionNewsEntityDetails(newsEntity.id);

        details.content = question.content;
        details.timeCreated = question.timeCreated;
        details.contentSrc = question.contentSrc;
        return details;
    }
}
