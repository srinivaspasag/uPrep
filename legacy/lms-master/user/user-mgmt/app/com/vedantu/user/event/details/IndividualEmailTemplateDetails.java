package com.vedantu.user.event.details;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.content.interfaces.AbstractEmailTemplateDetails;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.user.pojos.UserEmailInfo;

public abstract class IndividualEmailTemplateDetails extends AbstractEmailTemplateDetails {

    public UserEmailInfo user;

    
    public IndividualEmailTemplateDetails(String configurationName) {

        super(configurationName);
    }

    protected IndividualEmailTemplateDetails(IndividualEmailTemplateDetails details) {

        super(details);
        this.user = details.user;

    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();
        json.put(ConstantsGlobal.USER, user.toJSON());
        return json;

    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        user = (UserEmailInfo) JSONUtils.getJSONAware(new UserEmailInfo(), json,
                ConstantsGlobal.USER);
    }

    public String getFullName() {

        return user.getFullName();
    }
}
