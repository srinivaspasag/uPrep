package com.vedantu.organization.event.details;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.constants.configuration.EmailConfigurationConstants;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.organization.models.Organization;
import com.vedantu.organization.pojos.responses.organizations.OrgInfo;
import com.vedantu.user.event.details.IndividualEmailTemplateDetails;

public class AddOrganizationEmailDetails extends IndividualEmailTemplateDetails {

    public String       orgId;
    public Organization organization;
    public OrgInfo      orgInfo;

    public AddOrganizationEmailDetails() throws ClassNotFoundException {

        super(EmailConfigurationConstants.TEMPLATE_ADD_ORGANIZATION);

    }

    @Override
    public String getSubject() {

        return "Request for organization creation on Learnpedia";
    }

    @Override
    public boolean verify() {

        return true;
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

        json.put("orgId", orgId);
        return json;

    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        orgId = JSONUtils.getString(json, "orgId");

        if (StringUtils.isNotEmpty(orgId)) {
            organization = OrganizationDAO.INSTANCE.getById(orgId);
            orgInfo = (OrgInfo) organization.toExtendedInfo();
        }

    }

}
