package com.lms.models;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.models.analytics.AcademicDimension;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Document(value = "entityhighscores")
@CompoundIndexes({@CompoundIndex(name = "entity.id,entity.type,score"),
        @CompoundIndex(name = "entity.id,entity.type,userIds")})
@Setter
@Getter
public class EntityHighscore extends VedantuBaseMongoModel {

    // test, challenge, assignment, OR question
    public SrcEntity entity;
    // public int totalMarks; we do not need totalMarks, it can be fetched from
    // test collection

    public AcademicDimension acadDim;

    public double score;
    public Set<String> userIds;

    public EntityHighscore() {
        super();
        this.userIds = new HashSet<String>();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{entity:");
        builder.append(entity);
        builder.append(", score:");
        builder.append(score);
        builder.append(", userIds:");
        builder.append(userIds);
        builder.append("}");
        return builder.toString();
    }

}
