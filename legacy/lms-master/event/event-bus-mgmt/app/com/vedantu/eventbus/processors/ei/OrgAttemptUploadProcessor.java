package com.vedantu.eventbus.processors.ei;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.vedantu.content.enums.AnswerCorrectness;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Logger.ALogger;
import play.Play;
import play.libs.F.Callback;
import play.libs.WS;
import play.libs.WS.Response;

import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateResults;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.AuthType;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.content.daos.analytics.UserEntityAnalyticsDAO;
import com.vedantu.content.daos.analytics.UserEntityAttemptDAO;
import com.vedantu.content.daos.analytics.UserQuestionAnalyticsDAO;
import com.vedantu.content.managers.AnalyticsManager;
import com.vedantu.content.models.analytics.AcademicDimensionType;
import com.vedantu.content.models.analytics.UserEntityAnalytics;
import com.vedantu.content.models.analytics.UserEntityAttempt;
import com.vedantu.content.models.analytics.UserQuestionAnalytics;
import com.vedantu.content.models.tests.AbstractTestCommonModel;
import com.vedantu.content.pojos.tests.Marks;
import com.vedantu.ei.commons.enums.VedantuErrorCode;
import com.vedantu.ei.requests.UploadTestAttemptsRequest;
import com.vedantu.ei.requests.pojos.Answer;
import com.vedantu.ei.requests.pojos.Attempt;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;
import com.vedantu.events.utils.EventUtil;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.MongoManager.SortOrder;
import com.vedantu.mongo.VedantuDBResult;
import com.vedantu.organization.daos.OrgMemberDAO;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.organization.daos.ei.OrgUploadAttemptDAO;
import com.vedantu.organization.daos.ei.OrgUploadAttemptRequestStatusDAO;
import com.vedantu.organization.enums.UploadState;
import com.vedantu.organization.event.ei.details.OrgAttemptUploadDetails;
import com.vedantu.organization.models.OrgMember;
import com.vedantu.organization.models.Organization;
import com.vedantu.organization.models.ei.OrgUploadAttempt;
import com.vedantu.organization.models.ei.OrgUploadAttemptRequestStatus;

public class OrgAttemptUploadProcessor implements IProcessor {

    private static final ALogger LOGGER                       = Logger.of(OrgAttemptUploadProcessor.class);
    private static final String  UPLOAD_ID                    = "uploadId";
    private static final String  TRY_COUNT                    = "tryCount";

    private static final int     UPLOAD_BATCH_SIZE            = Play.application()
                                                                      .configuration()
                                                                      .getInt("org.ei.upload.attempt.batch.size");

    private static final int     UPLOAD_REQ_TIMEOUT           = Play.application()
                                                                      .configuration()
                                                                      .getInt("org.ei.upload.attempt.request.timeout");
    private static final long    FAILED_UPLOAD_RETRY_INTERVAL = Play.application()
                                                                      .configuration()
                                                                      .getInt("org.ei.upload.attempt.retry.interval");

    private static final int     UPLOAD_RETRY_COUNT           = Play.application()
                                                                      .configuration()
                                                                      .getInt("org.ei.upload.attempt.retry.count");

    @Override
    public Status process(IConsumable consumable) {

        Event event = (Event) consumable;

        LOGGER.info("processing Event for " + event.getType() + " for userId :" + event.getUserId());

        OrgAttemptUploadDetails details = (OrgAttemptUploadDetails) event.fetchEventDetails();

        // fetch all OrgUploadAttempt which all not yet processed

        Organization org = OrganizationDAO.INSTANCE.getById(details.orgId);
        if (org == null || org.authType != AuthType.EXT_AUTH_ORG || org.endPoint == null
                || StringUtils.isEmpty(org.endPoint.getTestAttemptDataUploadEndpoint())) {
            return Status.NOT_CONSUMABLE;
        }

        DBObject query = new BasicDBObject(ConstantsGlobal.ORG_ID, details.orgId);
        if (details.processUploadState == null) {
            // select only those records which are not yet processed
            query.put(UPLOAD_ID, null);
            query.put(ConstantsGlobal.STATE, null);
        } else {
            // select only those records which are FAILED due to not successfully connecting to
            // institute server
            query.put(ConstantsGlobal.STATE, details.processUploadState.name());
            query.put("httpStatus", new BasicDBObject(MongoManager.NE_QUERY, HttpStatus.SC_OK));
            query.put(TRY_COUNT, new BasicDBObject("$lt", UPLOAD_RETRY_COUNT));
        }
        Status status = startUploadTestAttempts(org, query);
        return status;
    }

