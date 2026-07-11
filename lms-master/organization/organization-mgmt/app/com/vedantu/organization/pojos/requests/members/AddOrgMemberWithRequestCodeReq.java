package com.vedantu.organization.pojos.requests.members;

import org.apache.commons.lang3.StringUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAppCheckReq;

public class AddOrgMemberWithRequestCodeReq extends AbstractAppCheckReq {

    @Required
    public String accessCode;

    @Required
    public String firstName;
    public String lastName = StringUtils.EMPTY;

    @Required
    public String email;

    public String twitterHandle;

    @Override
    public String toString() {

        return "AddOrgMemberWithRequestCodeReq [accessCode=" + accessCode + ", firstName="
                + firstName + ", lastName=" + lastName + ", email=" + email + ", twitterHandle="
                + twitterHandle + "]";
    }

    // public OrgMemberProfile profile; --> for now only student addition is enabled

}
