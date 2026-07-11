package com.lms.user.vedantu.service.impl;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.utils.*;
import com.lms.common.vedantu.commons.pojos.requests.SecurityCredentials;
import com.lms.common.vedantu.constants.EmailConfigurationConstants;
import com.lms.common.vedantu.constants.HardCodedConstants;
import com.lms.common.vedantu.constants.config.Configurations;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.entity.storage.FileCategory;
import com.lms.common.vedantu.entity.storage.StorageResult;
import com.lms.common.vedantu.entity.storage.UserProfilePicEntityFileStorage;
import com.lms.common.vedantu.enums.*;
import com.lms.common.vedantu.http.URLGenerator;
import com.lms.common.vedantu.mongo.FileMetaInfo;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.user.vedantu.dao.UserDao;
import com.lms.user.vedantu.service.UserService;
import com.lms.user.vedantu.user.dto.UserDto;
import com.lms.user.vedantu.user.events.EmailVerificationDetails;
import com.lms.user.vedantu.user.events.ForgotPasswordDetails;
import com.lms.user.vedantu.user.model.User;
import com.lms.user.vedantu.user.model.UserEmailUnsubscription;
import com.lms.user.vedantu.user.model.UserSalt;
import com.lms.user.vedantu.user.pojo.*;
import com.lms.user.vedantu.user.pojo.responce.*;
import com.lms.user.vedantu.user.repository.FileMetaInfoRepo;
import com.lms.user.vedantu.user.repository.UserEmailUnSubScriptionRepo;
import com.lms.user.vedantu.user.repository.UserRepo;
import com.lms.user.vedantu.user.repository.UserSaltrepo;
import com.lms.user.vedantu.user.requests.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;


