package com.lms.components;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.managers.AbstractTestManager;
import com.lms.models.tests.Assignment;
import com.lms.pojos.requests.tests.GetAssignmentInfoReq;
import com.lms.pojos.requests.tests.GetAssignmentsReq;
import com.lms.pojos.requests.tests.GetTestDetailsReq;
import com.lms.pojos.responce.SearchListResponse;
import com.lms.pojos.responce.tests.GetAssignmentInfoRes;
import com.lms.pojos.responce.tests.GetAssignmentQuestionsRes;
import com.lms.repository.AssignmentRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AssignmentComponent extends AbstractTestManager {
    private static final Logger logger = LoggerFactory.getLogger(AssignmentComponent.class);

    @Autowired
    private AssignmentRepo assignmentRepo;

    public GetAssignmentInfoRes getAssignmentInfo(GetAssignmentInfoReq getAssignmentInfoReq) {
        Assignment assignment = getAssignment(getAssignmentInfoReq.id);
        if (assignment.recordState == VedantuRecordState.TEMPORARY) {
            throw new VedantuException(VedantuErrorCode.CONTENT_IN_TEMPORARY_STAGE);
        }
        GetAssignmentInfoRes getAssignmentRes = new GetAssignmentInfoRes();
        getAssignmentRes.fromMongoModel(assignment);

        getAssignmentRes = (GetAssignmentInfoRes) annotateExtraInfo(
                getAssignmentInfoReq.userId,
                getAssignmentRes.contentSrc != null
                        && getAssignmentRes.contentSrc.type == EntityType.ORGANIZATION ? getAssignmentRes.contentSrc.id
                        : null, EntityType.ASSIGNMENT, getAssignmentRes);
        return getAssignmentRes;
    }

    private Assignment getAssignment(String id) {
        Optional<Assignment> assignmentOptional = assignmentRepo.findById(id);
        if (!assignmentOptional.isPresent()) {
            logger.error("no assignment found with id: " + id);
            throw new VedantuException(VedantuErrorCode.ASSIGNMENT_NOT_FOUND,
                    "no assignment found with id: " + id);
        }

        return assignmentOptional.get();
    }

    public GetAssignmentQuestionsRes getAssignmentQuestions(GetTestDetailsReq req) {
        Assignment assignment = getAssignment(req.id);
        if (assignment.recordState == VedantuRecordState.TEMPORARY) {
            throw new VedantuException(VedantuErrorCode.CONTENT_IN_TEMPORARY_STAGE);
        }
        if (assignment.qusCount != assignment.__getAllQIds().size()) {
            logger.error("details of an incomplete assignment[" + req.id + "] can not be fetched");
            throw new VedantuException(VedantuErrorCode.INCOMPLETE_ASSIGNMENT);
        }
        GetAssignmentQuestionsRes res = new GetAssignmentQuestionsRes(assignment._getStringId(),
                assignment.recordState, assignment.name, assignment.duration, assignment.type,
                assignment.code);

        res.boards = getTestBoardWiseQuestions(assignment, req);

        return res;
    }

    public SearchListResponse<GetAssignmentInfoRes> getAssignments(GetAssignmentsReq req) {
        SearchListResponse<GetAssignmentInfoRes> results = getEntityInfos(req,
                EntityType.ASSIGNMENT, GetAssignmentInfoRes.class, null);
        annotateExtraInfo(req.userId, req.orgId, EntityType.ASSIGNMENT, results.list);
        return results;
    }

}
