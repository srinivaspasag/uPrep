package com.vedantu.eventbus.processors;

import java.util.ArrayList;
import java.util.List;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.models.event.details.CalculateSizeDetails;
import com.vedantu.cmds.models.event.details.EntityPublishingDetails;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.content.interfaces.IContentManager;
import com.vedantu.commons.daos.EntityOperationStatusDAO;
import com.vedantu.commons.entity.factory.EntityTypeContentManagerFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;

public class EntitySizeCalculatorProcessor implements IProcessor {

    private static final ALogger LOGGER = Logger.of(EntitySizeCalculatorProcessor.class);

    @Override
    public Status process(IConsumable consumable) {

        boolean newsGenerationResult = false;
        Event event = (Event) consumable;
      
        LOGGER.info("processing Event for " + event.getType() + " process for userId :"
                + event.getUserId());
        try {

            List<String> ids = new ArrayList<String>();
            EntityType type = null;
            String jobId = null;
            boolean recalculate = true;
            IEventDetails details = event.fetchEventDetails();
            if (details instanceof CalculateSizeDetails) {
                ids.addAll(((CalculateSizeDetails) details).ids);
                type = ((CalculateSizeDetails) details).type;
                recalculate= true;
            } else if (details instanceof EntityPublishingDetails) {

                ids.add(((EntityPublishingDetails) details).content.id);
                type = ((EntityPublishingDetails) details).content.type;
                jobId = ((EntityPublishingDetails) details).jobId;
            }

            LOGGER.info("calculating using " + ids);

            IContentManager manager = EntityTypeContentManagerFactory.INSTANCE.get(type);

            if (manager == null) {
                LOGGER.debug("Failed to calculate size for content type" + type);
                return Status.FAILURE;
            }
            for (String id : ids) {
                
                manager.calculate(id,recalculate);

            }
            EntityOperationStatusDAO.INSTANCE.incCompletion(jobId);
            LOGGER.info("result : " + newsGenerationResult);
            return Status.SUCCESS;
        } catch (VedantuException e) {

            LOGGER.error("Failed to calculate size ", e);
        }
        return Status.FAILURE;
    }

}
