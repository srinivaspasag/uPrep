package com.vedantu.eventbus.processors.entities;

import java.util.HashMap;
import java.util.Map;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.events.apis.EntityIndexEventMapper;
import com.vedantu.content.commons.interfaces.IIndexable;
import com.vedantu.content.search.details.AbstractSearchDetail;
import com.vedantu.eventbus.processors.AbstractSearchIndexProcessor;
import com.vedantu.eventbus.processors.AssignmentSearchIndexProcessor;
import com.vedantu.eventbus.processors.QuestionSearchIndexProcessor;
import com.vedantu.eventbus.processors.TestSearchIndexProcessor;
import com.vedantu.eventbus.processors.challenges.ChallengeSearchIndexProcessor;
import com.vedantu.eventbus.processors.discussions.DiscussionSearchIndexProcessor;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;
import com.vedantu.events.utils.EventDetailsFactory;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.user.social.actions.event.details.UserEntityActionDetails;

public class EntitySearchIndexProcessor implements IProcessor {

    private static final ALogger                  LOGGER   = Logger.of(EntitySearchIndexProcessor.class);
    public static EntitySearchIndexProcessor      INSTANCE = new EntitySearchIndexProcessor();
    Map<EntityType, AbstractSearchIndexProcessor> processorMap;

    private EntitySearchIndexProcessor() {

        processorMap = new HashMap<EntityType, AbstractSearchIndexProcessor>();
        processorMap.put(EntityType.QUESTION, new QuestionSearchIndexProcessor());
        ChallengeSearchIndexProcessor challengeProcessor = new ChallengeSearchIndexProcessor(false);
        processorMap.put(EntityType.CHALLENGE, challengeProcessor);
        processorMap.put(EntityType.DISCUSSION, new DiscussionSearchIndexProcessor());
        processorMap.put(EntityType.TEST, new TestSearchIndexProcessor());
        processorMap.put(EntityType.ASSIGNMENT, new AssignmentSearchIndexProcessor());
    }

    @Override
    public Status process(IConsumable consumable) {

        UserEntityActionDetails details = load(consumable);
        if (details.target != null) {
            AbstractSearchIndexProcessor searchIndexProcessor = processorMap
                    .get(details.target.type);
            if (searchIndexProcessor == null) {
                return Status.SUCCESS;
            }
            AbstractSearchDetail searchDetails = null;
            @SuppressWarnings("rawtypes")
            VedantuBasicDAO dao = EntityTypeDAOFactory.INSTANCE.get(details.target.type);
            if (dao != null) {
                try {
                    VedantuBaseMongoModel mongoModel = dao.getById(details.target.id);
                    if (mongoModel == null || !(mongoModel instanceof IIndexable)) {
                        LOGGER.error("entity[" + details.target + "] not found ");
                        return Status.NOT_CONSUMABLE;
                    }
                    EventType indexEventType = EntityIndexEventMapper.INSTANCE
                            .get(details.target.type);
                    searchDetails = (AbstractSearchDetail) EventDetailsFactory.getInstance()
                            .getDetails(indexEventType);

                    searchDetails.fromMongoModel(mongoModel);
                    searchDetails.setAction(EventActionType.UPDATE.name());
                    searchDetails.setUserAction(UserActionType.UPDATED);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    return Status.FAILURE;
                }
            }

            if (searchDetails != null) {
                searchIndexProcessor.process(consumable, searchDetails, false);
            }
        }
        return Status.SUCCESS;
    }

    private UserEntityActionDetails load(IConsumable consumable) {

        UserEntityActionDetails details = new UserEntityActionDetails();
        Event event = (Event) consumable;
        LOGGER.info("processing event for " + this.getClass() + " process for userId :"
                + event.getUserId() + ", eventId : " + event._getStringId());
        try {
            LOGGER.info("loading the details object :" + event._getInfo());
            details.fromJSON(event._getInfo());
            LOGGER.info("event details obj is " + details.toJSON());
        } catch (Exception e) {
            LOGGER.error("could not load from json", e);

        }
        return details;
    }

}
