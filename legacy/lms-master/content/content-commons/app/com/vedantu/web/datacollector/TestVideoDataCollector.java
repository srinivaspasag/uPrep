package com.vedantu.web.datacollector;

import org.json.JSONObject;

import com.vedantu.content.pojos.LinkInfo;

public class TestVideoDataCollector extends AbstractVideoDataCollector {

    @Override
    public String getVideoId(String url) {
        return null;
    }

    @Override
    public String getJSONAPIUrl(String videoId) {
        return null;
    }

    public boolean isEmbeddable(String url){
        return false;
    }

    public static void main(String[] args) {
        TestVideoDataCollector t = new TestVideoDataCollector();
        System.out
                .println(t
                        .getData("http://www.youtube.com/watch?v=gWNRUVMboq4&feature=related"));
    }

    @Override
    public VideoInfo getVideoInfo(JSONObject json) {
        return null;
    }

    @Override
    public String formURL(LinkInfo info) {

        // TODO Auto-generated method stub
        return null;
    }
}
