package com.lms.controller;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.GetChallengeLeaderBoardReq;
import com.lms.pojos.GetChallengeReq;
import com.lms.pojos.requests.*;
import com.lms.services.ChallengesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/challenges")
public class Challenges {
    @Autowired
    private ChallengesService challengesServiceImpl;

    @PostMapping("/addChallenge")
    public ResponseEntity<VedantuResponse> addChallenge(AddChallengeReq addChannelReq) {
        return ResponseEntity.ok(challengesServiceImpl.addchallenge(addChannelReq));
    }

    @PostMapping("/getChallengeInfo")
    public ResponseEntity<VedantuResponse> getChallengeInfo(GetChallengeReq getChallengeReq) {
        return ResponseEntity.ok(challengesServiceImpl.getchallengeInfo(getChallengeReq));
    }

    @PostMapping("/getChallengeDetails")
    public ResponseEntity<VedantuResponse> getChallengeDetails(GetChallengeReq getChallengeReq) {
        return ResponseEntity.ok(challengesServiceImpl.getchallengeDetails(getChallengeReq));
    }

    @PostMapping("/getHint")
    public ResponseEntity<VedantuResponse> getHint(GetChallengeHintReq getChallengeHintReq) {
        return ResponseEntity.ok(challengesServiceImpl.gethint(getChallengeHintReq));
    }

    @PostMapping("/attemptChallenge")
    public ResponseEntity<VedantuResponse> attemptChallenge(AttemptChallengeReq attemptChallengeReq) {
        return ResponseEntity.ok(challengesServiceImpl.attemptchallenge(attemptChallengeReq));
    }

    @PostMapping("/getChallengeStats")
    public ResponseEntity<VedantuResponse> getChallengeStats(GetChallengeStatsReq getChallengeStatsReq) {
        return ResponseEntity.ok(challengesServiceImpl.getchallengeStats(getChallengeStatsReq));
    }

    @PostMapping("/getChallenges")
    public ResponseEntity<VedantuResponse> getChallenges(GetChallengesReq getChallengesReq) {
        return ResponseEntity.ok(challengesServiceImpl.getchallenges(getChallengesReq));
    }

    @PostMapping("/getChallengeUserAttemptInfo")
    public ResponseEntity<VedantuResponse> getChallengeUserAttemptInfo(GetChallengeReq getChallengeReq) {
        return ResponseEntity.ok(challengesServiceImpl.getchallengeUserAttemptInfo(getChallengeReq));
    }

    @PostMapping("/getUserChallengeInfo")
    public ResponseEntity<VedantuResponse> getUserChallengeInfo(GetChallengeUserInfoReq getChallengeUserInfoReq) {
        return ResponseEntity.ok(challengesServiceImpl.getUserchallengeInfo(getChallengeUserInfoReq));
    }

    @PostMapping("/getChallengeLeaderBoard")
    public ResponseEntity<VedantuResponse> getChallengeLeaderBoard(GetChallengeLeaderBoardReq getChallengeLeaderBoardReq) {
        return ResponseEntity.ok(challengesServiceImpl.getchallengeLeaderBoard(getChallengeLeaderBoardReq));
    }

    @PostMapping("/getChallengeGlobalLeaderBoard")
    public ResponseEntity<VedantuResponse> getChallengeGlobalLeaderBoard(GetChallengeGlobalLeaderBoardReq getChallengeGlobalLeaderBoardReq) {
        return ResponseEntity.ok(challengesServiceImpl.getchallengeGlobalLeaderBoard(getChallengeGlobalLeaderBoardReq));
    }


}
