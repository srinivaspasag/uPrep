package com.vedantu.comm.managers.news;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.vedantu.commons.pojos.SrcEntity;

public class SrcEntityDeserializer implements JsonDeserializer<SrcEntity> {
    
    private static final String KEY_COMMID = "commId";
    
    public static SrcEntityDeserializer INSTANCE = new SrcEntityDeserializer();

    @Override
    public SrcEntity deserialize(JsonElement json, Type typeOfNewsEntity,
            JsonDeserializationContext context) throws JsonParseException {
    	SrcEntity newsEntity = null;

        JsonObject jo = json.getAsJsonObject();
        if (null != jo) {
            // check if it is a comment
//            JsonElement je = jo.get(KEY_COMMID);
//            if (null != je && StringUtils.isNotEmpty(je.getAsString())) {
//                newsEntity = new Gson().fromJson(json, CommentNewsEntity.class);
//            }
        }
        // not a comment
        if (null == newsEntity) {
            newsEntity = new Gson().fromJson(json, SrcEntity.class);
        }
        return newsEntity;
    }

}
