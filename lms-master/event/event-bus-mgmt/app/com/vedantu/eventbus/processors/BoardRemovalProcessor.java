package com.vedantu.eventbus.processors;

import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.board.event.details.BoardRemovalDetails;
import com.vedantu.cmds.models.event.search.details.ReIndexDetails;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.content.interfaces.BoardUpdatable;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.events.manager.EventScheduler;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;

public class BoardRemovalProcessor implements IProcessor {

    private static final ALogger LOGGER = Logger.of(BoardRemovalProcessor.class);

    @Override
    public Status process(IConsumable consumable) {

        Event event = (Event) consumable;
        if (event.action == EventActionType.REMOVE) {
            LOGGER.info("as event action is remove hence not processing the event for newsActivity generation : "
                    + consumable._getConsumableId());
            return Status.SUCCESS;
        }
        LOGGER.info("processing Event for " + event.getType() + " process for userId :"
                + event.getUserId());

        LOGGER.info("fetching eventDetails");
        BoardRemovalDetails details = (BoardRemovalDetails) event.fetchEventDetails();

        if (details == null) {
            LOGGER.error(" Invalid event details " + event.getType());
            return Status.FAILURE;
        }

        // query 2
        // get all content update state completed false, pull all topicboards, get all content from

        // query 3

        // ILE corresponding to this and set to temporary state

        if (CollectionUtils.isNotEmpty(details.brdIds)) {
            for (EntityType entityType : EntityType.values()) {
                if (EntityType.isSupportedCMDSLibraryEntityType(entityType)
                        || EntityType.isSupportedContentType(entityType)) {

   
                    VedantuBasicDAO<VedantuBaseMongoModel, ObjectId>  vedantuBasicDAO = EntityTypeDAOFactory.INSTANCE
                            .get(entityType);
                    if (vedantuBasicDAO != null && vedantuBasicDAO instanceof BoardUpdatable) {
                        
                        LOGGER.debug("Instanceof board updateable" );
                        BoardUpdatable boardUpdatable = (BoardUpdatable) vedantuBasicDAO;
                        try {

                            ReIndexDetails reindexIndexDetails = new ReIndexDetails();

                            reindexIndexDetails.userId = details.userId;
                            reindexIndexDetails.type = entityType;
                            reindexIndexDetails.containerType = null;

                            reindexIndexDetails.ids = boardUpdatable.update(details.changeState,
                                    true, details.brdIds);
                            if (CollectionUtils.isNotEmpty(reindexIndexDetails.ids)) {

                                EventScheduler.generateEventAysc(details.userId,
                                        reindexIndexDetails, EventType.REINDEX_CMDS_RESOURCE);
                            }

                        } catch (VedantuException e) {
                            LOGGER.error(" Updating entity of type" + entityType + " failed. ", e);
                        }

                    }
                }
            }
        }

        return Status.SUCCESS;
    }

}
