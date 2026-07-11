package com.lms.web;

import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.pojos.LinkInfo;
import com.lms.web.util.WebUtils;
import common.utils.JSONUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YouTubeDataCollector extends AbstractVideoDataCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(YouTubeDataCollector.class);

    public static String YOUTUBE_APIKEY = "__GOOGLE_API_KEY_REDACTED__";

    public static void main(String[] args) {
        YouTubeDataCollector t = new YouTubeDataCollector();
        System.out
                .println(t
                        .getData("http://www.youtube.com/watch?v=gWNRUVMboq4&feature=related"));
    }

    @Override
    public VideoInfo getData(String url) {
        JSONObject snippetJson = WebUtils.getJSONData(getJSONAPIUrl(getVideoId(url), "snippet"));
        JSONObject contentDetailsJson = WebUtils.getJSONData(getJSONAPIUrl(getVideoId(url), "contentDetails"));
        VideoInfo videoInfo = getVideoInfo(snippetJson, contentDetailsJson);
        return videoInfo;
    }

    public boolean isEmbeddable(String url) {
        JSONObject json = WebUtils.getJSONData(getJSONAPIUrl(getVideoId(url), "status"));
        JSONArray items = JSONUtils.getJSONArray(json, "items");
        try {
            JSONObject item = items.getJSONObject(0);
            JSONObject status = JSONUtils.getJSONObject(item, "status");
            return JSONUtils.getBoolean(status, "embeddable");
        } catch (JSONException e) {
            LOGGER.error("Exception Occured while checking for embeddable file " + e.getMessage());
        }
        return false;
    }

    @Override
    public String getVideoId(String url) {
        return url.substring(url.substring(url.indexOf("?"), url.indexOf("v=")).indexOf("&"));
    }

    @Override
    public String getJSONAPIUrl(String videoId, String partType) {
        return "https://www.googleapis.com/youtube/v3/videos?id=" + videoId
                + "&part=" + partType + "&key=" + YOUTUBE_APIKEY;
    }

    @Override
    public VideoInfo getVideoInfo(JSONObject json) {
        VideoInfo videoInfo = new VideoInfo();
        json = JSONUtils.getJSONObject(json, "data");
        videoInfo.description = JSONUtils.getString(json,
                ConstantsGlobal.DESCRIPTION);
        videoInfo.duration = JSONUtils.getInt(json, ConstantsGlobal.DURATION);
        videoInfo.image = JSONUtils.getString(
                JSONUtils.getJSONObject(json, "thumbnail"), "hqDefault");
        videoInfo.site_name = "YouTube";
        videoInfo.tags = JSONUtils.getSet(json, "tags");
        videoInfo.title = JSONUtils.getString(json, ConstantsGlobal.TITLE);
        videoInfo.url = JSONUtils.getString(
                JSONUtils.getJSONObject(json, "player"), "default");
        videoInfo.videoId = JSONUtils.getString(json, "id");
//		videoInfo.video = JSONUtils.getString(
//				JSONUtils.getJSONObject(json, "content"), "5");
        videoInfo.video = "https://www.youtube.com/embed/" + videoInfo.videoId + "?feature=player_embedded";
        return videoInfo;
    }

    @Override
    public String getJSONAPIUrl(String videoId) {
        return null;
    }

    public int getDuration(String dur) {
        String time = dur.substring(2);
        int duration = 0;
        Object[][] indexs = new Object[][]{{"H", 3600}, {"M", 60}, {"S", 1}};
        for (int i = 0; i < indexs.length; i++) {
            int index = time.indexOf((String) indexs[i][0]);
            if (index != -1) {
                String value = time.substring(0, index);
                duration += Integer.parseInt(value) * (Integer) indexs[i][1];
                time = time.substring(value.length() + 1);
            }
        }
        return duration;
    }

    public VideoInfo getVideoInfo(JSONObject snippetJson, JSONObject contentDetailsJson) {
        VideoInfo videoInfo = new VideoInfo();
        JSONArray snippetItem = JSONUtils.getJSONArray(snippetJson, "items");
        JSONArray contentDetailsItem = JSONUtils.getJSONArray(contentDetailsJson, "items");
        try {
            JSONObject snippet = snippetItem.getJSONObject(0);
            videoInfo.duration = getDuration(contentDetailsItem.getJSONObject(0).getJSONObject("contentDetails").getString("duration"));
            LOGGER.debug("Duration after extraction is " + videoInfo.duration);
            videoInfo.videoId = JSONUtils.getString(snippet, "id");
            videoInfo.video = "https://www.youtube.com/embed/" + videoInfo.videoId + "?feature=player_embedded";
            videoInfo.site_name = "YouTube";
            snippet = JSONUtils.getJSONObject(snippet, "snippet");
            videoInfo.description = JSONUtils.getString(snippet, "description");
            videoInfo.title = JSONUtils.getString(snippet, "title");
            videoInfo.tags = JSONUtils.getSet(snippet, "tags");
            videoInfo.url = "https://www.youtube.com/watch?v=" + videoInfo.videoId;
            videoInfo.image = snippet.getJSONObject("thumbnails").getJSONObject("high").getString("url");

        } catch (JSONException e) {
            LOGGER.error("Exception Occured while building video Info " + e.getMessage());
        }
        return videoInfo;
    }

    @Override
    public String formURL(LinkInfo info) {
        return "https://www.youtube.com/embed/" + info.id + "?feature=player_embedded";
    }
}
