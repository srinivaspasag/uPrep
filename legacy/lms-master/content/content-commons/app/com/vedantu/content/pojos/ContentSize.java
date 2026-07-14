package com.vedantu.content.pojos;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.code.morphia.annotations.Embedded;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.events.apis.JSONAware;
import com.vedantu.commons.utils.JSONUtils;

/**
 * 
 * @author vikram
 * 
 */
@Embedded
public class ContentSize implements JSONAware {

    public static final String FIELD_ORIGIONAL = "original";
    public static final String FIELD_ENCRYPTED = "encrypted";
    public static final String FIELD_CONVERTED = "converted";
    public static final String FIELD_TOTALSIZE = "totalSize";

    private boolean            initialized;

    private long               original;
    private long               thumbnail;
    private long               encrypted;
    private long               converted;
    private long               totalSize;
    private boolean            finalized;

    public long getOriginal() {

        return original;
    }

    public void setOriginal(long original) {

        if (original == -1) {
            return;
        }
        this.original = original;
        addToTotal(this.original);
    }

    public void addOriginal(long inputoriginal) {

        if (inputoriginal == -1) {
            return;
        }
        this.original += inputoriginal;
        addToTotal(inputoriginal);
    }

    public long getThumbnail() {

        return thumbnail;
    }

    public void setThumbnail(long thumbnail) {

        if (thumbnail == -1) {
            return;
        }
        this.thumbnail = thumbnail;
        addToTotal(this.thumbnail);
    }

    public void addThumbnail(long thumbnail) {

        if (thumbnail == -1) {
            return;
        }
        this.thumbnail += thumbnail;

        addToTotal(thumbnail);
    }

    public long getEncrypted() {

        return encrypted;
    }

    public void setEncrypted(long encrypted) {

        if (encrypted == -1) {
            return;
        }
        this.encrypted = encrypted;
        addToTotal(this.encrypted);
    }

    public void addEncrypted(long encrypted) {

        if (encrypted == -1) {
            return;
        }
        this.encrypted += encrypted;

        addToTotal(encrypted);
    }

    public long getConverted() {

        return converted;

    }

    public void setConverted(long converted) {

        if (converted == -1) {
            return;
        }
        this.converted = converted;

        addToTotal(this.converted);
    }

    public void addConverted(long converted) {

        if (converted == -1) {
            return;
        }
        this.converted += converted;
        addToTotal(converted);
    }

    private void addToTotal(long size) {

        if (!initialized) {
            initialized = true;
        }
        totalSize += size;
    }

    public long getTotalSize() {

        return totalSize;
    }

    public boolean isInitialized() {

        return initialized;
    }

    public boolean isFinalized() {

        return initialized;
    }

    public void finalize() {

        finalized = true;
    }

    public void reset() {

        initialized = false;
        original = 0;
        thumbnail = 0;
        encrypted = 0;
        converted = 0;
        totalSize = 0;
        finalized = false;
    }

    public void add(ContentSize size) {

        this.addConverted(size.getConverted());
        this.addEncrypted(size.getEncrypted());
        this.addOriginal(size.getOriginal());
        this.addThumbnail(size.getThumbnail());
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = new JSONObject();
        json.put(FIELD_ORIGIONAL, original);
        json.put(ConstantsGlobal.THUMBNAIL, thumbnail);
        json.put(FIELD_ENCRYPTED, encrypted);
        json.put(FIELD_CONVERTED, converted);
        json.put(FIELD_TOTALSIZE, totalSize);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        original = JSONUtils.getLong(json, FIELD_ORIGIONAL);
        thumbnail = JSONUtils.getLong(json, ConstantsGlobal.THUMBNAIL);
        encrypted = JSONUtils.getLong(json, FIELD_ENCRYPTED);
        converted = JSONUtils.getLong(json, FIELD_CONVERTED);
        totalSize = JSONUtils.getLong(json, FIELD_TOTALSIZE);
    }
}
