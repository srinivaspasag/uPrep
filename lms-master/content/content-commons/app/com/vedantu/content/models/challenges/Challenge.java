package com.vedantu.content.models.challenges;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.ehcache.util.TimeUtil;

import org.apache.commons.collections.CollectionUtils;

import com.google.code.morphia.annotations.Entity;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.commons.interfaces.IIndexable;
import com.vedantu.content.enums.Difficulty;
import com.vedantu.content.enums.challenges.BidType;
import com.vedantu.content.enums.challenges.ChallengeStatus;
import com.vedantu.content.enums.challenges.ChallengeType;
import com.vedantu.content.models.AbstractContentStatsModel;

@Entity(value = "challenges", noClassnameStored = true)
public class Challenge extends AbstractContentStatsModel implements IIndexable {

    public ChallengeType   type;
    public ChallengeStatus status;
    public int             lifeTime;
    public long            endTime;
    public int             duration;
    public int             maxBid;
    public int             initailBidPool;
    public BidType         bidType;
    public Scope           publishType;

    public List<SrcEntity> entities;
    public int             minTargets;
    public int             bidPool;
    public List<String>    hints;
    public List<Integer>   hintsDeductionValues;
    public Set<String>     qTypes;
    public List<String>    topperIds;
    public String          endChallengeEventId;

    public int             attempts;

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
        this.endTime = System.currentTimeMillis() + TimeUtil.toMillis(lifeTime);
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
