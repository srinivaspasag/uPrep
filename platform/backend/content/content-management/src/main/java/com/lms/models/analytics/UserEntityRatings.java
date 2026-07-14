package com.lms.models.analytics;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.UserRatingType;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;

@Document(value = "userentityratings")
@CompoundIndexes(@CompoundIndex(name = "srcEntity.id,srcEntity.type"))
public class UserEntityRatings extends VedantuBaseMongoModel {
    // Test, Video, EBook, Assignment etc
    public SrcEntity  srcEntity;
    // ORGANISATION
    public SrcEntity  contentSrc;
    public String     userId;
    public UserRatingType rating;
    public String feedback;
    public boolean approved;

    public UserEntityRatings() {
    }

    public UserEntityRatings(String userId, SrcEntity srcEntity, SrcEntity contentSrc, UserRatingType rating, String feedback) {
        this.userId = userId;
        this.srcEntity = srcEntity;
        this.contentSrc = contentSrc;
        this.rating = rating;
        this.feedback = feedback;
    }

    @Override
    public String toString() {
        return "UserEntityRatings [srcEntity=" + srcEntity + ", contentSrc=" + contentSrc
                + ", userId=" + userId + ", rating=" + rating + ", feedback=" + feedback + "]";
    }

}