    private Status startUploadTestAttempts(Organization org, DBObject query) {

        // generate a upload id
        final String uploadId = UUID.randomUUID().toString();
        VedantuDBResult<OrgUploadAttempt> attempts = OrgUploadAttemptDAO.INSTANCE.getInfos(query,
                null, 0, UPLOAD_BATCH_SIZE,
                MongoManager.getSortQuery(ConstantsGlobal.TIME_CREATED, SortOrder.ASC.name()));

        // collect _id of above records so that we can update uploadId and status in these records
        Set<ObjectId> ids = new HashSet<ObjectId>();
        for (OrgUploadAttempt attempt : attempts.results) {
            attempt.uploadId = uploadId;
            attempt.state = UploadState.UPLOADING;
            attempt.tryCount++;
            ids.add(attempt.id);
        }

        if (CollectionUtils.isEmpty(ids)) {
            return Status.SUCCESS;
        }

        Query<OrgUploadAttempt> updateQuery = OrgUploadAttemptDAO.INSTANCE.createQuery();
        updateQuery.criteria(ConstantsGlobal._ID).in(ids);

        UpdateResults<OrgUploadAttempt> updateResult = OrgUploadAttemptDAO.INSTANCE.update(
                updateQuery,
                OrgUploadAttemptDAO.INSTANCE.createUpdateOperations().set(UPLOAD_ID, uploadId)
                        .set(ConstantsGlobal.STATE, UploadState.UPLOADING)
                        .set("uploadStartTime", System.currentTimeMillis()).inc(TRY_COUNT));
        if (updateResult.getHadError()) {
            LOGGER.error("updateResult.getHadError() : " + updateResult.getHadError()
                    + ", updated idsCount: " + updateResult.getUpdatedCount() + ", actualCount:"
                    + ids.size());
            return Status.FAILURE;
        }

        uploadTestAttemptsToOrgServer(attempts.results, uploadId, org);

        return Status.SUCCESS;
    }

    private void uploadTestAttemptsToOrgServer(final List<OrgUploadAttempt> attempts,
            final String uploadId, Organization org) {

        UploadTestAttemptsRequest uploadRequest = new UploadTestAttemptsRequest();
        uploadRequest.setUploadId(uploadId);
        for (OrgUploadAttempt attempt : attempts) {
            Attempt uploadReqAttempt = getUploadAttemptPOJO(attempt);
            if (uploadReqAttempt != null) {
                uploadRequest.addAttempt(uploadReqAttempt);
            }
        }

        final OrgUploadAttemptRequestStatus orgUploadAttemptRequestStatus = new OrgUploadAttemptRequestStatus();
        orgUploadAttemptRequestStatus.uploadId = uploadId;
        orgUploadAttemptRequestStatus.orgId = org._getStringId();
        orgUploadAttemptRequestStatus.request = new JSONObject(uploadRequest);
        WS.url(org.endPoint.getTestAttemptDataUploadEndpoint()).setFollowRedirects(true)
                .setTimeout(UPLOAD_REQ_TIMEOUT).setContentType("application/json")
                .post(uploadRequest.toJSONString()).onRedeem(new Callback<WS.Response>() {

                    @Override
                    public void invoke(Response response) throws Throwable {

                        // TODO: handle timeout event
                        JSONObject resObje = null;
                        orgUploadAttemptRequestStatus.responseCode = response.getStatus();
                        orgUploadAttemptRequestStatus.endTime = System.currentTimeMillis();
                        orgUploadAttemptRequestStatus.responseTime = (int) (orgUploadAttemptRequestStatus.endTime - orgUploadAttemptRequestStatus.timeCreated);

                        try {
                            resObje = new JSONObject(response.getBody());
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
                        }

                        orgUploadAttemptRequestStatus.response = resObje;
                        OrgUploadAttemptRequestStatusDAO.INSTANCE
                                .save(orgUploadAttemptRequestStatus);
                        processUploadAttemptResponse(response.getStatus(), resObje, attempts,
                                uploadId);
                    }
                });
    }

