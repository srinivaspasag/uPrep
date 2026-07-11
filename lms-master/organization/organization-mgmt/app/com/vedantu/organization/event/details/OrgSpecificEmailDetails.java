package com.vedantu.organization.event.details;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.organization.models.Organization;
import com.vedantu.organization.pojos.responses.organizations.OrgInfo;
import com.vedantu.user.event.details.IndividualEmailTemplateDetails;
import com.vedantu.user.pojos.UserEmailInfo;

public abstract class OrgSpecificEmailDetails extends IndividualEmailTemplateDetails {

    public String       orgId;

    public OrgInfo      orgInfo;
    public Organization organization;

    public OrgSpecificEmailDetails(String configurationName) {

        super(configurationName);
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();
        json.put(ConstantsGlobal.USER, user.toJSON());
        json.put(ConstantsGlobal.ORG_ID, orgId);
        return json;

    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        user = (UserEmailInfo) JSONUtils.getJSONAware(new UserEmailInfo(), json,
                ConstantsGlobal.USER);
        orgId = JSONUtils.getString(json, ConstantsGlobal.ORG_ID);
        if (StringUtils.isNotEmpty(orgId)) {
            organization = OrganizationDAO.INSTANCE.getById(orgId);
            orgInfo = (OrgInfo) organization.toExtendedInfo();
            
            if( StringUtils.isNotEmpty(orgInfo.referer)){
                this.appProtocol="http";
            }
            this.appDomain=orgInfo.orgURL;
           
        }

    }

}
