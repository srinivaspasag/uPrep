package com.vedantu.eventbus.processors.comm;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import play.Logger;
import play.Play;
import play.Logger.ALogger;

import com.vedantu.commons.content.interfaces.AbstractEmailTemplateDetails;
import com.vedantu.commons.content.interfaces.IEmailTemplateDetails;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.eventbus.managers.MailManager;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;
import com.vedantu.organization.daos.OrgMemberDAO;
import com.vedantu.organization.daos.OrganizationDAO;

public class EmailProcessor implements IProcessor {

    private final static ALogger LOGGER = Logger.of(EmailProcessor.class);

    @Override
    public Status process(IConsumable consumable) {

        boolean isEmailSent = false;
        try {
            Event e = (Event) consumable;
            JSONObject jsonInfo = e._getInfo();
            String userId = e.getUserId();
            LOGGER.debug("User Id of this event is "+userId);
            String className = JSONUtils.getString(jsonInfo, AbstractEmailTemplateDetails.CLAZZ);

            @SuppressWarnings("unchecked")
            Class<IEventDetails> detailsClazz = (Class<IEventDetails>) Class.forName(className);
            IEventDetails details = detailsClazz.newInstance();

            details.fromJSON(jsonInfo);

            if (details instanceof IEmailTemplateDetails) {
                IEmailTemplateDetails emailDetails = (IEmailTemplateDetails) details;
                String orgId = getOrgIdFromUserId(userId);
                LOGGER.debug("EmailProcessor : Before calling MailManager.sendEmail function. isEmailSent : "+isEmailSent);
                isEmailSent = MailManager.sendEmail(emailDetails,orgId);
                LOGGER.debug("EmailProcessor : After calling MailManager.sendEmail function. isEmailSent : "+isEmailSent);
            }

        } catch (Throwable ex) {
            LOGGER.error(ex.getMessage(), ex);
            // not handling as of now
        }
        if (isEmailSent) {
            return Status.SUCCESS;
        }
        return Status.FAILURE;
    }

    private String getOrgIdFromUserId(String userId) {
        if(StringUtils.isNotEmpty(userId)){
            return OrgMemberDAO.INSTANCE.getByUserId(userId).orgId;
        }
        return Play.application().configuration().getString("learnpedia.id");
    }
}
