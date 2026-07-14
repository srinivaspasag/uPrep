package controllers;

import java.util.Arrays;
import java.util.List;

import play.Logger;
import play.Logger.ALogger;
import play.mvc.Result;

import com.google.code.morphia.Key;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.utils.EncryptionUtils;
import com.vedantu.user.daos.UserDAO;
import com.vedantu.user.models.User;

public class UserUpgrades extends AbstractVedantuController {

    private final static ALogger LOGGER = Logger.of(UserUpgrades.class);

    public static Result generateCredentials() {

        List<Key<User>> users = UserDAO.INSTANCE.findIds();

        for (int i = 0; i < users.size(); i++) {

            StringBuilder buider = new StringBuilder();
            buider.append(users.get(i));
            LOGGER.debug("UserId" + users.get(i) + "  " + buider.toString());
            User user = UserDAO.INSTANCE.getById(buider.toString());
            if (user.credentials == null) {
                try {
                    user.credentials = EncryptionUtils.generateKeys();

                    UserDAO.INSTANCE.updateModel(user, Arrays.asList(User.FIELD_CREDENTIALS));
                } catch (VedantuException e) {
                    LOGGER.error("Can not generate user credentials for user " + users.get(i), e);
                }
            } else {
                LOGGER.info("User already has credentials created :  " + users.get(i));
            }

        }

        return ok();
    }
}
