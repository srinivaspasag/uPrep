package com.vedantu.ext.cmds.pojo.responses;

import java.util.List;

import org.json.JSONObject;

import com.vedantu.ext.cmds.web.JSONAware;
import com.vedantu.ext.cmds.web.JSONUtils;

public class GetSDCardGroupInfoRes implements JSONAware {

    private List<GetSDCardInfoRes> cards;

    @SuppressWarnings("unchecked")
    @Override
    public void fromJSON(JSONObject json) {

        json = JSONUtils.getJSONObject(json, "recordInfo");
        cards = (List<GetSDCardInfoRes>) JSONUtils.getJSONAwareCollection(GetSDCardInfoRes.class,
                json, "cardInfos");

    }

    @Override
    public JSONObject toJSON() {

        return new JSONObject(this);
    }

    public List<GetSDCardInfoRes> getCards() {

        return cards;
    }

    public void setCards(List<GetSDCardInfoRes> cards) {

        this.cards = cards;
    }

}
