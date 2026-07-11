package com.lms.web;

import com.lms.interfaces.IDataCollector;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public abstract class AbstractVideoDataCollector implements IDataCollector {
    private static final Logger logger = LoggerFactory.getLogger(AbstractVideoDataCollector.class);

    @Override
    public VideoInfo getData(String url) {
        return null;
    }

    public VideoInfo getVideoInfo(Map<String, String> openGraphKeysMap) {
        return null;
    }

    public abstract String getVideoId(String url);

    public abstract String getJSONAPIUrl(String videoId, String partType);

    public abstract VideoInfo getVideoInfo(JSONObject json);

    public abstract String getJSONAPIUrl(String videoId);


}
