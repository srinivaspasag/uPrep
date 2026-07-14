package com.lms.pojos.responce;

import com.lms.common.exception.VedantuException;
import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.enums.ModuleRun;
import com.lms.models.AbstractModuleModel;
import com.lms.models.ModuleEntry;
import com.lms.pojos.search.details.AbstractBoardSearchEntityTagDetails;
import common.utils.JSONUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import java.util.List;

public class AbstractModuleModelSearchDetails extends AbstractBoardSearchEntityTagDetails {

    public static final transient String FIELD_MODULE_RUN = "moduleRun";
    public List<ModuleEntry> children;
    public ModuleRun moduleRun;
    public boolean published;

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

        return !StringUtils.isEmpty(name);
    }

    @Override
    public SrcEntity __getSrcEntity() {
        return null;
    }

    @Override
    public NewsActivity toNewsActivity() throws VedantuException {
        return null;
    }
}
