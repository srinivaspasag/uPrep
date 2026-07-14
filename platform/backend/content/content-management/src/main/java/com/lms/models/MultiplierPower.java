package com.lms.models;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.enums.MultiplierPowerType;
import com.lms.enums.MultiplierPowerValidityType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "multiplierpowers")
@CompoundIndexes({@CompoundIndex(name = "userId,parent.id,parent.type")})
@Setter
@Getter
public class MultiplierPower extends VedantuBaseMongoModel {

    @Indexed
    public String userId;
    public MultiplierPowerType type;
    public long validFor;
    public int useCount;
    public MultiplierPowerValidityType validityType;
    public SrcEntity src;
    public String powerRule;   // MultiplierPowerRule

    public SrcEntity parent;      // organization

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
