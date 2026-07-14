package com.vedantu.content.search.details;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.IReverseImageMapperProcessor;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.commons.utils.image.ImageHTMLUtils;
import com.vedantu.content.commons.interfaces.ILibraryContent;
import com.vedantu.content.enums.Difficulty;
import com.vedantu.content.enums.LatexType;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.models.ContentSearchDetails;
import com.vedantu.content.models.Question;
import com.vedantu.content.pojos.tests.ITestQuestion;
import com.vedantu.content.pojos.tests.Marks;
import com.vedantu.mongo.VedantuBaseMongoModel;

public class QuestionSearchIndexDetails extends AbstractBoardSearchEntityTagDetails implements
        ITestQuestion, IReverseImageMapperProcessor, ILibraryContent {

    private static final ALogger     LOGGER = Logger.of(QuestionSearchIndexDetails.class);

    public String                    content;
//    public String                    paraContent;
    public QuestionType              type;
    public String                    source;
    public LatexType                 latexType;
    public List<String>              options;
    public Map<String, List<String>> matrix;
    public String                    code;

    public Marks                     marks;
    public int                       paraQuestionsCount;
    public int                       maxQuestionsToBeAttemptedForSection;
    public long                      solutions;
    public List<String>              key;
    public String                    solution;
    public List<String>              answerGiven;
    public long                      flags;
    public long                      attempts;
    public long                      notGradedCount;
    public boolean                   hasAns;
    public Difficulty                difficulty;
    // not null only if questio has been a part of some challenge in past
    public String                    challengeId;
    public int                       precision;

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();
        json.put(ConstantsGlobal.CONTENT, content);
        json.put(ConstantsGlobal.USER_ID, userId);
        if (type != null) {
            json.put(ConstantsGlobal.TYPE, type.name());
        }
        json.put(Constants.SOURCE, source);
        if (latexType != null) {
            json.put(Constants.LATEX_TYPE, latexType.name());
        }
        json.put(Constants.OPTIONS, options);
        json.put(Constants.MATRIX, matrix);
        json.put(ConstantsGlobal.CODE, code);
        json.put(ConstantsGlobal.SOLUTIONS, solutions);
        json.put(ConstantsGlobal.FLAGS, flags);
        json.put(ConstantsGlobal.ATTEMPTS, attempts);
        json.put(Constants.HAS_ANS, hasAns);
        if (difficulty != null) {
            json.put(ConstantsGlobal.DIFFICULTY, difficulty.name());
        } else {
            json.put(ConstantsGlobal.DIFFICULTY, Difficulty.UNKNOWN.name());
        }
        json.put(ConstantsGlobal.CHALLENGE_ID, challengeId);
        json.put(ConstantsGlobal.PRECISION, precision);
        json.put("maxQuestionsToBeAttemptedForSection",maxQuestionsToBeAttemptedForSection);
        return json;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        content = JSONUtils.getString(json, ConstantsGlobal.CONTENT);
        JSONObject gridJSON = JSONUtils.getJSONObject(json, Constants.MATRIX);
        if (gridJSON != null) {
            Iterator<String> keys = gridJSON.keys();
            while (keys.hasNext()) {
                try {
                    matrix.put(keys.next(), (List<String>) gridJSON.get(keys.next()));
                } catch (JSONException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        latexType = LatexType.valueOfKey(JSONUtils.getString(json, Constants.LATEX_TYPE));
        options = JSONUtils.getList(json, Constants.OPTIONS);
        source = JSONUtils.getString(json, Constants.SOURCE);
        type = QuestionType.valueOfKey(JSONUtils.getString(json, ConstantsGlobal.TYPE));
        code = JSONUtils.getString(json, ConstantsGlobal.CODE);
        solutions = JSONUtils.getLong(json, ConstantsGlobal.SOLUTIONS);
        flags = JSONUtils.getLong(json, ConstantsGlobal.FLAGS);
        attempts = JSONUtils.getLong(json, ConstantsGlobal.ATTEMPTS);
        hasAns = JSONUtils.getBoolean(json, Constants.HAS_ANS);
        difficulty = Difficulty.valueOfKey(JSONUtils.getString(json, ConstantsGlobal.DIFFICULTY));
        challengeId = JSONUtils.getString(gridJSON, ConstantsGlobal.CHALLENGE_ID);
        precision = JSONUtils.getInt(gridJSON, ConstantsGlobal.PRECISION);
        maxQuestionsToBeAttemptedForSection=JSONUtils.getInt(json,"maxQuestionsToBeAttemptedForSection");
    }

    @Override
    public void fromMongoModel(VedantuBaseMongoModel mongoModel) {

        super.fromMongoModel(mongoModel);
        Question q = (Question) mongoModel;
        content = q.content;
        matrix = q.matrix;
        latexType = q.latexType;
        options = q.options;
        source = q.source;
        type = q.type;
        code = q.code;
        userId = q.userId;
        flags = 0;// TODO; add question flag count here
        solutions = q.solutions;
        hasAns = q.hasAns;
        difficulty = q.difficulty;
        attempts = q.attempts;
        challengeId = q.challengeId;

    }

    @Override
    public SrcEntity __getSrcEntity() {

        return new SrcEntity(EntityType.QUESTION, id);
    }

    @Override
    public String toString() {

        return " [content=" + content + ", type=" + type + ", source=" + source + ", latexType="
                + latexType + ", options=" + options + ", matrix=" + matrix + ", solution="
                + solutions + ", flags=" + flags + ", attempts=" + attempts + ", hasAns=" + hasAns
                + ", difficulty=" + difficulty + ", challengeId=" + challengeId + ", precision="
                + precision + ", toString()=" + super.toString() + "]";
    }

    private static class Constants {

        static final String SOURCE     = "source";
        static final String LATEX_TYPE = "latexType";
        static final String MATRIX     = "matrix";
        static final String OPTIONS    = "options";
        static final String HAS_ANS    = "hasAns";
    }

    @Override
    public NewsActivity toNewsActivity() {

        return null;
    }

    @Override
    public void addImageSrcUrl() {

        content = ImageHTMLUtils.addImageSrcUrl(EntityType.QUESTION, content);
        if (options != null) {
            List<String> newOptions = new ArrayList<String>();
            for (String option : options) {
                option = ImageHTMLUtils.addImageSrcUrl(EntityType.QUESTION, option);
                newOptions.add(option);
            }
            options = newOptions;
        }

        if (matrix != null) {
            Map<String, List<String>> newMatrix = new LinkedHashMap<String, List<String>>();
            for (Entry<String, List<String>> entry : matrix.entrySet()) {
                for (String option : entry.getValue()) {
                    option = ImageHTMLUtils.addImageSrcUrl(EntityType.QUESTION, option);
                    if (newMatrix.get(entry.getKey()) == null) {
                        newMatrix.put(entry.getKey(), new ArrayList<String>());
                    }
                    newMatrix.get(entry.getKey()).add(option);
                }
            }
            matrix = newMatrix;
        }
    }

    @Override
    public void removeImageSrc(boolean moveImages) throws IOException, EntityFileStorageException {

    }

    @Override
    public ContentSearchDetails __getContentSearchDetails() throws JSONException {

        ContentSearchDetails contentDetails = new ContentSearchDetails();
        JSONObject json = toJSON();
        contentDetails.fromJSON(json);

        contentDetails.name = content;
        contentDetails.type = EntityType.QUESTION;
        contentDetails.desc = content;
        if (type != null) {
            contentDetails.subType = type.name();
        }
        JSONUtils.removeKeys(json, contentDetails.toJSON());
        if (json != null) {
            contentDetails.setInfo(json.toString());
        }
        return contentDetails;
    }

    @Override
    public boolean _isIndexable() {

        return StringUtils.isNotEmpty(content);
    }
}
