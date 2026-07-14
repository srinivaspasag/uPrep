package com.lms.user.vedantu.user.requests;

import com.lms.common.validation.Validation;
import com.lms.common.vedantu.commons.pojos.requests.AbstractAppCheckReq;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Setter
@Getter
@NoArgsConstructor
public class UserAuthReq extends AbstractAppCheckReq {

    @NotNull(message = "username is required")
    private String username;
    @NotBlank(message = "contact number should not be null")
    private String contactNumber;
    @NotBlank(message = "countryCode should not be null")
    private String countryCode;
    private String orgId;
    private String password;
    private boolean isOTPlogin;
    private String progType;
    private boolean isNewPhone;
    private boolean dl = false;

    public void setUsername(String username) {
        this.username = Validation.isStringNotEmpty(username) ? username : "";
    }

}