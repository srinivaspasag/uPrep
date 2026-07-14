package com.vedantu.user.managers;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.ning.http.util.Base64;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.Configurations;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.constants.configuration.EmailConfigurationConstants;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.entity.storage.ImageSize;
import com.vedantu.commons.entity.storage.StorageResult;
import com.vedantu.commons.entity.storage.UserProfilePicEntityFileStorage;
import com.vedantu.commons.enums.AuthType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.MailCategory;
import com.vedantu.commons.http.URLGenerator;
import com.vedantu.commons.pojos.SecurityCredentials;
import com.vedantu.commons.utils.EncryptionUtils;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.commons.utils.ImageFilter;
import com.vedantu.commons.utils.VedantuStringUtils;
import com.vedantu.commons.utils.image.ImageGenerator;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuDBResult;
import com.vedantu.user.daos.UserDAO;
import com.vedantu.user.daos.UserEmailUnsubscriptionDAO;
import com.vedantu.user.event.details.EmailVerificationDetails;
import com.vedantu.user.event.details.ForgotPasswordDetails;
import com.vedantu.user.models.User;
import com.vedantu.user.models.UserEmailUnsubscription;
import com.vedantu.user.pojos.ForgotPasswordReqInfo;
import com.vedantu.user.pojos.SocialInfo;
import com.vedantu.user.pojos.UserAuthPojo;
import com.vedantu.user.pojos.UserEmailInfo;
import com.vedantu.user.pojos.UserEmailUnsubscriptionInfo;
import com.vedantu.user.pojos.UserExtendedInfo;
import com.vedantu.user.pojos.UserUnsubscribePojo;
import com.vedantu.user.pojos.requests.AcceptTnCReq;
import com.vedantu.user.pojos.requests.AddUserReq;
import com.vedantu.user.pojos.requests.ChangeUserPasswordReq;
import com.vedantu.user.pojos.requests.EmailSubscribeReq;
import com.vedantu.user.pojos.requests.GetUserEmailSubscriptionReq;
import com.vedantu.user.pojos.requests.GetUserSelfFullProfileReq;
import com.vedantu.user.pojos.requests.ResendEmailVerificationReq;
import com.vedantu.user.pojos.requests.SendForgotPasswordEmailReq;
import com.vedantu.user.pojos.requests.UnsetEmailReq;
import com.vedantu.user.pojos.requests.UnsubscribeReq;
import com.vedantu.user.pojos.requests.UpdateUserForgottenPasswordReq;
import com.vedantu.user.pojos.requests.UpdateUserPasswordReq;
import com.vedantu.user.pojos.requests.UpdateUserReq;
import com.vedantu.user.pojos.requests.UpdateUsernameReq;
import com.vedantu.user.pojos.requests.UploadProfilePicReq;
import com.vedantu.user.pojos.requests.UserAuthReq;
import com.vedantu.user.pojos.requests.UserDirectLoginReq;
import com.vedantu.user.pojos.requests.UserExistenceReq;
import com.vedantu.user.pojos.requests.ValidateEmailReq;
import com.vedantu.user.pojos.responses.AcceptTnCRes;
import com.vedantu.user.pojos.responses.AddUserRes;
import com.vedantu.user.pojos.responses.ChangeUserPasswordRes;
import com.vedantu.user.pojos.responses.GetUserSelfFullProfileRes;
import com.vedantu.user.pojos.responses.ResendEmailVerificationRes;
import com.vedantu.user.pojos.responses.SendForgotPasswordEmailRes;
import com.vedantu.user.pojos.responses.UnsetEmailRes;
import com.vedantu.user.pojos.responses.UpdateUserForgottenPasswordRes;
import com.vedantu.user.pojos.responses.UpdateUserPasswordRes;
import com.vedantu.user.pojos.responses.UpdateUserRes;
import com.vedantu.user.pojos.responses.UpdateUsernameRes;
import com.vedantu.user.pojos.responses.UploadProfilePicRes;
import com.vedantu.user.pojos.responses.UserAuthRes;
import com.vedantu.user.pojos.responses.UserDirectLoginRes;
import com.vedantu.user.pojos.responses.UserEmailUnsubscriptionRes;
import com.vedantu.user.pojos.responses.UserExistenceRes;
import com.vedantu.user.pojos.responses.ValidateEmailRes;

