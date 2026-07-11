package com.lms.models;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "challengeleaderboard")
@CompoundIndexes(@CompoundIndex(name = "userId, challengeId, parent.id, parent.type", unique = true))
@Setter
@Getter
public class ChallengeLeaderBoard extends VedantuBaseMongoModel {

    @Indexed
    public String challengeId;

    public String userId;
    public int timeTaken;
    public int hint;
    public long ranker;
    public long rank;
    public SrcEntity parent;

    public ChallengeLeaderBoard() {

        super();
    }

    public ChallengeLeaderBoard(String userId, String challengeId, int timeTaken, int hint,
                                SrcEntity parent) {

        super();
        this.userId = userId;
        this.challengeId = challengeId;
        this.timeTaken = timeTaken;
        this.hint = hint;
        this.ranker = (new Long(hint) * Integer.MAX_VALUE) + timeTaken;
        this.parent = parent;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("ChallengeLeaderBoard [userId:").append(userId).append(", challengeId:")
                .append(challengeId).append(", timeTaken:").append(timeTaken).append(", hint:")
                .append(hint).append(", ranker:").append(ranker).append(", rank:").append(rank)
                .append(", parent:").append(parent).append("]");
        return builder.toString();
    }

}
