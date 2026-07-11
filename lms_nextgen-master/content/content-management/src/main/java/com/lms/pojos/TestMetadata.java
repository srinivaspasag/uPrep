package com.lms.pojos;

import com.lms.api.IAnalyticsBoardMember;
import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.BoardType;
import com.lms.common.vedantu.event.api.JSONAware;
import com.lms.enums.EnumBasket;
import com.lms.enums.QuestionType;
import com.lms.pojos.search.details.BoardSearchEntity;
import com.lms.pojos.tests.BoardAnalyticsInfo;
import com.lms.pojos.tests.BoardQus;
import com.lms.pojos.tests.Marks;
import common.utils.JSONUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TestMetadata implements JSONAware, Serializable, IAnalyticsBoardMember {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(TestMetadata.class);
    private static final String DETAILS = "details";
    @NotBlank
    public String id;                                              // board
    // name
    // _id
    @NotBlank
    public String name;                                            // borad
    public int qusCount;
    public int currentQuesCount;
    public List<TestDetails> details;
    public int totalMarks;
    public List<BoardQus> children;
    public List<String> qIds;
    public Map<String, Marks> marks;

    public TestMetadata() {

        super();
    }

    public TestMetadata(String id, String name, int qusCount) {

        this.id = id;
        this.name = name;
        this.qusCount = qusCount;
        this.qIds = new ArrayList<String>();
    }

    public void addDetails(TestDetails detail) {

        if (details == null) {
            details = new ArrayList<TestDetails>();
        }
        if (this.marks == null) {
            this.marks = new HashMap<String, Marks>();
        }
        for (String qId : detail.qIds) {
            this.marks.put(qId, detail.marks);
        }
        details.add(detail);
    }

    public void addChild(BoardQus brdQus) {

        if (children == null) {
            children = new ArrayList<BoardQus>();
        }
        children.add(brdQus);
    }

    public void addQuestion(String qId, QuestionType questionType, Marks marks) {

        if (this.qIds.contains(qId)) {
            return;
        }
        this.qIds.add(qId);
        this.qusCount++;
        this.totalMarks += marks.positive;

        if (this.details == null) {
            this.details = new ArrayList<TestDetails>();
        }
        if (this.marks == null) {
            this.marks = new HashMap<String, Marks>();
        }
        this.marks.put(qId, marks);

        TestDetails details = __getDetails(questionType);
        if (details == null) {
            details = new TestDetails(questionType, 0, marks.positive, marks.negative);
            details.qusCount++;
            details.qIds.add(qId);
            this.details.add(details);
        } else {
            details.qusCount++;
            details.qIds.add(qId);
        }
    }

    public boolean addQuestion(String qId, QuestionType qType, BoardSearchEntity childBoard,
                               String testId, EnumBasket.TestType testType, boolean addOnlyToBoard) throws VedantuException {

        BoardQus boardQus = childBoard == null ? null : __getBoardQus(childBoard.id);
        logger.info("boardQus for id[" + id + "], " + boardQus);
        if (boardQus == null && childBoard != null) {
            logger.info("adding new boardQus id[" + id + "] to testMeatada for " + childBoard.type
                    + "[id:" + this.id + ",name:" + this.name + " ] ");
            boardQus = new BoardQus(childBoard.id, childBoard.name, 0);
            this.addChild(boardQus);
        }
        if (this.marks == null) {
            this.marks = new HashMap<String, Marks>();
        }
        if (qIds == null) {
            qIds = new ArrayList<String>();
        }

        if (!addOnlyToBoard && qIds.size() == qusCount) {
            throw new VedantuException(VedantuErrorCode.ALREADY_ADDED, "already added " + qusCount
                    + " question  for questionType: " + qType + " for board[name: " + name
                    + ", id: " + id + "]  in test[" + testId + "]");
        }
        if (boardQus != null && boardQus.qIds == null) {
            boardQus.qIds = new ArrayList<String>();
        }

        if (boardQus != null && boardQus.qIds.contains(qId)) {
            throw new VedantuException(VedantuErrorCode.ALREADY_ADDED, "qId : " + qId
                    + " already added in boardId: " + id + " for test[" + testId + "]");
        } else if (boardQus != null) {
            logger.info("adding question to board : " + id + " for test[" + testId + "]");
            boardQus.qIds.add(qId);
            if (this.marks.get(qId) != null) {
                boardQus.totalMarks += this.marks.get(qId).positive;
            }

            boardQus.qusCount++;
        }

        if (addOnlyToBoard) {
            return true;
        }

        TestDetails detail = __getDetails(qType);
        if (detail == null && testType == EnumBasket.TestType.TEST) {
            logger.error("testDetails is null for id: " + this.id + " and questionType: " + qType
                    + "test[" + testId + "]");
            return false;
        }

        if (detail != null && detail.qIds == null) {
            detail.qIds = new ArrayList<String>();
        }

        if (qIds.contains(qId) || (detail != null && detail.qIds.contains(qId))) {
            throw new VedantuException(VedantuErrorCode.ALREADY_ADDED, "qid : " + qId
                    + " already added for questionType: " + qType + " in test[" + testId + "]");
        }
        if (detail != null && detail.qIds.size() == detail.qusCount) {
            throw new VedantuException(VedantuErrorCode.ALREADY_ADDED, "already added "
                    + detail.qusCount + " question  for questionType: " + qType + " in test["
                    + testId + "]");
        }
        /*Added by Shivank Removed for the current implementation*/
        ////detail.currentQuesCount++;
        /*Added by Shivank*/
        return (detail == null || detail.qIds.add(qId)) && qIds.add(qId);
    }

    public boolean removeQuestion(String qId, QuestionType qType, com.lms.pojos.search.details.BoardSearchEntity childBoard,
                                  String testId, EnumBasket.TestType testType) {

        logger.info(".............Entered removeQuestion" + qId + " ..............");
        BoardQus boardQus = childBoard == null ? null : __getBoardQus(childBoard.id);
        TestDetails detail = __getDetails(qType);
        if (detail == null && testType == EnumBasket.TestType.TEST) {
            return false;
        }
        if (detail != null && detail.qIds == null) {
            detail.qIds = new ArrayList<String>();
        }
        boolean removedFromBoardQus = false;
        if (boardQus != null && boardQus.qIds != null) {
            removedFromBoardQus = boardQus.qIds.remove(qId);
            /*Added by Shivank*/
            ////detail.currentQuesCount--;
            /*Added by Shivank*/
            if (boardQus.qIds.size() == 0) {
                this.children.remove(boardQus);
            } else if (boardQus.qusCount > 0) {
                boardQus.qusCount--;

            }

        }
        return ((detail == null || detail.qIds.remove(qId)) && qIds.remove(qId)) || removedFromBoardQus;
    }

    private BoardQus __getBoardQus(String id) {

        if (children == null || children.isEmpty() || (id).isEmpty()) {
            return null;
        }
        BoardQus boardQus = null;
        for (BoardQus bQ : children) {
            if ((id.equalsIgnoreCase(bQ.id))) {
                boardQus = bQ;
                break;
            }
        }
        return boardQus;
    }

    public TestDetails __getDetails(QuestionType qType) {

        if (details == null || details.isEmpty()) {
            return null;
        }

        for (TestDetails detail : details) {
            if (detail.type.equals(qType)) {
                return detail;
            }
        }
        return null;
    }

    public void finishEditing() {

        if (this.marks == null) {
            this.marks = new HashMap<String, Marks>();
        }
        if (details == null) {
            details = new ArrayList<TestDetails>();
        }
        for (TestDetails detail : details) {
            if (detail.qIds != null) {
                for (String qId : detail.qIds) {
                    this.marks.put(qId, detail.marks);
                }
            }
        }
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = new JSONObject();
        json.put(ConstantsGlobal.ID, id);
        json.put(ConstantsGlobal.NAME, name);
        json.put(ConstantsGlobal.QUS_COUNT, qusCount);
        json.put(ConstantsGlobal.QIDS, qIds);
        json.put(ConstantsGlobal.TOTAL_MARKS, totalMarks);
        JSONArray jC = new JSONArray();
        if (children != null && !children.isEmpty()) {
            for (BoardQus child : children) {
                jC.put(child.toJSON());
            }
        }
        json.put(ConstantsGlobal.CHILDREN, jC);
        JSONArray jD = new JSONArray();
        if (details != null && !details.isEmpty()) {
            for (TestDetails detail : details) {
                jD.put(detail.toJSON());
            }
        }
        json.put(DETAILS, jD);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        id = JSONUtils.getString(json, ConstantsGlobal.ID);
        name = JSONUtils.getString(json, ConstantsGlobal.NAME);
        qusCount = JSONUtils.getInt(json, ConstantsGlobal.QUS_COUNT);
        totalMarks = JSONUtils.getInt(json, ConstantsGlobal.TOTAL_MARKS);
        qIds = JSONUtils.getList(json, ConstantsGlobal.QIDS);
        JSONArray childrenJSONArray = JSONUtils.getJSONArray(json, ConstantsGlobal.CHILDREN);
        if (childrenJSONArray != null) {
            if (children == null) {
                children = new ArrayList<BoardQus>();
            }
            for (int i = 0; i < childrenJSONArray.length(); i++) {
                BoardQus bQ = new BoardQus();
                JSONObject childrenJSON;
                try {
                    childrenJSON = childrenJSONArray.getJSONObject(i);
                } catch (JSONException e) {
                    continue;
                }
                bQ.fromJSON(childrenJSON);
                children.add(bQ);
            }
        }

        JSONArray detailsJSONArray = JSONUtils.getJSONArray(json, DETAILS);
        if (detailsJSONArray != null) {
            if (details == null) {
                details = new ArrayList<TestDetails>();
            }
            for (int i = 0; i < detailsJSONArray.length(); i++) {
                TestDetails detail = new TestDetails();

                JSONObject detailsJSON;
                try {
                    detailsJSON = detailsJSONArray.getJSONObject(i);
                } catch (JSONException e) {
                    continue;
                }
                detail.fromJSON(detailsJSON);

                details.add(detail);
            }
        }
    }

    @Override
    public int hashCode() {

        return ((id == null) ? 0 : id.hashCode());
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null || getClass() != obj.getClass())
            return false;
        TestMetadata other = (TestMetadata) obj;
        return (id.equalsIgnoreCase(other.id));
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{id:");
        builder.append(id);
        builder.append(", name:");
        builder.append(name);
        builder.append(", qusCount:");
        builder.append(qusCount);
        builder.append(", details:");
        builder.append(details);
        builder.append(", totalMarks:");
        builder.append(totalMarks);
        builder.append(", children:");
        builder.append(children);
        builder.append(", qIds:");
        builder.append(qIds);
        builder.append(", marks:");
        builder.append(marks);
        builder.append("}");
        return builder.toString();
    }

    @Override
    public BoardAnalyticsInfo _getEntity() {

        return new BoardAnalyticsInfo(name, id, BoardType.COURSE);
    }

    @Override
    public List<? extends IAnalyticsBoardMember> _getChildrenBoards() {
        return null;
    }

    @Override
    public int _getTotalMarks() {

        return totalMarks;
    }

    @Override
    public int _getQusCount() {

        return qusCount;
    }
}
