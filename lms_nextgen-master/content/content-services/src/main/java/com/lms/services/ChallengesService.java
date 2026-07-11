package com.lms.services;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.GetChallengeLeaderBoardReq;
import com.lms.pojos.GetChallengeReq;
import com.lms.pojos.requests.*;

public interface ChallengesService {
    VedantuResponse addchallenge(AddChallengeReq addChannelReq);

    VedantuResponse getchallengeInfo(GetChallengeReq getChallengeReq);

    VedantuResponse getchallengeDetails(GetChallengeReq getChallengeReq);

    VedantuResponse gethint(GetChallengeHintReq getChallengeHintReq);

    VedantuResponse attemptchallenge(AttemptChallengeReq attemptChallengeReq);

    VedantuResponse getchallengeStats(GetChallengeStatsReq getChallengeStatsReq);

    VedantuResponse getchallenges(GetChallengesReq getChallengesReq);

    VedantuResponse getchallengeUserAttemptInfo(GetChallengeReq getChallengeReq);

    VedantuResponse getUserchallengeInfo(GetChallengeUserInfoReq getChallengeUserInfoReq);

    VedantuResponse getchallengeLeaderBoard(GetChallengeLeaderBoardReq getChallengeLeaderBoardReq);

    VedantuResponse getchallengeGlobalLeaderBoard(GetChallengeGlobalLeaderBoardReq getChallengeGlobalLeaderBoardReq);
}
