package com.vedantu.user.daos;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.vedantu.commons.VedantuException;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.user.models.UserSalt;

public class UserSaltDAO extends VedantuBasicDAO<UserSalt, ObjectId> {

	private static final ALogger LOGGER = Logger.of(UserSaltDAO.class);

	public static final UserSaltDAO INSTANCE = new UserSaltDAO();

	public UserSaltDAO() {
		super(UserSalt.class);
	}

	private static final String SYSTEM_SALT = "/vdntu/";

	String getSaltedPassword(String username, String password,
			boolean isOnlyCheck) {
		UserSalt userSalt = getDS().find(UserSalt.class)
				.filter("username", username).get();
		if (null == userSalt) {
			LOGGER.debug("user-salt not found for username: " + username);

			if (isOnlyCheck) {
				LOGGER.debug("will not create new user-salt for username: "
						+ username);
				return StringUtils.EMPTY;
			}

			userSalt = new UserSalt(username, UUID.randomUUID().toString());
			save(userSalt);
		}

		String saltedPassword = userSalt.salt + SYSTEM_SALT + password;
		return saltedPassword;
	}

    public List<UserSalt> getUserSalts(List<String> usernames, String... fields) {
        Query<UserSalt> query = getQuery();
        query.criteria("username").in(usernames);
        if(fields.length >0){
            query = query.retrievedFields(true, fields);
        }
        return query.asList();
    }

}
