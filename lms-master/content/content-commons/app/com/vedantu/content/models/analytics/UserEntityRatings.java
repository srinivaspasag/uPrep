package com.vedantu.content.models.analytics;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.commons.enums.UserRatingType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "userentityratings", noClassnameStored = true)
@Indexes(@Index("srcEntity.id,srcEntity.type"))
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
