package com.vedantu.cmds.mgmt.publishers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vedantu.cmds.managers.AbstractCMDSContentManager;
import com.vedantu.cmds.models.CMDSQuestion;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.daos.analytics.QuestionAnalyticsDAO;
import com.vedantu.content.models.analytics.QuestionMeasures;
import com.vedantu.content.models.tests.AbstractTestCommonModel;
import com.vedantu.content.pojos.tests.BoardQus;
import com.vedantu.content.pojos.tests.Marks;
import com.vedantu.content.pojos.tests.TestDetails;
import com.vedantu.content.pojos.tests.TestMetadata;

public class AbstractTestPublisher extends AbstractCMDSContentManager {

    protected List<TestMetadata> createGlobalMetadata(AbstractTestCommonModel cmdsTest,
            Map<String, CMDSQuestion> cmdsQuestionMap) {

        List<TestMetadata> gMetadatas = new ArrayList<TestMetadata>();
        for (TestMetadata cMdata : cmdsTest.metadata) {
            TestMetadata gMdata = new TestMetadata(cMdata.id, cMdata.name, cMdata.qusCount);
            gMdata.totalMarks = cMdata.totalMarks;
            gMdata.maxQuestionsToBeAttemptedForBoard=cMdata.maxQuestionsToBeAttemptedForBoard;
            List<String> gQIds = new ArrayList<String>();
            Map<String, Marks> gMarks = new HashMap<String, Marks>();
            for (String cqId : cMdata.qIds) {
                String gQId = cmdsQuestionMap.get(cqId).globalQid;
                gQIds.add(gQId);
                if (gMarks.get(cqId) != null) {
                    gMarks.put(gQId, gMarks.get(cqId));
                }
            }
            gMdata.qIds = gQIds;
            gMdata.marks = gMarks;
            if (cMdata.details != null) {
                for (TestDetails cDetails : cMdata.details) {
                    TestDetails gDetails = new TestDetails(cDetails.type, cDetails.qusCount,
                            cDetails.marks != null ? cDetails.marks.positive : 0,
                            cDetails.marks != null ? cDetails.marks.negative : 0);
                    List<String> gDetailQIds = new ArrayList<String>();
                    if (cDetails.qIds != null) {
                        for (String qId : cDetails.qIds) {
                            gDetailQIds.add(cmdsQuestionMap.get(qId).globalQid);
                        }
                    }
                    gDetails.maxQuestionsTobeAttempted=cDetails.maxQuestionsTobeAttempted;
                    gDetails.qIds = gDetailQIds;
                    gMdata.addDetails(gDetails);
                }
            }

            if (cMdata.children != null) {
                for (BoardQus cBoard : cMdata.children) {
                    BoardQus gBoard = new BoardQus(cBoard.id, cBoard.name, cBoard.qusCount);
                    List<String> gBrdQids = new ArrayList<String>();
                    if (cBoard.qIds != null) {
                        for (String qId : cBoard.qIds) {
                            gBrdQids.add(cmdsQuestionMap.get(qId).globalQid);
                        }
                    }
                    gBoard.qIds = gBrdQids;
                    gMdata.addChild(gBoard);
                }
            }
            gMetadatas.add(gMdata);
        }
        return gMetadatas;
    }

    protected void generateEntityQuestionAnalytics(AbstractTestCommonModel test,
            EntityType entityType) {

        for (TestMetadata mdata : test.metadata) {
            if (mdata.qIds != null) {
                for (String qId : mdata.qIds) {
                    QuestionAnalyticsDAO.INSTANCE.addAnalytics(qId,
                            new SrcEntity(entityType, test._getStringId()), new QuestionMeasures(),
                            null);
                }
            }
        }
    }
}
