package com.lms.services;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.*;
import com.lms.pojos.requests.analytics.GetQuestionAnalyticsReq;
import com.lms.pojos.requests.analytics.*;

public interface AnalyticsService {

    VedantuResponse testStatus(StartAttemptReq startAttemptReq);

    VedantuResponse _testStatus(GetTestInfoReq getTestInfoReq);

    VedantuResponse endStudentAttempt(StartAttemptReq startAttemptReq, long endTime);

    VedantuResponse resumeStudentTest(StartAttemptReq startAttemptReq);

    VedantuResponse getStudentsListFromEntityAttempts(GetEntityResultAnalyticsReq getEntityResultAnalyticsReq);

    VedantuResponse startAttempt(StartAttemptReq startAttemptReq);

    VedantuResponse endAttempt(EndAttemptReq endAttemptReq);

    VedantuResponse recordAttempt(RecordAttemptReq recordAttemptReq);

    VedantuResponse resetQuestionAttempt(ResetQuestionAttemptReq resetQuestionAttemptReq);

    VedantuResponse getQuestionAnalytics(GetQuestionAnalyticsReq getQuestionAnalyticsReq);

    VedantuResponse pauseStudentAttempt(StartAttemptReq startAttemptReq);

    VedantuResponse resetStudentTest(GetEntityResultAnalyticsReq getEntityResultAnalyticsReq);

    VedantuResponse regenerateStudentTestAnalytics(GetEntityResultAnalyticsReq getEntityResultAnalyticsReq);

    VedantuResponse recomputeEntitynalytics(StartAttemptReq startAttemptReq);

    VedantuResponse getUserEntityAnalyticsBySubject(GetUserEntityAnalyticsBySubjectReq getUserEntityAnalyticsBySubjectReq);

    VedantuResponse getAttemptedEntities(GetAttemptedEntitiesReq getAttemptedEntitiesReq);

    VedantuResponse getentityLeaderBoard(GetEntityLeaderBoardReq getEntityLeaderBoardReq);

    VedantuResponse syncTabletAnalytics(SyncTabletAnalyticsReq syncTabletAnalyticsReq);

    VedantuResponse getUserEntityRank(GetUserEntityRankReq getUserEntityRankReq);

    VedantuResponse getUserEntityMeasures(GetUserEntityMeasuresReq getUserEntityMeasuresReq);


    VedantuResponse getuserEntityAnalytics(GetUserEntityAnalyticsReq getUserEntityAnalyticsReq);

    VedantuResponse getuserEntityResultAnalytics(GetUserEntityResultAnalyticsReq getUserEntityResultAnalyticsReq);

    VedantuResponse getuserEntityQuestionAttempts(GetUserEntityQuestionAttemptStatsReq getUserEntityQuestionAttemptStatsReq);

    VedantuResponse getEntityResultAnalytics(GetEntityResultAnalyticsReq getEntityResultAnalyticsReq);

    VedantuResponse getEntityMeasures(GetEntityMeasuresReq getEntityMeasuresReq);

    VedantuResponse getUserEntityQuestionsAttemptStatInfo(GetEntityQuestionsAttemptStatReq getEntityQuestionsAttemptStatReq);

    VedantuResponse getUserEntityAttemptStatusInfo(GetUserEntityAttemptStatusInfoReq getUserEntityAttemptStatusInfoReq);

    VedantuResponse getUserAnalyticsStats(GetUserAnalyticsStatsReq getUserAnalyticsStatsReq);

    VedantuResponse getentityQuestionAttempts(GetEntityQuestionsAttemptStatReq getEntityQuestionsAttemptStatReq);

    VedantuResponse getstudentsQuestionsAnsweredList(GetQuestionAnalyticsReq getQuestionAnalyticsReq);

    VedantuResponse getentityMarkDistribution(GetEntityMarkDistributionReq getEntityMarkDistributionReq);

    VedantuResponse getentityScheduleAnalytics(GetEntityScheduleAnalyticsReq getEntityScheduleAnalyticsReq);

    VedantuResponse getentityScheduleInfo(GetEntityScheduleInfoReq getEntityScheduleInfoReq);
}
