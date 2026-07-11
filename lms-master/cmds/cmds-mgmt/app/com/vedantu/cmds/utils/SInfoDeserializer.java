package com.vedantu.cmds.utils;

import java.lang.reflect.Type;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.vedantu.cmds.pojos.content.solution.metadata.SolutionInfo;

public class SInfoDeserializer implements JsonDeserializer<SolutionInfo> {
	final static private ALogger LOGGER  = Logger.of(SInfoDeserializer.class);
    @SuppressWarnings("unchecked")
	@Override
    public SolutionInfo deserialize(JsonElement jsonElement, Type typeOfIInfo,
            JsonDeserializationContext context) throws JsonParseException {
        SolutionInfo info = null;
        JsonObject jsonObject = jsonElement.getAsJsonObject();
       LOGGER.info("JSON object in des" + jsonObject);
        if (jsonObject != null) {
            JsonElement element = jsonObject.get("gsonClassName");
           LOGGER.info("json element [classname] in des " + element);
            if (element != null
                    && StringUtils.isNotEmpty(element.getAsString())) {
                String className = element.getAsString();
                Class<SolutionInfo> clazz = null;
                try {
                    if (className == null) {
                       LOGGER.error("Classname is null");
                        return null;
                    }
                    clazz = (Class<SolutionInfo>) Class.forName(className);
                   LOGGER.info("obj class is : " + clazz);
                    info = new Gson().fromJson(jsonElement, clazz);
                } catch (ClassNotFoundException e) {
                   LOGGER.error("error", e);
                }
            } else {
               LOGGER.error("JSON element is null or empty");
            }
        } else {
           LOGGER.error("JSON object is null");
        }
        return info;
    }

}