@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final String  TNC_VERSION = "tnc.version";

    public static String getLatestTnC() {

        return TNC_VERSION;
    }

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserEmailUnSubScriptionRepo userEmailUnSubScriptionRepo;

    @Autowired
    private UserSaltrepo userSaltrepo;

    @Autowired
    private FileMetaInfoRepo fileMetaInfoRepo;

    @Autowired
    private GridFsOperations gridFsOperations;

    private static final String SYSTEM_SALT = "/vdntu/";
    @Autowired
    private ImageDisplayURLUtil imageDisplayURLUtil;
    @Autowired
    private UserProfilePicEntityFileStorage picStorage;
    @Autowired
    private EventUtil eventUtil;

    @Override
    public VedantuResponse authenticateUser(UserAuthReq userAuthReq) {
        logger.info("authenticateUser" + userAuthReq);
        if (userAuthReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        UserAuthRes userAuthRes = null;
        userAuthRes = getauthenticateUser(userAuthReq);
        return new VedantuResponse(userAuthRes);
    }

    private  UserAuthRes getauthenticateUser(UserAuthReq userAuthReq) throws VedantuException {
        User user = null;
        if (!userAuthReq.isDl()) {
            user =userDao.authenticateUser(userAuthReq.getUsername(), userAuthReq.getPassword());

        } else {
            Optional<User> userForKeys = userRepo.findByUsername(userAuthReq.getUsername());
            if (!userForKeys.isPresent() || userForKeys.get().getCredentials() == null) {
                throw new VedantuException(VedantuErrorCode.ACCESS_DENIED);
            }

            UserAuthPojo userPojo = new UserAuthPojo();
            JSONObject json;
            try {
                logger.debug("userName: " + userAuthReq.getUsername());
                String userPojoDecrypted = EncryptionUtils.decryptWithPrivateKey(
                        userAuthReq.getPassword(), userForKeys.get().getCredentials().getPrivateKey());
                logger.debug("userPojodecrepted" + userPojoDecrypted);
                json = new JSONObject(userPojoDecrypted);

            } catch (JSONException e) {
                throw new VedantuException(VedantuErrorCode.ACCESS_DENIED);
            }
            userPojo.fromJSON(json);
            logger.debug("user auth pojo " + userPojo);

            user = userDao.authenticateUserWithSaltedPassword(userPojo.getUserName(),
                    userPojo.getPassword());
        }
        return getAuthResFromUser(user);
    }

    private UserAuthRes getAuthResFromUser(User user) {
        UserAuthRes userAuthRes = new UserAuthRes();

        if (null != user) {
            userAuthRes.setId(user._getStringId());
            userAuthRes.setFirstName(user.getFirstName());
            userAuthRes.setLastName(user.getLastName());
            userAuthRes.setLatestTnCVersion(getLatestTnC());
            boolean b=false;
if(user.getTncAcceptance()==null){
    b=true;
}else if(user.getTncAcceptance().getVersion()==null||!user.getTncAcceptance().getVersion().equals(getLatestTnC())){
                b=true;

}
            userAuthRes.setNeedsTnCAcceptance(b);

            userAuthRes.setAcceptedTNCVersion(user.getTncAcceptance()!=null?user.getTncAcceptance().getVersion():null);

            userAuthRes.setThumbnail(user._getThumbnailUrl());
            userAuthRes.setAuthType(user.getAuthType());
            logger.debug("user username: " + user.getUsername() + " authenticated with id: "
                    + userAuthRes.getId());
        }
        return userAuthRes;
    }

    @Override
    public VedantuResponse doesUserExists(UserExistenceReq userExistenceReq) {
        if (userExistenceReq == null || userExistenceReq.getEmail() == null) {
            throw new VedantuException(VedantuErrorCode.EMAIL_SHOULD_MANDATORY, "email should be mandatory");
        }
        boolean doesEmailExists = userDao.doesUserExists(userExistenceReq);
        UserExistenceRes userExistenceRes = new UserExistenceRes();
        userExistenceRes.doesEmailExists = doesEmailExists;
        return new VedantuResponse(userExistenceRes);
    }

    @Override
    public VedantuResponse updateUsername(UpdateUsernameReq updateUsernameReq) throws VedantuException {
        if (updateUsernameReq == null) {
            throw new VedantuException(VedantuErrorCode.SHOULD_NOT_BE_NULL,"request should not be null") ;
        }
        return userDao.updateUsername(updateUsernameReq);
    }

    @Override
    public VedantuResponse updateUserPassword(UpdateUserPasswordReq updateUserPasswordReq) throws VedantuException {
        if (updateUserPasswordReq == null) {
            throw new VedantuException(VedantuErrorCode.SHOULD_NOT_BE_NULL,"request should not be null") ;
        }
        return userDao.updateUserPassword(updateUserPasswordReq);
    }

    @Override
    public VedantuResponse changeUserPassword(ChangeUserPasswordReq changeUserPasswordReq) throws VedantuException {


            if(changeUserPasswordReq==null){
                throw new VedantuException(VedantuErrorCode.SHOULD_NOT_BE_NULL,"request should not be null") ;
            }
            return userDao.changeUserPasword(changeUserPasswordReq);

    }

    @Override
    public VedantuResponse addUser(AddUserReq addUserReq) throws VedantuException {
        if(addUserReq==null){
            throw new VedantuException(VedantuErrorCode.SHOULD_NOT_BE_NULL,"request should not be null") ;
        }


            if (!VedantuStringUtils.isValidDOB(addUserReq.dob)) {
                throw new VedantuException(VedantuErrorCode.INVALID_DATE_FORMAT);
            }
            AtomicBoolean isEmailVerificationNeeded = new AtomicBoolean(false);

            // check if encryption keys are need to be generated

            SecurityCredentials credentials = EncryptionUtils.generateKeys();
            SocialInfo socialInfo = null;
            if (addUserReq.twitterHandle != null) {
                socialInfo = new SocialInfo();
                socialInfo.twitter = addUserReq.twitterHandle;
            }
            User user=userDao.addUser(addUserReq,isEmailVerificationNeeded,credentials,socialInfo);
            AddUserRes addUserRes = new AddUserRes();
            if (null != user) {
                addUserRes.setId(user.getId().toString());
                logger.debug("user created with id: " + addUserRes.getId());
                if (isEmailVerificationNeeded.get()) {
                   generateEmailVerificationEvent(user, addUserReq.getOrgId(),addUserReq.getCallingAppId());
                }
            }
            return new VedantuResponse(addUserRes);
        }

    @Override
    public VedantuResponse updateUser(UpdateUserReq updateUserReq) throws VedantuException {
        if(updateUserReq.getTargetUserId()==null){
            throw new VedantuException(VedantuErrorCode.SHOULD_NOT_BE_NULL,"userId is required ");
        }
        if (!VedantuStringUtils.isValidDOB(updateUserReq.dob)) {
            throw new VedantuException(VedantuErrorCode.INVALID_DATE_FORMAT);
        }
        AtomicBoolean isEmailVerificationNeeded = new AtomicBoolean(false);

        User user = userDao.updateUser(updateUserReq, isEmailVerificationNeeded);


        if (updateUserReq.getPassword() != null && updateUserReq.getPassword().isEmpty()) {
            UserDto userDto = userDao.updateUserPassword(updateUserReq.getTargetUserId(), updateUserReq.getPassword());
            user.setPassword(userDto.getPassword());
        }
        if (isEmailVerificationNeeded.get()) {
            generateEmailVerificationEvent(user, updateUserReq.getOrgId(), updateUserReq.getCallingAppId());
        }
        // UpdateUserRes updateUserRes = new UpdateUserRes(user);*/
        UserDto userDto = userDao.convertToUserDto(user);

        return new VedantuResponse(userDto);
    }

    @Override
    public VedantuResponse getUserSelfFullProfile(GetUserSelfFullProfileReq getUserSelfFullProfileReq) throws VedantuException {

        if(getUserSelfFullProfileReq.getUserId()==null){
                throw new VedantuException(VedantuErrorCode.SHOULD_NOT_BE_NULL,"UserId required");
            }


        if (getUserSelfFullProfileReq==null) {
             throw new  VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        GetUserSelfFullProfileRes getUserSelfFullProfileRes = null;

            getUserSelfFullProfileRes = getSelfFullInFo(getUserSelfFullProfileReq);


        return new VedantuResponse(getUserSelfFullProfileRes);

    }

    @Override
    public VedantuResponse validateEmail(ValidateEmailReq validateEmailReq) throws VedantuException {

        if (validateEmailReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        ValidateEmailRes   validateEmailRes = checkValidateEmail(validateEmailReq);

        return new VedantuResponse(validateEmailRes);

    }

    @Override
    public VedantuResponse resendEmailVerification(ResendEmailVerificationReq resendEmailVerificationReq) {

        if (resendEmailVerificationReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);

        }

        ResendEmailVerificationRes resendEmailVerificationRes = null;

            resendEmailVerificationRes = getResendEmailVerification(resendEmailVerificationReq);

        return new VedantuResponse(resendEmailVerificationRes);

    }

    @Override
    public VedantuResponse unsetEmail(UnsetEmailReq unsetEmailReq) throws VedantuException {
        if (unsetEmailReq==null) {
           throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        UnsetEmailRes unsetEmailRes = getUnsetEmail(unsetEmailReq);

        return new VedantuResponse(unsetEmailRes);

    }

    @Override
    public VedantuResponse sendForgotPasswordMail(SendForgotPasswordEmailReq sendForgotPasswordEmailReq) throws VedantuException {

        User user =generateForgotPasswordReq(sendForgotPasswordEmailReq.getUsername());

        if (user.authType == AuthType.EXT_AUTH_ORG) {
            throw new VedantuException(VedantuErrorCode.EXTERNAL_AUTH_SUPPORTED);
        }
//TODO generate mail Event;
        boolean generatedEmail = generateSendForgotPasswordEmailEvent(user,
                sendForgotPasswordEmailReq.getOrgId(),sendForgotPasswordEmailReq.callingAppId);

        SendForgotPasswordEmailRes sendForgotPasswordEmailRes = new SendForgotPasswordEmailRes();
        sendForgotPasswordEmailRes.done = generatedEmail;

        return new VedantuResponse(sendForgotPasswordEmailRes);
    }

    @Override
    public VedantuResponse updateUserForgottenPassword(UpdateUserForgottenPasswordReq updateUserForgottenPasswordReq) {

        if (updateUserForgottenPasswordReq==null) {
           throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        UpdateUserForgottenPasswordRes updateUserForgottenPasswordRes = null;

            updateUserForgottenPasswordRes =getUpdateUserForgottenPassword(updateUserForgottenPasswordReq);

        return new VedantuResponse(updateUserForgottenPasswordRes);
    }

    @Override
    public VedantuResponse acceptTnC(AcceptTnCReq acceptTnCReq) {
        if (acceptTnCReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        AcceptTnCRes acceptTnCRes = null;

            acceptTnCRes = getacceptTnC(acceptTnCReq);

        return new VedantuResponse(acceptTnCRes);
    }

    @Override
    public VedantuResponse unsubscribeEmail(UnsubscribeReq unsubscribeReq) {
        if (unsubscribeReq==null) {
            logger.error("unsubscribe request should noy be null");
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        UnsetEmailRes response = null;


            logger.debug(" Unsubscribing now");
            response = unsubscribe(unsubscribeReq);


        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse getDirectURL(UserDirectLoginReq userDirectLoginReq) {

        if (userDirectLoginReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        UserDirectLoginRes response = getUserDirectLogin(userDirectLoginReq);

        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse subscribeEmail(EmailSubscribeReq emailSubscribeReq) {

        if (emailSubscribeReq==null) {

            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        logger.debug(" Subscribing now");

        UnsetEmailRes response  = subscribe(emailSubscribeReq);


        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse getUnsubscriptions(GetUserEmailSubscriptionReq getUserEmailSubscriptionReq) {



        if (getUserEmailSubscriptionReq==null) {
             throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        logger.debug(" Subscribing now");
        UserEmailUnsubscriptionRes  response = userEmailSubscriptions(getUserEmailSubscriptionReq);
        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse uploadFile(MultipartFile file, UploadProfilePicReq request) throws IOException {
        validateFile(file);
        Date date = new Date();
      //  String userId = tokenUtilService.getUserId(request);
       /* DBObject metaData = new BasicDBObject();
        metaData.put("type", file.getContentType());
        metaData.put("title", file.getOriginalFilename());

        ObjectId gridFSFile = gridFsOperations.store(file.getInputStream(),file.getOriginalFilename(),file.getContentType(),metaData);

        FileMetaInfo fileDocument = new FileMetaInfo();
        fileDocument.setName(file.getOriginalFilename());
        fileDocument.setType(EntityType.FILE);
        fileDocument.setFileId(gridFSFile.toString());

        fileMetaInfoRepo.save(fileDocument);*/
        //return  gridFSFile.toString();
        FileMetaInfo fileDocument = new FileMetaInfo();
        fileDocument.setName(file.getOriginalFilename());
        fileDocument.setType(EntityType.FILE);
        request.inputFile = picStorage.convertMultiPartToFile(file);
        UploadProfilePicRes uploadOrgPicRes = uploadProfilePic(fileDocument,request);
        FileUtils.deleteFile(request.fileName, request.inputFile);

        logger.info("File uploaded successfully");
        return new VedantuResponse(uploadOrgPicRes);
    }

    private UploadProfilePicRes uploadProfilePic(FileMetaInfo fileDocument, UploadProfilePicReq uploadProfilePicReq) {

        ImageFilter filter = new ImageFilter();
        boolean isImg = filter.accept(new File(uploadProfilePicReq.fileName));
        if (!isImg) {
            throw new VedantuException(VedantuErrorCode.UNSUPPORTED_FILE_TYPE, "not an image file");
        }

        Optional<User> user1 = userRepo.findById(uploadProfilePicReq.userId.trim());
        if (!user1.isPresent()) {
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        User user= user1.get();

        final String imageName = user._getStringId();

        try {
            picStorage.AbstractEntityFileStorageEntity(EntityType.USER);

            StorageResult picStorageResult = picStorage.storeImage(imageName,
                    uploadProfilePicReq.inputFile, FileCategory.ORIGINAL, ImageSize.ORIGINAL, null);
            logger.debug(picStorageResult.toString());

            for (ImageSize imageSize : new ImageSize[] { ImageSize.MEDIUM, ImageSize.SMALL,
                    ImageSize.EXTRA_SMALL }) {
                File convertedFile = picStorage.createImage(uploadProfilePicReq.inputFile,
                        imageSize, uploadProfilePicReq.fileName);
                picStorageResult = picStorage.storeImage(imageName, convertedFile,
                        FileCategory.CONVERTED, imageSize, null);
                logger.debug(picStorageResult.toString());

                FileUtils.deleteFile(convertedFile.getName(), convertedFile);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new VedantuException(VedantuErrorCode.FILE_UPLOAD_ERROR);
        }

        user.thumbnail = imageName;
       userRepo.save(user);
        String thumbnailUrl;
        if(!StringUtils.isEmpty(imageName) ) {
            thumbnailUrl = picStorage.getSecuredURL(imageName, EntityType.USER,
                    FileUtils.JPG_EXTENTION_WITHOUT_DOT, com.lms.common.vedantu.entity.storage.MediaType.IMAGE,
                    FileCategory.CONVERTED, ImageSize.SMALL).getSecuredURL();
        }else {
            thumbnailUrl = ImageDisplayURLUtil.getEntityStaticThumbnail(
                    EntityType.USER, Arrays.asList("default"));
        }
        UploadProfilePicRes uploadProfilePicRes = new UploadProfilePicRes(true, thumbnailUrl);
        return uploadProfilePicRes;

    }

    private void validateFile(MultipartFile multipartFile) throws InputMismatchException {
        if(multipartFile.isEmpty() ||multipartFile.getOriginalFilename().isEmpty() || multipartFile.getContentType().isEmpty()){
            throw new InputMismatchException("Uploaded File is not valid");
        }
    }


    private UserEmailUnsubscriptionRes userEmailSubscriptions(GetUserEmailSubscriptionReq request) throws VedantuException {

            UserEmailUnsubscriptionRes response = new UserEmailUnsubscriptionRes();
            response.info = getUserEmailSubscriptions(request.getUserId(), request.getTargetUserId());
            return response;
    }

    private UnsetEmailRes subscribe(EmailSubscribeReq emailSubscribeReq) throws VedantuException {
        logger.debug(" Unsubcribing");
        Optional<User> getUser = userRepo.findById(emailSubscribeReq.getTargetUserId());


        if (!getUser.isPresent()) {
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        User user= getUser.get();
        if (user.getCredentials() == null) {
            user.setCredentials(EncryptionUtils.generateKeys());
            userRepo.save(user);

        }

        String restrictedEmail = user.getEmail();
        MailCategory category = MailCategory.valueOfKey(emailSubscribeReq.getMailCategory());

        if (category == MailCategory.UNKNOWN) {
            throw new VedantuException(VedantuErrorCode.EMAIL_UNSUBSCRIPTION_FAILED);
        }
        UnsetEmailRes response = new UnsetEmailRes();

        response.done = allowEmails(emailSubscribeReq.getUserId(),
                restrictedEmail, category);

        return response;
    }

    private boolean allowEmails(String userId, String restrictedEmail, MailCategory category) {
        {

            if (category == null || category == MailCategory.UNKNOWN) {
                return true;
            }

            boolean all = (category == MailCategory.ALL);
            logger.debug(" Category " + category);


            if (all) {
                UserEmailUnsubscription userEmailUnsubscription=userEmailUnSubScriptionRepo.findByUserIdAndEmail(userId,restrictedEmail);
                if(userEmailUnsubscription!=null)
                userEmailUnSubScriptionRepo.delete(userEmailUnsubscription);

            } else {

                UserEmailUnsubscription emailUnsubscriptions = userEmailUnSubScriptionRepo.findByUserIdAndEmail(userId,restrictedEmail);
                if (emailUnsubscriptions != null) {
                    logger.debug(" Email subscription found");
                    if (!emailUnsubscriptions.getRestrictions().isEmpty()) {
                        List<UserRestrictedEmailCategory> list = new ArrayList<UserRestrictedEmailCategory>();
                        for (UserRestrictedEmailCategory restrictedEmailCategory : emailUnsubscriptions.restrictions) {
                            logger.debug(" Email subscription found " + restrictedEmailCategory);
                            if (restrictedEmailCategory.category != category) {
                                list.add(restrictedEmailCategory);
                            }
                        }
                        if (list.isEmpty()) {
                            UserEmailUnsubscription userEmailUnsubscription = userEmailUnSubScriptionRepo.findByUserIdAndEmail(userId, restrictedEmail);
                        } else {
                            emailUnsubscriptions.setRestrictions(list);
                            userEmailUnSubScriptionRepo.save(emailUnsubscriptions);

                        }
                    }

                }
            }

            return true;
        }
    }

    private UserDirectLoginRes getUserDirectLogin(UserDirectLoginReq userDirectLoginReq) throws VedantuException {
        Optional<User> getUser=userRepo.findById(userDirectLoginReq.getTargetUserId());


        if (!getUser.isPresent()) {
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        User user=getUser.get();
        if (user.getCredentials() == null) {
            user.setCredentials(EncryptionUtils.generateKeys());
            userRepo.save(user);


        }
        UserAuthPojo userPojo = new UserAuthPojo();
        String[] userSplit = user.getUsername().split(":");

        if (userSplit.length > 1) {
            if (!userSplit[0].equals(userDirectLoginReq.getOrgId())) {
                throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
            }
            userPojo.setOrgId(userSplit[1]) ;
            userPojo.setMemberAuth(true);
        }
        userPojo.setOrgId(userDirectLoginReq.getOrgId()) ;
        userPojo.setUserName(user.getUsername());
        userPojo.setPassword(user.getPassword());


        String userPojoEnc = null;
        try {
            String userPojoJSON = null;
            userPojoJSON = userPojo.toJSON().toString();

            userPojoEnc = EncryptionUtils.encryptWithPublicKey(userPojoJSON,
                    user.getCredentials().getPublicKey());
            logger.debug(userPojo + " user pojo encrypted " + userPojoEnc);

        } catch (JSONException e) {
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
        }

        UserDirectLoginRes response = new UserDirectLoginRes();
        response.password = userPojoEnc;
        response.username = user.getUsername();

        return response;
    }

    private UnsetEmailRes unsubscribe(UnsubscribeReq unsubscribeReq) throws VedantuException {
        {

            logger.debug(" Unsubcribing external " + unsubscribeReq.external);
            Optional<User> getuser = userRepo.findById(unsubscribeReq.getTargetUserId());

            if (!getuser.isPresent()) {
                throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
            }
            User user=getuser.get();
            if (user.getCredentials()== null) {
                user.setCredentials(EncryptionUtils.generateKeys());
                userRepo.save(user);

            }

            String restrictedEmail = user.getEmail();

            MailCategory category = MailCategory.UNKNOWN;
            if (unsubscribeReq.external) {
                UserUnsubscribePojo userPojo = new UserUnsubscribePojo();

                JSONObject json;
                try {
                    logger.debug("Password" + unsubscribeReq.getMailCategory());
                    String userPojoDecrypted = EncryptionUtils.decryptWithPrivateKey(
                            unsubscribeReq.mailCategory, user.credentials.getPrivateKey());

                    logger.debug("userPojodecrepted" + userPojoDecrypted);
                    json = new JSONObject(userPojoDecrypted);

                } catch (JSONException e) {
                    throw new VedantuException(VedantuErrorCode.ACCESS_DENIED);
                }
                userPojo.fromJSON(json);
                restrictedEmail = userPojo.getEmail();
                category = userPojo.category;

            } else {
                category = MailCategory.valueOfKey(unsubscribeReq.getMailCategory());
            }

            if (category == MailCategory.UNKNOWN) {
                throw new VedantuException(VedantuErrorCode.EMAIL_UNSUBSCRIPTION_FAILED);
            }

            UnsetEmailRes response = new UnsetEmailRes();
            boolean b=getIsEmailAllowed(unsubscribeReq.targetUserId,restrictedEmail,category);
            if (b) {

                getRestrictEmail(unsubscribeReq.userId,unsubscribeReq.getTargetUserId(),restrictedEmail ,unsubscribeReq.getReason(),category);
            } else {
                logger.debug("Already restricted ");
                response.done = false;

                throw new VedantuException(VedantuErrorCode.ALREADY_UNSUBSCRIBED);
            }

            response.done = true;

            return response;

        }
    }
    public void getRestrictEmail(String userId, String targetUserId, String email, String reason,
                              MailCategory category) throws VedantuException {


        UserRestrictedEmailCategory emailCategory = new UserRestrictedEmailCategory(category,
                userId, reason);
        UserEmailUnsubscription usersub=new UserEmailUnsubscription();
        UserEmailUnsubscription  userEmailUnsubscription=userEmailUnSubScriptionRepo.findByUserIdAndEmailAndRecordState(targetUserId,email,VedantuRecordState.ACTIVE);
        if(userEmailUnsubscription!=null){

            throw new VedantuException(VedantuErrorCode.EMAIL_UNSUBSCRIPTION_FAILED,"the given uesr already unsubscribed");

        }
        List<UserRestrictedEmailCategory> restrictions=new ArrayList<>();
        restrictions.add(emailCategory);
        usersub.setUserId(userId);
        usersub.setEmail(email);
        usersub.setRestrictions(restrictions);
      //  UserEmailUnsubscription userEmailunsub=userEmailUnSubScriptionRepo.fi

        UserEmailUnsubscription unsubscription=userEmailUnSubScriptionRepo.save(usersub);
        if (unsubscription == null) {
            throw new VedantuException(VedantuErrorCode.EMAIL_UNSUBSCRIPTION_FAILED);
        }



    }

    private boolean getIsEmailAllowed(String targetUserId, String restrictedEmail, MailCategory category) {
        if (category == null || category == MailCategory.UNKNOWN) {
            return true;
        }

      UserEmailUnsubscription userEmailUnsubscription=userEmailUnSubScriptionRepo.findByUserIdAndEmailAndRecordStateAndRestrictionsIn(targetUserId,restrictedEmail, VedantuRecordState.ACTIVE,MailCategory.ALL);

return (userEmailUnsubscription==null);

    }

    private AcceptTnCRes getacceptTnC(AcceptTnCReq acceptTnCReq) throws VedantuException {
        logger.debug("acceptTnC userId: " + acceptTnCReq.getUserId() + ", agrees: " + acceptTnCReq.agrees + ", version: "
                + acceptTnCReq.getVersion());

        Optional<User> getUser =userRepo.findById(acceptTnCReq.getUserId());
        logger.debug("found user: " + getUser.get());
        User user=new User();
        user=getUser.get();

        if (!getUser.isPresent()) {
            logger.error("cannot acceptTnC as user does not exist for userId: " + user.getUsername());
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND,"given userId is not found");
        }

        user.tncAcceptance = new TnCAcceptance(acceptTnCReq.agrees, acceptTnCReq.getVersion(), System.currentTimeMillis());
        userRepo.save(user);

        logger.info("acceptTnC updated user: " + user);

        AcceptTnCRes acceptTnCRes = new AcceptTnCRes(user.tncAcceptance.agrees);
        return acceptTnCRes;
    }

    private UpdateUserForgottenPasswordRes getUpdateUserForgottenPassword(UpdateUserForgottenPasswordReq updateUserForgottenPasswordReq) throws VedantuException {
        logger.debug("updateUserForgottenPassword userId: " + updateUserForgottenPasswordReq.getUserId() + ", code: " + updateUserForgottenPasswordReq.getCode());

        Optional<User> getUser = userRepo.findById(updateUserForgottenPasswordReq.getUserId());
        logger.debug("found user: " + getUser);

        if (!getUser.isPresent()) {
            logger.error("cannot update password as user does not exist for userId: " + updateUserForgottenPasswordReq.getUserId());
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        User user=getUser.get();
        if (null == user.getForgotPasswordReq() || !user.getForgotPasswordReq().getVerificationCode().equals(updateUserForgottenPasswordReq.getCode())) {
            logger.error("cannot update password as forgotPassword verification code did not match for userId: "
                    + updateUserForgottenPasswordReq.getUserId());
            throw new VedantuException(VedantuErrorCode.INVALID_VERIFICATION_CODE);
        }

        if (user.getAuthType() == AuthType.EXT_AUTH_ORG) {
            throw new VedantuException(VedantuErrorCode.EXTERNAL_AUTH_SUPPORTED);
        }

        final boolean isOnlyCheck = false;
        String s=getUserPassHash(user.username, updateUserForgottenPasswordReq.getNewPassword(), isOnlyCheck);
        user.setPassword(s);
        user.setForgotPasswordReq(null);

        userRepo.save(user);
        logger.info("updateUserForgottenPassword password updated user: " + user);


        UpdateUserForgottenPasswordRes updateUserForgottenPasswordRes = new UpdateUserForgottenPasswordRes(
                null != user);

        return updateUserForgottenPasswordRes;
    }
    private String getUserPassHash(String username, String password, boolean isOnlyCheck) {

        logger.error("getUserPassHash username: " + username);
        String hashedPass = UserDao.getHashed(getSaltedPassword(username, password, isOnlyCheck), "SHA-256");
        logger.error("Hashed password for username: " + username + " : " + hashedPass);
        return hashedPass;
    }
   private String getSaltedPassword(String username, String password,
                             boolean isOnlyCheck) {

        UserSalt userSalt = userSaltrepo.findByUsername(username);
        if (null == userSalt) {
            logger.debug("user-salt not found for username: " + username);

            if (isOnlyCheck) {
                logger.debug("will not create new user-salt for username: "
                        + username);
                return HardCodedConstants.emptyString;
            }

            userSalt = new UserSalt(username, UUID.randomUUID().toString());
            userSaltrepo.save(userSalt);
        }

        String saltedPassword = userSalt.salt + SYSTEM_SALT + password;
        return saltedPassword;
    }


    private boolean generateSendForgotPasswordEmailEvent(User user, String orgId, String callingAppId) throws VedantuException {
     //TODO need to generate ForgotPasswordEmailEvent
        ForgotPasswordDetails details;
        try {
            details = new ForgotPasswordDetails();
        } catch (ClassNotFoundException e) {
            logger.debug(" Not found email details", e);
            throw new VedantuException(VedantuErrorCode.EMAIL_CAN_NOT_BE_SEND);
        }

        details.user = new UserEmailInfo();
      //  details.user.fromUserExtendedInfo((UserExtendedInfo) user.toExtendedInfo());
        details.user.fromUserExtendedInfo(user);

        details.verificationLink = generatePasswordResetURL(user, orgId,callingAppId);
        details.addRecepient(details.user.getFullName(), user.email);

        generateEventAyscForForgotPassword(user._getStringId(), details, EventType.SEND_INSTANT_EMAIL);
        return true;
    }

    private void generateEventAyscForForgotPassword(String userId, ForgotPasswordDetails details, EventType sendInstantEmail) {
        CompletableFuture.runAsync(() -> {
            eventUtil.generateEvent(sendInstantEmail, null, userId, details,
                    details.__getSrcEntity(), UserActionType.EventActionType.ADD, 0);
        });
    }

    private String generatePasswordResetURL(User user, String orgId, String callingAppId) throws VedantuException {
        final String emailVerifyHost = Configurations.getAppLearnHost(callingAppId);

        final String emailVerifyEndPoint = EmailConfigurationConstants.EMAIL_FORGOTPASSWORD_ENDPOINT;

        URLGenerator generator = new URLGenerator();
        generator.host = emailVerifyHost;
        generator.endPoint = emailVerifyEndPoint;
        generator.protocol = Configurations.APP_PROTOCOL;

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("code", URLEncoder.encode(user.forgotPasswordReq.verificationCode, StandardCharsets.UTF_8));
        params.put("userId", URLEncoder.encode(user._getStringId(), StandardCharsets.UTF_8));
        params.put("email", URLEncoder.encode(user.email, StandardCharsets.UTF_8));
        if (!orgId.isEmpty()) {
            logger.debug("Update happen in organization" + orgId);
            params.put("orgId", URLEncoder.encode(orgId, StandardCharsets.UTF_8));
        }

        generator.params = params;
        return generator.generate();

    }

    private User generateForgotPasswordReq(String username) throws VedantuException {


        logger.debug("generateForgotPasswordReq username: " + username);

        Optional<User> getuser = userRepo.findByUsername(username);
        if (!getuser.isPresent()) {
            logger.debug("generateForgotPasswordReq user not found for username: " + username);
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        User user=getuser.get();
        if (user.getAuthType() == AuthType.EXT_AUTH_ORG) {
            throw new VedantuException(VedantuErrorCode.EXTERNAL_AUTH_SUPPORTED);
        }

        if (null == user.getForgotPasswordReq()) {
            user.setForgotPasswordReq(new ForgotPasswordReqInfo(UUID.randomUUID().toString()));
            userRepo.save(user);
            logger.debug("generateForgotPasswordReq saved user: " + user);
        } else {
            logger.debug("generateForgotPasswordReq user already has a forgotPasswordReq for user: "
                    + user);
        }
        logger.info("generateForgotPasswordReq user: " + user);
        return user;


    }

    private UnsetEmailRes getUnsetEmail(UnsetEmailReq unsetEmailReq) throws VedantuException {
        logger.debug("unsetEmail userId: " + unsetEmailReq.getUserId());

        Optional<User> user = userRepo.findById(unsetEmailReq.getUserId());

        if (!user.isPresent()) {
            logger.error("cannot unset email of user as user does not exist for userId: " + unsetEmailReq.getUserId());
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }

        if (null != user.get().getEmailChangeReq()&&!user.get().getEmailChangeReq().getEmail().equals(user.get().getEmail())) {
            user.get().setEmailChangeReq(null);
            logger.debug("removed email verification request as it matches the specified email: "
                    + user.get().getEmail());
        }
        user.get().setEmail(HardCodedConstants.emptyString);
        user.get().setEmailVerified(false);

        userRepo.save(user.get());


        logger.info("unsetEmail user: " + user.get());

        UnsetEmailRes unsetEmailRes = new UnsetEmailRes();
        unsetEmailRes.done = null != user.get();

        return unsetEmailRes;

    }

    private ResendEmailVerificationRes getResendEmailVerification(ResendEmailVerificationReq resendEmailVerificationReq) throws VedantuException {

           Optional<User> user=userRepo.findById(resendEmailVerificationReq.getUserId());
        if (!user.isPresent()) {
            logger.error("cannot find user for id: " + resendEmailVerificationReq.getUserId());
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);

        } else {
            logger.info("findUserById user: " + user.get());
        }

            boolean generatedEmail = generateEmailVerificationEvent(user.get(),
                   resendEmailVerificationReq.orgId,resendEmailVerificationReq.getCallingUserId());
            ResendEmailVerificationRes resendEmailVerificationRes = new ResendEmailVerificationRes();
            resendEmailVerificationRes.setDone(generatedEmail);
            return resendEmailVerificationRes;
    }

    private ValidateEmailRes checkValidateEmail(ValidateEmailReq validateEmailReq) throws VedantuException {

        logger.debug("validateEmail userId: " + validateEmailReq.getUserId() + ", emailVerificationCode: "
                + validateEmailReq.getCode()+ ", isVerified: " + validateEmailReq.getIsVerified());

        Optional<User> user = userRepo.findById(validateEmailReq.getUserId());

        if (!user.isPresent()) {
            logger.error("cannot validate email as user does not exist for userId: " + validateEmailReq.getUserId());
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        if (null == user.get().getEmailChangeReq()) {
            logger.debug("cannot validate email as user did not request email change for userId: "
                    + validateEmailReq.getUserId());
            throw new VedantuException(VedantuErrorCode.INVALID_VERIFICATION_CODE);
        }

        boolean isUpdated = false;
        if (user.get().getEmailChangeReq().getVerificationCode().equals(validateEmailReq.getCode())) {
            logger.debug("email update verified: " + validateEmailReq.getIsVerified());
            if (validateEmailReq.getIsVerified()) {
                user.get().setEmail(user.get().getEmailChangeReq().getEmail());
                user.get().setEmailVerified(true);
            }
            user.get().setEmailChangeReq(null);
            isUpdated = true;

            logger.info("validateEmail user: " + user.get());
        } else {
            logger.debug("validateEmail emailVerificationCode mismatched");
            throw new VedantuException(VedantuErrorCode.INVALID_VERIFICATION_CODE);
        }


            ValidateEmailRes validateEmailRes = new ValidateEmailRes(isUpdated);
        return validateEmailRes;

    }

    private GetUserSelfFullProfileRes getSelfFullInFo(GetUserSelfFullProfileReq getUserSelfFullProfileReq) throws VedantuException {

        logger.debug("getUserSelfFullProfile userId: " + getUserSelfFullProfileReq.userId);
        Optional<User> getuser=userRepo.findById(getUserSelfFullProfileReq.getUserId());

        if (getuser.isEmpty()) {
            logger.error("getUserSelfFullProfile user not found for userId: "
                    + getUserSelfFullProfileReq.userId);
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        User user=getuser.get();

        GetUserSelfFullProfileRes getUserSelfFullProfileRes = new GetUserSelfFullProfileRes();
        UserExtendedInfo userExtendedInfo =new   UserExtendedInfo(user);
        getUserSelfFullProfileRes.info = userExtendedInfo;
        getUserSelfFullProfileRes.unsubscribeInfo = getUserEmailSubscriptions(
                getUserSelfFullProfileReq.getUserId(), getUserSelfFullProfileReq.getUserId());

        logger.info("getUserSelfFullProfile userExtendedInfo: " + userExtendedInfo);

        return getUserSelfFullProfileRes;
    }

    private UserEmailUnsubscriptionInfo getUserEmailSubscriptions(String userId, String userId1) throws VedantuException {
       Optional<User> user = userRepo.findById(userId);

        if (user.get()== null) {
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        if (user.get().getCredentials() == null) {
            user.get().setCredentials( EncryptionUtils.generateKeys());
            userRepo.save(user.get());
        }

        if (!user.get().getEmail().isEmpty()) {
            UserEmailUnsubscription model = userEmailUnSubScriptionRepo.findByUserIdAndEmail(user.get().getId().toString(),user.get().getEmail());

            if (model != null) {
                return (UserEmailUnsubscriptionInfo) model.toExtendedInfo();
            }
        }

        return null;
    }

    public Boolean generateEmailVerificationEvent(User user, String orgId, String callingAppId) throws VedantuException {

        // TODO: verification of user email needs to be done through event
        final String emailVerifyHost = Configurations.getAppLearnHost(callingAppId);
        final String emailVerifyEndPoint = EmailConfigurationConstants.EMAIL_VERIFICATION_ENDPOINT;

        URLGenerator generator = new URLGenerator();
        generator.host = emailVerifyHost;
        generator.endPoint = emailVerifyEndPoint;
        generator.protocol = Configurations.APP_PROTOCOL;

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("code", URLEncoder.encode(user.emailChangeReq.verificationCode, StandardCharsets.UTF_8));
        params.put("userId", URLEncoder.encode(user.getUsername(), StandardCharsets.UTF_8));
        params.put("email", URLEncoder.encode(user.getEmailChangeReq().getEmail(), StandardCharsets.UTF_8));
        if (orgId != null) {
            params.put("orgId", URLEncoder.encode(orgId, StandardCharsets.UTF_8));
        }

        generator.params = params;

        EmailVerificationDetails details;
        try {
            details = new EmailVerificationDetails();
        } catch (Exception e) {
            logger.debug(" Not found email details", e);
            throw new VedantuException(VedantuErrorCode.EMAIL_CAN_NOT_BE_SEND);
        }

       details.user = new UserEmailInfo();
      //  details.user.fromUserExtendedInfo((UserExtendedInfo) user.toExtendedInfo());
        details.user.fromUserExtendedInfo(user);
        details.verificationLink = generator.generate();
        details.orgId = orgId;
        details.addRecepient(details.user.getFullName(), user.emailChangeReq.email);
        generateEventAysc(user._getStringId(), details, EventType.SEND_INSTANT_EMAIL);

        return true;
    }

    @Override
    public String generatePasswordUpdate(String id, String getStringId, String callingAppId) {
        Optional<User> user = userRepo.findById(id);
        if (!user.isPresent()) {
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }

        if (user.get().getForgotPasswordReq()== null) {
            user.get().setForgotPasswordReq( new ForgotPasswordReqInfo(UUID.randomUUID().toString()));
           userRepo.save(user.get());
            logger.debug("updated password saved user: " + user);
        } else {
            logger.debug("generateForgotPasswordReq user already has a forgotPasswordReq for user: "
                    + user);
        }

        return generatePasswordResetURL(user.get(), id, callingAppId );
    }

    private void generateEventAysc(String userId, EmailVerificationDetails details, EventType eventType) {
        CompletableFuture.runAsync(() -> {
            eventUtil.generateEvent(eventType, null, userId, details,
                    details.__getSrcEntity(), UserActionType.EventActionType.ADD, 0);
        });

    }


}
