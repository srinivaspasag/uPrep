package com.vedantu.eventbus.requests;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;
import play.data.validation.Constraints.Required;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class TestDummyEmailReq extends AbstractAuthCheckReq {

    private static final ALogger LOGGER = Logger.of(TestDummyEmailReq.class);
    public String                emails;
    @Required
    public File                  htmlFile;

    public String                htmlFileName;

    public String                subject;

    public String                orgId;

    public TestDummyEmailReq(MultipartFormData body) {

        super(body.asFormUrlEncoded());
        LOGGER.debug("Body content" + body.asFormUrlEncoded());
        FilePart videoFilePart = body.getFile("htmlFile");
        if (null != videoFilePart) {

            this.htmlFile = videoFilePart.getFile();
            this.htmlFileName = videoFilePart.getFilename();

        }
        orgId = _getValueFromMultipart(body.asFormUrlEncoded(), "orgId");
        LOGGER.debug("Org" + orgId);
        emails = _getValueFromMultipart(body.asFormUrlEncoded(), "emails");
        // LOGGER.debug("emailsFromReqArr: " + StringUtils.join(emailsFromReqArr, ", "));

        // if (null == emailsFromReqArr) {
        // String email = _getValueFromMultipart(body.asFormUrlEncoded(), "emails");
        // if (StringUtils.isNotEmpty(email)) {
        // emailsFromReqArr = new String[] { email };
        // } else {
        // return;
        // }
        // }
        // for (String email : emailsFromReqArr) {
        // if (StringUtils.isEmpty(email)) {
        // continue;
        // }
        // emails.add(email);
        // }
        subject = _getValueFromMultipart(body.asFormUrlEncoded(), "subject");

    }

    @Override
    public String validate() {

        if (StringUtils.isEmpty(emails)) {
            return "no recepients provided ";
        }

        return null;
    }
}
