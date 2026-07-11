package com.lms.web;

import com.lms.pojos.LinkInfo;
import org.json.JSONObject;

public class OpenGraphVideoDataCollector extends AbstractVideoDataCollector {

    @Override
    public VideoInfo getVideoInfo(JSONObject json) {
        return null;
    }

    @Override
    public String getVideoId(String url) {
        return null;
    }

    @Override
    public String getJSONAPIUrl(String videoId, String partType) {
        return null;
    }

    public boolean isEmbeddable(String url) {
        return false;
    }

    @Override
    public String getJSONAPIUrl(String videoId) {
        return null;
    }

    @Override
    public String formURL(LinkInfo info) {

        // TODO Auto-generated method stub
        return null;
    }

}
