package com.vedantu.content.daos.challenges;

import org.bson.types.ObjectId;

import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.models.challenges.ChallengeLeaderBoard;
import com.vedantu.mongo.VedantuBasicDAO;

public class ChallengeLeaderBoardDAO extends VedantuBasicDAO<ChallengeLeaderBoard, ObjectId> {

    public static final ChallengeLeaderBoardDAO INSTANCE = new ChallengeLeaderBoardDAO();

    private ChallengeLeaderBoardDAO() {

        super(ChallengeLeaderBoard.class);
    }

    public ChallengeLeaderBoard addLearderBoard(String userId, String challengeId, int timeTaken,
            int hint, SrcEntity parent) {

        ChallengeLeaderBoard leaderBoard = new ChallengeLeaderBoard(userId, challengeId, timeTaken,
                hint, parent);
        save(leaderBoard);
        return leaderBoard;
    }

}
