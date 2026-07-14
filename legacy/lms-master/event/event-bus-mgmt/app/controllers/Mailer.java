package controllers;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

import play.Logger;
import play.Logger.ALogger;
import play.Play;
import play.data.Form;
import play.mvc.Http.MultipartFormData;
import play.mvc.Result;

import com.google.code.morphia.query.QueryResults;
import com.vedantu.comm.email.details.FeatureEmailDetails;
import com.vedantu.commons.JSONResponse;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.Configurations;
import com.vedantu.eventbus.managers.MailManager;
import com.vedantu.eventbus.requests.EmailStatusAndEnableReq;
import com.vedantu.eventbus.requests.TestDummyEmailReq;
import com.vedantu.eventbus.response.EmailStatusRes;
import com.vedantu.eventbus.response.TestDummyEmailRes;
import com.vedantu.organization.daos.OrgMemberDAO;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.organization.enums.OrgMemberProfile;
import com.vedantu.organization.models.OrgMember;
import com.vedantu.organization.models.Organization;
import com.vedantu.user.pojos.UserEmailInfo;

public class Mailer extends AbstractVedantuController {

    private static ALogger LOGGER = Logger.of(Mailer.class);

    private static Set<String> getMailReceipients(String emails) {

        if (StringUtils.isEmpty(emails)) {
            return null;
        }
        List<String> emailList = Arrays.asList(StringUtils.split(emails, ","));
        Set<String> mailRecepients = new HashSet<String>();
        mailRecepients.addAll(emailList);
        return mailRecepients;
    }

