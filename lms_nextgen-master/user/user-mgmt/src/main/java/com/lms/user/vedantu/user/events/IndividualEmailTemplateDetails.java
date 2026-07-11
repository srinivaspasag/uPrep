package com.lms.user.vedantu.user.events;

import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.content.AbstractEmailTemplateDetails;
import com.lms.user.vedantu.user.pojo.UserEmailInfo;
import common.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

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
