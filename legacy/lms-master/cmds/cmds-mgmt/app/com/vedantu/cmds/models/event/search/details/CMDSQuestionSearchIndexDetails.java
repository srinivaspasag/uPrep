package com.vedantu.cmds.models.event.search.details;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.cmds.models.CMDSQuestion;
import com.vedantu.cmds.pojos.content.question.HintInfo;
import com.vedantu.cmds.pojos.content.question.QuestionFormat;
import com.vedantu.cmds.pojos.content.solution.metadata.GridSolutionInfo;
import com.vedantu.cmds.pojos.content.solution.metadata.MCQsolutionInfo;
import com.vedantu.cmds.pojos.content.solution.metadata.NumericSolutionInfo;
import com.vedantu.cmds.pojos.content.solution.metadata.SCQSolutionInfo;
import com.vedantu.cmds.pojos.content.solution.metadata.SolutionInfo;
import com.vedantu.cmds.pojos.content.solution.metadata.TextSolutionInfo;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.events.apis.JSONAware;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.content.enums.Difficulty;
import com.vedantu.content.enums.QuestionStatus;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.search.details.AbstractBoardSearchEntityTagDetails;
import com.vedantu.mongo.VedantuBaseMongoModel;

public class CMDSQuestionSearchIndexDetails extends AbstractBoardSearchEntityTagDetails implements
        JSONAware, IListResponseObj {

    public static final String  PUBLISHED         = "published";
    public static final String  ORIG_REF_NO       = "origRefNo";
    public static final String  STATUS            = "status";
    public static final String  GLOBAL_QID        = "globalQid";
    public static final String  SOURCE            = "source";
    public static final String  QUESTION_SET_ID   = "questionSetId";
    public static final String  QUESTION_SET_NAME = "questionSetName";
    public static final String  TYPE              = "type";
    public static final String  HINTS             = "hints";
    public static final String  QUESTION_BODY     = "questionBody";
    private static final String SOLUTION_INFO     = "solutionInfo";

    public boolean              published;
    public SolutionInfo         solutionInfo;
    public QuestionFormat       questionBody;
    public QuestionType         type;
    public String               questionSetName;
    public String               questionSetId;
    public String               source;
    public String               globalQid;
    public QuestionStatus       status;
    public HintInfo             hints;
    public String               origRefNo;

    // public long publishedOn;
    // public SrcEntity publishedBy;

    public CMDSQuestionSearchIndexDetails(String qid, QuestionType type, String userId,
            String questionSetName, String assignedTo, String source, String questionSetId,
            boolean published, /*
                                * Metadata metadata,
                                */
            Difficulty difficulty, QuestionStatus status) {

        super();
        this.type = type;
        this.userId = userId;
        this.questionSetName = questionSetName;
        this.source = source;
        this.questionSetId = questionSetId;
        this.published = published;
        this.status = status;
        this.difficulty = difficulty;
    }

    public CMDSQuestionSearchIndexDetails() {

        // TODO Auto-generated constructor stub
    }

    @Override
    public SrcEntity __getSrcEntity() {

        return new SrcEntity(EntityType.CMDSQUESTION, this.id);
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();
        if (solutionInfo != null) {
            json.put(SOLUTION_INFO, solutionInfo.toJSON());
        }
        if (questionBody != null) {
            json.put(QUESTION_BODY, questionBody.toJSON());
        }
        if (hints != null) {
            json.put(HINTS, hints.toJSON());
        }
        json.put(TYPE, type);
        json.put(QUESTION_SET_NAME, questionSetName);
        json.put(QUESTION_SET_ID, questionSetId);
        json.put(SOURCE, SOURCE);
        json.put(GLOBAL_QID, globalQid);
        json.put(STATUS, status);
        json.put(ORIG_REF_NO, origRefNo);
        json.put(PUBLISHED, published);

        return json;

    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);

        type = QuestionType.valueOfKey(JSONUtils.getString(json, TYPE));
        if (type == QuestionType.SCQ) {
            solutionInfo = new SCQSolutionInfo();

        } else if (type == QuestionType.MCQ || type == QuestionType.PARA) {
            solutionInfo = new MCQsolutionInfo();
        } else if (type == QuestionType.TEXT || type == QuestionType.SUBJECTIVE) {
            solutionInfo = new TextSolutionInfo();
        } else if (type == QuestionType.MATRIX) {
            solutionInfo = new MCQsolutionInfo();
        } else if (type == QuestionType.NUMERIC) {
            solutionInfo = new NumericSolutionInfo();
        }

        solutionInfo.fromJSON(JSONUtils.getJSONObject(json, SOLUTION_INFO));

        questionBody = new QuestionFormat();
        questionBody.fromJSON(JSONUtils.getJSONObject(json, QUESTION_BODY));

        hints = new HintInfo();
        hints.fromJSON(JSONUtils.getJSONObject(json, HINTS));

        globalQid = JSONUtils.getString(json, GLOBAL_QID);

        questionSetName = JSONUtils.getString(json, QUESTION_SET_NAME);

        source = JSONUtils.getString(json, SOURCE);
        questionSetId = JSONUtils.getString(json, QUESTION_SET_ID);
        published = JSONUtils.getBoolean(json, PUBLISHED);

        status = QuestionStatus.valueOfKey(JSONUtils.getString(json, STATUS));

    }

    @Override
    public void fromMongoModel(VedantuBaseMongoModel mongoModel) {

        super.fromMongoModel(mongoModel);

        CMDSQuestion q = (CMDSQuestion) mongoModel;
        this.solutionInfo = q.solutionInfo;
        this.questionBody = q.questionBody;
        this.hints = q.hints;
        this.type = q.type;
        this.questionSetId = q.questionSetId;
        this.questionSetName = q.questionSetName;
        this.globalQid = q.globalQid;
        this.origRefNo = q.origRefNo;
        this.isNotificationEnabled = false;
        this.status = q.status;
        this.published = q.published;
        this.name = StringUtils.EMPTY;

    }

    @Override
    public NewsActivity toNewsActivity() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean _isIndexable() {

        return questionBody != null && StringUtils.isNotEmpty(questionBody.newText);
    }

}
