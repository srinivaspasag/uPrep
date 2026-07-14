package com.vedantu.eventbus.processors;

import java.util.List;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.daos.TestDAO;
import com.vedantu.content.daos.analytics.UserEntityAnalyticsDAO;
import com.vedantu.content.daos.analytics.UserEntityAttemptDAO;
import com.vedantu.content.event.details.EndTestDetails;
import com.vedantu.content.managers.AnalyticsManager;
import com.vedantu.content.models.analytics.AcademicDimensionType;
import com.vedantu.content.models.analytics.UserEntityAnalytics;
import com.vedantu.content.models.analytics.UserEntityAttempt;
import com.vedantu.content.models.tests.Test;
import com.vedantu.content.pojos.requests.analytics.EndAttemptReq;
import com.vedantu.content.pojos.requests.analytics.GetEntityResultAnalyticsReq;
import com.vedantu.content.pojos.responses.analytics.EndAttemptRes;
import com.vedantu.content.pojos.responses.analytics.GetEntityResultAnalyticsRes;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;

public class EndTestProcessor implements IProcessor {

    private static final ALogger LOGGER = Logger.of(EndTestProcessor.class);

    @Override
    public Status process(IConsumable consumable) {

        Event event = (Event) consumable;
        EndTestDetails details = (EndTestDetails) event.fetchEventDetails();
        if(details.processType.equals("USER")){
            UserEntityAnalytics userEntityAnalytics = UserEntityAnalyticsDAO.INSTANCE.getAnalytics(
                    details.userId, details.attemptId, new SrcEntity(details.entityType,
                            details.entityId), AcademicDimensionType.OVERALL,
                    AcademicDimensionType.OVERALL.name());
            if (userEntityAnalytics != null) {
                return Status.SUCCESS;
            }
            EndAttemptReq endAttemptReq = new EndAttemptReq(details.userId, details.userId,
                    details.entityId, details.entityType, details.setName, details.attemptId, details.orgId);
            long endTime = details.startTime + details.duration;
            try {
                EndAttemptRes endAttemptRes = AnalyticsManager.endTest(endAttemptReq, endTime, false);
                LOGGER.info("end attempt response : " + endAttemptRes + ", for user[" + details.userId
                        + "], test[" + details.entityId + "], with attemptId: " + details.attemptId);
            } catch (VedantuException e) {
                LOGGER.error(e.getMessage(), e);
            }
            return Status.SUCCESS;
        }else if(details.processType.equals("TEST")){
            Test test = TestDAO.INSTANCE.getById(details.entityId);
            List<UserEntityAttempt> userEntityAttempts = UserEntityAttemptDAO.INSTANCE.getAllTestAttemptsList(details.entityType,details.entityId);
            LOGGER.debug("Total Students taken this test with id "+details.entityId+" is "+userEntityAttempts.size());
            GetEntityResultAnalyticsReq request = new GetEntityResultAnalyticsReq();
            request.entity = new SrcEntity(details.entityType, details.entityId);
            for(UserEntityAttempt userEntityAttempt : userEntityAttempts){
                request.studentUserId = userEntityAttempt.userId;
                request.orgId = userEntityAttempt.orgId;
                try {
                    AnalyticsManager.regenerateStudentTestAnalytics(request);
                } catch (VedantuException e) {
                    LOGGER.debug("Exception came "+e.getMessage());
                }
            }
            test.regeneratingAnalytics = false;
            TestDAO.INSTANCE.save(test);
            return Status.SUCCESS;
        }else{
            return Status.NOT_CONSUMABLE;
        }
    }
}
