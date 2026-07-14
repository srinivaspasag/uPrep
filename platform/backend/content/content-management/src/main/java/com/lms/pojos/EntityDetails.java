package com.lms.pojos;

import com.lms.common.exception.VedantuException;
import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.event.api.IEventDetails;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@Setter
@Getter
public class EntityDetails implements IEventDetails {
    public String type;
    public List<String> ids;
    public List<String> cmdsIds;

    @Override
    public SrcEntity __getSrcEntity() {
        return null;
    }

    @Override
    public NewsActivity toNewsActivity() throws VedantuException {
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

    @Override
    public JSONObject toJSON() throws JSONException {
        return null;
    }

    @Override
    public void fromJSON(JSONObject json) {

    }
}
