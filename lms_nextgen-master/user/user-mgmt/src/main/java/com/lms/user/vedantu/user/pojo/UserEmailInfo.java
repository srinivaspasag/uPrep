package com.lms.user.vedantu.user.pojo;

import com.lms.user.vedantu.user.enums.Gender;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.constants.HardCodedConstants;
import com.lms.common.vedantu.enums.MailCategory;
import com.lms.common.vedantu.event.api.JSONAware;
import com.lms.user.vedantu.user.model.User;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Setter
@Getter
public class UserEmailInfo extends ModelBasicInfo implements JSONAware {
    private static final Logger logger = LoggerFactory.getLogger(UserEmailInfo.class);

    public static final String GENERATED_CODE    = "generatedCode";
    public static final String IS_EMAIL_VERIFIED = "isEmailVerified";
    public static final String GENDER            = "gender";


    public String              firstName;
    public String              lastName;
    public String              email;
    public Gender gender;
    public String              thumbnail;
    public boolean             isEmailVerified;
    private String             generatedCode     = null;
    public MailCategory category;

    public UserEmailInfo() {

    }
    public UserEmailInfo(String firstName, String lastName, String email) {

        super();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
    public void fromUserExtendedInfo(User extendedInfo) {

        this.id = extendedInfo.getId().toString();
        this.recordState = extendedInfo.recordState;
        this.firstName = extendedInfo.firstName;
        this.lastName = extendedInfo.lastName;
        this.gender = extendedInfo.gender;
        this.thumbnail = extendedInfo.thumbnail;
        this.email = extendedInfo.email;
        this.isEmailVerified = extendedInfo.isEmailVerified;

    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{firstName:").append(firstName).append(", lastName:").append(lastName)
                .append(", gender:").append(gender).append(", thumbnail:").append(thumbnail)
                .append(", email:").append(email).append(", isEmailVerified:")
                .append(isEmailVerified).append(", id:").append(id).append(", recordState:")
                .append(recordState).append("}");
        return builder.toString();
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = new JSONObject();
        json.put(ConstantsGlobal.FIRST_NAME, firstName);
        json.put(ConstantsGlobal.LAST_NAME, lastName);
        json.put(GENDER, gender);
        json.put(ConstantsGlobal.THUMBNAIL, thumbnail);
        json.put(ConstantsGlobal.EMAIL, email);
        json.put(IS_EMAIL_VERIFIED, isEmailVerified);
        json.put(ConstantsGlobal.ID, id);
        json.put(ConstantsGlobal.RECORD_STATE, recordState);
        json.put(GENERATED_CODE, generatedCode);
        return json;

    }

    @Override
    public void fromJSON(JSONObject json) {
//TODO need to implement

    }

    public String getFullName() {
        if(firstName.isEmpty()) {
            return HardCodedConstants.emptyString;
        }
        return this.firstName
                + (!(this.lastName.isEmpty() ) ? " " + this.lastName : HardCodedConstants.emptyString);
    }

    public String getGeneratedCode() {

        return generatedCode;
    }

    public void setGeneratedCode(String generatedCode) {

        this.generatedCode = generatedCode;
    }

    public boolean hasGeneratedCode() {

        logger.debug(" checking if value is not empty Generated code is " + generatedCode
                + !generatedCode.isEmpty() + generatedCode.toLowerCase());
        return !generatedCode.isEmpty();
    }

    public boolean hasNoGeneratedCode() {

        logger.debug(" Generated code is " + generatedCode + generatedCode.isEmpty());
        return generatedCode.isEmpty();
    }

    public MailCategory getCategory() {

        return category;
    }

    public void setCategory(MailCategory category) throws VedantuException {

       // generatedCode = UserManager.getUnsubscribeAccessCode(this.id, category);
        this.category = category;
    }

}