package com.lms.models.event.search.details;

import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.entity.media.AudioPresets;
import com.lms.common.vedantu.entity.media.VideoPresets;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.event.api.IEventDetails;
import com.lms.common.vedantu.event.api.JSONAware;
import common.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class VideoTranscodingDetails implements JSONAware, IEventDetails {

    private static final String GENERATE_NEW_VIDEO = "generateNewVideo";
    private static final String GENERATE_FILE_SIZE = "generateFileSize";
    private static final String GENERATE_DURATION = "generateDuration";
    private static final String GENERATE_THUMBNAIL = "generateThumbnail";
    private static final String ENCRYPT_IF_NEEDED = "encryptIfNeeded";
    private static final String CONVERTED_FILE_NAME = "convertedFileFormat";
    private static final String VIDEO_PRESET = "videoPreset";
    private static final String AUDIO_PRESET = "audioPreset";

    public VideoPresets videoPreset;
    public AudioPresets audioPreset;
    public String videoId;
    public String convertedFileFormat;
    public boolean generateThumbnail;
    public boolean generateDuration;
    public boolean generateFileSize;
    public boolean generateNewVideo;
    public boolean encryptIfNeeded;
    public String jobId;

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = new JSONObject();
        if (videoPreset != null) {
            json.put(VIDEO_PRESET, videoPreset.toJSON());
        }

        if (audioPreset != null) {
            json.put(AUDIO_PRESET, audioPreset.toJSON());
        }

        json.put(GENERATE_FILE_SIZE, generateFileSize);
        json.put(GENERATE_THUMBNAIL, generateThumbnail);
        json.put(GENERATE_DURATION, generateDuration);
        json.put(CONVERTED_FILE_NAME, convertedFileFormat);
        json.put(GENERATE_NEW_VIDEO, generateNewVideo);
        json.put(ENCRYPT_IF_NEEDED, encryptIfNeeded);
        json.put(ConstantsGlobal.VIDEO_ID, videoId);
        json.put(ConstantsGlobal.JOB_ID, jobId);
        return json;

    }

    @Override
    public void fromJSON(JSONObject json) {

        videoId = JSONUtils.getString(json, ConstantsGlobal.VIDEO_ID);
        audioPreset = new AudioPresets();
        JSONUtils.getJSONAware(audioPreset, json, AUDIO_PRESET);

        videoPreset = new VideoPresets();
        JSONUtils.getJSONAware(videoPreset, json, VIDEO_PRESET);

        convertedFileFormat = JSONUtils.getString(json, CONVERTED_FILE_NAME);
        generateThumbnail = JSONUtils.getBoolean(json, GENERATE_THUMBNAIL);
        generateDuration = JSONUtils.getBoolean(json, GENERATE_DURATION);
        generateFileSize = JSONUtils.getBoolean(json, GENERATE_FILE_SIZE);
        generateNewVideo = JSONUtils.getBoolean(json, GENERATE_NEW_VIDEO);
        encryptIfNeeded = JSONUtils.getBoolean(json, ENCRYPT_IF_NEEDED);
        videoId = JSONUtils.getString(json, ConstantsGlobal.VIDEO_ID);
        jobId = JSONUtils.getString(json, ConstantsGlobal.JOB_ID);
    }

    @Override
    public SrcEntity __getSrcEntity() {

        return new SrcEntity(EntityType.CMDSVIDEO, videoId);
    }

    @Override
    public NewsActivity toNewsActivity() {

        return null;
    }

    @Override
    public boolean enableNotifcation(boolean value) {

        return false;
    }

    @Override
    public boolean getNotificationEnabled() {

        return false;
    }

}
