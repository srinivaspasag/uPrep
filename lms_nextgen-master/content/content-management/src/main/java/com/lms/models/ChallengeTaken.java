package com.lms.models;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.enums.MultiplierPowerType;
import com.lms.pojos.ChallengeTakenBasicInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Document(value = "challengetakens")
@CompoundIndexes(@CompoundIndex(name = "userId,challengeId, parent.id, parent.type", unique = true))
@Getter
@Setter
public class ChallengeTaken extends VedantuBaseMongoModel {

    @Indexed
    public String challengeId;
    @Indexed
    public String userId;

    // startTime will be timeCreated
    public long endTime;
    public int bid;
    public boolean bidded;
    public int hint;
    public long answerTime;
    public int timeTaken;
    public boolean success;
    public boolean processed;
    public MultiplierPowerType multiplierPower;
    public int basePoint;
    public int totalPoint;
    public List<String> answer;
    public SrcEntity parent;

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
