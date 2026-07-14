package com.lms.controller;


import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.*;
import com.lms.pojos.requests.analytics.GetQuestionAnalyticsReq;
import com.lms.pojos.requests.analytics.*;
import com.lms.services.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/analytics")
public class Analytics {
    Date date = new Date();
    @Autowired
    AnalyticsService analyticsServiceImpl;

    @PostMapping("/testStatus")
    public ResponseEntity<VedantuResponse> testStatus(StartAttemptReq startAttemptReq) {
        return ResponseEntity.ok(analyticsServiceImpl.testStatus(startAttemptReq));
    }

    @PostMapping("/_testStatus")
    public ResponseEntity<VedantuResponse> _testStatus(GetTestInfoReq getTestInfoReq) {
        return ResponseEntity.ok(analyticsServiceImpl._testStatus(getTestInfoReq));
    }

    @PostMapping("/endStudentAttempt")
    public ResponseEntity<VedantuResponse> endStudentAttempt(StartAttemptReq startAttemptReq) {
        return ResponseEntity.ok(analyticsServiceImpl.endStudentAttempt(startAttemptReq, date.getTime()));
    }

    @PostMapping("/resumeStudentTest")
    public ResponseEntity<VedantuResponse> resumeStudentTest(StartAttemptReq startAttemptReq) {
        return ResponseEntity.ok(analyticsServiceImpl.resumeStudentTest(startAttemptReq));
    }

    @PostMapping("/getStudentsListFromEntityAttempts")
    public ResponseEntity<VedantuResponse> getStudentsListFromEntityAttempts(GetEntityResultAnalyticsReq getEntityResultAnalyticsReq) {
        return ResponseEntity.ok(analyticsServiceImpl.getStudentsListFromEntityAttempts(getEntityResultAnalyticsReq));
    }


    @PostMapping("/pauseStudentTest")
    public ResponseEntity<VedantuResponse> pauseStudentTest(StartAttemptReq startAttemptReq) {
        return ResponseEntity.ok(analyticsServiceImpl.pauseStudentAttempt(startAttemptReq));
    }

    @PostMapping("/resetStudentTest")
    public ResponseEntity<VedantuResponse> resetStudentTest(GetEntityResultAnalyticsReq getEntityResultAnalyticsReq) {
        return ResponseEntity.ok(analyticsServiceImpl.resetStudentTest(getEntityResultAnalyticsReq));
    }

    @PostMapping("/regenerateStudentTestAnalytics")
    public ResponseEntity<VedantuResponse> regenerateStudentTestAnalytics(GetEntityResultAnalyticsReq getEntityResultAnalyticsReq) {
        return ResponseEntity.ok(analyticsServiceImpl.regenerateStudentTestAnalytics(getEntityResultAnalyticsReq));
    }

    @PostMapping("/recomputeEntitynalytics")
    public ResponseEntity<VedantuResponse> recomputeEntitynalytics(StartAttemptReq startAttemptReq) {
        return ResponseEntity.ok(analyticsServiceImpl.recomputeEntitynalytics(startAttemptReq));
    }

    @PostMapping("/getUserEntityAnalyticsBySubject")
    public ResponseEntity<VedantuResponse> getUserEntityAnalyticsBySubject(GetUserEntityAnalyticsBySubjectReq getUserEntityAnalyticsBySubjectReq) {
        return ResponseEntity.ok(analyticsServiceImpl.getUserEntityAnalyticsBySubject(getUserEntityAnalyticsBySubjectReq));
    }

    @PostMapping("/getAttemptedEntities")
    public ResponseEntity<VedantuResponse> getAttemptedEntities(GetAttemptedEntitiesReq getAttemptedEntitiesReq) {
        return ResponseEntity.ok(analyticsServiceImpl.getAttemptedEntities(getAttemptedEntitiesReq));
    }

    @PostMapping("/startAttempt")
    public ResponseEntity<VedantuResponse> startAttempt(StartAttemptReq startAttemptReq) {
        return ResponseEntity.ok(analyticsServiceImpl.startAttempt(startAttemptReq));
    }

    @PostMapping("/endAttempt")
    public ResponseEntity<VedantuResponse> endAttempt(EndAttemptReq endAttemptReq) {
        return ResponseEntity.ok(analyticsServiceImpl.endAttempt(endAttemptReq));
    }

    @PostMapping("/recordAttempt")
    public ResponseEntity<VedantuResponse> recordAttempt(RecordAttemptReq recordAttemptReq) {
        return ResponseEntity.ok(analyticsServiceImpl.recordAttempt(recordAttemptReq));
    }

    @PostMapping("/resetQuestionAttempt")
    public ResponseEntity<VedantuResponse> resetQuestionAttempt(ResetQuestionAttemptReq resetQuestionAttemptReq) {
        return ResponseEntity.ok(analyticsServiceImpl.resetQuestionAttempt(resetQuestionAttemptReq));
    }

    @PostMapping("/getQuestionAnalytics")
    public ResponseEntity<VedantuResponse> getQuestionAnalytics(GetQuestionAnalyticsReq getQuestionAnalyticsReq) {
        return ResponseEntity.ok(analyticsServiceImpl.getQuestionAnalytics(getQuestionAnalyticsReq));
    }

    @PostMapping("/getEntityLeaderBoard")
    public ResponseEntity<VedantuResponse> getEntityLeaderBoard(GetEntityLeaderBoardReq getEntityLeaderBoardReq) {
        return ResponseEntity.ok(analyticsServiceImpl.getentityLeaderBoard(getEntityLeaderBoardReq));
    }