    /**
     * Accepts comma separated email addresses Only admins can sendout this email for an
     * organization no else will be allowed to do so.
     *
     * @return
     */
    public static Result testEmail() {

        LOGGER.debug(" Called sending emails ");

        TestDummyEmailRes response = null;

        try {
            FileInputStream htmlStream = null;
            try {
                MultipartFormData body = request().body().asMultipartFormData();
                LOGGER.debug("Requested body" + body);
                TestDummyEmailReq request = new TestDummyEmailReq(body);

                if (StringUtils.isEmpty(request.orgId) || StringUtils.isEmpty(request.userId)) {
                    throw new VedantuException(VedantuErrorCode.NOT_ALLOWED);
                }

                OrgMember member = OrgMemberDAO.INSTANCE.getMemberByUserId(request.orgId,
                        request.userId);

                if (member == null || (member.profile != OrgMemberProfile.MANAGER)) {
                    throw new VedantuException(VedantuErrorCode.NOT_ALLOWED);
                }

                response = new TestDummyEmailRes();
                htmlStream = new FileInputStream(request.htmlFile);
                FeatureEmailDetails details;
                details = new FeatureEmailDetails();
                details.user= new UserEmailInfo("Vedantu","","xyz@vedantu.com");

                details.messageContent = IOUtils.toString(htmlStream);
                details.setSubject(StringUtils.isEmpty(request.subject) ? "" : request.subject);
                boolean result = false;
                Set<String> mailRecepients = getMailReceipients(request.emails);

                if (CollectionUtils.isNotEmpty(mailRecepients)) {
                    EmailValidator validator = EmailValidator.getInstance();
                    for (String email : mailRecepients) {
                        if (validator.isValid(email)) {

                            details.addRecepient("", email);

                            result = MailManager.sendEmail(details,request.orgId);
                            details.resetRecepients();
                        }
                    }

                }
                response.sent = result;

            } catch (Exception ex) {
                LOGGER.error("Exception : ", ex);
                throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, ex);
            } finally {
                IOUtils.closeQuietly(htmlStream);
            }

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());

    }

    /*
     * Accepts comma separated email addresses Only admins can sendout this email for an
     * organization no else will be allowed to do so.
     *
     * @return
     */
    @SuppressWarnings("unused")
    public static Result informOrgs() {

        LOGGER.debug(" Called sending emails ");

        TestDummyEmailRes response = null;

        try {
            FileInputStream htmlStream = null;
            try {
                MultipartFormData body = request().body().asMultipartFormData();
                LOGGER.debug("Requested body" + body);
                TestDummyEmailReq request = new TestDummyEmailReq(body);

                Set<UserEmailInfo> pojos = new HashSet<UserEmailInfo>();
                Set<String> mailRecepients = getMailReceipients(request.emails);
                for (String email : mailRecepients) {

                    pojos.add(new UserEmailInfo("", "", email));
                }
                if (mailRecepients == null) {
                    mailRecepients = new HashSet<String>();

                }

                if (StringUtils.isNotEmpty(request.orgId)) {
                    Organization org = OrganizationDAO.INSTANCE.getById(request.orgId);
                    pojos.add(new UserEmailInfo(org.representative.firstName,
                            org.representative.lastName, org.representative.getEmail()));
                } else {
                    QueryResults<Organization> results = OrganizationDAO.INSTANCE.find();

                    List<Organization> organizations = results.asList();
                    if (CollectionUtils.isNotEmpty(organizations)) {
                        for (Organization org : organizations) {

                            pojos.add(new UserEmailInfo(org.representative.firstName,
                                    org.representative.lastName, org.representative.getEmail()));
                        }
                    }
                }
                response = new TestDummyEmailRes();
                htmlStream = new FileInputStream(request.htmlFile);
                FeatureEmailDetails details;

                details = new FeatureEmailDetails();

                details.messageContent = IOUtils.toString(htmlStream);
                details.setSubject(StringUtils.isEmpty(request.subject) ? "" : request.subject);
                boolean result = false;

                if (CollectionUtils.isNotEmpty(mailRecepients)) {
                    EmailValidator validator = EmailValidator.getInstance();
                    for (UserEmailInfo userInfo : pojos) {
                        if (validator.isValid(userInfo.email)) {
                            details.user = userInfo;
                            details.addRecepient(details.user.getFullName(), userInfo.email);
                            result = MailManager.sendEmail(details,request.orgId);
                            details.resetRecepients();
                        }
                    }

                }
                response.sent = result;

            } catch (Exception ex) {
                LOGGER.error("Exception : ", ex);
                throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, ex);
            } finally {
                IOUtils.closeQuietly(htmlStream);
            }

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());

    }

    /**
     * Maintenance api for mailing system
     *
     * @return
     */
    public static Result getStatus() {

        LOGGER.debug(" Called add ");
        EmailStatusAndEnableReq request = null;
        EmailStatusRes response = new EmailStatusRes();
        Form<EmailStatusAndEnableReq> requestForm = Form.form(EmailStatusAndEnableReq.class)
                .bindFromRequest();

        LOGGER.debug("Request " + requestForm.data());

        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        request = requestForm.get();

        response.emailEnabled = MailManager.getEmailSendingStatus();

        return ok(getResultResponse(response).toObjectNode());

    }

    /**
     * Maintenance api for mailing system
     *
     * @return
     */

    public static Result setEmailStatus() {

        EmailStatusAndEnableReq request = null;

        EmailStatusRes response = new EmailStatusRes();
        LOGGER.debug(" Called add ");

        Form<EmailStatusAndEnableReq> requestForm = Form.form(EmailStatusAndEnableReq.class)
                .bindFromRequest();

        LOGGER.debug("Request " + requestForm.data());

        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        request = requestForm.get();
        if (request.newStatus) {
            MailManager.enableEmail();
        } else {
            MailManager.disableEmail();
        }

        response.emailEnabled = MailManager.getEmailSendingStatus();

        return ok(getResultResponse(response).toObjectNode());

    }

    /**
     * Accepts comma sepearted email addresses Only admins can sendout this email for an
     * organization no else will be allowed to do so.
     *
     * @return
     */
    public static Result testConfig() {

        List<Object> domains = Play.application().configuration()
                .getList(Configurations.AMAZON_S3_BUCKET_ALLOWEDORIGINS);
        List<String> allowedOrigins = new ArrayList<String>();

        if (CollectionUtils.isNotEmpty(domains)) {
            for (Object domain : domains) {
                Logger.debug("Allowed origin for s3 :" + domain.toString());
                allowedOrigins.add(domain.toString());
            }
        }

        return ok();
    }
}