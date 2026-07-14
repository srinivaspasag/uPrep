package com.vedantu.ext.cmds.pojo.responses;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.vedantu.ext.cmds.utils.web.WebCommunicator;
import com.vedantu.ext.cmds.web.JSONAware;
import com.vedantu.ext.cmds.web.JSONUtils;

public class ListResponse<T extends JSONAware> implements JSONAware {

    public int       totalHits;
    public List<T>   list;

    private Class<T> genericType;

    public ListResponse(Class<T> genericType) {

        this.genericType = genericType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void fromJSON(JSONObject json) {

        totalHits = JSONUtils.getInt(json, "totalHits");
        list = (List<T>) JSONUtils.getJSONAwareCollection(genericType, json,
                WebCommunicator.KEY_LIST);
    }

    @Override
    public JSONObject toJSON() {

        JSONObject json = new JSONObject();
        json.put("totalHits", totalHits);
        JSONArray jsonList = new JSONArray();
        for (T t : list) {
            jsonList.put(t.toJSON());
        }
        json.put(WebCommunicator.KEY_LIST, jsonList);
        return json;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{totalHits:").append(totalHits).append(", list:").append(list).append("}");
        return builder.toString();
    }

}
