package com.lms.models;


import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.content.IIndexable;
import com.lms.common.vedantu.enums.Scope;
import com.lms.enums.BidType;
import com.lms.enums.ChallengeStatus;
import com.lms.enums.ChallengeType;
import com.lms.enums.Difficulty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Setter
@Getter
@Document(value = "challenges")
public class Challenge extends AbstractContentStatsModel implements IIndexable {

    public ChallengeType type;
    public ChallengeStatus status;
    public int lifeTime;
    public long endTime;
    public int duration;
    public int maxBid;
    public int initailBidPool;
    public BidType bidType;
    public Scope publishType;

    public List<SrcEntity> entities;
    public int minTargets;
    public int bidPool;
    public List<String> hints;
    public List<Integer> hintsDeductionValues;
    public Set<String> qTypes;
    public List<String> topperIds;
    public String endChallengeEventId;

    public int attempts;

    public Challenge() {

        super();
    }

    public Challenge(String userId, String name, ChallengeType type, int lifeTime, int duration,
                     Scope scope, Difficulty difficulty, BidType bidType, int maxBid, int initailBidPool) {

        super();
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.lifeTime = lifeTime;// in seconds
        if (lifeTime == 0) {
            lifeTime = 24 * 60 * 60;// TODO: take this as configuration
            // property
        }
        this.initailBidPool = initailBidPool;
        this.bidPool = initailBidPool;
        this.endTime = System.currentTimeMillis() + TimeUnit.MILLISECONDS.toMillis(lifeTime);
        this.duration = duration;// in seconds
        this.maxBid = maxBid;
        this.scope = scope;
        this.difficulty = difficulty;
        this.status = ChallengeStatus.ACTIVE;
        this.bidType = bidType;
        publishType = Scope.PUBLIC;
        entities = new ArrayList<SrcEntity>();
        minTargets = 0;
        hints = new ArrayList<String>();
        topperIds = new ArrayList<String>();
        qTypes = new HashSet<String>();
    }

    public void addEntity(SrcEntity entity) {

        if (entities == null) {
            entities = new ArrayList<SrcEntity>();
        }
        entities.add(entity);
    }

    public void addHint(List<String> hints) {

        if (CollectionUtils.isEmpty(hints)) {
            return;
        }
        if (this.hints == null) {
            this.hints = new ArrayList<String>();
        }
        this.hints.addAll(hints);
    }

    // @Override
    // public void addImageSrcUrl() {
    // List<String> convertedOptn = QuestionUtilCommon
    // .convertUUIDCollectionToImageURLs(hints);
    // hints = convertedOptn;
    // }

}
