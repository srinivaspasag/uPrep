package com.lms.pojo.request;

import javax.validation.constraints.NotBlank;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAppCheckReq;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class AddOrgMemberWithRequestCodeReq extends AbstractAppCheckReq {

	@NotBlank(message = "accessCode should not be null")
    public String accessCode;

	@NotBlank(message = "firstName should not be null")
    public String firstName;
    public String lastName = "";

    @NotBlank(message = "email should not be null")
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
