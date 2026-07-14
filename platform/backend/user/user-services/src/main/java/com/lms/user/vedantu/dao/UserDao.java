package com.lms.user.vedantu.dao;


import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.validation.Validation;
import com.lms.common.vedantu.commons.pojos.requests.SecurityCredentials;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.enums.AuthType;
import com.lms.user.vedantu.user.daos.UserSaltDao;
import com.lms.user.vedantu.user.dto.UserDto;
import com.lms.user.vedantu.user.model.User;
import com.lms.user.vedantu.user.pojo.EmailChangeReqInfo;
import com.lms.user.vedantu.user.pojo.SocialInfo;
import com.lms.user.vedantu.user.pojo.responce.ChangeUserPasswordRes;
import com.lms.user.vedantu.user.repository.UserRepo;
import com.lms.user.vedantu.user.requests.*;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Repository
public class UserDao {

    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private UserSaltDao userSaltDao;


    public Boolean doesUserExists(UserExistenceReq userExistenceReq) {

        Optional<User> user = userRepo.findByEmail(userExistenceReq.getEmail());
        return user.isPresent();
    }

    public VedantuResponse updateUsername(UpdateUsernameReq updateUsernameReq) throws VedantuException {
        if(updateUsernameReq.getNewUsername()==null){
            throw new VedantuException(VedantuErrorCode.SHOULD_NOT_BE_NULL,"NewUserName required");
        }
        if(updateUsernameReq.getTargetUserId()==null){
            throw new VedantuException(VedantuErrorCode.SHOULD_NOT_BE_NULL,"UserId required");
        }
        Optional<User> user = userRepo.findById(updateUsernameReq.getTargetUserId());
        Optional<User> user1 = userRepo.findByUsername(updateUsernameReq.getNewUsername());
        UserDto userDto = null;
        if (user.isPresent()) {
            userDto = convertToUserDto(user.get());
            if (user.get().getUsername() == updateUsernameReq.getNewUsername()) {
                throw new VedantuException(VedantuErrorCode.USER_ALREADY_ACTIVE,"User Alreday Exist");

            } else {
                if (user1.isPresent()) {
                    throw new VedantuException(VedantuErrorCode.USER_ALREADY_EXISTS,"this new username already exist");
                } else {
                    user.get().setUsername(updateUsernameReq.getNewUsername());
                    final boolean isOnlyCheck = false;
                    String pwd=getUserPassHash(updateUsernameReq.getNewUsername(), updateUsernameReq.getNewPassword(), isOnlyCheck);
                    user.get().setPassword(pwd);  ;
              userRepo.save(user.get());
              userDto.setUsername(updateUsernameReq.getNewUsername());
          }
          }

       }else{
           throw new VedantuException(VedantuErrorCode.INVALID_CODE,"Given target id is not valid");
       }

        return new VedantuResponse(userDto);
    }
    public UserDto convertToUserDto(User user){

        UserDto userDto=new UserDto();
        userDto.setDob(user.getDob());
        userDto.setUserId(user.getId().toString());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setUsername(user.getUsername());
        userDto.setPassword(user.getPassword());
        userDto.setSysGenPassword(user.isSysGenPassword);
        userDto.setLastName(user.getLastName());
        userDto.setEmailVerified(user.isEmailVerified);
        userDto.setOTPuser(user.isOTPuser);
        userDto.setThumbnail(user.getThumbnail());
        userDto.setPhoneVerified(user.isPhoneVerified);
        userDto.setEmailChangeReq(user.getEmailChangeReq());
        return userDto;
    }

    public VedantuResponse updateUserPassword(UpdateUserPasswordReq updateUserPasswordReq) throws VedantuException {

        logger.debug("updateUserPassword userId: " + updateUserPasswordReq.getTargetUserId());
        UserDto userDto=updateUserPassword(updateUserPasswordReq.getTargetUserId(),updateUserPasswordReq.getNewPassword());
        return new VedantuResponse(userDto);

    }
    private String getUserPassHash(String username, String password, boolean isOnlyCheck) {

        logger.error("getUserPassHash username: " + username);
       String pass= userSaltDao.getSaltedPassword(username,password,isOnlyCheck);
        String hashedPass = getHashed(pass, "SHA-256");
        logger.error("Hashed password for username: " + username + " : " + hashedPass);
        return hashedPass;
    }

