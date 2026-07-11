package com.vedantu.cmds.mgmt.publishers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.cmds.daos.CMDSAssignmentDAO;
import com.vedantu.cmds.daos.CMDSQuestionDAO;
import com.vedantu.cmds.models.CMDSAssignment;
import com.vedantu.cmds.models.CMDSQuestion;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.content.daos.AssignmentDAO;
import com.vedantu.content.daos.QuestionDAO;
import com.vedantu.content.models.tests.Assignment;
import com.vedantu.content.pojos.tests.TestQuestionSet;
import com.vedantu.content.search.details.AssignmentSearchIndexDetails;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuBaseMongoModel;

public class AssignmentPublisher extends AbstractTestPublisher {

    private static final ALogger            LOGGER   = Logger.of(AssignmentPublisher.class);

    public static final AssignmentPublisher INSTANCE = new AssignmentPublisher();

    private AssignmentPublisher() {

        super();
    }

    @Override
    public void prePublish(SrcEntity content) {

    }

    @Override
    protected VedantuBaseMongoModel publish(String userId, String orgId, SrcEntity content)
            throws VedantuException {

        CMDSAssignment cmdsAssignment = CMDSAssignmentDAO.INSTANCE.getAssignment(content.id);
        // if (cmdsAssignment.published || StringUtils.isNotEmpty(cmdsAssignment.globalId)) {
        //     LOGGER.error("cmdsAssignment[" + cmdsAssignment._getStringId() + "] already published");
        //     throw new VedantuException(VedantuErrorCode.ALREADY_PUBLISHED);
        // }

        cmdsAssignment.computeTotalQusAndMarks();
        List<String> cmdsQIds = new ArrayList<String>(cmdsAssignment.__getAllQIds());
        String errorMsg = null;
        Map<String, CMDSQuestion> cmdsQuestionMap = CMDSQuestionDAO.INSTANCE
                .toInfosMap(CMDSQuestionDAO.INSTANCE.getByIds(ObjectIdUtils.toObjectIds(cmdsQIds)));
        if (cmdsQIds.size() != cmdsQuestionMap.size()) {
            errorMsg = "all question are not valid for cmdsassignment : " + cmdsAssignment
                    + ", totalQuestions: " + cmdsQIds.size() + ", avalibaleQuestions: "
                    + cmdsQuestionMap.size();
            LOGGER.error(errorMsg);
            throw new VedantuException(VedantuErrorCode.CMDS_QUESTION_NOT_FOUND, errorMsg);
        }

        // check if all questions are published
        DBObject query = new BasicDBObject("cmdsQId", new BasicDBObject(MongoManager.IN_QUERY,
                cmdsQIds.toArray()));
        long publishedQuestionCount = QuestionDAO.INSTANCE.count(query);
        if (publishedQuestionCount < cmdsQIds.size()) {
            errorMsg = "all questions of the assignment are not published [publishedCount: "
                    + publishedQuestionCount + ", actualQidsCount" + cmdsQIds.size() + "]";
            throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED, errorMsg);
        }
        Assignment assignment;
        if(StringUtils.isNotEmpty(cmdsAssignment.globalId)){
            assignment = AssignmentDAO.INSTANCE.getAssignment(cmdsAssignment.globalId);
        }else{

            assignment = new Assignment(cmdsAssignment.userId, cmdsAssignment.name,
            cmdsAssignment.desc, cmdsAssignment.qusCount, cmdsAssignment.duration,
            cmdsAssignment.totalMarks, null, cmdsAssignment.type, cmdsAssignment.mode,
            cmdsAssignment.code, cmdsAssignment.scope, cmdsAssignment.resultVisibility);
        }
        assignment.resultVisibilityMessage = cmdsAssignment.resultVisibilityMessage;
        assignment.metadata = createGlobalMetadata(cmdsAssignment, cmdsQuestionMap);
        assignment.computeTotalQusAndMarks();
        // no sets
        assignment.sets = new ArrayList<TestQuestionSet>();

        assignment.cmdsId = cmdsAssignment._getStringId();
        assignment.published = true;
        assignment.contentSrc = cmdsAssignment.contentSrc;
        assignment.difficulty = cmdsAssignment.difficulty;
        assignment.boardIds = cmdsAssignment.boardIds;
        assignment.tags = cmdsAssignment.tags;
        assignment.type = cmdsAssignment.type;
        assignment.mode = cmdsAssignment.mode;
        assignment.targetIds = cmdsAssignment.targetIds;
        assignment.size = cmdsAssignment.size;
        AssignmentDAO.INSTANCE.save(assignment);

        cmdsAssignment.globalId = assignment._getStringId();
        cmdsAssignment.published = true;
        cmdsAssignment.publishingInProgress = false;
        CMDSAssignmentDAO.INSTANCE.save(cmdsAssignment);

        // live add global test to search index
        AssignmentSearchIndexDetails details = new AssignmentSearchIndexDetails();
        details.fromMongoModel(assignment);
        addLiveEntityToSearchIndex(details, EntityType.ASSIGNMENT, true);

        // it is already added to index, right we just need to update the index
        generateEventAysc(userId, cmdsAssignment, EventActionType.UPDATE,
                EventType.INDEX_CMDS_ASSIGNMENT, UserActionType.UPDATED, false);
        generateEntityQuestionAnalytics(assignment, EntityType.ASSIGNMENT);
        return cmdsAssignment;
    }

    public void postPublish(VedantuBaseMongoModel mongoModel) {

    }

}
