package com.vedantu.cmds.pojos.content.question;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.cmds.mgmt.interfaces.IImageUrlProcessor;
import com.vedantu.cmds.mgmt.interfaces.ILatexProcessor;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.events.apis.JSONAware;
import com.vedantu.commons.utils.IReverseImageMapperProcessor;
import com.vedantu.commons.utils.JSONUtils;

public class HintInfo implements ILatexProcessor, IImageUrlProcessor, JSONAware, IReverseImageMapperProcessor {

    private static final String DEDUCTIONS = "deductions";
    public static final String  HINTS      = "hints";
    public List<HintFormat>     hints;
    public List<Integer>        deductions;

    // nt
    public HintInfo() {

        hints = new ArrayList<HintFormat>();
        deductions = new ArrayList<Integer>();
    }

    @Override
    public void convertUuidsToImageUrls(boolean permanentImageUrl, String questionSetId) {

        if (!CollectionUtils.isEmpty(hints)) {
            for (HintFormat hf : hints) {
                hf.convertUuidsToImageUrls(permanentImageUrl, questionSetId);
            }
        }
    }

    @Override
    public void convertImageUrlToUuids(boolean saveImages, boolean permanentImageUrl,
            String questionSetId) throws IOException {

        if (!CollectionUtils.isEmpty(hints)) {
            for (HintFormat hf : hints) {
                hf.convertImageUrlToUuids(saveImages, permanentImageUrl, questionSetId);
            }
        }
    }

    @Override
    public void addHook() {

        if (!CollectionUtils.isEmpty(hints)) {
            for (HintFormat hf : hints) {
                hf.addHook();
            }
        }
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = new JSONObject();

        if (hints != null) {
            JSONArray jsonArray = new JSONArray();
            for (HintFormat hint : hints) {
                jsonArray.put(hint.toJSON());
            }
            json.put(HINTS, jsonArray);
        }
        if (deductions != null) {
            JSONArray jsonArray = new JSONArray();
            for (Integer hint : deductions) {
                jsonArray.put(hint);
            }
            json.put(DEDUCTIONS, jsonArray);
        }

        return json;

    }

    @Override
    public void fromJSON(JSONObject json) {

        JSONArray hints = JSONUtils.getJSONArray(json, HINTS);
        List<HintFormat> hintFormats = new ArrayList<HintFormat>();
        if (hints != null) {
            for (int i = 0; i < hints.length(); i++) {
                hintFormats.add((HintFormat) JSONUtils.getJSONAware(new HintFormat(), hints, i));
            }
        }
        this.hints = hintFormats;
        this.deductions = JSONUtils.getIntegerList(json, DEDUCTIONS);

    }

    @Override
    public void addImageSrcUrl() {
        if (CollectionUtils.isNotEmpty(hints)) {
            for (HintFormat hf : hints) {
                hf.addImageSrcUrl();
            }
        }
        
    }

    @Override
    public void removeImageSrc(boolean moveImages) throws IOException, EntityFileStorageException {

        if (CollectionUtils.isNotEmpty(hints)) {
            for (HintFormat hf : hints) {
                hf.removeImageSrc(moveImages);
            }
        }
    }

}