    public static String getHashed(String input, String hashType) {
        try {
            MessageDigest m = MessageDigest.getInstance(hashType);
            byte[] out = m.digest(input.getBytes());
            return new String(Base64.encodeBase64(out));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public VedantuResponse changeUserPasword(ChangeUserPasswordReq changeUserPasswordReq) throws VedantuException {

        ChangeUserPasswordRes response = new ChangeUserPasswordRes();

           // String userName = UserDAO.INSTANCE.getUsername(request.targetUserId);

            Optional<User> user=userRepo.findById(changeUserPasswordReq.getTargetUserId());
            if (!user.isPresent()) {
                logger.error("cannot change password as user does not exist for userId: " + changeUserPasswordReq.getTargetUserId());
                throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND,"cannot change password as user does not exist for userId");
            }
            if (!user.get().getEmail().equals(changeUserPasswordReq.getEmail())) {
                throw new VedantuException(VedantuErrorCode.INVALID_CODE,"the given email is invalid");

            }
            String userName=user.get().getUsername();
            logger.error("changeUserPassword: "+userName);

            authenticateUser(userName,changeUserPasswordReq.getOldPassword());
            updateUserPassword(changeUserPasswordReq.targetUserId, changeUserPasswordReq.newPassword);

        response.done = true;
        return  new VedantuResponse(response);
    }

    public UserDto updateUserPassword(String targetUserId, String newPassword) throws VedantuException {
        logger.debug("updateUserPassword userId: " + targetUserId);
        Optional<User> user = userRepo.findById(targetUserId);
        logger.debug("found user: " + user);

        if (!user.isPresent()) {
            logger.error("cannot update password as user does not exist for userId: " + targetUserId);
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND,"cannot update password as user does not exist for userId");
        }

        if (user.get().getAuthType() == AuthType.EXT_AUTH_ORG) {
            throw new VedantuException(VedantuErrorCode.EXTERNAL_AUTH_SUPPORTED);
        }

        final boolean isOnlyCheck = false;

        String s= getUserPassHash(user.get().getUsername(), newPassword, isOnlyCheck);
        user.get().setPassword(s);
        user.get().setSysGenPassword(false);

        userRepo.save(user.get());
        logger.info("updateUserPassword password updated user: " + user);
        UserDto userDto=convertToUserDto(user.get());

      return userDto;
    }

    public User authenticateUser(String username, String password) throws VedantuException {

        logger.debug("authenticateUser username: " + username);
        final boolean isOnlyCheck = true;
        String saltedPassword = getUserPassHash(username, password, isOnlyCheck);

        return authenticateUserWithSaltedPassword(username, saltedPassword);
    }
    public User authenticateUserWithSaltedPassword(String username, String saltedPassword)
            throws VedantuException {

        logger.debug("authenticateUserWithSaltedPassword username: " + username);

        Optional<User> user =userRepo.findByUsernameAndPassword(username,saltedPassword);

        if (!user.isPresent()) {
            logger.error("authentication failed for username: " + username);
            throw new VedantuException(VedantuErrorCode.AUTHENTICATION_FAILED,"user not found with given username or password "+username+" "+saltedPassword );
        }
        logger.info("authenticateUserWithSaltedPassword user: " + user);
        return user.get();
    }

    public User addUser(AddUserReq addUserReq, AtomicBoolean isEmailVerificationNeeded,
                        SecurityCredentials credentials, SocialInfo socialIn
                        ) throws VedantuException {


            logger.debug("addUser username: " + addUserReq.getUsername() + ", firstName: " + addUserReq.getFirstName() + ", lastName: "
                    + addUserReq.getLastName() + ", dob: " + addUserReq.getDob() + ", gender: " + addUserReq.getGender() + ", email: " + addUserReq.getEmail()
                    + ", isEmailVerificationNeeded: " + isEmailVerificationNeeded);

            Optional<User> user = userRepo.findByUsername(addUserReq.getUsername());

            if (user.isPresent()) {
                logger.error("cannot add user as user already exists for username: " + addUserReq.getUsername());
                throw new VedantuException(VedantuErrorCode.USER_ALREADY_EXISTS);
            }

            final boolean isOnlyCheck = false;
            User user1 = convertToUser(addUserReq, isEmailVerificationNeeded, credentials, socialIn, isOnlyCheck);


            if (Validation.isStringNotEmpty(addUserReq.getEmail().trim())) {
                user1.setEmailChangeReq(new EmailChangeReqInfo(addUserReq.getEmail().trim(), UUID.randomUUID().toString()));
                isEmailVerificationNeeded.set(true);
                logger.debug("generated email verification code");
            }

            userRepo.save(user1);

            logger.info("addUser user: " + user1);

            return user1;

    }

