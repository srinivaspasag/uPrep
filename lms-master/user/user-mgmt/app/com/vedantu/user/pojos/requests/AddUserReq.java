package com.vedantu.user.pojos.requests;

import org.apache.commons.lang3.StringUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.enums.AuthType;
import com.vedantu.commons.pojos.requests.AbstractAppCheckReq;
import com.vedantu.user.enums.Gender;
import com.vedantu.user.pojos.UserBasicInfo;

public class AddUserReq extends AbstractAppCheckReq {

    @Required
    private String  username;
    @Required
    public String   password;
    @Required
    public String   firstName;
    public String   lastName = StringUtils.EMPTY;
    @Required
    public String   dob;
    @Required
    public Gender   gender;
    private String  email    = StringUtils.EMPTY;
    public String   twitterHandle;

    public AuthType authType = AuthType.VEDANTU;

    public String   orgId;

    public boolean  isPhoneVerified;
    public boolean  isSysGenPassword;
    public boolean  isOTPuser;

    public AddUserReq() {

    }

    public AddUserReq(UserBasicInfo userBasicInfo, String password, String dob, Gender gender) {

        this(userBasicInfo.getEmail(), password, userBasicInfo.firstName, userBasicInfo.lastName,
                dob, gender, userBasicInfo.getEmail());
    }

    public AddUserReq(String username, String password, String firstName, String lastName,
            String dob, Gender gender, String email) {

        this(username, password, firstName, lastName, dob, gender, email, AuthType.VEDANTU);
    }

    public AddUserReq(String username, String password, String firstName, String lastName,
            String dob, Gender gender, String email, AuthType type) {

        setUsername(username);
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.gender = gender;
        this.authType = type;
        setEmail(email);
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = StringUtils.lowerCase(username);
    }

    public String getEmail() {

        return email;
    }

    public void setEmail(String email) {

        this.email = StringUtils.lowerCase(email);
    }

}
