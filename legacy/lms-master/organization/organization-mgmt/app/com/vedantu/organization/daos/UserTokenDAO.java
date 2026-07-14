package com.vedantu.organization.daos;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.organization.models.UserToken;

public class UserTokenDAO extends VedantuBasicDAO<UserToken, ObjectId> {

    private static final ALogger     LOGGER   = Logger.of(UserTokenDAO.class);

    public static final UserTokenDAO INSTANCE = new UserTokenDAO();

    private UserTokenDAO() {
        super(UserToken.class);
    }

    public UserToken getUserTokenByUserId(String userId) {
        UserToken userToken = getQuery().filter("userId", userId).get();
        if (userToken == null) {
            LOGGER.error("cannot find user token for userId :" + userId);
        }
        return userToken;
    }

}
