package com.vedantu.content.models.challenges;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "challengerankbucket", noClassnameStored = true)
public class ChallengeRankBucket extends VedantuBaseMongoModel {

    @Indexed
    public long      minPoint;
    @Indexed
    public long      maxPoint;
    @Indexed
    public int       bucketNo;

    public String    name;

    public long      size;
    public SrcEntity parent;

    public ChallengeRankBucket() {

        super();
    }

    public ChallengeRankBucket(long minPoint, long maxPoint, String name, SrcEntity parent) {

        super();
        this.minPoint = minPoint;
        this.maxPoint = maxPoint;
        this.name = name;
        this.parent = parent;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{minPoint:").append(minPoint).append(", maxPoint:").append(maxPoint)
                .append(", bucketNo:").append(bucketNo).append(", name:").append(name)
                .append(", size:").append(size).append(", parent:").append(parent).append(", id:")
                .append(id).append(", timeCreated:").append(timeCreated).append(", lastUpdated:")
                .append(lastUpdated).append(", recordState:").append(recordState).append("}");
        return builder.toString();
    }

}
