package com.lms.models;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.enums.RankType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.TreeMap;

@Setter
@Getter
@Document(value = "challengeuserinfos")
@CompoundIndexes(@CompoundIndex(name = "userId,rankIdentifier"))
public class ChallengeUserInfo extends VedantuBaseMongoModel {

    public String userId;
    public int points;
    public int totalAttempts;
    public int correctAttempts;
    public float strikeRate;
    public SrcEntity parent;

    public TreeMap<Integer, Integer> hintsCountMap;
    public String rankBucketId;
    public int bucketNo;
    public RankType type;
    public String rankIdentifier;

    public ChallengeUserInfo() {

        super();
    }

    public ChallengeUserInfo(String userId, RankType rankType, SrcEntity parent) {

        super();
        this.userId = userId;
        this.hintsCountMap = new TreeMap<Integer, Integer>();
        this.type = rankType;
        this.rankIdentifier = this.type.identifier();
        this.parent = parent;
    }

    public void updateHintCount(int hint) {

        if (hintsCountMap == null) {
            hintsCountMap = new TreeMap<Integer, Integer>();
        }
        Integer hintKey = new Integer(hint);
        Integer value = hintsCountMap.get(hintKey);
        if (value == null) {
            value = 0;
        }
        value++;
        hintsCountMap.put(hintKey, value);
    }

    public void calculateStrikeRate() {

        strikeRate = totalAttempts == 0 ? 0 : correctAttempts * 100 / totalAttempts;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{userId:").append(userId).append(", points:").append(points)
                .append(", totalAttempts:").append(totalAttempts).append(", correctAttempts:")
                .append(correctAttempts).append(", strikeRate:").append(strikeRate)
                .append(", parent:").append(parent).append(", hintsCountMap:")
                .append(hintsCountMap).append(", rankBucketId:").append(rankBucketId)
                .append(", bucketNo:").append(bucketNo).append(", type:").append(type)
                .append(", rankIdentifier:").append(rankIdentifier).append(", id:").append(id)
                .append(", timeCreated:").append(timeCreated).append(", lastUpdated:")
                .append(lastUpdated).append(", recordState:").append(recordState).append("}");
        return builder.toString();
    }

}
