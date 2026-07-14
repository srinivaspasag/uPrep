package com.vedantu.cmds.daos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.vedantu.cmds.mgmt.interfaces.ICMDSResource;
import com.vedantu.cmds.models.CMDSAssignment;
import com.vedantu.cmds.models.CMDSContentLink;
import com.vedantu.cmds.models.CMDSQuestion;
import com.vedantu.cmds.models.CMDSQuestionSet;
import com.vedantu.cmds.models.CMDSTest;
import com.vedantu.cmds.models.event.search.details.CMDSResourceDetails;
import com.vedantu.cmds.pojos.content.question.CMDSQuestionInfo;
import com.vedantu.cmds.pojos.content.question.SolutionFormat;
import com.vedantu.cmds.pojos.content.solution.metadata.MCQsolutionInfo;
import com.vedantu.cmds.pojos.content.solution.metadata.NumericSolutionInfo;
import com.vedantu.cmds.pojos.content.solution.metadata.SCQSolutionInfo;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.content.interfaces.IPublishable;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.content.daos.QuestionDAO;
import com.vedantu.content.enums.Difficulty;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.pojos.Attachment;
import com.vedantu.content.pojos.SrcEntityPublishableState;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuRecordState;

public class CMDSQuestionDAO extends CmdsContentDAO<CMDSQuestion, ObjectId> implements
        IPublishable, ICMDSResource {

    private static final ALogger        LOGGER   = Logger.of(CMDSQuestionDAO.class);

    public static final CMDSQuestionDAO INSTANCE = new CMDSQuestionDAO();

    private CMDSQuestionDAO() {

        super(CMDSQuestion.class);
    }

    public CMDSQuestion addQuestion(CMDSQuestion question) throws Exception, VedantuException {

        save(question);
        return question;
    }

    public Query<CMDSQuestion> getQuery(Query<CMDSQuestion> query, String... fields) {

        return query.retrievedFields(true, fields);

    }

    @SuppressWarnings("unchecked")
    @Override
    public ModelBasicInfo getBasicInfo(String id) {

        return getBasicInfo(id, VedantuRecordState.ACTIVE);
    }

    public ModelBasicInfo getBasicInfo(String id, VedantuRecordState state) {

        LOGGER.debug("Creating basic info for question Id : " + id);

        CMDSQuestion question = CMDSQuestionDAO.INSTANCE.getById(id, state);
        if (question == null) {
            LOGGER.debug(" No question found for id " + id + "  " + state);
            return null;
        }
        CMDSQuestionInfo info = (CMDSQuestionInfo) question.toBasicInfo();
        LOGGER.debug("Created basic info for question  : " + info);
        return info;
    }

    @Override
    public SrcEntity getGlobalEntity(String id) {

        CMDSQuestion question = CMDSQuestionDAO.INSTANCE.getById(id);
        return new SrcEntity(EntityType.QUESTION, question.globalQid);
    }

    @Override
    public VedantuBaseMongoModel getPublishedEntity(String id) throws VedantuException {

        CMDSQuestion question = CMDSQuestionDAO.INSTANCE.getById(id);
        return QuestionDAO.INSTANCE.getQuestion(question.globalQid);

    }

    public List<SrcEntityPublishableState> getAssociatedContent(String globalQid)
    {
        LOGGER.info(".............Entered getAssociatedContent DAO function..............");
        List<SrcEntityPublishableState> entitiesPublishableState = new ArrayList<SrcEntityPublishableState>();

        List<CMDSTest> tests = ds.find(CMDSTest.class).field("metadata.details.qIds").equal(globalQid).asList();
        for(CMDSTest test: tests)
        {
            SrcEntity entity = new SrcEntity();
            entity.id = test._getStringId();
            entity.type = EntityType.CMDSTEST;

            SrcEntityPublishableState entityPublishableState = new SrcEntityPublishableState();
            entityPublishableState.entity = entity;
            entityPublishableState.name = test.name;
            entityPublishableState.published = test.published;

            entitiesPublishableState.add(entityPublishableState);
        }

        List<CMDSAssignment> assignments = ds.find(CMDSAssignment.class).field("metadata.qIds").equal(globalQid).asList();
        for(CMDSAssignment assignment: assignments)
        {
            SrcEntity entity = new SrcEntity();
            entity.id = assignment._getStringId();
            entity.type = EntityType.CMDSASSIGNMENT;

            SrcEntityPublishableState entityPublishableState = new SrcEntityPublishableState();
            entityPublishableState.entity = entity;
            entityPublishableState.name = assignment.name;
            entityPublishableState.published = assignment.published;

            entitiesPublishableState.add(entityPublishableState);
        }
        LOGGER.info(".............Exited getAssociatedContent DAO function..............");
        return entitiesPublishableState;
    }

    public List<String> getFolderIds(String globalQid)
    {
        LOGGER.info(".............Entered getFolderIds DAO function..............");
        List<String> folderIds = new ArrayList<String>();


        List<CMDSContentLink> cmdsContentLinks = ds.find(CMDSContentLink.class).
                field("source.id").equal(globalQid).field("target.type").equal(EntityType.FOLDER).asList();
        for(CMDSContentLink cmdsContentLink: cmdsContentLinks)
        {
            folderIds.add(cmdsContentLink.target.id);
        }

        return folderIds;
    }

    public CMDSQuestion getQuestionById(String questionId) throws VedantuException {

        CMDSQuestion question = getById(questionId);
        if (null == question) {
            LOGGER.error("cannot find cmds question for _id: " + questionId);
            throw new VedantuException(VedantuErrorCode.CMDS_QUESTION_NOT_FOUND);
        }

        return question;
    }

    @Override
    public boolean isPublished(String id) {

        CMDSQuestion question = CMDSQuestionDAO.INSTANCE.getById(id);
        return question != null && question.published;
    }

    @Override
    public List<SrcEntity> getChildren(String id) {

        LOGGER.debug("..... inside function getPublishable children.....");
        CMDSQuestion question = getById(id);
        List<SrcEntity> childEntities = new ArrayList<SrcEntity>();
        if (question.solutionInfo != null
                && CollectionUtils.isNotEmpty(question.solutionInfo.solutions)) {
            for (SolutionFormat solution : question.solutionInfo.solutions) {
                if (CollectionUtils.isNotEmpty(solution.attachments)) {
                    for (Attachment attachment : solution.attachments) {
                        if (attachment != null && attachment.entity != null) {
                            childEntities.add(attachment.entity);
                        }
                    }
                }
            }
        }
        return childEntities;
    }

    @Override
    public boolean isReadyToPublished(String id) throws VedantuException {

        CMDSQuestion question = CMDSQuestionDAO.INSTANCE.getById(id);
        return question.completed;
    }

    @Override
    public boolean isReadyToPublished(VedantuBaseMongoModel cmdsModel) throws VedantuException {

        if (cmdsModel instanceof CMDSQuestion) {

            CMDSQuestion question = (CMDSQuestion) cmdsModel;

            boolean canBePublished = true;

            if (canBePublished && (question.recordState != VedantuRecordState.ACTIVE)) {
                canBePublished &= false;
                LOGGER.debug(" Question is not in active state id:" + question._getStringId());
            }

            if (canBePublished) {
                if (question.type == QuestionType.SCQ) {
                    SCQSolutionInfo sCQSolutionInfo = (SCQSolutionInfo) question.solutionInfo;
                    if (StringUtils.isEmpty(sCQSolutionInfo.answer)) {
                        LOGGER.debug(" Answer is not provided for SCQ id:"
                                + question._getStringId());
                    }
                }

                if (question.type == QuestionType.MCQ || question.type == QuestionType.PARA || question.type == QuestionType.MATRIX) {
                    MCQsolutionInfo mCQsolutionInfo = (MCQsolutionInfo) question.solutionInfo;
                    if (CollectionUtils.isEmpty(mCQsolutionInfo.answer)) {
                        LOGGER.debug(" Answer is not provided for MCQ/PARA id:"
                                + question._getStringId());
                    }
                }

                if (question.type == QuestionType.NUMERIC) {
                    NumericSolutionInfo numericSolutionInfo = (NumericSolutionInfo) question.solutionInfo;
                    if (StringUtils.isEmpty(numericSolutionInfo.answer)) {
                        LOGGER.debug(" Answer is not provided for Numeric id:"
                                + question._getStringId());
                    }
                }
            }

            if (canBePublished
                    && (question.questionBody == null || StringUtils
                            .isEmpty(question.questionBody.newText))) {
                canBePublished &= false;
                LOGGER.debug(" Invalid Question Body id:" + question._getStringId());
            }

            if (canBePublished && (CollectionUtils.isEmpty(question.boardIds))) {
                canBePublished &= false;
                LOGGER.debug(" Question is added but no boards id:" + question._getStringId());
            }

            if (canBePublished && (question.type == null || question.type == QuestionType.UNKNOWN)) {
                canBePublished &= false;
                LOGGER.debug(" Invalid Question Type id:" + question._getStringId());
            }

            if (canBePublished
                    && (question.difficulty == null || question.difficulty == Difficulty.UNKNOWN)) {
                canBePublished &= false;
                LOGGER.debug(" Invalid Question Difficulty id:" + question._getStringId());
            }

            return canBePublished;
        }
        return false;
    }

    @Override
    public CMDSResourceDetails getCMDSResourceDetails(VedantuBaseMongoModel model) {

        // TODO Auto-generated method stub
        CMDSResourceDetails details = new CMDSResourceDetails();
        details.fromMongoModel(model);
        CMDSQuestion question = (CMDSQuestion) model;

        details.content = new SrcEntity(EntityType.CMDSQUESTION, question._getStringId());
        details.queryContext = question.questionBody.newText;
        return details;
    }

    @Override
    public boolean isMovingAllowed(String id) throws VedantuException {

        CMDSQuestion question = getById(id);
        if (null == question) {
            LOGGER.error("cannot find cmds question for _id: " + id);
            throw new VedantuException(VedantuErrorCode.CMDS_QUESTION_NOT_FOUND);
        }

        return StringUtils.isEmpty(question.questionSetId);
    }

    @Override
    public boolean isPublished(VedantuBaseMongoModel cmdsModel) {

        if (cmdsModel instanceof CMDSQuestion) {
            CMDSQuestion question = (CMDSQuestion) cmdsModel;
            return question.published;
        }

        return false;
    }

    @Override
    public boolean deleteByModel(VedantuBaseMongoModel model) throws VedantuException {

        if (!(model instanceof CMDSQuestion)) {
            return false;

        }

        CMDSQuestion question = (CMDSQuestion) model;

//        if (question.published == true || question.globalQid != null) {
//            throw new VedantuException(VedantuErrorCode.ALREADY_PUBLISHED);
//        }

        MutableLong testHits = new MutableLong(0);
        MutableLong assignmentHits = new MutableLong(0);
        MutableLong questionSetHits = new MutableLong(0);

        List<CMDSQuestionSet> questionSets = CMDSQuestionSetDAO.INSTANCE.getContainers(
                model._getStringId(), 0, 0, VedantuRecordState.ACTIVE, questionSetHits);
        List<CMDSTest> tests = CMDSTestDAO.INSTANCE.getContainers(model._getStringId(), 0, 0,
                VedantuRecordState.ACTIVE, testHits);
        List<CMDSAssignment> assignments = CMDSAssignmentDAO.INSTANCE.getContainers(
                model._getStringId(), 0, 0, VedantuRecordState.ACTIVE, assignmentHits);

//        if (CollectionUtils.isNotEmpty(questionSets) || CollectionUtils.isNotEmpty(tests)
//                || CollectionUtils.isNotEmpty(assignments)) {
//            LOGGER.debug("part of containers ");
//            throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_REMOVED);
//        }

        super.markDeleted(question);
        updateModel(question, Arrays.asList(ConstantsGlobal.RECORD_STATE));
        return true;

    }

    public long countByBoard(String orgId,String sharedToOrgId, List<String> boardIds,List<String> types){
        Query<CMDSQuestion> query = getQuery();
        query.filter("contentSrc.type", EntityType.ORGANIZATION);
        query.filter("contentSrc.id", orgId);
        query.filter("recordState", VedantuRecordState.ACTIVE);
        query.field("boardIds").in(boardIds);
        query.field("type").in(types);
        List<String> sharedOrgIds = new ArrayList<String>();
        sharedOrgIds.add(sharedToOrgId);
        query.field("sharedToOrgIds").notIn(sharedOrgIds);
        query.disableValidation();
        return query.countAll();
    }

    public List<CMDSQuestion> getQuestionsByBoard(String orgId,String sharedToOrgId,List<String> boardIds,List<String> types, int start, int size){
        Query<CMDSQuestion> query = getQuery();
        query.filter("contentSrc.type", EntityType.ORGANIZATION);
        query.filter("contentSrc.id", orgId);
        query.filter("recordState", VedantuRecordState.ACTIVE);
        query.field("boardIds").in(boardIds);
        List<String> sharedOrgIds = new ArrayList<String>();
        sharedOrgIds.add(sharedToOrgId);
        query.field("sharedToOrgIds").notIn(sharedOrgIds);
        query.field("type").in(types);
        query.limit(size);
        query.disableValidation();
        return query.asList();
    }

    public List<CMDSQuestion> getQuestionsByOrgIdAndScope(String orgId,String scope, String cmdsQId){
        Query<CMDSQuestion> query = getQuery();
        if(!StringUtils.isEmpty(cmdsQId)){
            query.filter("_id", new ObjectId(cmdsQId));
        }
        query.filter("contentSrc.type", EntityType.ORGANIZATION);
        query.filter("contentSrc.id", orgId);
        query.filter("recordState", VedantuRecordState.ACTIVE);
        query.filter("scope",scope);
        query.disableValidation();
        return query.asList();
    }
}
