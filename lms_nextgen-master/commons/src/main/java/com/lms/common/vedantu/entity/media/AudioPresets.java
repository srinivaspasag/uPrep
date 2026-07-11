package com.lms.common.vedantu.entity.media;

import com.lms.common.vedantu.entity.storage.MediaType;
import com.lms.common.vedantu.event.api.JSONAware;
import common.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class AudioPresets extends MediaPresets implements JSONAware {

    public final static MediaType type = MediaType.AUDIO;
    public static final String SAMPLERATE = "samplerate";
    public int samplerate = -1;             // in Hz

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = new JSONObject();
        json.put(SAMPLERATE, samplerate);

        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        samplerate = JSONUtils.getInt(json, SAMPLERATE);
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("AudioPresets [samplerate=");
        builder.append(samplerate);
        builder.append(", toString()=");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }

}
