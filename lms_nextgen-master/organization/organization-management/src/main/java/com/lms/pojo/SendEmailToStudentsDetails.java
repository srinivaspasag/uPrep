package com.lms.pojo;

import org.json.JSONException;
import org.json.JSONObject;

import com.lms.common.vedantu.constants.EmailConfigurationConstants;
import com.lms.common.vedantu.content.AbstractEmailTemplateDetails;

import common.utils.JSONUtils;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendEmailToStudentsDetails extends AbstractEmailTemplateDetails{

    public String message;
    protected SendEmailToStudentsDetails(AbstractEmailTemplateDetails details) {
        super(details);
    }

    public SendEmailToStudentsDetails() throws ClassNotFoundException {
        super(EmailConfigurationConstants.TEMPLATE_SEND_EMAIL_TO_STUDENTS );
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public boolean verify() {
        if (!bccEmails.isEmpty()){
            return true;
        }
        return false;
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();

        json.put("subject", subject);
        json.put("message", message);
        return json;

    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);

        subject = JSONUtils.getString(json, "subject");
        message = JSONUtils.getString(json, "message");
    }

}