    @PostMapping("/syncTabletAnalytics")
    public ResponseEntity<VedantuResponse> syncTabletAnalytics(SyncTabletAnalyticsReq syncTabletAnalyticsReq) {
        return ResponseEntity.ok(analyticsServiceImpl.syncTabletAnalytics(syncTabletAnalyticsReq));
    }

    @PostMapping("/getUserEntityRank")
    public ResponseEntity<VedantuResponse> getUserEntityRank(GetUserEntityRankReq getUserEntityRankReq) {
        return ResponseEntity.ok(analyticsServiceImpl.getUserEntityRank(getUserEntityRankReq));
    }

    @PostMapping("/getUserEntityMeasures")
    public ResponseEntity<VedantuResponse> getUserEntityMeasures(GetUserEntityMeasuresReq getUserEntityMeasuresReq) {
        return ResponseEntity.ok(analyticsServiceImpl.getUserEntityMeasures(getUserEntityMeasuresReq));
    }


    @PostMapping("/getUserEntityAnalytics")
    public ResponseEntity<VedantuResponse> getUserEntityAnalytics(GetUserEntityAnalyticsReq getUserEntityAnalyticsReq) {
        return ResponseEntity.ok(analyticsServiceImpl.getuserEntityAnalytics(getUserEntityAnalyticsReq));
    }

    @PostMapping("/getUserEntityResultAnalytics")
    public ResponseEntity<VedantuResponse> getUserEntityResultAnalytics(GetUserEntityResultAnalyticsReq getUserEntityResultAnalyticsReq) {
        return ResponseEntity.ok(analyticsServiceImpl.getuserEntityResultAnalytics(getUserEntityResultAnalyticsReq));
    }

    @PostMapping("/getUserEntityQuestionAttempts")
    public ResponseEntity<VedantuResponse> getUserEntityQuestionAttempts(GetUserEntityQuestionAttemptStatsReq getUserEntityQuestionAttemptStatsReq) {
        return ResponseEntity.ok(analyticsServiceImpl.getuserEntityQuestionAttempts(getUserEntityQuestionAttemptStatsReq));
    }

    @PostMapping("/getEntityResultAnalytics")
    public ResponseEntity<VedantuResponse> getEntityResultAnalytics(GetEntityResultAnalyticsReq getEntityResultAnalyticsReq) {
        return ResponseEntity.ok(analyticsServiceImpl.getEntityResultAnalytics(getEntityResultAnalyticsReq));
    }

    @PostMapping("/getEntityMeasures")
    public ResponseEntity<VedantuResponse> getEntityMeasures(GetEntityMeasuresReq getEntityMeasuresReq) {
        return ResponseEntity.ok(analyticsServiceImpl.getEntityMeasures(getEntityMeasuresReq));
    }

    @PostMapping("/getUserEntityQuestionsAttemptStatInfo")
    public ResponseEntity<VedantuResponse> getUserEntityQuestionsAttemptStatInfo(GetEntityQuestionsAttemptStatReq getEntityQuestionsAttemptStatReq) {
        return ResponseEntity.ok(analyticsServiceImpl.getUserEntityQuestionsAttemptStatInfo(getEntityQuestionsAttemptStatReq));
    }

    @PostMapping("/getUserEntityAttemptStatusInfo")
    public ResponseEntity<VedantuResponse> getUserEntityAttemptStatusInfo(GetUserEntityAttemptStatusInfoReq getUserEntityAttemptStatusInfoReq) {
        return ResponseEntity.ok(analyticsServiceImpl.getUserEntityAttemptStatusInfo(getUserEntityAttemptStatusInfoReq));
    }

    @PostMapping("/getUserAnalyticsStats")
    public ResponseEntity<VedantuResponse> getUserAnalyticsStats(GetUserAnalyticsStatsReq getUserAnalyticsStatsReq) {
        return ResponseEntity.ok(analyticsServiceImpl.getUserAnalyticsStats(getUserAnalyticsStatsReq));
    }

    @PostMapping("/getEntityQuestionAttempts")
    public ResponseEntity<VedantuResponse> getEntityQuestionAttempts(GetEntityQuestionsAttemptStatReq getEntityQuestionsAttemptStatReq) {
        return ResponseEntity.ok(analyticsServiceImpl.getentityQuestionAttempts(getEntityQuestionsAttemptStatReq));
    }

    @PostMapping("/getStudentsQuestionsAnsweredList")
    public ResponseEntity<VedantuResponse> getStudentsQuestionsAnsweredList(GetQuestionAnalyticsReq getQuestionAnalyticsReq) {
        return ResponseEntity.ok(analyticsServiceImpl.getstudentsQuestionsAnsweredList(getQuestionAnalyticsReq));
    }

    @PostMapping("/getEntityMarkDistribution")
    public ResponseEntity<VedantuResponse> getEntityMarkDistribution(GetEntityMarkDistributionReq getEntityMarkDistributionReq) {
        return ResponseEntity.ok(analyticsServiceImpl.getentityMarkDistribution(getEntityMarkDistributionReq));
    }

    @PostMapping("/getEntityScheduleAnalytics")
    public ResponseEntity<VedantuResponse> getEntityScheduleAnalytics(GetEntityScheduleAnalyticsReq getEntityScheduleAnalyticsReq) {
        return ResponseEntity.ok(analyticsServiceImpl.getentityScheduleAnalytics(getEntityScheduleAnalyticsReq));
    }

    @PostMapping("/getEntityScheduleInfo")
    public ResponseEntity<VedantuResponse> getEntityScheduleInfo(GetEntityScheduleInfoReq getEntityScheduleInfoReq) {
        return ResponseEntity.ok(analyticsServiceImpl.getentityScheduleInfo(getEntityScheduleInfoReq));
    }

}

