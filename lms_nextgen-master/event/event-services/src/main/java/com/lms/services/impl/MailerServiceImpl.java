package com.lms.services.impl;

import com.lms.Component.MailManager;
import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.component.CMDSLIbraryMaanager;
import com.lms.email.details.FeatureEmailDetails;
import com.lms.enums.OrgMemberProfile;
import com.lms.models.OrgMember;
import com.lms.models.Organization;
import com.lms.pojos.EmailStatusAndEnableReq;
import com.lms.pojos.requests.TestDummyEmailReq;
import com.lms.pojos.responces.EmailStatusRes;
import com.lms.pojos.responces.TestDummyEmailRes;
import com.lms.repository.OrganizationRepo;
import com.lms.services.MailerService;
import com.lms.user.vedantu.user.pojo.UserEmailInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.management.Query;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@Service
public class MailerServiceImpl implements MailerService {
    private static final Logger logger = LoggerFactory.getLogger(MailerServiceImpl.class);

    @Autowired
    private CMDSLIbraryMaanager cmdslIbraryMaanager;
    @Autowired
    private MailManager mailManager;
    @Autowired
    private OrganizationRepo organizationRepo;
    @Value("${amazon.s3.bucket.allowedorigins}")
    private List<Object> amazonS3bucketAllowedOrigins;

    @Override
    public VedantuResponse testMail(TestDummyEmailReq request) throws IOException {
        logger.debug(" Called sending emails ");

        TestDummyEmailRes response = null;


        FileInputStream htmlStream = null;


        if (StringUtils.isEmpty(request.orgId) || StringUtils.isEmpty(request.userId)) {
            throw new VedantuException(VedantuErrorCode.NOT_ALLOWED);
        }

        OrgMember member = cmdslIbraryMaanager.getMemberByUserId(request.orgId, request.userId);

        if (member == null || (member.profile != OrgMemberProfile.MANAGER)) {
            throw new VedantuException(VedantuErrorCode.NOT_ALLOWED);
        }

        response = new TestDummyEmailRes();
        htmlStream = new FileInputStream(request.htmlFile);
        FeatureEmailDetails details;
        details = new FeatureEmailDetails();
        details.user = new UserEmailInfo("Vedantu", "", "xyz@vedantu.com");

        details.messageContent = IOUtils.toString(htmlStream);
        details.setSubject(StringUtils.isEmpty(request.subject) ? "" : request.subject);
        boolean result = false;
        Set<String> mailRecepients = getMailReceipients(request.emails);

        if (CollectionUtils.isNotEmpty(mailRecepients)) {
            EmailValidator validator = EmailValidator.getInstance();
            for (String email : mailRecepients) {
                if (validator.isValid(email)) {

                    details.addRecepient("", email);

                    result = mailManager.sendEmail(details, request.orgId);
                    details.resetRecepients();
                }
            }

        }
        response.sent = result;

        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse getStatus(EmailStatusAndEnableReq emailStatusAndEnableReq) {

        EmailStatusRes response = new EmailStatusRes();

        response.emailEnabled = MailManager.getEmailSendingStatus();

        return new VedantuResponse(response);


    }

    @Override
    public VedantuResponse setEmailStatus(EmailStatusAndEnableReq emailStatusAndEnableReq) {
        EmailStatusAndEnableReq request = null;

        EmailStatusRes response = new EmailStatusRes();

        if (request.newStatus) {
            MailManager.enableEmail();
        } else {
            MailManager.disableEmail();
        }

        response.emailEnabled = MailManager.getEmailSendingStatus();

        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse testConfig(EmailStatusAndEnableReq emailStatusAndEnableReq) {
        List<Object> domains = amazonS3bucketAllowedOrigins;
        List<String> allowedOrigins = new ArrayList<String>();

        if (CollectionUtils.isNotEmpty(domains)) {
            for (Object domain : domains) {
                allowedOrigins.add(domain.toString());
            }
        }

        return new VedantuResponse(allowedOrigins);
    }

    @Override
    public VedantuResponse informOrgs(TestDummyEmailReq request) {


        logger.debug(" Called sending emails ");

        TestDummyEmailRes response = null;

        FileInputStream htmlStream = null;
        try {

            Set<UserEmailInfo> pojos = new HashSet<UserEmailInfo>();
            Set<String> mailRecepients = getMailReceipients(request.emails);
            for (String email : mailRecepients) {

                pojos.add(new UserEmailInfo("", "", email));
            }
            if (mailRecepients == null) {
                mailRecepients = new HashSet<String>();

            }

            if (!StringUtils.isEmpty(request.orgId)) {

                Optional<Organization> org1 = organizationRepo.findById(request.orgId);
                Organization org = org1.get();
                pojos.add(new UserEmailInfo(org.representative.firstName,
                        org.representative.lastName, org.representative.getEmail()));
            } else {
                Query query = new Query();
                Criteria criteria = new Criteria();


                List<Organization> organizations = organizationRepo.findAll();
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
                        result = mailManager.sendEmail(details, request.orgId);
                        details.resetRecepients();
                    }
                }

            }
            response.sent = result;

        } catch (Exception ex) {
            logger.error("Exception : ", ex);
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, ex);
        } finally {
            IOUtils.closeQuietly(htmlStream);
        }


        return new VedantuResponse(response);


    }

    private Set<String> getMailReceipients(String emails) {

        if (StringUtils.isEmpty(emails)) {
            return null;
        }
        List<String> emailList = Arrays.asList(StringUtils.split(emails, ","));
        Set<String> mailRecepients = new HashSet<String>();
        mailRecepients.addAll(emailList);
        return mailRecepients;
    }

}
