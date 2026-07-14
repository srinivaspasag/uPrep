package com.vedantu.user.daos;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.google.code.morphia.query.Query;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.AuthType;
import com.vedantu.commons.pojos.SecurityCredentials;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.utils.EncryptionUtils;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.user.enums.Gender;
import com.vedantu.user.models.User;
import com.vedantu.user.pojos.EmailChangeReqInfo;
import com.vedantu.user.pojos.ForgotPasswordReqInfo;
import com.vedantu.user.pojos.SocialInfo;
import com.vedantu.user.pojos.TnCAcceptance;

public class UserDAO extends VedantuBasicDAO<User, ObjectId> {

    private static final ALogger LOGGER      = Logger.of(UserDAO.class);

    public static final UserDAO  INSTANCE    = new UserDAO();

    public static final String   UNKNOWN_DOB = "1970-01-01";

    private UserDAO() {

        super(User.class);
    }

    private static String getHashed(String input, String hashType) {

        try {
            MessageDigest m = MessageDigest.getInstance(hashType.toString());
            byte[] out = m.digest(input.getBytes());
            return new String(Base64.encodeBase64(out));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean _createDefaultUser() {

        LOGGER.debug("in _createDefaultUser");
        final String defaultUsername = Play.application().configuration()
                .getString("app.defaultUsername");
        User defaultUser = getQuery().filter("username", defaultUsername).get();
        if (null == defaultUser) {
            LOGGER.debug("default user not found");

            try {
                MutableBoolean isEmailVerificationNeeded = new MutableBoolean(false);
                defaultUser = addUser(defaultUsername, "password", defaultUsername, null,
                        UNKNOWN_DOB, Gender.UNKNOWN, null, isEmailVerificationNeeded,
                        EncryptionUtils.generateKeys());
            } catch (VedantuException e) {
                LOGGER.error("could not create default user", e);
                return false;
            }

            LOGGER.info("created default user");

            return true;
        }
        LOGGER.info("default user found");
        return false;
    }

    /**
     * Authenticate userName and password in plain string
     *
     * @param username
     * @param password
     * @return
     * @throws VedantuException
     */
    public User authenticateUser(String username, String password) throws VedantuException {

        LOGGER.debug("authenticateUser username: " + username);
        final boolean isOnlyCheck = true;
        String saltedPassword = getUserPassHash(username, password, isOnlyCheck);

        return authenticateUserWithSaltedPassword(username, saltedPassword);
    }

    public User authenticateUserWithSaltedPassword(String username, String saltedPassword)
            throws VedantuException {

        LOGGER.debug("authenticateUserWithSaltedPassword username: " + username);

        User user = getQuery().filter("username", username).filter("password", saltedPassword)
                .get();

        if (null == user) {
            LOGGER.error("authentication failed for username: " + username);
            throw new VedantuException(VedantuErrorCode.AUTHENTICATION_FAILED);
        }
        LOGGER.info("authenticateUserWithSaltedPassword user: " + user);
        return user;
    }

    public User addUser(String username, String password, String firstName, String lastName,
            String dob, Gender gender, String email, MutableBoolean isEmailVerificationNeeded,
            SecurityCredentials credentials) throws VedantuException {

        return addUser(username, password, firstName, lastName, dob, gender, email,
                isEmailVerificationNeeded, credentials, null);
    }

    public User addUser(String username, String password, String firstName, String lastName,
            String dob, Gender gender, String email, MutableBoolean isEmailVerificationNeeded,
            SecurityCredentials credentials, SocialInfo socialInfo) throws VedantuException {

        return addUser(username, password, firstName, lastName, dob, gender, email,
                isEmailVerificationNeeded, credentials, socialInfo, AuthType.VEDANTU,false,false,false);
    }

    public boolean doesUserExists(String email) {

        LOGGER.debug("entering the function doesUserExists " + email);
        User user = getQuery().filter("email", email).get();
        LOGGER.debug("exiting the function doesUserExists " + email);
        return (null != user);
    }

    public User getUserByEmail(String email) {

        LOGGER.debug("entering the function doesUserExists " + email);
        User user = getQuery().filter("email", email).get();
        LOGGER.debug("exiting the function doesUserExists " + email);
        return user;
    }

    public User addUser(String username, String password, String firstName, String lastName,
            String dob, Gender gender, String email, MutableBoolean isEmailVerificationNeeded,
            SecurityCredentials credentials, SocialInfo socialInfo, AuthType authType,
            boolean isPhoneVerified, boolean isSysGenPassword,boolean isOTPuser) throws VedantuException {

        LOGGER.debug("addUser username: " + username + ", firstName: " + firstName + ", lastName: "
                + lastName + ", dob: " + dob + ", gender: " + gender + ", email: " + email
                + ", isEmailVerificationNeeded: " + isEmailVerificationNeeded.getValue());

        User user = getQuery().filter("username", username).get();

        if (null != user) {
            LOGGER.error("cannot add user as user already exists for username: " + username);
            throw new VedantuException(VedantuErrorCode.USER_ALREADY_EXISTS);
        }

        final boolean isOnlyCheck = false;

        user = new User();
        user.username = username;
        user.firstName = firstName;
        user.lastName = lastName;
        user.dob = dob;
        user.gender = gender;
        user.password = getUserPassHash(username, password, isOnlyCheck);
        user.email = email;
        user.isEmailVerified = false;
        user.isSysGenPassword = isSysGenPassword;
        user.isPhoneVerified = isPhoneVerified;
        user.isOTPuser = isOTPuser;
        user.credentials = credentials;
        user.socialInfo = socialInfo;
        user.authType = authType;
        if (StringUtils.isNotEmpty(email)) {
            user.emailChangeReq = new EmailChangeReqInfo(email, UUID.randomUUID().toString());
            isEmailVerificationNeeded.setValue(true);
            LOGGER.debug("generated email verification code");
        }

        save(user);

        LOGGER.info("addUser user: " + user);

        return user;
    }

    public User updateUsername(String userId, String newUsername, String newPassword)
            throws VedantuException {

        LOGGER.debug("updateUsername userId: " + userId + ", newUsername: " + newUsername);

        User user = getById(userId);
        LOGGER.debug("found user: " + user);

        if (null == user) {
            LOGGER.error("cannot update username as user does not exist for userId: " + userId);
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }

        if (user.authType == AuthType.EXT_AUTH_ORG) {
            throw new VedantuException(VedantuErrorCode.EXTERNAL_AUTH_SUPPORTED);
        }

        User alreadyUsingUsername = findUser(newUsername);
        if (null != alreadyUsingUsername) {
            if (StringUtils.equals(user._getStringId(), alreadyUsingUsername._getStringId())) {
                LOGGER.debug("will not update username as this user already has the same username newUsername: "
                        + newUsername + ", existing username: " + user.username);
                return user;
            } else {
                LOGGER.error("cannot update username as some other user exist for newUsername: "
                        + newUsername);
                throw new VedantuException(VedantuErrorCode.USER_ALREADY_EXISTS);
            }
        }

        final boolean isOnlyCheck = false;

        user.username = newUsername;
        user.password = getUserPassHash(newUsername, newPassword, isOnlyCheck);

        save(user);

        LOGGER.info("updateUsername user: " + user);

        return user;
    }

    public User updateUserPassword(String userId, String newPassword) throws VedantuException {

        LOGGER.debug("updateUserPassword userId: " + userId);

        User user = getById(userId);
        LOGGER.debug("found user: " + user);

        if (null == user) {
            LOGGER.error("cannot update password as user does not exist for userId: " + userId);
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }

        if (user.authType == AuthType.EXT_AUTH_ORG) {
            throw new VedantuException(VedantuErrorCode.EXTERNAL_AUTH_SUPPORTED);
        }

        final boolean isOnlyCheck = false;

        user.password = getUserPassHash(user.username, newPassword, isOnlyCheck);
        user.isSysGenPassword = false;

        save(user);
        LOGGER.info("updateUserPassword password updated user: " + user);

        return user;
    }

    public User updateUser(String userId, String firstName, String lastName, String dob,
            Gender gender, String email, MutableBoolean isEmailVerificationNeeded)
            throws VedantuException {

        LOGGER.debug("updateUser userId: " + userId + ", firstName: " + firstName + ", lastName: "
                + lastName + ", dob: " + dob + ", gender: " + gender + ", email: " + email
                + ", isEmailVerificationNeeded: " + isEmailVerificationNeeded.getValue());

        User user = findUserById(userId);

        User testUser = getQuery().filter("email", email).get();

        if (null == user) {
            LOGGER.error("cannot update user as user does not exist for userId: " + userId);
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }

        if (null != testUser) {
            LOGGER.error("cannot update user email as user email already exist for userId: " + userId);
            throw new VedantuException(VedantuErrorCode.USER_ALREADY_EXISTS);
        }

        // TODO check with ujjawal whether to allow this fields to be updated

        if (user.authType == AuthType.EXT_AUTH_ORG) {
            throw new VedantuException(VedantuErrorCode.EXTERNAL_AUTH_SUPPORTED);
        }

        user.firstName = firstName;
        user.lastName = lastName;
        user.dob = dob;
        user.gender = gender;
        if (!StringUtils.equals(user.email, email)) {
            user.emailChangeReq = new EmailChangeReqInfo(email, UUID.randomUUID().toString());
            if (!user.isEmailVerified) {
                user.email = email;
                user.username = user.isOTPuser && user.isSysGenPassword == true ? email : user.username;
            }
            isEmailVerificationNeeded.setValue(true);
            LOGGER.debug("generated email verification code");
        }

        save(user);

        LOGGER.info("updateUser user: " + user);

        return user;
    }

    public boolean validateEmail(String userId, String emailVerificationCode, boolean isVerified)
            throws VedantuException {

        LOGGER.debug("validateEmail userId: " + userId + ", emailVerificationCode: "
                + emailVerificationCode + ", isVerified: " + isVerified);

        User user = findUserById(userId);

        if (null == user) {
            LOGGER.error("cannot validate email as user does not exist for userId: " + userId);
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        if (null == user.emailChangeReq) {
            LOGGER.debug("cannot validate email as user did not request email change for userId: "
                    + userId);
            throw new VedantuException(VedantuErrorCode.INVALID_VERIFICATION_CODE);
        }

        boolean isUpdated = false;
        if (StringUtils.equals(user.emailChangeReq.verificationCode, emailVerificationCode)) {
            LOGGER.debug("email update verified: " + isVerified);
            if (isVerified) {
                user.email = user.emailChangeReq.email;
                user.isEmailVerified = true;
            }
            user.emailChangeReq = null;
            save(user);
            isUpdated = true;

            LOGGER.info("validateEmail user: " + user);
        } else {
            LOGGER.debug("validateEmail emailVerificationCode mismatched");
            throw new VedantuException(VedantuErrorCode.INVALID_VERIFICATION_CODE);
        }

        return isUpdated;
    }

    public void validatePhoneNumber(String userId)
            throws VedantuException {
        User user = findUserById(userId);
        if (null == user) {
            LOGGER.error("cannot validate contact number, as user does not exist for userId: " + userId);
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        user.isPhoneVerified = true;
        save(user);
    }

    public boolean isPhoneVerified(String userId){
        User user = getById(userId);
        return user.isPhoneVerified;
    }

    public User findUser(String username) {

        LOGGER.debug("findUser username: " + username);

        User user = getQuery().filter("username", username).get();

        if (null == user) {
            LOGGER.debug("cannot find user for username: " + username);
        } else {
            LOGGER.info("findUser user: " + user);
        }

        return user;
    }

    private String getUserPassHash(String username, String password, boolean isOnlyCheck) {

        LOGGER.error("getUserPassHash username: " + username);
        String hashedPass = getHashed(
                UserSaltDAO.INSTANCE.getSaltedPassword(username, password, isOnlyCheck), "SHA-256");
        LOGGER.error("Hashed password for username: " + username + " : " + hashedPass);
        return hashedPass;
    }

    public User findUserById(String id) {

        LOGGER.debug("findUserById id: " + id);
        User user = getById(id);

        if (null == user) {
            LOGGER.error("cannot find user for id: " + id);
        } else {
            LOGGER.info("findUserById user: " + user);
        }

        return user;
    }

    public Map<String, ModelBasicInfo> getBasicInfoMap(Collection<String> ids) {

        if (CollectionUtils.isEmpty(ids)) {
            return new HashMap<String, ModelBasicInfo>();
        }
        return toBasicInfosMap(getByIds(ObjectIdUtils.toObjectIds(new ArrayList<String>(ids), true)));
    }

    public List<User> getUsers(List<String> userIds, String... fields) {

        Query<User> query = getQuery();

        query.criteria(FIELD_ID).in(ObjectIdUtils.toObjectIds(userIds));
        if(fields.length >0){
            query = query.retrievedFields(true, fields);
        }
        return query.asList();
    }

    public User unsetEmail(String userId) throws VedantuException {

        LOGGER.debug("unsetEmail userId: " + userId);

        User user = findUserById(userId);

        if (null == user) {
            LOGGER.error("cannot unset email of user as user does not exist for userId: " + userId);
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }

        if (null != user.emailChangeReq
                && !StringUtils.equals(user.email, user.emailChangeReq.email)) {
            user.emailChangeReq = null;
            LOGGER.debug("removed email verification request as it matches the specified email: "
                    + user.email);
        }
        user.email = StringUtils.EMPTY;
        user.isEmailVerified = false;

        save(user);

        LOGGER.info("unsetEmail user: " + user);

        return user;
    }

    public User generateForgotPasswordReq(String username) throws VedantuException {

        LOGGER.debug("generateForgotPasswordReq username: " + username);

        User user = UserDAO.INSTANCE.findUser(username);
        if (null == user) {
            LOGGER.debug("generateForgotPasswordReq user not found for username: " + username);
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        if (user.authType == AuthType.EXT_AUTH_ORG) {
            throw new VedantuException(VedantuErrorCode.EXTERNAL_AUTH_SUPPORTED);
        }

//        if (!user.isEmailVerified) {
//            LOGGER.debug("generateForgotPasswordReq user does not have a verified email user: "
//                    + user);
//            throw new VedantuException(VedantuErrorCode.USER_NO_VERIFIED_EMAIL);
//        }
        if (null == user.forgotPasswordReq) {
            user.forgotPasswordReq = new ForgotPasswordReqInfo(UUID.randomUUID().toString());
            save(user);
            LOGGER.debug("generateForgotPasswordReq saved user: " + user);
        } else {
            LOGGER.debug("generateForgotPasswordReq user already has a forgotPasswordReq for user: "
                    + user);
        }
        LOGGER.info("generateForgotPasswordReq user: " + user);
        return user;
    }

    public User updateUserForgottenPassword(String userId, String code, String newPassword)
            throws VedantuException {

        LOGGER.debug("updateUserForgottenPassword userId: " + userId + ", code: " + code);

        User user = getById(userId);
        LOGGER.debug("found user: " + user);

        if (null == user) {
            LOGGER.error("cannot update password as user does not exist for userId: " + userId);
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        if (null == user.forgotPasswordReq
                || !StringUtils.equals(user.forgotPasswordReq.verificationCode, code)) {
            LOGGER.error("cannot update password as forgotPassword verification code did not match for userId: "
                    + userId);
            throw new VedantuException(VedantuErrorCode.INVALID_VERIFICATION_CODE);
        }

        if (user.authType == AuthType.EXT_AUTH_ORG) {
            throw new VedantuException(VedantuErrorCode.EXTERNAL_AUTH_SUPPORTED);
        }

        final boolean isOnlyCheck = false;

        user.password = getUserPassHash(user.username, newPassword, isOnlyCheck);
        user.forgotPasswordReq = null;

        save(user);
        LOGGER.info("updateUserForgottenPassword password updated user: " + user);

        return user;
    }

    public User acceptTnC(String userId, boolean agrees, String version) throws VedantuException {

        LOGGER.debug("acceptTnC userId: " + userId + ", agrees: " + agrees + ", version: "
                + version);

        User user = getById(userId);
        LOGGER.debug("found user: " + user);

        if (null == user) {
            LOGGER.error("cannot acceptTnC as user does not exist for userId: " + userId);
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }

        user.tncAcceptance = new TnCAcceptance(agrees, version, System.currentTimeMillis());
        save(user);

        LOGGER.info("acceptTnC updated user: " + user);

        return user;
    }

    public String getUsername(String targetUserId) {
        User user = findUserById(targetUserId);
        return user.username;
    }
}
