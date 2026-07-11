package com.lms.pojos.requests;

import com.lms.pojo.request.AbstractOrgScopeReq;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class NotificationRegIDReq extends AbstractOrgScopeReq {
    private static final Logger logger = LoggerFactory.getLogger(NotificationRegIDReq.class);

    @NotBlank
    public String regId;
    @NotBlank
    public String deviceId;
    @NotBlank
    public String programName;

    public String validate() {
        logger.debug(".....Inside NotificationRegIdReqValidation validate function.........");

        if (regId == null) {
            return "regId is null";
        }
        if (deviceId == null) {
            return "deviceId is null";
        }
        if (userId == null)
            return "userId is null";
        return null;
    }
}
