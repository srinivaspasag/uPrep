package com.lms.pojos;

import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.event.api.JSONAware;
import com.lms.enums.ModuleEntryCompletionRuleType;
import common.utils.JSONUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;

@Getter
@Setter
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

