package com.lms.cmds;

import com.lms.common.cmds.IImageUrlProcessor;
import com.lms.common.pojos.ILatexProcessor;
import com.lms.common.vedantu.entity.storage.EntityFileStorageException;
import com.lms.common.vedantu.event.api.JSONAware;
import com.lms.interfaces.IReverseImageMapperProcessor;
import common.utils.JSONUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HintInfo implements ILatexProcessor, IImageUrlProcessor, JSONAware, IReverseImageMapperProcessor {

    public static final String HINTS = "hints";
    private static final String DEDUCTIONS = "deductions";
    public List<HintFormat> hints;
    public List<Integer> deductions;

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
