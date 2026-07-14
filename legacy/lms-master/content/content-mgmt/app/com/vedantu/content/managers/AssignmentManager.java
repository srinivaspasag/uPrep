package com.vedantu.content.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.pojos.DownloadableFileInfo;
import com.vedantu.commons.pojos.responses.SearchListResponse;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.content.daos.AssignmentDAO;
import com.vedantu.content.daos.LibraryContentLinksDAO;
import com.vedantu.content.daos.QuestionDAO;
import com.vedantu.content.models.AbstractContentModel;
import com.vedantu.content.models.Question;
import com.vedantu.content.models.tests.AbstractTestCommonModel;
import com.vedantu.content.models.tests.Assignment;
import com.vedantu.content.pojos.requests.EditContentReq;
import com.vedantu.content.pojos.requests.tests.GetAssignmentDetailsReq;
import com.vedantu.content.pojos.requests.tests.GetAssignmentInfoReq;
import com.vedantu.content.pojos.requests.tests.GetAssignmentsReq;
import com.vedantu.content.pojos.requests.tests.GetTestDetailsReq;
import com.vedantu.content.pojos.responses.tests.GetAssignmentInfoRes;
import com.vedantu.content.pojos.responses.tests.GetAssignmentQuestionsRes;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuRecordState;

public class AssignmentManager extends AbstractTestManager {

    private static final ALogger    LOGGER   = Logger.of(AssignmentManager.class);

    public static AssignmentManager INSTANCE = new AssignmentManager();

    public static GetAssignmentInfoRes getAssignmentInfo(GetAssignmentInfoReq getAssignmentReq)
            throws VedantuException {

        Assignment assignment = AssignmentDAO.INSTANCE.getAssignment(getAssignmentReq.id);
        if (assignment.recordState == VedantuRecordState.TEMPORARY) {
            throw new VedantuException(VedantuErrorCode.CONTENT_IN_TEMPORARY_STAGE);
        }
        GetAssignmentInfoRes getAssignmentRes = new GetAssignmentInfoRes();
        getAssignmentRes.fromMongoModel(assignment);

        getAssignmentRes = (GetAssignmentInfoRes) annotateExtraInfo(
                getAssignmentReq.userId,
                getAssignmentRes.contentSrc != null
                        && getAssignmentRes.contentSrc.type == EntityType.ORGANIZATION ? getAssignmentRes.contentSrc.id
                        : null, EntityType.ASSIGNMENT, getAssignmentRes);
        return getAssignmentRes;
    }

    public static GetAssignmentQuestionsRes getAssignmentQuestions(GetTestDetailsReq req)
            throws VedantuException {

        Assignment assignment = AssignmentDAO.INSTANCE.getAssignment(req.id);
        if (assignment.recordState == VedantuRecordState.TEMPORARY) {
            throw new VedantuException(VedantuErrorCode.CONTENT_IN_TEMPORARY_STAGE);
        }
        if (assignment.qusCount != assignment.__getAllQIds().size()) {
            LOGGER.error("details of an incomplete assignment[" + req.id + "] can not be fetched");
            throw new VedantuException(VedantuErrorCode.INCOMPLETE_ASSIGNMENT);
        }
        GetAssignmentQuestionsRes res = new GetAssignmentQuestionsRes(assignment._getStringId(),
                assignment.recordState, assignment.name, assignment.duration, assignment.type,
                assignment.code);

        res.boards = getTestBoardWiseQuestions(assignment, req);

        return res;
    }

    public static SearchListResponse<GetAssignmentInfoRes> getAssignments(GetAssignmentsReq req)
            throws VedantuException {

        SearchListResponse<GetAssignmentInfoRes> results = getEntityInfos(req,
                EntityType.ASSIGNMENT, GetAssignmentInfoRes.class, null);
        annotateExtraInfo(req.userId, req.orgId, EntityType.ASSIGNMENT, results.list);
        return results;
    }

    @Override
    public boolean update(EditContentReq request) throws VedantuException {

        Assignment content = AssignmentDAO.INSTANCE.getById(request.entity.id);

        List<String> updateList = new ArrayList<String>();
        if (StringUtils.isNotEmpty(request.name)
                && request.updateList.contains(EditContentReq.NAME)) {
            content.name = request.name;
            updateList.add(AbstractContentModel.NAME);
        }

        if (request.updateList.contains(EditContentReq.DESCRIPTION)) {
            content.desc = request.description;
            updateList.add(AbstractTestCommonModel.DESC);

        }

        AssignmentDAO.INSTANCE.updateModel(content, updateList);
        LibraryContentLinksDAO.INSTANCE.updateLastUpdated(request.entity);
        generateEventAysc(request.userId, content, EventActionType.UPDATE,
                EventType.INDEX_ASSIGNMENT, UserActionType.UPDATED, false);

        return true;
    }

    @Override
    public boolean calculate(String id,boolean recalculate, VedantuBaseMongoModel... contents) throws VedantuException {

        List<Assignment> assignments = new ArrayList<Assignment>();

        if (StringUtils.isNotEmpty(id)) {
            Assignment question = AssignmentDAO.INSTANCE.getById(id);

            if (question == null) {
                return false;
            }
            assignments.add(question);
        }

        if (contents != null && contents.length != 0) {
            for (VedantuBaseMongoModel content : contents) {
                if (content instanceof Assignment) {
                    assignments.add((Assignment) content);
                }
            }
        }

        // calculate question image size;

        for (Assignment assignment : assignments) {
            if( assignment.size.isFinalized() && !recalculate){
                continue;
            }
            assignment.size.reset();
            LOGGER.debug(" question ids" + assignment.__getAllQIds());
            List<Question> questions = QuestionDAO.INSTANCE.getByIds(ObjectIdUtils
                    .toObjectIds(assignment.__getAllQIds()));
            if (CollectionUtils.isEmpty(questions)) {
                continue;
            }
            QuestionManager questionManager = new QuestionManager();
            for (com.vedantu.content.models.Question question : questions) {
                if (!question.size.isFinalized()) {
                    questionManager.calculate(null,true, question);
                }
                assignment.size.add(question.size);

            }
            assignment.size.finalize();
            AssignmentDAO.INSTANCE.updateModel(assignment, Arrays.asList(Assignment.SIZE));

        }
        return true;
    }

    @Override
    public List<DownloadableFileInfo> getFiles(EntityType entityType, String entityId) throws VedantuException, EntityFileStorageException {

        List<DownloadableFileInfo> fileInfos = new ArrayList<DownloadableFileInfo>();
        Assignment assignment = AssignmentDAO.INSTANCE.getById(entityId);
        List<String> qids = assignment.__getAllQIds();

        QuestionManager questionManager = new QuestionManager();
        if (CollectionUtils.isNotEmpty(qids)) {
            for (String qId : qids) {
                List<DownloadableFileInfo> questionFiles = questionManager.getFiles(EntityType.QUESTION, qId);
                fileInfos.addAll(questionFiles);
            }
        }
        return fileInfos;
    }

}
