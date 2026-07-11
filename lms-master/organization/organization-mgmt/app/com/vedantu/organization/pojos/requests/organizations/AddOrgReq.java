package com.vedantu.organization.pojos.requests.organizations;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.Location;
import com.vedantu.user.pojos.UserBasicInfo;

public class AddOrgReq extends AbstractAddOrgReq {

    @Required
    public UserBasicInfo representative;

    @Required
    public String        planId;

    @Required
    public String        tncVersion;
    public boolean       isNewUI;
    public String        theme;
    public boolean       showSharedSubjects;

    public String validate() {

        String superValidate = super.validate();
        if (null != superValidate) {
            return superValidate;
        }
        if (null != representative) {
            String representativeValidation = representative.validate();
            if (null != representativeValidation) {
                return representativeValidation;
            }
        }
        if (null != locations) {
            for (Location location : locations) {
                if (null != location) {
                    String locationValidation = location.validate();
                    if (null != locationValidation) {
                        return locationValidation;
                    }
                }
            }
        }
        return null;
    }
}
