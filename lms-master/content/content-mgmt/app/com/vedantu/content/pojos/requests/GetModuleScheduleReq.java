package com.vedantu.content.pojos.requests;

import play.data.validation.Constraints.Required;

public class GetModuleScheduleReq {
    @Required
    public String sectionId;
    public String testId;
    @Required
    public String moduleId; //MODULE
}