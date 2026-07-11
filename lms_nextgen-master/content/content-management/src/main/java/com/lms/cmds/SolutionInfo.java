package com.lms.cmds;

import com.lms.common.cmds.IImageUrlProcessor;
import com.lms.common.pojos.ILatexProcessor;
import com.lms.common.vedantu.entity.storage.EntityFileStorageException;
import com.lms.common.vedantu.event.api.JSONAware;
import com.lms.interfaces.IReverseImageMapperProcessor;
import com.lms.question.OptionFormat;
import com.lms.question.SolutionFormat;
import common.utils.JSONUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.annotation.Transient;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SolutionInfo implements JSONAware, Serializable, ILatexProcessor, IImageUrlProcessor,
        IReverseImageMapperProcessor {

    @Transient
    public static final String ANSWER = "answer";

    private static final long serialVersionUID = 1L;
    public OptionFormat optionBody;
    public List<SolutionFormat> solutions;
    public String globalAnsId;
    public String gsonClassName;

    public SolutionInfo() {

        this(new OptionFormat());

    }

    public SolutionInfo(OptionFormat op) {

        this.optionBody = op;
        this.solutions = new ArrayList<SolutionFormat>();
        this.gsonClassName = this.getClass().getName();
    }

    @Override
    public void addHook() {

        if (optionBody != null) {
            optionBody.addHook();
        }

        if (solutions != null && !solutions.isEmpty()) {
            for (SolutionFormat solutionBody : solutions) {
                solutionBody.addHook();
            }
        }
    }

    @Override
    public void convertUuidsToImageUrls(boolean permanentImageUrl, String questionSetId) {

        if (optionBody != null) {
            optionBody.convertUuidsToImageUrls(permanentImageUrl, questionSetId);
        }
        if (solutions != null && !solutions.isEmpty()) {
            for (SolutionFormat solution : solutions) {
                solution.convertUuidsToImageUrls(permanentImageUrl, questionSetId);
            }
        }
    }

    @Override
    public void convertImageUrlToUuids(boolean saveImages, boolean permanentImageUrl,
                                       String questionSetId) throws IOException {

        if (optionBody != null) {
            optionBody.convertImageUrlToUuids(saveImages, permanentImageUrl, questionSetId);
        }
        if (solutions != null && !solutions.isEmpty()) {
            for (SolutionFormat solution : solutions) {
                solution.convertImageUrlToUuids(saveImages, permanentImageUrl, questionSetId);
            }
        }
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = new JSONObject();
        if (this.optionBody != null) {
            json.put("optionBody", this.optionBody.toJSON());
        }

        if (CollectionUtils.isNotEmpty(solutions)) {
            JSONArray jsonArray = new JSONArray();
            for (SolutionFormat sol : solutions) {
                jsonArray.put(sol.toJSON());
            }
            json.put("solutions", jsonArray);
        }
        json.put("globalAnsId", globalAnsId);
        json.put("gsonClassName", gsonClassName);

        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        JSONArray sols = JSONUtils.getJSONArray(json, "solutions");
        List<SolutionFormat> solutions = new ArrayList<SolutionFormat>();
        if (sols != null) {
            for (int i = 0; i < sols.length(); i++) {
                solutions.add((SolutionFormat) JSONUtils
                        .getJSONAware(new SolutionFormat(), sols, i));
            }
        }
        this.solutions = solutions;
        this.optionBody = (OptionFormat) JSONUtils.getJSONAware(new OptionFormat(), json,
                "optionBody");
        this.globalAnsId = JSONUtils.getString(json, "globalAnsId");
        this.gsonClassName = JSONUtils.getString(json, "gsonClassName");
    }

    @Override
    public void addImageSrcUrl() {

        if (optionBody != null) {
            optionBody.addImageSrcUrl();

        }

        if (solutions != null && !solutions.isEmpty()) {
            for (SolutionFormat solution : solutions) {
                solution.addImageSrcUrl();
            }
        }

    }

    @Override
    public void removeImageSrc(boolean moveImages) throws IOException, EntityFileStorageException {

        if (optionBody != null) {
            optionBody.removeImageSrc(moveImages);

        }

        if (solutions != null && !solutions.isEmpty()) {
            for (SolutionFormat solution : solutions) {
                solution.removeImageSrc(moveImages);
            }
        }

    }

}
