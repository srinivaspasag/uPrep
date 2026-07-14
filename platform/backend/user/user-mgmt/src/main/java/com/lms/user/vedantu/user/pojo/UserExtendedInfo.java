package com.lms.user.vedantu.user.pojo;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelExtendedInfo;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.user.vedantu.user.enums.Gender;
import com.lms.user.vedantu.user.model.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserExtendedInfo extends ModelExtendedInfo {


    public String username;
    public String firstName;
    public String lastName;
    public String dob;
    public Gender gender;
    public String thumbnail;
    public String email;
    public boolean isEmailVerified;
    public boolean isPhoneVerified;
    public boolean isSysGenPassword;
    public boolean isOTPuser;
    public boolean isEmailSubscribed;
    public String emailChangeRequested;

    public UserExtendedInfo(User user) {

        this.username = user.getUsername();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.dob = user.getDob();
        this.gender = user.getGender();
        this.thumbnail = user.getThumbnail();
        this.email = user.getEmail();
        this.isEmailVerified = user.isEmailVerified;
        this.isPhoneVerified = user.isPhoneVerified;
        this.isSysGenPassword = user.isSysGenPassword;
        this.isOTPuser = user.isOTPuser;
        if (user.getEmailChangeReq() != null)
            this.emailChangeRequested = user.getEmailChangeReq().toString();
    }

    public UserExtendedInfo(String id, VedantuRecordState recordState, String name,
                            long timeCreated, long lastUpdated, String username, String firstName, String lastName,
                            String dob, Gender gender, String thumbnail, String email, boolean isEmailVerified,
                            boolean isPhoneVerified, boolean isSysGenPassword, boolean isOTPuser,
                            String emailChangeRequested) {
        super(id, recordState, name, timeCreated, lastUpdated);
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.gender = gender;
        this.thumbnail = thumbnail;
        this.email = email;
        this.isEmailVerified = isEmailVerified;
        this.isPhoneVerified = isPhoneVerified;
        this.isSysGenPassword = isSysGenPassword;
        this.isOTPuser = isOTPuser;
        this.emailChangeRequested = emailChangeRequested;
    }

    @Override
    public String toString() {
        return "UserExtendedInfo [username=" + username + ", firstName=" + firstName
                + ", lastName=" + lastName + ", dob=" + dob + ", gender=" + gender + ", thumbnail="
                + thumbnail + ", email=" + email + ", isEmailVerified=" + isEmailVerified
                + ", isPhoneVerified=" + isPhoneVerified + ", isSysGenPassword=" + isSysGenPassword
                + ", isOTPuser=" + isOTPuser + ", isEmailSubscribed=" + isEmailSubscribed
                + ", emailChangeRequested=" + emailChangeRequested + "]";
    }



}
