package com.vedantu.content.daos;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.UserRatingType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.models.analytics.UserEntityRatings;
import com.vedantu.ei.utils.StringUtils;
import com.vedantu.mongo.VedantuBasicDAO;

public class UserEntityRatingsDAO extends VedantuBasicDAO<UserEntityRatings, ObjectId> {

    private static final ALogger             LOGGER   = Logger.of(UserEntityRatings.class);
    public static final UserEntityRatingsDAO INSTANCE = new UserEntityRatingsDAO();

    private UserEntityRatingsDAO() {
        super(UserEntityRatings.class);
    }

    public UserEntityRatings getUserRating(String userId, SrcEntity srcEntity, SrcEntity contentSrc) {
        // TODO Auto-generated method stub
        LOGGER.debug("Inside GetUserRating Function");
        Query<UserEntityRatings> query = getQuery().filter(ConstantsGlobal.USER_ID, userId)
                .filter(ConstantsGlobal.SRC_ENTITY, srcEntity)
                .filter(ConstantsGlobal.CONTENT_SRC, contentSrc);

        UserEntityRatings userRating = findOne(query);
        return userRating;
    }

    public void addUserRatingAndReview(String userId, SrcEntity srcEntity, SrcEntity contentSrc,
            UserRatingType rating, String feedback) {
        // TODO Auto-generated method stub
        UserEntityRatings userRating = new UserEntityRatings(userId, srcEntity, contentSrc, rating,
                feedback);
        save(userRating);
    }

    public long getReviewCount(SrcEntity srcEntity, SrcEntity contentSrc) {
        List<String> condition = new ArrayList<String>();
        condition.add(StringUtils.EMPTY);
        Query<UserEntityRatings> query = getQuery().filter(ConstantsGlobal.SRC_ENTITY, srcEntity)
                .filter(ConstantsGlobal.CONTENT_SRC, contentSrc).field("feedback").exists();
        long entityReviewCount = count(query);
        return entityReviewCount;
    }

}
