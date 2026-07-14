package com.lms.user.vedantu.user.requests;

import com.lms.user.vedantu.user.enums.Gender;
import com.lms.user.vedantu.user.pojo.UserBasicInfo;
import com.lms.common.vedantu.constants.HardCodedConstants;
import com.lms.common.vedantu.enums.AuthType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class AddUserReq extends AbstractAppCheckReq{
    @NotBlank(message = "User name is required")
    private String  username;
    @NotBlank(message = "Password is required")
    public String   password;
    @NotBlank(message = "First name is required")
    public String   firstName;
    public String   lastName = HardCodedConstants.emptyString;
    @NotBlank(message = "Date of birth is required")
    public String   dob;
    @NotBlank(message = "Gender is required")
    public Gender gender;
    private String  email    =  HardCodedConstants.emptyString;;
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

        this.username = username;
    }

    public String getEmail() {

        return email;
    }

    public void setEmail(String email) {

        this.email = email.toLowerCase();
    }
}
