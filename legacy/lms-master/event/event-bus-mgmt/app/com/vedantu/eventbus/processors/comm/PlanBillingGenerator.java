package com.vedantu.eventbus.processors.comm;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;
import com.vedantu.organization.event.details.PlanBillingDetails;

public class PlanBillingGenerator implements IProcessor {

    private static final ALogger LOGGER = Logger.of(PlanBillingGenerator.class);

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
        PlanBillingDetails details = (PlanBillingDetails) event.fetchEventDetails();

        if (details == null) {
            LOGGER.error(" Invalid event details " + event.getType());
            return Status.FAILURE;
        }
        if (StringUtils.isEmpty(details.orgId) || StringUtils.isEmpty(details.planId)) {
            LOGGER.error("Bill can not be generated as organization has not signed up for any plans"
                    + details);
            return Status.FAILURE;
        }

        return Status.SUCCESS;
    }
}
