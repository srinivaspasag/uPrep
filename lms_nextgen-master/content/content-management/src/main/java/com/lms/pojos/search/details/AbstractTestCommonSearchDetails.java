package com.lms.pojos.search.details;

import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.enums.EnumBasket.TestType;
import com.lms.enums.TestMode;
import com.lms.enums.TestResultVisibility;
import com.lms.models.tests.AbstractTestCommonModel;
import com.lms.pojos.TestDetails;
import com.lms.pojos.TestMetadata;
import com.lms.pojos.UniqueId;
import com.lms.pojos.tests.BoardQus;
import common.utils.JSONUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public abstract class AbstractTestCommonSearchDetails extends AbstractBoardSearchEntityTagDetails {

    public String code;
    public String desc;
    public BoardSearchEntity target;
    public int qusCount;
    public long duration;
    public double totalMarks;
    public double avgMarks;
    public long attempts;
    public boolean published;
    public List<TestMetadata> metadata;
    public TestType type;
    public TestMode mode;
    public List<String> childrenIds;            // (if this is testGroup then members of
    // this group)
    public String parentId;               // if this is part of a testGroup
    // (testGroupId)
    public TestResultVisibility resultVisibility;
    public String resultVisibilityMessage;
    public String pdfId;
    public String password;
    public String resultPassword;

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();
        json.put(ConstantsGlobal.NAME, name);
        json.put(ConstantsGlobal.CODE, code);
        json.put(ConstantsGlobal.DESC, desc);
        JSONUtils.addJSONAwareObject(Constants.TARGET, target, json);
        json.put(ConstantsGlobal.DURATION, duration);
        json.put(Constants.TOTAL_MARKS, totalMarks);
        json.put(ConstantsGlobal.AVG_MARKS, avgMarks);
        json.put(ConstantsGlobal.ATTEMPTS, attempts);
        json.put(ConstantsGlobal.PUBLISHED, published);
        json.put(Constants.QUS_COUNT, qusCount);
        if (type == null) {
            type = TestType.TEST;
        }
        json.put(ConstantsGlobal.TYPE, type.name());
        if (mode == null) {
            mode = TestMode.ONLINE;
        }
        json.put(ConstantsGlobal.MODE, mode.name());
        if (metadata != null) {
            JSONArray jsonArray = new JSONArray();
            for (TestMetadata m : metadata) {
                jsonArray.put(m.toJSON());
            }
            json.put(Constants.METADATA, jsonArray);
        }
        JSONUtils.addStringCollection("childrenIds", childrenIds, json);
        json.put(ConstantsGlobal.PARENT_ID, parentId);
        if (resultVisibility == null) {
            resultVisibility = TestResultVisibility.VISIBLE;
        }
        json.put(ConstantsGlobal.RESULT_VISIBILITY, resultVisibility.name());
        json.put(ConstantsGlobal.RESULT_VISIBILITY_MESSAGE, resultVisibilityMessage);
        json.put(ConstantsGlobal.PDF_ID, pdfId);
        json.put(ConstantsGlobal.PASSWORD, password);
        json.put(ConstantsGlobal.RESULT_PASSWORD, resultPassword);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        desc = JSONUtils.getString(json, ConstantsGlobal.DESC);
        duration = JSONUtils.getLong(json, ConstantsGlobal.DURATION);
        if (metadata == null) {
            metadata = new ArrayList<TestMetadata>();
        }
        JSONArray jsonArray = JSONUtils.getJSONArray(json, Constants.METADATA);
        for (int i = 0; i < jsonArray.length(); i++) {
            TestMetadata mdata = new TestMetadata();
            JSONUtils.getJSONAware(mdata, jsonArray, i);
            metadata.add(mdata);
        }
        name = JSONUtils.getString(json, ConstantsGlobal.NAME);
        code = JSONUtils.getString(json, ConstantsGlobal.CODE);
        target = new BoardSearchEntity();

        target.fromJSON(JSONUtils.getJSONObject(json, Constants.TARGET));
        totalMarks = JSONUtils.getDouble(json, Constants.TOTAL_MARKS);
        avgMarks = JSONUtils.getDouble(json, ConstantsGlobal.AVG_MARKS);
        published = JSONUtils.getBoolean(json, ConstantsGlobal.PUBLISHED);
        attempts = JSONUtils.getLong(json, ConstantsGlobal.ATTEMPTS);
        qusCount = JSONUtils.getInt(json, Constants.QUS_COUNT);
        type = TestType.valueOfKey(JSONUtils.getString(json, ConstantsGlobal.TYPE));
        mode = TestMode.valueOfKey(JSONUtils.getString(json, ConstantsGlobal.MODE));
        childrenIds = JSONUtils.getList(json, "childrenIds");
        parentId = JSONUtils.getString(json, ConstantsGlobal.PARENT_ID);
        resultVisibility = TestResultVisibility.valueOfKey(JSONUtils.getString(json,
                ConstantsGlobal.RESULT_VISIBILITY));
        resultVisibilityMessage = JSONUtils.getString(json,
                ConstantsGlobal.RESULT_VISIBILITY_MESSAGE);
        pdfId = JSONUtils.getString(json, ConstantsGlobal.PDF_ID);
        password = JSONUtils.getString(json, ConstantsGlobal.PASSWORD);
        resultPassword = JSONUtils.getString(json, ConstantsGlobal.RESULT_PASSWORD);

    }

    @Override
    public void fromMongoModel(VedantuBaseMongoModel mongoModel) {

        super.fromMongoModel(mongoModel);
        AbstractTestCommonModel test = (AbstractTestCommonModel) mongoModel;
        desc = test.desc;
        duration = test.duration;
        metadata = test.metadata;
        ensureNotNullQids();
        name = test.name;
        code = test.code;
        scope = test.scope;
        totalMarks = test.totalMarks;
        qusCount = test.qusCount;
        published = test.published;
        // TODO: add it from analytics module.. avgMarks are added at the time
        // of event
        // generation
        attempts = test.attempts;
        views = test.views;
        // avgRating = test.av;
        upVotes = test.upVotes;
        type = test.type;
        mode = test.mode;
        childrenIds = test.childrenIds;
        parentId = test.parentId;
        resultVisibility = test.resultVisibility;
        resultVisibilityMessage = test.resultVisibilityMessage;
        pdfId = test.pdfId;
        password = test.password;
        resultPassword = test.resultPassword;
    }

    @Override
    public UniqueId _getUniqueId() {

        return new UniqueId(ConstantsGlobal.ID, id);
    }

    private void ensureNotNullQids() {

        if (CollectionUtils.isEmpty(metadata)) {
            metadata = new ArrayList<TestMetadata>();
            return;
        }
        for (TestMetadata mdata : metadata) {
            if (mdata.qIds == null) {
                mdata.qIds = new ArrayList<String>();
            }
            if (mdata.children != null) {
                for (BoardQus topic : mdata.children) {
                    if (topic.qIds == null) {
                        topic.qIds = new ArrayList<String>();
                    }
                }
            }
            if (mdata.details != null) {
                for (TestDetails detail : mdata.details) {
                    if (detail.qIds == null) {
                        detail.qIds = new ArrayList<String>();
                    }
                }
            }
        }

    }

    @Override
    public NewsActivity toNewsActivity() {

        return null;
    }

    @Override
    public SrcEntity __getSrcEntity() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean _isIndexable() {

        return !StringUtils.isEmpty(name);
    }

    private static class Constants {

        static final String TARGET = "target";
        static final String METADATA = "metadata";
        static final String QUS_COUNT = "qusCount";
        static final String TOTAL_MARKS = "totalMarks";
    }

}



