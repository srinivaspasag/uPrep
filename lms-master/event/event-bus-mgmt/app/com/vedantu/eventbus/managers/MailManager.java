package com.vedantu.eventbus.managers;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import play.Logger;
import play.Logger.ALogger;
import play.Play;
import play.api.templates.Html;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.configuration.EmailConfigurationConstants;
import com.vedantu.commons.content.interfaces.AbstractEmailTemplateDetails.UserEmailPojos;
import com.vedantu.commons.content.interfaces.IEmailTemplateDetails;
import com.vedantu.eventbus.email.details.MailTemplateEmailDetails;
import com.vedantu.eventbus.factory.EmailFactory;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.organization.models.Organization;
import com.vedantu.user.daos.UserEmailUnsubscriptionDAO;
import com.vedantu.user.event.details.IndividualEmailTemplateDetails;
import com.vedantu.user.pojos.UserEmailInfo;

public class MailManager {

    private final static ALogger LOGGER = Logger.of(MailManager.class);
    private static boolean       isMailingEnabled;
    private static long          mailerSleepInMS;
    static {

        isMailingEnabled = Play.application().configuration()
                .getBoolean(EmailConfigurationConstants.EMAIL_CONFIGURATION_ENABLED);

        mailerSleepInMS = Play.application().configuration()
                .getLong(EmailConfigurationConstants.EMAIL_POSTEMAIL_SLEEP_TIME);

    }

    private MailManager() {

    }

    synchronized public static boolean enableEmail() {

        isMailingEnabled = true;
        return true;
    }

    synchronized public static boolean disableEmail() {

        isMailingEnabled = false;
        return true;
    }

    synchronized public static boolean getEmailSendingStatus() {

        return isMailingEnabled;
    }

    synchronized public static boolean sendEmail(IEmailTemplateDetails details, String orgId) {

        try {
            if (!isMailingEnabled) {
                LOGGER.debug("Mailing not enabled ");
                return false;
            }
            HtmlEmail mail = null;
            String email = StringUtils.EMPTY;
            Organization org = OrganizationDAO.INSTANCE.getById(orgId);
            // MailerAPI mail = Play.application().plugin(MailerPlugin.class).email();
            if(org == null){
                LOGGER.debug("OrgId is null");
                mail = EmailFactory.getInstance(Play.application().configuration().getString("smtp.user"),
                        Play.application().configuration().getString("smtp.password"),Play.application().configuration().getString("smtp.host", "127.0.0.1")).getEmail();
            }else{
                if(StringUtils.isNotEmpty(org.smtpUser) && StringUtils.isNotEmpty(org.smtpPassword)){
                    email = org.smtpUser;
                    LOGGER.debug("smtp user and password exists in DB");
                    mail = EmailFactory.getInstance(org.smtpUser,org.smtpPassword,org.smtpHost).getEmail();
                }else{
                    LOGGER.debug("smtp user and password doesnt exist in DB");
                    mail = EmailFactory.getInstance(Play.application().configuration().getString("smtp.user"),
                            Play.application().configuration().getString("smtp.password"),Play.application().configuration().getString("smtp.host", "127.0.0.1")).getEmail();
                }
            }

            if (!details.verify()) {
                LOGGER.error("Can not send email using details  " + details
                        + " as its not verified ");
                return false;
            }
            mail.setSubject(details.getSubject());
            if (details.getHeaders() != null
                    && CollectionUtils.isNotEmpty(details.getHeaders().keySet())) {
                for (String key : details.getHeaders().keySet()) {
                    LOGGER.debug("Header " + key + "  value" + details.getHeaders().get(key));
                    mail.addHeader(key, details.getHeaders().get(key));
                }
            }
            for (UserEmailPojos recepient : details.getRecepients()) {
                LOGGER.debug("email recepient : " + recepient);
                if (StringUtils.isNotEmpty(recepient.email)) {
                    // mail.addRecipient(recepient);
                    // mail.addTo(recepient);
                    try {
                        mail.addTo(recepient.email, recepient.name);
                    }catch(EmailException e){
                        LOGGER.debug(":::::::::::::          Unable to add this user to bcc "+ e.getMessage());
                    }
                }
            }

            if (CollectionUtils.isNotEmpty(details.getCCRecepients())) {
                for (UserEmailPojos recepient : details.getCCRecepients()) {
                    LOGGER.debug("email recepient : " + recepient);
                    if (StringUtils.isNotEmpty(recepient.email)) {
                        try {
                            mail.addCc(recepient.email, recepient.name);
                        }catch(EmailException e){
                            LOGGER.debug(":::::::::::::          Unable to add this user to bcc "+ e.getMessage());
                        }
                    }
                }

            }

            if (CollectionUtils.isNotEmpty(details.getBCCRecepients())) {
                for (UserEmailPojos recepient : details.getBCCRecepients()) {
                    LOGGER.debug("email recepient : " + recepient);
                    if (StringUtils.isNotEmpty(recepient.email)) {
                        try {
                            mail.addBcc(recepient.email, recepient.name);
                        }catch(EmailException e){
                            LOGGER.debug(":::::::::::::          Unable to add this user to bcc "+ e.getMessage());
                        }
                    }
                }

            }

            LOGGER.debug("Sender" + details.getSender());

            mail.setFrom(StringUtils.isNotEmpty(org.communicationMail) ? org.communicationMail : "noreply@"+org.slug+".com", (org.representative.firstName+" "+org.representative.lastName).trim());
            String htmlContent = details.__getContent();
            LOGGER.debug(" Mail body generated is " + htmlContent);

            MailTemplateEmailDetails mailDetails = new MailTemplateEmailDetails();
            if (details instanceof IndividualEmailTemplateDetails) {
                mailDetails.user = ((IndividualEmailTemplateDetails) details).user;

                if (mailDetails.user != null && !UserEmailUnsubscriptionDAO.INSTANCE.isEmailAllowed(mailDetails.user.id,
                        mailDetails.user.email, mailDetails.user.category)) {
                    LOGGER.debug("Email sending is not allwoed for email" + mailDetails.user);
                    return true;
                }
            }else{
            	 mailDetails.user = new UserEmailInfo();
            }
            mailDetails.title = details.getSubject();
            mailDetails.emailContent = Html.apply(htmlContent);

            String finalContent = mailDetails.__getContent();

            LOGGER.debug(" Mail body generated is " + finalContent);
            if (StringUtils.isEmpty(finalContent)) {
                throw new VedantuException(VedantuErrorCode.EMAIL_CAN_NOT_BE_SEND,
                        "empty body for email ");

            }
            mail.setHtmlMsg(finalContent);
            // mail.sendHtml(finalContent);
            try{
                mail.send();
            }catch(EmailException e){
                LOGGER.debug(":::::::::::::          Unable to send email for this user "+ e);
                return false;
            }
            LOGGER.debug(" Sleeping now ");
            Thread.sleep(mailerSleepInMS);
            return true;
        } catch (Throwable ex) {
            LOGGER.error("Failed email sending ", ex);
            return false;
        }
    }
}