public class UserManager extends AbstractVedantuEventManager {

    private static final ALogger LOGGER      = Logger.of(UserManager.class);
    private static final String  TNC_VERSION = Play.application().configuration()
                                                     .getString("tnc.version");

    public static String getLatestTnC() {

        return TNC_VERSION;
    }

    public static UserAuthRes authenticateUser(UserAuthReq userAuthReq) throws VedantuException {

        User user = null;
        if (!userAuthReq.dl) {
            user = UserDAO.INSTANCE.authenticateUser(userAuthReq.getUsername(),
                    userAuthReq.password);
        } else {
            User userForKeys = UserDAO.INSTANCE.getById(userAuthReq.getUsername());
            if (userForKeys == null || userForKeys.credentials == null) {
                throw new VedantuException(VedantuErrorCode.ACCESS_DENIED);
            }

            UserAuthPojo userPojo = new UserAuthPojo();
            JSONObject json;
            try {
                LOGGER.debug("userName: " + userAuthReq.getUsername());
                String userPojoDecrypted = EncryptionUtils.decryptWithPrivateKey(
                        userAuthReq.password, userForKeys.credentials.getPrivateKey());
                LOGGER.debug("userPojodecrepted" + userPojoDecrypted);
                json = new JSONObject(userPojoDecrypted);

            } catch (JSONException e) {
                throw new VedantuException(VedantuErrorCode.ACCESS_DENIED);
            }
            userPojo.fromJSON(json);
            LOGGER.debug("user auth pojo " + userPojo);

            user = UserDAO.INSTANCE.authenticateUserWithSaltedPassword(userPojo.userName,
                    userPojo.password);
        }
        return getAuthResFromUser(user);
        // return userAuthRes;
    }

    public static UserAuthRes getAuthResFromUser(User user) {

        UserAuthRes userAuthRes = new UserAuthRes();

        if (null != user) {
            userAuthRes.id = user._getStringId();
            userAuthRes.firstName = user.firstName;
            userAuthRes.lastName = user.lastName;
            userAuthRes.latestTnCVersion = getLatestTnC();
            userAuthRes.needsTnCAcceptance = null == user.tncAcceptance
                    || !StringUtils.equalsIgnoreCase(getLatestTnC(), user.tncAcceptance.version);
            userAuthRes.acceptedTNCVersion = user.tncAcceptance != null ? user.tncAcceptance.version
                    : null;
            userAuthRes.thumbnail = user._getThumbnailUrl();
            userAuthRes.authType = user.authType;
            LOGGER.debug("user username: " + user.username + " authenticated with id: "
                    + userAuthRes.id);
        }
        return userAuthRes;
    }

    public static AddUserRes addUser(AddUserReq addUserReq) throws VedantuException {

        if (!VedantuStringUtils.isValidDOB(addUserReq.dob)) {
            throw new VedantuException(VedantuErrorCode.INVALID_DATE_FORMAT);
        }
        MutableBoolean isEmailVerificationNeeded = new MutableBoolean(false);

        // check if encryption keys are need to be generated

        SecurityCredentials credentials = EncryptionUtils.generateKeys();
        SocialInfo socialInfo = null;
        if (addUserReq.twitterHandle != null) {
            socialInfo = new SocialInfo();
            socialInfo.twitter = addUserReq.twitterHandle;
        }

        User user = UserDAO.INSTANCE.addUser(addUserReq.getUsername(), addUserReq.password,
                addUserReq.firstName, addUserReq.lastName, addUserReq.dob, addUserReq.gender,
                addUserReq.getEmail().trim(), isEmailVerificationNeeded, credentials, socialInfo,
                addUserReq.authType,addUserReq.isPhoneVerified,addUserReq.isSysGenPassword,addUserReq.isOTPuser);
        AddUserRes addUserRes = new AddUserRes();
        if (null != user) {
            addUserRes.id = user._getStringId();
            LOGGER.debug("user created with id: " + addUserRes.id);
            if (isEmailVerificationNeeded.getValue()) {
                generateEmailVerificationEvent(user, addUserReq.orgId,addUserReq.callingAppId);
            }
        }
        return addUserRes;
    }

