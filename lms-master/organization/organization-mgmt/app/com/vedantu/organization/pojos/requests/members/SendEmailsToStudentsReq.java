package com.vedantu.organization.pojos.requests.members;

import play.data.validation.Constraints.Required;

public class SendEmailsToStudentsReq {
    @Required
    public String subject;
    @Required
    public String message;
    @Required
    public String programId;
    @Required
    public String sectionId;
    @Required
    public String centerId;
    @Required
    public String orgId;
    public String userId;

}
