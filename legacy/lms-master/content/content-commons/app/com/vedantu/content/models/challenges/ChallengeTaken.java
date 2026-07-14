package com.vedantu.content.models.challenges;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.content.enums.challenges.MultiplierPowerType;
import com.vedantu.content.pojos.ChallengeTakenBasicInfo;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "challengetakens", noClassnameStored = true)
@Indexes(@Index(value = "userId,challengeId, parent.id, parent.type", unique = true))
public class ChallengeTaken extends VedantuBaseMongoModel {

    @Indexed
    public String              challengeId;
    @Indexed
    public String              userId;

    // startTime will be timeCreated
    public long                endTime;
    public int                 bid;
    public boolean             bidded;
    public int                 hint;
    public long                answerTime;
    public int                 timeTaken;
    public boolean             success;
    public boolean             processed;
    public MultiplierPowerType multiplierPower;
    public int                 basePoint;
    public int                 totalPoint;
    public List<String>        answer;
    public SrcEntity           parent;

    public ChallengeTaken() {

        super();
    }

    public ChallengeTaken(String challengeId, String userId, long endTime, SrcEntity parent) {

        super();
        this.challengeId = challengeId;
        this.userId = userId;
        this.endTime = endTime;
        this.answer = new ArrayList<String>();
        this.parent = parent;
    }

    public void addAnswer(List<String> answer) {

        if (CollectionUtils.isEmpty(answer)) {
            return;
        }
        if (this.answer == null) {
            this.answer = new ArrayList<String>();
        }
        this.answer.addAll(answer);
        this.answerTime = System.currentTimeMillis();
        this.timeTaken = (int) (this.answerTime - this.timeCreated);
    }

    @Override
    public ModelBasicInfo toBasicInfo() {

        return new ChallengeTakenBasicInfo(_getStringId(), recordState, challengeId, userId,
                endTime, bid, bidded, hint, answerTime, timeTaken, success, processed,
                multiplierPower, basePoint, totalPoint, answer);
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{challengeId:").append(challengeId).append(", userId:").append(userId)
                .append(", endTime:").append(endTime).append(", bid:").append(bid)
                .append(", bidded:").append(bidded).append(", hint:").append(hint)
                .append(", answerTime:").append(answerTime).append(", timeTaken:")
                .append(timeTaken).append(", success:").append(success).append(", processed:")
                .append(processed).append(", multiplierPower:").append(multiplierPower)
                .append(", basePoint:").append(basePoint).append(", totalPoint:")
                .append(totalPoint).append(", answer:").append(answer).append(", parent:")
                .append(parent).append(", id:").append(id).append(", timeCreated:")
                .append(timeCreated).append(", lastUpdated:").append(lastUpdated)
                .append(", recordState:").append(recordState).append("}");
        return builder.toString();
    }

}
