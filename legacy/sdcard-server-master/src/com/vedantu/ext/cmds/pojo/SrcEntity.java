package com.vedantu.ext.cmds.pojo;

import org.json.JSONObject;

import com.vedantu.ext.cmds.enums.EntityType;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;
import com.vedantu.ext.cmds.web.JSONAware;
import com.vedantu.ext.cmds.web.JSONUtils;

public class SrcEntity implements JSONAware {

    public String     id;
    public EntityType type;

    public SrcEntity() {

        super();
    }

    public SrcEntity(String id, EntityType type) {

        super();
        this.id = id;
        this.type = type;
    }

    /**
     * NOTE: these getMethods are used for JONSOBject generation
     * 
     * @return
     */
    public String getId() {

        return id;
    }

    public String getType() {

        return type != null ? type.name() : EntityType.UNKNOWN.name();
    }

    @Override
    public void fromJSON(JSONObject json) {

        id = JSONUtils.getString(json, ConstantGlobal.ID);
        type = EntityType.valueOfKey(JSONUtils.getString(json, ConstantGlobal.TYPE));
    }

    @Override
    public JSONObject toJSON() {

        JSONObject json = new JSONObject();
        json.put(ConstantGlobal.ID, id);
        json.put(ConstantGlobal.TYPE, type != null ? type.name() : EntityType.UNKNOWN.name());
        return json;
    }

}
