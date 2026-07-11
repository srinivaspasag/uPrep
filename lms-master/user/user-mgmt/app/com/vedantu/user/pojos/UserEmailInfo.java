package com.vedantu.user.pojos;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;

import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.MailCategory;
import com.vedantu.commons.events.apis.JSONAware;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.user.enums.Gender;
import com.vedantu.user.managers.UserManager;

public class UserEmailInfo extends ModelBasicInfo implements JSONAware {

    public static final String GENERATED_CODE    = "generatedCode";
    public static final String IS_EMAIL_VERIFIED = "isEmailVerified";
    public static final String GENDER            = "gender";
 

    public String              firstName;
    public String              lastName;
    public String              email;
    public Gender              gender;
    public String              thumbnail;
    public boolean             isEmailVerified;
    private String             generatedCode     = null;
    public MailCategory        category;

    public UserEmailInfo() {

    }
    public UserEmailInfo(String firstName, String lastName, String email) {

        super();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
    public void fromUserExtendedInfo(UserExtendedInfo extendedInfo) {

        this.id = extendedInfo.id;
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

        firstName = JSONUtils.getString(json, ConstantsGlobal.FIRST_NAME);
        lastName = JSONUtils.getString(json, ConstantsGlobal.LAST_NAME);
        gender = Gender.valueOfKey(JSONUtils.getString(json, GENDER));
        thumbnail = JSONUtils.getString(json, ConstantsGlobal.THUMBNAIL);
        email = JSONUtils.getString(json, ConstantsGlobal.EMAIL);
        isEmailVerified = JSONUtils.getBoolean(json, IS_EMAIL_VERIFIED);
        id = JSONUtils.getString(json, ConstantsGlobal.ID);
        generatedCode = JSONUtils.getString(json, GENERATED_CODE);

    }

    public String getFullName() {
        if(StringUtils.isEmpty(firstName)) {
        	return StringUtils.EMPTY;
        }
        return this.firstName
                + (StringUtils.isNotEmpty(this.lastName) ? " " + this.lastName : StringUtils.EMPTY);
    }

    public String getGeneratedCode() {

        return generatedCode;
    }

    public void setGeneratedCode(String generatedCode) {

        this.generatedCode = generatedCode;
    }

    public boolean hasGeneratedCode() {

        Logger.debug(" checking if value is not empty Generated code is " + generatedCode
                + StringUtils.isNotEmpty(generatedCode) + generatedCode.toLowerCase());
        return StringUtils.isNotEmpty(generatedCode);
    }

    public boolean hasNoGeneratedCode() {

        Logger.debug(" Generated code is " + generatedCode + StringUtils.isEmpty(generatedCode));
        return StringUtils.isEmpty(generatedCode);
    }

    public MailCategory getCategory() {

        return category;
    }

    public void setCategory(MailCategory category) throws VedantuException {

        generatedCode = UserManager.getUnsubscribeAccessCode(this.id, category);
        this.category = category;
    }

}
