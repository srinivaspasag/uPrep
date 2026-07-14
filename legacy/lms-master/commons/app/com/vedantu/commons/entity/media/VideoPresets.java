package com.vedantu.commons.entity.media;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.events.apis.JSONAware;
import com.vedantu.commons.utils.JSONUtils;

public class VideoPresets extends MediaPresets implements JSONAware {

    public final static MediaType type      = MediaType.VIDEO;
    private static final String   FILE_EXT  = "fileExt";
    private static final String   FRAMERATE = "framerate";
    public static final String    BITRATE   = "bitrate";
    public static final String    HEIGHT    = "height";
    public static final String    WIDTH     = "width";
    public static final String    DURATION  = "duration";

    public int                    width;
    public int                    height;                     // 720 p
    public int                    framerate;                  // in frame/s
    public String                 fileExt;
    public double                 duration;                   // in milliseconds
    public int                    bitrate;                    // bit rate per second

    public VideoPresets() {

        super();

        framerate = -1;
        width = -1;
        height = -1;
        fileExt = null;
        duration = -1;
        bitrate = -1;
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = new JSONObject();
        json.put(WIDTH, width);
        json.put(HEIGHT, height);
        json.put(FRAMERATE, framerate);
        json.put(FILE_EXT, fileExt);
        json.put(BITRATE, bitrate);
        json.put(DURATION, duration);

        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        width = JSONUtils.getInt(json, WIDTH);
        height = JSONUtils.getInt(json, HEIGHT);
        framerate = JSONUtils.getInt(json, FRAMERATE);
        fileExt = JSONUtils.getString(json, FILE_EXT);

        bitrate = JSONUtils.getInt(json, BITRATE);
        duration = JSONUtils.getInt(json, DURATION);

    }

    @Override
    public String toString() {

        return "VideoPresets [width=" + width + ", height=" + height + ", framerate=" + framerate
                + ", fileExt=" + fileExt + "]";
    }
}
