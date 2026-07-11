package com.lms.pojos.requests;

import com.lms.enums.OrgMemberProfile;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.Map;

@Setter
@Getter
public abstract class AbstractGetContentReq {

    @NotBlank(message = "id should not be null")
    public String id;

    // will be set from API call wherever required
    private Map<String, String> sessionParams;

    public Map<String, String> __getSessionParams() {

        return sessionParams;
    }

    public void __setSessionParams(Map<String, String> sessionParams) {

        this.sessionParams = sessionParams;
    }

    public String orgId;

    public OrgMemberProfile orgMemberProfile;
    @NotBlank(message = "Calling user ID is required")
    public String callingUserId;
    @NotBlank(message = "User ID is required")
    public String userId;
    @NotBlank(message = "callingAppId should not be null")
    public String callingAppId;
    @NotBlank(message = "callingAppId should not be null")
    public String callingApp;

    public boolean isWebReq() {

        return callingAppId.equalsIgnoreCase("cmds-app") || callingAppId.equalsIgnoreCase("web-app");

    }
}
