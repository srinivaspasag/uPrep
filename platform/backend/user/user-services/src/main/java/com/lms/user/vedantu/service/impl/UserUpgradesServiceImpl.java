package com.lms.user.vedantu.service.impl;

import com.lms.common.exception.VedantuException;
import com.lms.common.utils.EncryptionUtils;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.user.vedantu.service.UserUpgradesService;
import com.lms.user.vedantu.user.model.User;
import com.lms.user.vedantu.user.repository.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserUpgradesServiceImpl implements UserUpgradesService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    @Autowired
    private UserRepo userRepo;

    @Override
    public VedantuResponse generateCreds() {
        List<User> users=userRepo.findAll();

        for (int i = 0; i < users.size(); i++) {

            StringBuilder buider = new StringBuilder();
            buider.append(users.get(i).getUsername());
            logger.debug("UserId" + users.get(i).getUsername() + "  " + buider.toString());
            User user = users.get(i);
            if (user.getCredentials() == null) {
                try {
                    user.setCredentials(EncryptionUtils.generateKeys());
                     userRepo.save(user);

                } catch (VedantuException e) {
                    logger.error("Can not generate user credentials for user " + users.get(i), e);
                }
            } else {
                logger.info("User already has credentials created :  " + users.get(i));
            }

        }

        return new VedantuResponse("OK");
        }
}
