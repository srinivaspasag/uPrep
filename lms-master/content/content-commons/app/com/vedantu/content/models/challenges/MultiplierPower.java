package com.vedantu.content.models.challenges;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.enums.challenges.MultiplierPowerType;
import com.vedantu.content.enums.challenges.MultiplierPowerValidityType;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "multiplierpowers", noClassnameStored = true)
@Indexes({ @Index("userId,parent.id,parent.type") })
public class MultiplierPower extends VedantuBaseMongoModel {

    @Indexed
    public String                      userId;
    public MultiplierPowerType         type;
    public long                        validFor;
    public int                         useCount;
    public MultiplierPowerValidityType validityType;
    public SrcEntity                   src;
    public String                      powerRule;   // MultiplierPowerRule

    public SrcEntity                   parent;      // organization

    public MultiplierPower() {

        super();
    }

    public MultiplierPower(String userId, MultiplierPowerType type, long validFor,
            MultiplierPowerValidityType validityType, SrcEntity src, String powerRule,
            SrcEntity parent) {

        super();
        this.userId = userId;
        this.type = type;
        this.validFor = validFor;
        this.validityType = validityType;
        this.src = src;
        this.powerRule = powerRule;
        this.parent = parent;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{userId:").append(userId).append(", type:").append(type)
                .append(", validFor:").append(validFor).append(", useCount:").append(useCount)
                .append(", validityType:").append(validityType).append(", src:").append(src)
                .append(", powerRule:").append(powerRule).append(", parent:").append(parent)
                .append(", id:").append(id).append(", timeCreated:").append(timeCreated)
                .append(", lastUpdated:").append(lastUpdated).append(", recordState:")
                .append(recordState).append("}");
        return builder.toString();
    }

}
