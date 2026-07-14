package com.lms.pojos.responce;

import com.lms.enums.MultiplierPowerType;
import com.lms.enums.RankType;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class GetChallengeUserInfoRes {

    public long points;
    public int totalAttempts;
    public int correctAttempts;
    public double strikeRate;
    public Map<Integer, Integer> hintsCountMap;
    public RankType type;
    public String rankIdentifier;
    public MultiplierPowerType multiplier;

    public GetChallengeUserInfoRes(long points, int totalAttempts,
                                   int correctAttempts, double strikeRate,
                                   Map<Integer, Integer> hintsCountMap, RankType type,
                                   String rankIdentifier, MultiplierPowerType multiplier) {
        super();
        this.points = points;
        this.totalAttempts = totalAttempts;
        this.correctAttempts = correctAttempts;
        this.strikeRate = strikeRate;
        this.hintsCountMap = hintsCountMap;
        this.type = type;
        this.rankIdentifier = rankIdentifier;
        this.multiplier = multiplier;
    }


}
