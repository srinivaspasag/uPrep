package com.vedantu.user.models;

import java.util.Collections;

import org.apache.commons.lang3.StringUtils;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.annotations.Transient;
import com.vedantu.commons.enums.AuthType;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SecurityCredentials;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.pojos.responses.ModelExtendedInfo;
import com.vedantu.commons.utils.ImageDisplayURLUtil;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.user.enums.Gender;
import com.vedantu.user.pojos.EmailChangeReqInfo;
import com.vedantu.user.pojos.ForgotPasswordReqInfo;
import com.vedantu.user.pojos.SocialInfo;
import com.vedantu.user.pojos.TnCAcceptance;
import com.vedantu.user.pojos.UserExtendedInfo;
import com.vedantu.user.pojos.UserInfo;

@Entity(value = "users", noClassnameStored = true)
public class User extends VedantuBaseMongoModel {

    @Transient
    public static final String   FIELD_DOB         = "dob";
    @Transient
    public static final String   FIELD_CREDENTIALS = "credentials";

    @Indexed(unique = true)
    public String                username;
    public String                password;
    public String                firstName;
    public String                lastName;
    public String                dob;
    public Gender                gender;
    public String                thumbnail;
    public String                email;

    public boolean               isEmailVerified   = false;
    public boolean               isSysGenPassword  = false;
    public boolean               isPhoneVerified   = false;
    public boolean               isOTPuser         = false;

    public EmailChangeReqInfo    emailChangeReq;
    public ForgotPasswordReqInfo forgotPasswordReq;
    public TnCAcceptance         tncAcceptance;
    public SecurityCredentials   credentials;
    public SocialInfo            socialInfo;
    public AuthType              authType;

    public User() {

        super();

    }

    @Override
    public ModelBasicInfo toBasicInfo() {

        return new UserInfo(this._getStringId(), firstName, lastName, _getThumbnailUrl(),
                recordState);
    }

    @Override
    public ModelExtendedInfo toExtendedInfo() {

        return new UserExtendedInfo(_getStringId(), recordState, firstName, timeCreated,
                lastUpdated, username, firstName, lastName, dob, gender, _getThumbnailUrl(), email,
                isEmailVerified,isPhoneVerified,isSysGenPassword,isOTPuser, null != emailChangeReq ? emailChangeReq.email : StringUtils.EMPTY);
    }

    public String _getThumbnailUrl() {

        Gender tGender = (null != gender && Gender.UNKNOWN != gender) ? gender : Gender.UNKNOWN;
        return StringUtils.isNotEmpty(thumbnail) ? ImageDisplayURLUtil.getEntityThumbnail(
                EntityType.USER, thumbnail) : ImageDisplayURLUtil.getEntityStaticThumbnail(
                EntityType.USER, Collections.singletonList(tGender.name()));
    }

    @Override
    public String toString() {

        return "User [username=" +
                username +
                ", firstName=" +
                firstName +
                ", lastName=" +
                lastName +
                ", email=" +
                email +
                ", isEmailVerified=" +
                isEmailVerified +
                ", isPhoneVerified=" +
                isPhoneVerified +
                "]";
    }

    public String _getFullName() {

        return this.firstName
                + (StringUtils.isNotEmpty(this.lastName) ? " " + this.lastName : StringUtils.EMPTY);
    }

    /**
     *
     * @return if user has specified her email as someemail+123@somethings.com then
     *         someemail+13@somethings.com will be returned, default value will be null
     */
    public String _getCommunicationEmail() {

        if (StringUtils.isNotEmpty(email) && email.contains("+")) {
            return StringUtils.substringBefore(email, "+") + "@"
                    + StringUtils.substringAfter(email, "@");
        }
        return email;
    }

}
