package com.vedantu.comm.managers.news;

import java.lang.reflect.Type;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.vedantu.commons.news.AbstractInfo;

public class IInfoDeserializer implements JsonDeserializer<AbstractInfo> {
	private final static ALogger LOGGER= Logger.of(IInfoDeserializer.class); 
    @SuppressWarnings("unchecked")
	@Override
    public AbstractInfo deserialize(JsonElement json, Type typeOfIInfo,
            JsonDeserializationContext context) throws JsonParseException {
        AbstractInfo info = null;
        JsonObject jo = json.getAsJsonObject();
        if (null != jo) {
            JsonElement je = jo.get("className");
            if (null != je && StringUtils.isNotEmpty(je.getAsString())) {
                String className = je.getAsString();
                Class<? extends AbstractInfo> clazz;
                try {
                    clazz = (Class<? extends AbstractInfo>) Class.forName(className);
                    
                    info = new Gson().fromJson(json, clazz);
                } catch (ClassNotFoundException e) {
                    LOGGER.error(e.getMessage(), e);
                	Logger.error(e.getMessage(), e);
                }
                
            } else {
                LOGGER.error("No className  is found in json "+ jo);
                Logger.error("No className  is found in json "+ jo);
                   
            }
        } else {
            LOGGER.error("Null json can not be parsed ");
            Logger.error("Null json can not be parsed ");
            
     
        }
        return info;
    }

}