    private void processUploadAttemptResponse(int statusCode, JSONObject response,
            List<OrgUploadAttempt> attempts, String uploadId) {

        String errorCode = JSONUtils.getString(response, ConstantsGlobal.ERROR_CODE);
        Map<String, String> attemptIdToErrorCodeMap = new HashMap<String, String>();

        if (StringUtils.isNotEmpty(errorCode)
                && StringUtils.equals(errorCode, VedantuErrorCode.UPLOAD_FAILED)) {
            // some of attempts has failed

            JSONObject result = JSONUtils.getJSONObject(response, "result");
            JSONArray failedAttempts = JSONUtils.getJSONArray(result, "failedAttempts");
            for (int i = 0; i < failedAttempts.length(); i++) {
                try {
                    JSONObject failedAttempt = failedAttempts.getJSONObject(i);
                    attemptIdToErrorCodeMap.put(
                            JSONUtils.getString(failedAttempt, ConstantsGlobal.ATTEMPT_ID),
                            JSONUtils.getString(failedAttempt, ConstantsGlobal.ERROR_CODE));
                } catch (JSONException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }

        // update status SUCCESS or FAILED on OrgUploadAttempt collection

        // if any attempt upload is failed then we will generate another event to process failed
        // event after FAILED_UPLOAD_RETRY_INTERVAL
        boolean generatedFailedEvent = false;
        for (OrgUploadAttempt orgUploadAttempt : attempts) {
            String eCode = attemptIdToErrorCodeMap.get(orgUploadAttempt.attemptId);
            UploadState status = statusCode != HttpStatus.SC_OK || eCode == null
                    || response == null ? UploadState.FAILED : UploadState.UPLOADED;
            orgUploadAttempt.state = status;
            orgUploadAttempt.errorCode = eCode;
            orgUploadAttempt.httpStatus = statusCode;
            if (status == UploadState.FAILED) {
                orgUploadAttempt.addFailedUploadId(orgUploadAttempt.uploadId);
                if (!generatedFailedEvent && statusCode != HttpStatus.SC_OK) {
                    // generate another event to process failed event after
                    // FAILED_UPLOAD_RETRY_INTERVAL
                    OrgAttemptUploadDetails eventDetails = new OrgAttemptUploadDetails();
                    eventDetails.attemptId = orgUploadAttempt.attemptId;
                    eventDetails.orgId = orgUploadAttempt.orgId;
                    eventDetails.processUploadState = status;
                    eventDetails.userId = orgUploadAttempt.userId;
                    EventUtil.generateEvent(EventType.UPLOAD_ATTEMPT_TO_ORG, null,
                            orgUploadAttempt.userId, eventDetails, new SrcEntity(
                                    EntityType.ORGANIZATION, orgUploadAttempt.orgId), null,
                            System.currentTimeMillis() + FAILED_UPLOAD_RETRY_INTERVAL);
                    generatedFailedEvent = true;
                }
            }

            OrgUploadAttemptDAO.INSTANCE.save(orgUploadAttempt);
        }
    }

    private Attempt getUploadAttemptPOJO(OrgUploadAttempt attempt) {

        UserEntityAttempt userEntityAttempt = UserEntityAttemptDAO.INSTANCE
                .getById(attempt.attemptId);

        OrgMember orgMember = OrgMemberDAO.INSTANCE
                .getMemberByUserId(attempt.orgId, attempt.userId);

        UserEntityAnalytics userEntityAnalytics = UserEntityAnalyticsDAO.INSTANCE.getAnalytics(
                attempt.userId, attempt.attemptId, attempt.entity, AcademicDimensionType.OVERALL,
                AcademicDimensionType.OVERALL.name());

        AbstractTestCommonModel test = null;
        try {
            test = (AbstractTestCommonModel) AnalyticsManager.getAttemptedEntity(attempt.entity);
        } catch (VedantuException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }

        Map<String, Marks> qIdToMarksMap = test.__getMarksMap();

        if (userEntityAttempt == null || orgMember == null || userEntityAnalytics == null) {
            LOGGER.error("no userEntityAttempt or userEntityAnalytics or orgMember for " + attempt);
            return null;
        }

        Attempt uploadUserAttemptReq = new Attempt();
        uploadUserAttemptReq.setAttemptId(attempt.attemptId);

        // fetch UserEntityAattempt and user UserQuestionAttempt

        uploadUserAttemptReq.setAttemptStartTime(userEntityAttempt.timeCreated);
        uploadUserAttemptReq.setAttemptEndTime(userEntityAttempt.endTime);
        uploadUserAttemptReq.setUserId(orgMember.extUserId);
        uploadUserAttemptReq.setCode(test.code);
        uploadUserAttemptReq.setUserScore((float) userEntityAnalytics.measures.score);
        uploadUserAttemptReq.setMaxScore(test.totalMarks);
        // fetch user answers
        DBObject query = new BasicDBObject();
        query.put(ConstantsGlobal.USER_ID, attempt.userId);
        query.put(ConstantsGlobal.ATTEMPT_ID, attempt.attemptId);
        query.put("parentEntity.id", attempt.entity.id);
        query.put("parentEntity.type", attempt.entity.type.name());

        VedantuDBResult<UserQuestionAnalytics> uerQuestionsAnalytics = UserQuestionAnalyticsDAO.INSTANCE
                .getInfos(query, null, MongoManager.NO_START, MongoManager.NO_LIMIT, null);
        Map<String, UserQuestionAnalytics> qIdToQuestionAnalyticsMap = new HashMap<String, UserQuestionAnalytics>();
        for (UserQuestionAnalytics uQuestionAnalytics : uerQuestionsAnalytics.results) {
            qIdToQuestionAnalyticsMap.put(uQuestionAnalytics.qId, uQuestionAnalytics);
        }

        for (int i = 0; i < userEntityAttempt.qIds.size(); i++) {
            String qId = userEntityAttempt.qIds.get(i);
            UserQuestionAnalytics qAnalytics = qIdToQuestionAnalyticsMap.get(qId);
            Answer ansReq = new Answer();

            uploadUserAttemptReq.addAnswer(ansReq);

            ansReq.setQuestionNumber(i);
            ansReq.setAttempted(qAnalytics != null);
            ansReq.setCorrect(qAnalytics != null && qAnalytics.isCorrect == AnswerCorrectness.CORRECT);
            ansReq.setMaxScore(qIdToMarksMap.get(qId).positive);

            if (qAnalytics == null) {
                continue;
            }

            ansReq.setTimeTaken(qAnalytics.timeTaken);
            ansReq.setUserAnswer(StringUtils.join(qAnalytics.answerGiven, ","));
            ansReq.setUserScore((float) qAnalytics.score);
        }

        return uploadUserAttemptReq;
    }
}
