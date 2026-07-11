package com.lms.pojos;

import com.lms.common.vedantu.event.api.JSONAware;
import com.lms.enums.ExternalContentSrc;
import com.lms.interfaces.IDataCollector;
import common.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

public class LinkInfo implements JSONAware {

    public static final String ID           = "id";

    public static final String DOMAIN_URL   = "domainURL";

    public static final String DOMAIN_NAME  = "domainName";

    public static final String ORIGINAL_URL = "originalURL";
    public static final String EMBED_URL    = "embedURL";

    public String              originalURL;
    public String              domainName;
    public String              domainURL;
    public String              id;
    public String              embedURL;

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = new JSONObject();
        putNotNullValue(json, ORIGINAL_URL, originalURL);
        putNotNullValue(json, DOMAIN_NAME, domainName);
        putNotNullValue(json, DOMAIN_URL, domainURL);
        putNotNullValue(json, DOMAIN_URL, domainURL);
        putNotNullValue(json, EMBED_URL, embedURL);
        putNotNullValue(json, ID, id);

        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        if (json != null) {
            originalURL = JSONUtils.getString(json, ORIGINAL_URL);
            domainName = JSONUtils.getString(json, DOMAIN_NAME);
            domainURL = JSONUtils.getString(json, DOMAIN_URL);
            embedURL = JSONUtils.getString(json, EMBED_URL);
            id = JSONUtils.getString(json, ID);

        }
    }

    private void putNotNullValue(JSONObject json, String key, String value) throws JSONException {

        if (!StringUtils.isEmpty(value)) {
            json.put(key, value);
        }
    }

    @Override
    public String toString() {

        return "LinkInfo [originalURL=" + originalURL + ", domainName=" + domainName
                + ", domainURL=" + domainURL + ", id=" + id + "]";
    }

    public boolean populate() {

        ExternalContentSrc source = ExternalContentSrc.getSrc(domainURL);
        // TODO can be saved directly in linkInfo
        if (source != null && source != ExternalContentSrc.UNKNOWN) {
            IDataCollector collector = source.getDataCollector();
            if (collector != null) {
                embedURL = collector.formURL(this);
            }
        }
        return true;
    }
}
