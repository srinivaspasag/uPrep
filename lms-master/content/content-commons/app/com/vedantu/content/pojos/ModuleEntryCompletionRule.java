package com.vedantu.content.pojos;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.events.apis.JSONAware;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.content.enums.ModuleEntryCompletionRuleType;

public class ModuleEntryCompletionRule implements JSONAware {

    public ModuleEntryCompletionRuleType type;

    @Override
    public void fromJSON(JSONObject json) {

        type = ModuleEntryCompletionRuleType.valueOfKey(JSONUtils.getString(json,
                ConstantsGlobal.TYPE));

    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = new JSONObject();
        json.put(ConstantsGlobal.TYPE, type);
        return json;
    }

}
