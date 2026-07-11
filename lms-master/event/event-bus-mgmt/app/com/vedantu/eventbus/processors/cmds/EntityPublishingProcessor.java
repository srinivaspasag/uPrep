package com.vedantu.eventbus.processors.cmds;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.mgmt.publishers.EntityTypePublisherFactory;
import com.vedantu.cmds.mgmt.publishers.IPublisher;
import com.vedantu.cmds.models.event.details.EntityPublishingDetails;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.daos.EntityOperationStatusDAO;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.models.mongo.EntityOperationStatus;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;

public class EntityPublishingProcessor implements IProcessor {

    private static final ALogger LOGGER = Logger.of(EntityPublishingProcessor.class);

    public EntityPublishingProcessor() {

    }

    @Override
    public Status process(IConsumable consumable) {

        if (consumable == null || !(consumable instanceof IConsumable)) {
            LOGGER.debug(" Invalid event " + consumable);
            return Status.FAILURE;
        }

        Event event = (Event) consumable;

        IEventDetails details = event.fetchEventDetails();
        Logger.info("fetched eventDetails " + details);

        if (details == null || !(details instanceof EntityPublishingDetails)) {
            LOGGER.debug(" Invalid details " + consumable + " not of type "
                    + EntityPublishingDetails.class);
            return Status.FAILURE;
        }

        EntityPublishingDetails publishableEntityDetails = (EntityPublishingDetails) details;
        final IPublisher publisher = EntityTypePublisherFactory.INSTANCE
                .get(publishableEntityDetails.content.type);

        if (publisher == null) {
            LOGGER.debug(" Invalid publisher for type " + publishableEntityDetails.content.type);
            return Status.FAILURE;

        }

        String errorCode = null;

        try {
            publisher.publish(publishableEntityDetails.userId, publishableEntityDetails.orgId,
                    publishableEntityDetails.content, publishableEntityDetails.jobId);
        } catch (VedantuException ex) {

            LOGGER.debug("Vedantu exception", ex);

            errorCode = ex.errorCode.name();

        } catch (Exception ex) {
            LOGGER.error("Non vedantu exception", ex);

            errorCode = VedantuErrorCode.SERVICE_ERROR.name();
        }

        EntityOperationStatus status = EntityOperationStatusDAO.INSTANCE
                .getById(publishableEntityDetails.jobId);
        status.errorCode = errorCode == null ? StringUtils.EMPTY : errorCode;

        EntityOperationStatusDAO.INSTANCE.save(status);

        if (StringUtils.isNotEmpty(status.errorCode)) {
            return Status.FAILURE;
        }

        return Status.SUCCESS;
    }
}
