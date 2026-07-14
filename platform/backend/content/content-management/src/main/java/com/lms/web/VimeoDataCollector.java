package com.lms.web;

import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.pojos.LinkInfo;
import com.lms.web.util.WebUtils;
import common.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

public class VimeoDataCollector extends AbstractVideoDataCollector {

    private static final Logger logger = LoggerFactory.getLogger(VimeoDataCollector.class);

    @Override
    public VideoInfo getData(String url) {
        String data = WebUtils.getStringData(getJSONAPIUrl(getVideoId(url)));
        logger.debug("VIMEO Received data is " + data);
//		data = StringUtils.substringBeforeLast(
//				StringUtils.substringAfter(data, "["), "]");
        JSONObject json = null;
        try {
            json = new JSONObject(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        VideoInfo videoInfo = null;
        if (json != null) {
            videoInfo = getVideoInfo(json);
        }

        return videoInfo;
    }

    public boolean isEmbeddable(String url) {
        return false;
    }

    @Override
    public String getVideoId(String url) {
        return url.substring(url.indexOf("?"), url.indexOf("/"));
    }

    @Override
    public String getJSONAPIUrl(String videoId, String partType) {
        return null;
    }

    @Override
    public String getJSONAPIUrl(String videoId) {
        //NEW API :https://vimeo.com/api/oembed.json?url=https://vimeo.com/286128419
        //Old API: http://vimeo.com/api/v2/video/286128419.json
        logger.debug("VIMEO " + videoId);
        return "http://vimeo.com/api/oembed.json?url=https://vimeo.com/" + videoId;
    }

    @Override
    public VideoInfo getVideoInfo(JSONObject json) {
        VideoInfo videoInfo = new VideoInfo();
        videoInfo.description = JSONUtils.getString(json,
                ConstantsGlobal.DESCRIPTION);
        videoInfo.duration = JSONUtils.getInt(json, ConstantsGlobal.DURATION);
        //videoInfo.image = JSONUtils.getString(json, "thumbnail_medium");
        videoInfo.image = JSONUtils.getString(json, "thumbnail_url");
        videoInfo.site_name = "Vimeo";
        //videoInfo.tags = new HashSet<String>(Arrays.asList(StringUtils.split(JSONUtils.getString(json, "tags"), ",")));
        videoInfo.tags = new HashSet<String>();
        videoInfo.title = JSONUtils.getString(json, ConstantsGlobal.TITLE);
        //videoInfo.url = JSONUtils.getString(json, "url");
        videoInfo.url = JSONUtils.getString(json, "provider_url")+JSONUtils.getString(json, "video_id");
        //videoInfo.videoId = JSONUtils.getString(json, "id");
        videoInfo.videoId = JSONUtils.getString(json, "video_id");
        videoInfo.video = "https://player.vimeo.com/video/" + videoInfo.videoId
                + "?title=0&amp;byline=0&amp;portrait=0";
        return videoInfo;
    }

    public static void main(String[] args) {
        VimeoDataCollector v = new VimeoDataCollector();
        System.out.println(v.getData("https://vimeo.com/31611694"));
    }

    @Override
    public String formURL(LinkInfo info) {
        return "https://player.vimeo.com/video/" + info.id
                + "?title=0&amp;byline=0&amp;portrait=0";
    }
}
