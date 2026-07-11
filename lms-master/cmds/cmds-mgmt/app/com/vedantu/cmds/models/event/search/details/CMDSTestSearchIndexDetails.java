package com.vedantu.cmds.models.event.search.details;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.cmds.models.CMDSTest;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.content.search.details.AbstractTestCommonSearchDetails;
import com.vedantu.mongo.VedantuBaseMongoModel;

public class CMDSTestSearchIndexDetails extends AbstractTestCommonSearchDetails {

	public String globalTestId;

	@Override
	public void fromMongoModel(VedantuBaseMongoModel mongoModel) {
		super.fromMongoModel(mongoModel);
		CMDSTest test = (CMDSTest) mongoModel;
		globalTestId = test.globalTestId;
	}

	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		json.put(ConstantsGlobal.GLOBAL_TEST_ID, globalTestId);
		return json;
	}

	@Override
	public void fromJSON(JSONObject json) {
		super.fromJSON(json);
		globalTestId = JSONUtils
				.getString(json, ConstantsGlobal.GLOBAL_TEST_ID);
	}

	@Override
	public SrcEntity __getSrcEntity() {
		return new SrcEntity(EntityType.CMDSTEST, id);
	}

	@Override
	public NewsActivity toNewsActivity() {
		return null;
	}

}