    public static UserExistenceRes doesUserExists(UserExistenceReq userExistenceReq)
            throws VedantuException {

        boolean doesEmailExists = UserDAO.INSTANCE.doesUserExists(userExistenceReq.email);
        UserExistenceRes userExistenceRes = new UserExistenceRes();
        userExistenceRes.doesEmailExists = doesEmailExists;
        return userExistenceRes;
    }

    public static UpdateUsernameRes updateUsername(UpdateUsernameReq updateUsernameReq)
            throws VedantuException {

        // TODO throw if authtype is EXT
        User user = UserDAO.INSTANCE.updateUsername(updateUsernameReq.targetUserId,
                updateUsernameReq.getNewUsername(), updateUsernameReq.newPassword);
        UpdateUsernameRes updateUsernameRes = new UpdateUsernameRes();
        updateUsernameRes.done = null != user;
        return updateUsernameRes;
    }

    public static UpdateUserPasswordRes updateUserPassword(
            UpdateUserPasswordReq updateUserPasswordReq) throws VedantuException {

        User user = UserDAO.INSTANCE.updateUserPassword(updateUserPasswordReq.targetUserId,
                updateUserPasswordReq.newPassword);
        UpdateUserPasswordRes updateUserPasswordRes = new UpdateUserPasswordRes();
        updateUserPasswordRes.done = null != user;
        return updateUserPasswordRes;
    }

    public static ChangeUserPasswordRes changeUserPassword(ChangeUserPasswordReq request) throws VedantuException{

        ChangeUserPasswordRes response = new ChangeUserPasswordRes();
        try {
            String userName = UserDAO.INSTANCE.getUsername(request.targetUserId);
            LOGGER.error("changeUserPassword: "+userName);
            UserDAO.INSTANCE.authenticateUser(userName, request.oldPassword);
            UserDAO.INSTANCE.updateUserPassword(request.targetUserId, request.newPassword);
        } catch (VedantuException e) {
            throw new VedantuException(VedantuErrorCode.AUTHENTICATION_FAILED,"Invalid Current Password");
        }
        response.done = true;
        return response;
    }

    public static UpdateUserRes updateUser(UpdateUserReq updateUserReq) throws VedantuException {

        if (!VedantuStringUtils.isValidDOB(updateUserReq.dob)) {
            throw new VedantuException(VedantuErrorCode.INVALID_DATE_FORMAT);
        }
        MutableBoolean isEmailVerificationNeeded = new MutableBoolean(false);
        User user = UserDAO.INSTANCE.updateUser(updateUserReq.targetUserId,
                updateUserReq.firstName, updateUserReq.lastName, updateUserReq.dob,
                updateUserReq.gender, updateUserReq.getEmail(), isEmailVerificationNeeded);
        if(StringUtils.isNotEmpty(updateUserReq.password) && null != updateUserReq.password){
            user = UserDAO.INSTANCE.updateUserPassword(updateUserReq.targetUserId, updateUserReq.password);
        }
        if (isEmailVerificationNeeded.getValue()) {
            generateEmailVerificationEvent(user, updateUserReq.orgId, updateUserReq.callingAppId);
        }
        UpdateUserRes updateUserRes = new UpdateUserRes(user.toBasicInfo());
        updateUserRes.username = user.username;
        return updateUserRes;
    }

