package com.lms.user.vedantu.user.model;

import com.lms.common.utils.ImageDisplayURLUtil;
import com.lms.common.vedantu.commons.pojos.requests.SecurityCredentials;
import com.lms.common.vedantu.constants.HardCodedConstants;
import com.lms.common.vedantu.enums.AuthType;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.user.vedantu.user.enums.Gender;
import com.lms.user.vedantu.user.pojo.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collections;

@Document(collection = "users")
@Setter
@Getter
public class User extends VedantuBaseMongoModel {
    @Transient
    public static final String FIELD_DOB = "dob";
    @Transient
    public static final String FIELD_CREDENTIALS = "credentials";

    @Transient
    public static final String SEQUENCE_NAME = "user_sequence";

    @Indexed(unique = true)
    public String username;
    public String password;
    public String                firstName;
    public String                lastName;
    public String                dob;
    public String                thumbnail;
    public String                email;
    public Gender                gender;


    public boolean               isEmailVerified   = false;
    public boolean               isSysGenPassword  = false;
    public boolean               isPhoneVerified   = false;
    public boolean               isOTPuser         = false;



    public EmailChangeReqInfo emailChangeReq;
    public ForgotPasswordReqInfo forgotPasswordReq;
    public TnCAcceptance tncAcceptance;
    public SecurityCredentials credentials;
    public SocialInfo socialInfo;
    public AuthType authType;


    public User() {

        super();

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
    @Override
    public UserExtendedInfo toExtendedInfo() {

        return new UserExtendedInfo(_getStringId(), recordState, firstName, timeCreated,
                lastUpdated, username, firstName, lastName, dob, gender, _getThumbnailUrl(), email,
                isEmailVerified,isPhoneVerified,isSysGenPassword,isOTPuser, null != emailChangeReq ? emailChangeReq.email : HardCodedConstants.emptyString);
    }

    public String _getThumbnailUrl() {

        Gender tGender = (null != gender && Gender.UNKNOWN != gender) ? gender : Gender.UNKNOWN;
        if(thumbnail!=null){
            return ImageDisplayURLUtil.getEntityThumbnail(
                    EntityType.USER, thumbnail);
        }
        else{

            return  ImageDisplayURLUtil.getEntityStaticThumbnail(EntityType.USER, Collections.singletonList(tGender.name()));
        }
       /* return !(thumbnail).isEmpty() ? ImageDisplayURLUtil.getEntityThumbnail(
                EntityType.USER, thumbnail) : ImageDisplayURLUtil.getEntityStaticThumbnail(
                EntityType.USER, Collections.singletonList(tGender.name()));*/
    }
    public String _getCommunicationEmail() {

        if (!email.isEmpty() && email.contains("+")) {
            int before=email.indexOf("+");
            int after=email.indexOf("@");
            return email.substring(0,before)+"@"+email.substring(after);

        }
        return email;
    }
    public String _getFullName() {

        return this.firstName
                + (this.lastName!=null ? " " + this.lastName : HardCodedConstants.emptyString);
    }




}