    private User convertToUser(AddUserReq addUserReq, AtomicBoolean isEmailVerificationNeeded, SecurityCredentials credentials, SocialInfo socialIn, boolean isOnlyCheck) {
        User user = new User();
        user.setUsername(addUserReq.getUsername());
        user.setFirstName(addUserReq.getFirstName());
        user.setLastName(addUserReq.getLastName());
        user.setDob(addUserReq.getDob());
        user.setGender(addUserReq.getGender());
        user.setPassword(getUserPassHash(addUserReq.getUsername(), addUserReq.getPassword(), isOnlyCheck));
        user.setEmail(addUserReq.getEmail().trim());
        user.isEmailVerified = false;
        user.setSysGenPassword(addUserReq.isSysGenPassword);
        user.setPhoneVerified(addUserReq.isPhoneVerified);
        user.setOTPuser(addUserReq.isOTPuser);

        user.setCredentials(credentials);
        user.setSocialInfo(socialIn);
        user.setAuthType(addUserReq.getAuthType());
        user.authType = addUserReq.authType;
        return user;
    }

    public User updateUser(UpdateUserReq updateUserReq, AtomicBoolean isEmailVerificationNeeded) throws VedantuException {
        logger.debug("updateUser userId: " + updateUserReq.getTargetUserId() + ", firstName: " + updateUserReq.getFirstName() + ", lastName: "
                + updateUserReq.getLastName() + ", dob: " + updateUserReq.getDob() + ", gender: " + updateUserReq.getGender() + ", email: " + updateUserReq.getEmail()
                + ", isEmailVerificationNeeded: " + isEmailVerificationNeeded.get());

        Optional<User> user = userRepo.findById(updateUserReq.getTargetUserId());



        if (!user.isPresent()) {
            logger.error("cannot update user as user does not exist for userId: " + updateUserReq.getTargetUserId());
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        if(updateUserReq.getEmail()!=null) {
            Optional<User> testUser = userRepo.findByEmail(updateUserReq.getEmail());
            if (testUser.isPresent()&&!user.get().getEmail().equals(updateUserReq.getEmail())) {
                logger.error("cannot update user email as user email already exist for userId: " + updateUserReq.getTargetUserId());
                throw new VedantuException(VedantuErrorCode.USER_ALREADY_EXISTS);
            }

        }

        // TODO check with ujjawal whether to allow this fields to be updated

        User user2 = user.get();
        if (user2.getAuthType() == AuthType.EXT_AUTH_ORG) {
            throw new VedantuException(VedantuErrorCode.EXTERNAL_AUTH_SUPPORTED);
        }

        user2.setFirstName(updateUserReq.getFirstName());
        user2.setLastName(updateUserReq.getLastName());
        user2.setDob(updateUserReq.getDob());
        user2.setGender(updateUserReq.getGender());
        if (!user2.getEmail().equals( updateUserReq.getEmail())) {
            user2.setEmailChangeReq(new EmailChangeReqInfo(updateUserReq.getEmail(), UUID.randomUUID().toString()));
            if (!user2.isEmailVerified) {
                user2.setEmail(updateUserReq.getEmail());
                user2.setUsername(user2.isOTPuser && user2.isSysGenPassword == true ? updateUserReq.getEmail() : user2.getUsername());
            }
            isEmailVerificationNeeded.set(true);
            logger.debug("generated email verification code");
        }
        try{
            userRepo.save(user2);
        }catch (Exception e){
            logger.error("coming here");
        }

        logger.info("updateUser user: " + user);

        return user2;
    }
}
