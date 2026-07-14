package com.lms.Component;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.constants.HardCodedConstants;
import com.lms.common.vedantu.content.AbstractEmailTemplateDetails;
import com.lms.common.vedantu.content.IEmailTemplateDetails;
import com.lms.common.vedantu.enums.MailCategory;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.models.Organization;
import com.lms.pojos.EmailFactory;
import com.lms.pojos.MailTemplateEmailDetails;
import com.lms.repository.OrganizationRepo;
import com.lms.user.vedantu.user.events.IndividualEmailTemplateDetails;
import com.lms.user.vedantu.user.model.UserEmailUnsubscription;
import com.lms.user.vedantu.user.pojo.UserEmailInfo;
import com.lms.user.vedantu.user.repository.UserEmailUnSubScriptionRepo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Component
public class MailManager {
        private final static Logger logger = LoggerFactory.getLogger(MailManager.class);
        private static final long mailerSleepInMS;
        private static boolean isMailingEnabled;

        static {

                isMailingEnabled = true;

                mailerSleepInMS = 300000;

        }

        @Value("${smtp.port}")
        private final int smtpPort = 25;
        private final boolean isUsingTLS = false;
        private final Boolean EMAIL_CONFIGURATION_ENABLED = true;
        private String smtpHost;
        @Value("${smtp.user}")
        private String smtpUserName;
        @Value("${smtp.password}")
        private String smtpUserPassword;
        @Autowired
        private OrganizationRepo organizationRepo;
        @Autowired
        private UserEmailUnSubScriptionRepo userEmailUnSubScriptionRepo;
        @Autowired
        private MongoTemplate mongoTemplate;

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

        synchronized public boolean sendEmail(IEmailTemplateDetails details, String orgId) {

                try {
                        if (!isMailingEnabled) {
                                logger.debug("Mailing not enabled ");
                                return false;
                        }
                        HtmlEmail mail = null;
                        String email = HardCodedConstants.emptyString;
                        Optional<Organization> org1 = organizationRepo.findById(orgId);
                        // MailerAPI mail = Play.application().plugin(MailerPlugin.class).email();
                        Organization org = new Organization();
                        if (!org1.isPresent()) {
                                logger.debug("OrgId is null");
                                mail = EmailFactory.getInstance(smtpUserName, smtpUserPassword, smtpHost).getEmail();
                        } else {
                                if (!StringUtils.isEmpty(org.smtpUser) && !StringUtils.isEmpty(org.smtpPassword)) {
                                        email = org.smtpUser;
                                        logger.debug("smtp user and password exists in DB");
                                        mail = EmailFactory.getInstance(org.smtpUser, org.smtpPassword, org.smtpHost).getEmail();
                                } else {
                                        logger.debug("smtp user and password doesnt exist in DB");
                                        mail = EmailFactory.getInstance(smtpUserName, smtpUserPassword, smtpHost).getEmail();
                                }
                        }

                        if (!details.verify()) {
                                logger.error("Can not send email using details  " + details
                                        + " as its not verified ");
                                return false;
                        }
                        mail.setSubject(details.getSubject());
                        if (details.getHeaders() != null
                                && CollectionUtils.isNotEmpty(details.getHeaders().keySet())) {
                                for (String key : details.getHeaders().keySet()) {
                                        logger.debug("Header " + key + "  value" + details.getHeaders().get(key));
                                        mail.addHeader(key, details.getHeaders().get(key));
                                }
                        }
                        for (AbstractEmailTemplateDetails.UserEmailPojos recepient : details.getRecepients()) {
                                logger.debug("email recepient : " + recepient);
                                if (!StringUtils.isEmpty(recepient.email)) {
                                        // mail.addRecipient(recepient);
                                        // mail.addTo(recepient);
                                        try {
                                                mail.addTo(recepient.email, recepient.name);
                                        } catch (EmailException e) {
                                                logger.debug(":::::::::::::          Unable to add this user to bcc " + e.getMessage());
                                        }
                                }
                        }

                        if (CollectionUtils.isNotEmpty(details.getCCRecepients())) {
                                for (AbstractEmailTemplateDetails.UserEmailPojos recepient : details.getCCRecepients()) {
                                        logger.debug("email recepient : " + recepient);
                                        if (!StringUtils.isEmpty(recepient.email)) {
                                                try {
                                                        mail.addCc(recepient.email, recepient.name);
                                                } catch (EmailException e) {
                                                        logger.debug(":::::::::::::          Unable to add this user to bcc " + e.getMessage());
                                                }
                                        }
                                }

                        }

                        if (CollectionUtils.isNotEmpty(details.getBCCRecepients())) {
                                for (AbstractEmailTemplateDetails.UserEmailPojos recepient : details.getBCCRecepients()) {
                                        logger.debug("email recepient : " + recepient);
                                        if (!StringUtils.isEmpty(recepient.email)) {
                                                try {
                                                        mail.addBcc(recepient.email, recepient.name);
                                                } catch (EmailException e) {
                                                        logger.debug(":::::::::::::          Unable to add this user to bcc " + e.getMessage());
                                                }
                                        }
                                }

                        }

                        logger.debug("Sender" + details.getSender());

                        mail.setFrom(!StringUtils.isEmpty(org.communicationMail) ? org.communicationMail : "noreply@" + org.slug + ".com", (org.representative.firstName + " " + org.representative.lastName).trim());
                        //   String htmlContent = details.__getContent();
                        //logger.debug(" Mail body generated is " + htmlContent);

                        MailTemplateEmailDetails mailDetails = new MailTemplateEmailDetails();
                        if (details instanceof IndividualEmailTemplateDetails) {
                                mailDetails.user = ((IndividualEmailTemplateDetails) details).user;

                                if (mailDetails.user != null && !isEmailAllowed(mailDetails.user.id,
                                        mailDetails.user.email, mailDetails.user.category)) {
                                        logger.debug("Email sending is not allwoed for email" + mailDetails.user);
                                        return true;
                                }
                        } else {
                                mailDetails.user = new UserEmailInfo();
                        }
                        mailDetails.title = details.getSubject();
                        //   mailDetails.emailContent = Html.apply(htmlContent);

                        String finalContent = mailDetails.__getContent();

                        logger.debug(" Mail body generated is " + finalContent);
                        if (StringUtils.isEmpty(finalContent)) {
                                throw new VedantuException(VedantuErrorCode.EMAIL_CAN_NOT_BE_SEND,
                                        "empty body for email ");

                        }
                        mail.setHtmlMsg(finalContent);
                        // mail.sendHtml(finalContent);
                        try {
                                mail.send();
                        } catch (EmailException e) {
                                logger.debug(":::::::::::::          Unable to send email for this user " + e);
                                return false;
                        }
                        logger.debug(" Sleeping now ");
                        Thread.sleep(mailerSleepInMS);
                        return true;
                } catch (Throwable ex) {
                        logger.error("Failed email sending ", ex);
                        return false;
                }
        }

        public boolean isEmailAllowed(String userId, String email, MailCategory category) {

                if (category == null || category == MailCategory.UNKNOWN) {
                        return true;
                }

                Query findQuery = new Query();
                Criteria criteria = new Criteria();
                criteria.and(ConstantsGlobal.USER_ID).is(userId);
                criteria.and("email").equals(email);
                criteria.and("recordState").equals(VedantuRecordState.ACTIVE);

                criteria.and("restrictions.category").in(Arrays.asList(MailCategory.ALL, category));
                List<UserEmailUnsubscription> unsubcription = mongoTemplate.find(findQuery.addCriteria(criteria), UserEmailUnsubscription.class);
                return (unsubcription == null);
        }
}
