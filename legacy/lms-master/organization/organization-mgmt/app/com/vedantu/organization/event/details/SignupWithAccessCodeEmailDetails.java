package com.vedantu.organization.event.details;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.constants.configuration.EmailConfigurationConstants;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;

public class SignupWithAccessCodeEmailDetails extends OrgSpecificEmailDetails {

    public static final String PASSWORD = "password";
    public static final String USERNAME = "userName";

    public String              password;
    public String              userName;


    public SignupWithAccessCodeEmailDetails() throws ClassNotFoundException {

        super(EmailConfigurationConstants.TEMPLATE_SIGNUP_WITH_ACCESS_CODE);

    }

    @Override
    public String getSubject() {

        return "Welcome to " + orgInfo.name + " " + getFullName();
    }

    @Override
    public boolean verify() {
 
        if (StringUtils.isNotEmpty(user.firstName) && StringUtils.isNotEmpty(password)
                && StringUtils.isNotEmpty(password)) {
            return true;
        }
        return false;
    }

    @Override
    public SrcEntity __getSrcEntity() {

        return null;
    }

    @Override
    public NewsActivity toNewsActivity() {

        return null;
    }

    @Override
    public boolean enableNotifcation(boolean value) {

        return false;
    }

    @Override
    public boolean getNotificationEnabled() {

        return false;
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();

        json.put(USERNAME, userName);
        json.put(PASSWORD, password);

        return json;

    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);

        userName = JSONUtils.getString(json, USERNAME);
        password = JSONUtils.getString(json, PASSWORD);
    }

}
