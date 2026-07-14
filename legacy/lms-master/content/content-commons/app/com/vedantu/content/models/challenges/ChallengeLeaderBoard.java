package com.vedantu.content.models.challenges;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "challengeleaderboard", noClassnameStored = true)
@Indexes(@Index(value = "userId, challengeId, parent.id, parent.type", unique = true))
public class ChallengeLeaderBoard extends VedantuBaseMongoModel {

    @Indexed
    public String    challengeId;

    public String    userId;
    public int       timeTaken;
    public int       hint;
    public long      ranker;
    public long      rank;
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
