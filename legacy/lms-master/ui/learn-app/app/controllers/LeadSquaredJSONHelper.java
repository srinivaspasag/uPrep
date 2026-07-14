package controllers;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LeadSquaredJSONHelper {
    private JSONArray leadAttributes = null;

    public LeadSquaredJSONHelper() {
        leadAttributes = new JSONArray();
    }

    public void addLeadAttribute(String attributeName, String valueOfAttribute) throws JSONException {
        JSONObject field = new JSONObject();
        field.put("Attribute", attributeName);
        field.put("Value", valueOfAttribute);
        leadAttributes.put(field);
    }

    public String getJSONString() {
        if (leadAttributes == null)
            return "{}";
        else
            return leadAttributes.toString();
    }
}