    private static boolean generateEmailVerificationEvent(User user, String orgId,String appId)
            throws VedantuException {

        // TODO: verification of user email needs to be done through event
        final String emailVerifyHost = Play.application().configuration()
                .getString(Configurations.getAppLearnHost(appId));
        final String emailVerifyEndPoint = Play.application().configuration()
                .getString(EmailConfigurationConstants.EMAIL_VERIFICATION_ENDPOINT);

        URLGenerator generator = new URLGenerator();
        generator.host = emailVerifyHost;
        generator.endPoint = emailVerifyEndPoint;
        generator.protocol = Play.application().configuration()
                .getString(Configurations.APP_PROTOCOL);

        Map<String, Object> params = new HashMap<String, Object>();
        try {
            params.put("code", URLEncoder.encode(user.emailChangeReq.verificationCode, "UTF-8"));
            params.put("userId", URLEncoder.encode(user._getStringId(), "UTF-8"));
            params.put("email", URLEncoder.encode(user.emailChangeReq.email, "UTF-8"));
            if (StringUtils.isNotEmpty(orgId)) {
                params.put("orgId", URLEncoder.encode(orgId, "UTF-8"));
            }
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error(" unsupported URL found", ex);
            throw new VedantuException(VedantuErrorCode.INVALID_URL);
        }

        generator.params = params;

        EmailVerificationDetails details;
        try {
            details = new EmailVerificationDetails();
        } catch (ClassNotFoundException e) {
            Logger.debug(" Not found email details", e);
            throw new VedantuException(VedantuErrorCode.EMAIL_CAN_NOT_BE_SEND);
        }

        details.user = new UserEmailInfo();
        details.user.fromUserExtendedInfo((UserExtendedInfo) user.toExtendedInfo());

        details.verificationLink = generator.generate();
        details.orgId = orgId;
        details.addRecepient(details.user.getFullName(), user.emailChangeReq.email);
        generateEventAysc(user._getStringId(), details, EventType.SEND_INSTANT_EMAIL);

        return true;
    }

    public static ValidateEmailRes validateEmail(ValidateEmailReq validateEmailReq)
            throws VedantuException {

        boolean done = UserDAO.INSTANCE.validateEmail(validateEmailReq.userId,
                validateEmailReq.code, validateEmailReq.isVerified);
        ValidateEmailRes validateEmailRes = new ValidateEmailRes(done);

        return validateEmailRes;
    }

    public static GetUserSelfFullProfileRes getUserSelfFullProfile(
            GetUserSelfFullProfileReq getUserSelfFullProfileReq) throws VedantuException {

        LOGGER.debug("getUserSelfFullProfile userId: " + getUserSelfFullProfileReq.userId);

        UserExtendedInfo userExtendedInfo = UserDAO.INSTANCE
                .getExtendedInfo(getUserSelfFullProfileReq.userId);
        if (null == userExtendedInfo) {
            LOGGER.error("getUserSelfFullProfile user not found for userId: "
                    + getUserSelfFullProfileReq.userId);
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }

        GetUserSelfFullProfileRes getUserSelfFullProfileRes = new GetUserSelfFullProfileRes();
        getUserSelfFullProfileRes.info = userExtendedInfo;
        getUserSelfFullProfileRes.unsubscribeInfo = getUserEmailSubscriptions(
                getUserSelfFullProfileReq.userId, getUserSelfFullProfileReq.userId);

        LOGGER.info("getUserSelfFullProfile userExtendedInfo: " + userExtendedInfo);

        return getUserSelfFullProfileRes;
    }

    public static GetUserSelfFullProfileRes getUserFullProfile(
            String userId) throws VedantuException {

        LOGGER.debug("getUserSelfFullProfile userId: " + userId);

        UserExtendedInfo userExtendedInfo = UserDAO.INSTANCE
                .getExtendedInfo(userId);
        if (null == userExtendedInfo) {
            LOGGER.error("getUserSelfFullProfile user not found for userId: "
                    + userId);
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }

        GetUserSelfFullProfileRes getUserSelfFullProfileRes = new GetUserSelfFullProfileRes();
        getUserSelfFullProfileRes.info = userExtendedInfo;

        LOGGER.info("getUserSelfFullProfile userExtendedInfo: " + userExtendedInfo);

        return getUserSelfFullProfileRes;
    }

