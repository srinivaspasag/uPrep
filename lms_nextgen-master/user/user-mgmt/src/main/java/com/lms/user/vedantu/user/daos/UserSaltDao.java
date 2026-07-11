package com.lms.user.vedantu.user.daos;

import com.lms.user.vedantu.user.model.UserSalt;
import com.lms.user.vedantu.user.repository.UserSaltrepo;
import com.lms.common.vedantu.constants.HardCodedConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


import java.util.UUID;
@Repository
public class UserSaltDao {

    private static final Logger logger = LoggerFactory.getLogger(UserSaltDao.class);

    @Autowired
    private UserSaltrepo userSaltrepo;

    private static final String SYSTEM_SALT = "/vdntu/";



    public String getSaltedPassword(String username, String password, boolean isOnlyCheck) {


            UserSalt userSalt =userSaltrepo.findByUsername(username);

            if(userSalt==null) {
                logger.debug("user-salt not found for username: " + username);

                if (isOnlyCheck) {
                    logger.debug("will not create new user-salt for username: "
                            + username);
                    return HardCodedConstants.emptyString;
                }

                userSalt = new UserSalt(username, UUID.randomUUID().toString());
                userSaltrepo.save(userSalt);
            }

            String saltedPassword = userSalt.getSalt() + SYSTEM_SALT + password;
            return saltedPassword;
    }

}
