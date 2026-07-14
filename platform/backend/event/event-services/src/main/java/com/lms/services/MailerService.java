package com.lms.services;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.EmailStatusAndEnableReq;
import com.lms.pojos.requests.TestDummyEmailReq;

import java.io.IOException;

public interface MailerService {
    VedantuResponse testMail(TestDummyEmailReq testDummyEmailReq) throws IOException;

    VedantuResponse getStatus(EmailStatusAndEnableReq emailStatusAndEnableReq);

    VedantuResponse setEmailStatus(EmailStatusAndEnableReq emailStatusAndEnableReq);

    VedantuResponse testConfig(EmailStatusAndEnableReq emailStatusAndEnableReq);

    VedantuResponse informOrgs(TestDummyEmailReq testDummyEmailReq);
}