    public static ResendEmailVerificationRes resendEmailVerification(
            ResendEmailVerificationReq resendEmailVerificationReq) throws VedantuException {

        User user = UserDAO.INSTANCE.findUserById(resendEmailVerificationReq.userId);
        if (null == user) {
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        boolean generatedEmail = generateEmailVerificationEvent(user,
                resendEmailVerificationReq.orgId,resendEmailVerificationReq.callingAppId);
        ResendEmailVerificationRes resendEmailVerificationRes = new ResendEmailVerificationRes();
        resendEmailVerificationRes.done = generatedEmail;
        return resendEmailVerificationRes;
    }

    public static UnsetEmailRes unsetEmail(UnsetEmailReq unsetEmailReq) throws VedantuException {

        User user = UserDAO.INSTANCE.unsetEmail(unsetEmailReq.userId);
        UnsetEmailRes unsetEmailRes = new UnsetEmailRes();
        unsetEmailRes.done = null != user;

        return unsetEmailRes;
    }

    public static String generatePasswordUpdate(String userId, String orgId, String appId)
            throws VedantuException {

        User user = UserDAO.INSTANCE.getById(userId);
        if (user == null) {
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }

        if (user.forgotPasswordReq == null) {
            user.forgotPasswordReq = new ForgotPasswordReqInfo(UUID.randomUUID().toString());
            UserDAO.INSTANCE.save(user);
            LOGGER.debug("updated password saved user: " + user);
        } else {
            LOGGER.debug("generateForgotPasswordReq user already has a forgotPasswordReq for user: "
                    + user);
        }

        return generatePasswordResetURL(user, orgId, appId);

    }

    public static String generatePasswordResetURL(User user, String orgId,String appId) throws VedantuException {

        final String emailVerifyHost = Play.application().configuration()
                .getString(Configurations.getAppLearnHost(appId));

        final String emailVerifyEndPoint = Play.application().configuration()
                .getString(EmailConfigurationConstants.EMAIL_FORGOTPASSWORD_ENDPOINT);

        URLGenerator generator = new URLGenerator();
        generator.host = emailVerifyHost;
        generator.endPoint = emailVerifyEndPoint;
        generator.protocol = Play.application().configuration()
                .getString(Configurations.APP_PROTOCOL);

        Map<String, Object> params = new HashMap<String, Object>();
        try {
            params.put("code", URLEncoder.encode(user.forgotPasswordReq.verificationCode, "UTF-8"));
            params.put("userId", URLEncoder.encode(user._getStringId(), "UTF-8"));
            params.put("email", URLEncoder.encode(user.email, "UTF-8"));
            if (StringUtils.isNotEmpty(orgId)) {
                LOGGER.debug("Update happen in organization" + orgId);
                params.put("orgId", URLEncoder.encode(orgId, "UTF-8"));
            }
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error(" unsupported URL found", ex);
            throw new VedantuException(VedantuErrorCode.INVALID_URL);
        }

        generator.params = params;
        return generator.generate();
    }

    private static boolean generateSendForgotPasswordEmailEvent(User user, String orgId,String appId)
            throws VedantuException {

        // TODO: verification of user email needs to be done through event
        ForgotPasswordDetails details;
        try {
            details = new ForgotPasswordDetails();
        } catch (ClassNotFoundException e) {
            Logger.debug(" Not found email details", e);
            throw new VedantuException(VedantuErrorCode.EMAIL_CAN_NOT_BE_SEND);
        }

        details.user = new UserEmailInfo();
        details.user.fromUserExtendedInfo((UserExtendedInfo) user.toExtendedInfo());

        details.verificationLink = generatePasswordResetURL(user, orgId,appId);
        details.addRecepient(details.user.getFullName(), user.email);

        generateEventAysc(user._getStringId(), details, EventType.SEND_INSTANT_EMAIL);
        return false;
    }

    public static SendForgotPasswordEmailRes sendForgotPasswordEmail(
            SendForgotPasswordEmailReq sendForgotPasswordEmailReq) throws VedantuException {

        // Internally does check for valid email
        User user = UserDAO.INSTANCE.generateForgotPasswordReq(sendForgotPasswordEmailReq
                .getUsername());

        if (user.authType == AuthType.EXT_AUTH_ORG) {
            throw new VedantuException(VedantuErrorCode.EXTERNAL_AUTH_SUPPORTED);
        }

        boolean generatedEmail = generateSendForgotPasswordEmailEvent(user,
                sendForgotPasswordEmailReq.getOrgId(),sendForgotPasswordEmailReq.callingAppId);

        SendForgotPasswordEmailRes sendForgotPasswordEmailRes = new SendForgotPasswordEmailRes();
        sendForgotPasswordEmailRes.done = generatedEmail;

        return sendForgotPasswordEmailRes;
    }

    public static UpdateUserForgottenPasswordRes updateUserForgottenPassword(
            UpdateUserForgottenPasswordReq updateUserForgottenPasswordReq) throws VedantuException {

        User user = UserDAO.INSTANCE.updateUserForgottenPassword(
                updateUserForgottenPasswordReq.userId, updateUserForgottenPasswordReq.code,
                updateUserForgottenPasswordReq.newPassword);
        UpdateUserForgottenPasswordRes updateUserForgottenPasswordRes = new UpdateUserForgottenPasswordRes(
                null != user);
        return updateUserForgottenPasswordRes;
    }

    public static AcceptTnCRes acceptTnC(AcceptTnCReq acceptTnCReq) throws VedantuException {

        User user = UserDAO.INSTANCE.acceptTnC(acceptTnCReq.userId, acceptTnCReq.agrees,
                acceptTnCReq.version);
        AcceptTnCRes acceptTnCRes = new AcceptTnCRes(user.tncAcceptance.agrees);
        return acceptTnCRes;
    }

    public static UploadProfilePicRes uploadProfilePic(UploadProfilePicReq uploadProfilePicReq)
            throws VedantuException {

        ImageFilter filter = new ImageFilter();
        boolean isImg = filter.accept(new File(uploadProfilePicReq.fileName));
        if (!isImg) {
            throw new VedantuException(VedantuErrorCode.UNSUPPORTED_FILE_TYPE, "not an image file");
        }

        User user = UserDAO.INSTANCE.findUserById(uploadProfilePicReq.userId);
        if (null == user) {
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }

        final String imageName = user._getStringId();

        UserProfilePicEntityFileStorage picStorage = new UserProfilePicEntityFileStorage();
        try {
            StorageResult picStorageResult = picStorage.storeImage(imageName,
                    uploadProfilePicReq.inputFile, FileCategory.ORIGINAL, ImageSize.ORIGINAL, null);
            LOGGER.debug(picStorageResult.toString());

            for (ImageSize imageSize : new ImageSize[] { ImageSize.MEDIUM, ImageSize.SMALL,
                    ImageSize.EXTRA_SMALL }) {
                File convertedFile = ImageGenerator.createImage(uploadProfilePicReq.inputFile,
                        imageSize, uploadProfilePicReq.fileName);
                picStorageResult = picStorage.storeImage(imageName, convertedFile,
                        FileCategory.CONVERTED, imageSize, null);
                LOGGER.debug(picStorageResult.toString());

                FileUtils.deleteFile(convertedFile.getName(), convertedFile);
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new VedantuException(VedantuErrorCode.FILE_UPLOAD_ERROR);
        }

        user.thumbnail = imageName;
        UserDAO.INSTANCE.save(user);

        String thumbnailUrl = user._getThumbnailUrl();
        UploadProfilePicRes uploadProfilePicRes = new UploadProfilePicRes(true, thumbnailUrl);
        return uploadProfilePicRes;
    }

    public static String getPrivateKey(String userId) throws VedantuException {

        User user = UserDAO.INSTANCE.getById(userId);
        if (user == null) {
            return null;
        }
        SecurityCredentials credentials = user.credentials;
        if (credentials == null) {
            credentials = setCredentials(user);
        }
        // DatatypeConverter.printHexBinary(credentials.getPrivateKey())
        return Base64.encode(credentials.getPrivateKey());
    }

    public static String getPublicKey(String userId) throws VedantuException {

        User user = UserDAO.INSTANCE.getById(userId);
        if (user == null) {
            return null;
        }
        SecurityCredentials credentials = user.credentials;
        if (credentials == null) {
            credentials = setCredentials(user);
        }

        return DatatypeConverter.printHexBinary(credentials.getPublicKey());
    }

    private static synchronized SecurityCredentials setCredentials(User user)
            throws VedantuException {

        if (user.credentials != null) {
            return user.credentials;

        }
        user.credentials = EncryptionUtils.generateKeys();
        UserDAO.INSTANCE.save(user);
        return user.credentials;
    }

    public static UserDirectLoginRes getUserDirectLogin(UserDirectLoginReq request)
            throws VedantuException {

        User user = UserDAO.INSTANCE.getById(request.targetUserId);

        if (user == null) {
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        if (user.credentials == null) {
            user.credentials = EncryptionUtils.generateKeys();
            UserDAO.INSTANCE.updateModel(user, Arrays.asList(User.FIELD_CREDENTIALS));

        }
        UserAuthPojo userPojo = new UserAuthPojo();
        String[] userSplit = StringUtils.split(user.username, ":");

        if (userSplit.length > 1) {
            if (!userSplit[0].equals(request.orgId)) {
                throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
            }
            userPojo.orgId = userSplit[1];
            userPojo.isMemberAuth = true;
        }

        userPojo.orgId = request.orgId;
        userPojo.userName = user.username;
        userPojo.password = user.password;

        String userPojoEnc = null;
        try {
            String userPojoJSON = null;
            userPojoJSON = userPojo.toJSON().toString();

            userPojoEnc = EncryptionUtils.encryptWithPublicKey(userPojoJSON,
                    user.credentials.getPublicKey());
            LOGGER.debug(userPojo + " user pojo encrypted " + userPojoEnc);

        } catch (JSONException e) {
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
        }

        UserDirectLoginRes response = new UserDirectLoginRes();
        response.password = userPojoEnc;
        response.username = user._getStringId();

        return response;

    }

    public static UserEmailUnsubscriptionRes getUserEmailSubscriptions(
            GetUserEmailSubscriptionReq request) throws VedantuException {

        UserEmailUnsubscriptionRes response = new UserEmailUnsubscriptionRes();
        response.info = getUserEmailSubscriptions(request.userId, request.targetUserId);
        return response;
    }

    public static UserEmailUnsubscriptionInfo getUserEmailSubscriptions(String userId,
            String targetUserId) throws VedantuException {

        User user = UserDAO.INSTANCE.getById(targetUserId);

        if (user == null) {
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        if (user.credentials == null) {
            user.credentials = EncryptionUtils.generateKeys();
            UserDAO.INSTANCE.updateModel(user, Arrays.asList(User.FIELD_CREDENTIALS));

        }

        if (StringUtils.isNotEmpty(user.email)) {
            UserEmailUnsubscription model = UserEmailUnsubscriptionDAO.INSTANCE.findBy(
                    user._getStringId(), user.email);
            if (model != null) {
                return (UserEmailUnsubscriptionInfo) model.toExtendedInfo();
            }
        }

        return null;

    }

    public static UnsetEmailRes unsubscribe(UnsubscribeReq request) throws VedantuException {

        LOGGER.debug(" Unsubcribing external " + request.external);
        User user = UserDAO.INSTANCE.getById(request.targetUserId);

        if (user == null) {
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        if (user.credentials == null) {
            user.credentials = EncryptionUtils.generateKeys();
            UserDAO.INSTANCE.updateModel(user, Arrays.asList(User.FIELD_CREDENTIALS));

        }

        String restrictedEmail = user.email;

        MailCategory category = MailCategory.UNKNOWN;
        if (request.external) {
            UserUnsubscribePojo userPojo = new UserUnsubscribePojo();

            JSONObject json;
            try {
                LOGGER.debug("Password" + request.mailCategory);
                String userPojoDecrypted = EncryptionUtils.decryptWithPrivateKey(
                        request.mailCategory, user.credentials.getPrivateKey());
                LOGGER.debug("userPojodecrepted" + userPojoDecrypted);
                json = new JSONObject(userPojoDecrypted);

            } catch (JSONException e) {
                throw new VedantuException(VedantuErrorCode.ACCESS_DENIED);
            }
            userPojo.fromJSON(json);
            restrictedEmail = userPojo.email;
            category = userPojo.category;

        } else {
            category = MailCategory.valueOfKey(request.mailCategory);
        }

        if (category == MailCategory.UNKNOWN) {
            throw new VedantuException(VedantuErrorCode.EMAIL_UNSUBSCRIPTION_FAILED);
        }

        UnsetEmailRes response = new UnsetEmailRes();
        if (UserEmailUnsubscriptionDAO.INSTANCE.isEmailAllowed(request.targetUserId,
                restrictedEmail, category)) {
            UserEmailUnsubscriptionDAO.INSTANCE.restrictEmail(request.userId, request.targetUserId,
                    restrictedEmail, request.reason, category);
        } else {
            LOGGER.debug("Already restricted ");
            response.done = false;

            throw new VedantuException(VedantuErrorCode.ALREADY_UNSUBSCRIBED);
        }

        response.done = true;

        return response;

    }

    public static UnsetEmailRes subscribe(EmailSubscribeReq request) throws VedantuException {

        LOGGER.debug(" Unsubcribing");
        User user = UserDAO.INSTANCE.getById(request.targetUserId);

        if (user == null) {
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        if (user.credentials == null) {
            user.credentials = EncryptionUtils.generateKeys();
            UserDAO.INSTANCE.updateModel(user, Arrays.asList(User.FIELD_CREDENTIALS));

        }

        String restrictedEmail = user.email;
        MailCategory category = MailCategory.valueOfKey(request.mailCategory);

        if (category == MailCategory.UNKNOWN) {
            throw new VedantuException(VedantuErrorCode.EMAIL_UNSUBSCRIPTION_FAILED);
        }
        UnsetEmailRes response = new UnsetEmailRes();

        response.done = UserEmailUnsubscriptionDAO.INSTANCE.allowEmails(request.userId,
                restrictedEmail, category);

        return response;

    }

    public static String getUnsubscribeAccessCode(String userId, MailCategory category)
            throws VedantuException {

        if (category == null) {
            return null;
        }
        User user = UserDAO.INSTANCE.getById(userId);

        if (user == null) {
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        if (user.credentials == null) {
            user.credentials = EncryptionUtils.generateKeys();
            UserDAO.INSTANCE.updateModel(user, Arrays.asList(User.FIELD_CREDENTIALS));

        }
        UserUnsubscribePojo userPojo = new UserUnsubscribePojo();

        userPojo.userId = userId;
        userPojo.email = user.email;
        userPojo.category = category;

        String userPojoEnc = null;

        try {
            String userPojoJSON = null;
            userPojoJSON = userPojo.toJSON().toString();

            userPojoEnc = EncryptionUtils.encryptWithPublicKey(userPojoJSON,
                    user.credentials.getPublicKey());
            LOGGER.debug(userPojo + " user pojo encrypted " + userPojoEnc);

        } catch (JSONException e) {
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
        }
        return userPojoEnc;
    }

    public static Set<String> getExistingUsernames(Set<String> usernames) {

        Set<String> lowercaseUserNames = new HashSet<String>();
        for (String username : usernames) {
            lowercaseUserNames.add(StringUtils.lowerCase(username).trim());
        }
        Set<String> existingUsernames = new HashSet<String>();
        DBObject query = new BasicDBObject(ConstantsGlobal.USERNAME, new BasicDBObject(
                MongoManager.IN_QUERY, lowercaseUserNames.toArray()));
        DBObject fields = MongoManager.getFieldsDBObject(Arrays.asList(ConstantsGlobal.USERNAME),
                MongoManager.INCLUDE_FIELD);
        VedantuDBResult<User> tResults = UserDAO.INSTANCE.getInfos(query, fields,
                MongoManager.NO_START, MongoManager.NO_LIMIT, null);
        for (User t : tResults.results) {
            existingUsernames.add(t.username);
        }
        return existingUsernames;
    }
}
