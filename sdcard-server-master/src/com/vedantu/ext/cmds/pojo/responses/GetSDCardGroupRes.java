package com.vedantu.ext.cmds.pojo.responses;

import org.json.JSONArray;
import org.json.JSONObject;

import com.vedantu.ext.cmds.db.models.SDCardGroup;
import com.vedantu.ext.cmds.pojo.SrcEntity;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;
import com.vedantu.ext.cmds.web.JSONAware;
import com.vedantu.ext.cmds.web.JSONUtils;

public class GetSDCardGroupRes implements JSONAware {

    public String    name;
    public String    id;
    public SrcEntity target;
    public long      size;       // in bytes
    public long      cardSize;   // in bytes
    public int       noOfCards;
    public JSONArray cardIds;
    public long      timeCreated;

    @Override
    public void fromJSON(JSONObject json) {

        json = JSONUtils.getJSONObject(json, "recordInfo");
        name = JSONUtils.getString(json, ConstantGlobal.NAME);
        id = JSONUtils.getString(json, ConstantGlobal.ID);
        JSONObject targetJSON = JSONUtils.getJSONObject(json, "target");
        target = new SrcEntity();
        target.fromJSON(targetJSON);
        size = JSONUtils.getLong(json, ConstantGlobal.SIZE);
        cardSize = JSONUtils.getLong(json, SDCardGroup.FIELD_CARD_SIZE);
        cardIds = JSONUtils.getJSONArray(json, "cards");
        noOfCards = cardIds.length();
        timeCreated = JSONUtils.getLong(json, ConstantGlobal.TIME_CREATED);
    }

    @Override
    public JSONObject toJSON() {

        JSONObject json = new JSONObject();
        json.put(ConstantGlobal.NAME, name);
        json.put(ConstantGlobal.ID, id);
        if (target != null) {
            json.put("target", target.toJSON());
        }
        json.put(ConstantGlobal.SIZE, size);
        json.put(SDCardGroup.FIELD_CARD_SIZE, cardSize);
        json.put(SDCardGroup.FIELD_CARD_IDS, cardIds);
        json.put(SDCardGroup.FIELD_NO_OF_CARDS, noOfCards);
        json.put(ConstantGlobal.TIME_CREATED, timeCreated);
        return json;
    }

}
