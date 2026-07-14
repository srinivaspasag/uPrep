/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.i18n.Messages;

/**
 *
 * @author anirban
 */
public class TemplateHelper {
    private enum DOWNLOADABLE_ENTITIES{
        CMDSDOCUMENT,CMDSFILE,CMDSVIDEO,CMDSMODULE
    }
    public static boolean isEntityDownloadable(JSONObject entity){
        boolean allow = false;
        try {
            String entityType = entity.getString("type");
            DOWNLOADABLE_ENTITIES dEntity = DOWNLOADABLE_ENTITIES.valueOf(entityType);
            //Logger.log4j.info("entity type >>>>>>>>>>>>>>>>>>> "+dEntity);
                switch(dEntity){ 
                    case CMDSVIDEO : String linkType = entity.optString("linkType", "ADDED");
                                    if("UPLOADED".equals(linkType)){
                                        allow = true;
                                    }
                        break;
                    default : allow = true;
                }
        } catch (Exception ex) {
            allow = false;
        }
        return allow;
    }
    private final static String[] SELLABLE_ENTITIES = {"SDCARDGROUP", "SECTION"};
    private final static Map<String,Boolean> SELLABLE_ENTITIES_SHIPABLE = new HashMap<String,Boolean>(){{
       put("SDCARDGROUP",true); 
    }};
    public static JSONArray _getSellableEntities() throws JSONException{
        JSONArray map = new JSONArray();
        for(int index=0;index<SELLABLE_ENTITIES.length;index++){
            JSONObject data = new JSONObject();
            String value = SELLABLE_ENTITIES[index];
            data.put("text", Messages.get("ENTITY_NAME_"+value));
            data.put("value", value);
            Boolean isShipable = SELLABLE_ENTITIES_SHIPABLE.get(value);
            isShipable = isShipable == null ? false : isShipable;
            data.put("shipable", isShipable);
            map.put(index,data);
        }
        return map;
    }
    public final static String[] SHIPMENT_STATUS = {"NOT_DISPATCHED", "DISPATCHED", "RECEIVED"};
    public static JSONArray _getTempArray() throws JSONException{
        JSONArray map = new JSONArray();
        for(int index=0;index<2;index++){
            map.put(index,"1234567890");
        }
        return map;
    }
}