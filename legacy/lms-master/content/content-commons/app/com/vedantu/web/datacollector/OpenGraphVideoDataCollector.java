package com.vedantu.web.datacollector;

import org.json.JSONObject;

import com.vedantu.content.pojos.LinkInfo;

public class OpenGraphVideoDataCollector extends AbstractVideoDataCollector {

	@Override
	public VideoInfo getVideoInfo(JSONObject json) {
		return null;
	}

	@Override
	public String getVideoId(String url) {
		return null;
	}

	public boolean isEmbeddable(String url){
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
