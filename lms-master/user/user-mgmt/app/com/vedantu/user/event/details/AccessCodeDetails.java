package com.vedantu.user.event.details;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.constants.configuration.EmailConfigurationConstants;
import com.vedantu.commons.content.interfaces.AbstractEmailTemplateDetails;
import com.vedantu.commons.utils.JSONUtils;

public class AccessCodeDetails extends AbstractEmailTemplateDetails {

	public String code;
	public String name;
	public String email;
	public String pointOfSale;
	public String sellerReferenceNo;
	public String program;

	protected AccessCodeDetails(IndividualEmailTemplateDetails details) {
		super(details);
	}

	public AccessCodeDetails() throws ClassNotFoundException {
		super(EmailConfigurationConstants.TEMPLATE_EMAIL_ACCESS_CODE );
	}

	public String getSubject() {
		return "Email for Access Code";
	}

	@Override
	public boolean verify() {

		if (StringUtils.isNotEmpty(email) && StringUtils.isNotEmpty(code)){
			return true;
		}
		return false;
	}

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();

        json.put(ConstantsGlobal.CODE, code);
        json.put(ConstantsGlobal.NAME, name);
        json.put(ConstantsGlobal.EMAIL, email);
        json.put(ConstantsGlobal.SELLER_REF_NO, sellerReferenceNo);
        json.put(ConstantsGlobal.POINT_OF_SALE, pointOfSale);
        json.put(ConstantsGlobal.PROGRAM, program);
        return json;

    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);

        code = JSONUtils.getString(json, ConstantsGlobal.CODE);
        name = JSONUtils.getString(json, ConstantsGlobal.NAME);
        email = JSONUtils.getString(json, ConstantsGlobal.EMAIL);
        sellerReferenceNo = JSONUtils.getString(json, ConstantsGlobal.SELLER_REF_NO);
        pointOfSale = JSONUtils.getString(json, ConstantsGlobal.POINT_OF_SALE);
        program = JSONUtils.getString(json, ConstantsGlobal.PROGRAM);
    }


}
