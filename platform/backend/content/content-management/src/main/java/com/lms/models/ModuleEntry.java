package com.lms.models;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.event.api.JSONAware;
import com.lms.pojos.ModuleEntryCompletionRule;
import common.utils.JSONUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;

@Getter
@Setter
public class ModuleEntry implements JSONAware {

    public SrcEntity entity;
    public String name;
    public ModuleEntryCompletionRule completionRule;

    public ModuleEntry() {

    }

    public ModuleEntry(SrcEntity entity, String name,
                       ModuleEntryCompletionRule moduleEntryCompletionRule) {

        this.entity = entity;
        this.name = name;
        this.completionRule = moduleEntryCompletionRule;
    }

    @Override
    public boolean equals(Object o) {

        if (null == o || !(o instanceof ModuleEntry)) {
            return false;
        }
        ModuleEntry e = (ModuleEntry) o;
        return (entity != null && entity.equals(e.entity));
    }

    @Override
    public int hashCode() {

        return entity == null ? 0 : entity.hashCode();
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = new JSONObject();
        json.put(ConstantsGlobal.NAME, name);
        if (entity != null) {
            json.put(ConstantsGlobal.ENTITY, entity.toJSON());
        }
        if (completionRule != null) {
            json.put(ConstantsGlobal.COMPLETION_RULE, completionRule.toJSON());
        }
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        name = JSONUtils.getString(json, ConstantsGlobal.NAME);
        JSONObject entityJSON = JSONUtils.getJSONObject(json, ConstantsGlobal.ENTITY);
        if (entityJSON != null && entityJSON.length() > 0) {
            entity = new SrcEntity();
            entity.fromJSON(entityJSON);
        }
        JSONObject completionRuleJSON = JSONUtils.getJSONObject(json,
                ConstantsGlobal.COMPLETION_RULE);
        if (completionRuleJSON != null && completionRuleJSON.length() > 0) {
            completionRule = new ModuleEntryCompletionRule();
            completionRule.fromJSON(completionRuleJSON);
        }

    }
}