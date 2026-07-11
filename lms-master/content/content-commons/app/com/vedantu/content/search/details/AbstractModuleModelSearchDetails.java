package com.vedantu.content.search.details;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.content.models.AbstractModuleModel;
import com.vedantu.content.models.ModuleEntry;
import com.vedantu.content.models.ModuleRun;
import com.vedantu.mongo.VedantuBaseMongoModel;

public abstract class AbstractModuleModelSearchDetails extends AbstractBoardSearchEntityTagDetails {

    public static final transient String FIELD_MODULE_RUN = "moduleRun";
    public List<ModuleEntry>             children;
    public ModuleRun                     moduleRun;
    public boolean                       published;

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();
        JSONArray childrenArray = new JSONArray();
        if (children != null) {
            for (ModuleEntry entry : children) {
                childrenArray.put(entry.toJSON());
            }
        }
        json.put(ConstantsGlobal.CHILDREN, childrenArray);
        if (moduleRun != null) {
            json.put(FIELD_MODULE_RUN, moduleRun.name());
        }
        json.put(ConstantsGlobal.PUBLISHED, published);
        return json;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        children = (List<ModuleEntry>) JSONUtils.getJSONAwareCollection(ModuleEntry.class, json,
                ConstantsGlobal.CHILDREN);
        moduleRun = ModuleRun.valueOfKey(JSONUtils.getString(json, FIELD_MODULE_RUN));
        published = JSONUtils.getBoolean(json, ConstantsGlobal.PUBLISHED);

    }

    @Override
    public void fromMongoModel(VedantuBaseMongoModel mongoModel) {

        super.fromMongoModel(mongoModel);
        AbstractModuleModel moduleModel = (AbstractModuleModel) mongoModel;
        children = moduleModel.children;
        moduleRun = moduleModel.moduleRun;
        published = moduleModel.published;

    }

    @Override
    public boolean _isIndexable() {

        return StringUtils.isNotEmpty(name);
    }

}
