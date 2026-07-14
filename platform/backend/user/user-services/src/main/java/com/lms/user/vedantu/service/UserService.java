package com.lms.user.vedantu.service;

import com.lms.user.vedantu.user.model.User;
import com.lms.user.vedantu.user.requests.*;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserService {

    VedantuResponse authenticateUser(UserAuthReq userAuthReq);

    VedantuResponse doesUserExists(UserExistenceReq userExistenceReq);

    VedantuResponse updateUsername(UpdateUsernameReq updateUsernameReq) throws VedantuException;

    VedantuResponse updateUserPassword(UpdateUserPasswordReq updateUserPasswordReq) throws VedantuException;

    VedantuResponse changeUserPassword(ChangeUserPasswordReq changeUserPasswordReq) throws VedantuException;

    VedantuResponse addUser(AddUserReq addUserReq) throws VedantuException;

    VedantuResponse updateUser(UpdateUserReq updateUserReq) throws VedantuException;

    VedantuResponse getUserSelfFullProfile(GetUserSelfFullProfileReq getUserSelfFullProfileReq) throws VedantuException;

    VedantuResponse validateEmail(ValidateEmailReq validateEmailReq) throws VedantuException;

    VedantuResponse resendEmailVerification(ResendEmailVerificationReq resendEmailVerificationReq);

    VedantuResponse unsetEmail(UnsetEmailReq unsetEmailReq) throws VedantuException;

    VedantuResponse sendForgotPasswordMail(SendForgotPasswordEmailReq sendForgotPasswordEmailReq) throws VedantuException;

    VedantuResponse updateUserForgottenPassword(UpdateUserForgottenPasswordReq updateUserForgottenPasswordReq);

    VedantuResponse acceptTnC(AcceptTnCReq acceptTnCReq);

    VedantuResponse unsubscribeEmail(UnsubscribeReq unsubscribeReq);

    VedantuResponse getDirectURL(UserDirectLoginReq userDirectLoginReq);

    VedantuResponse subscribeEmail(EmailSubscribeReq emailSubscribeReq);

    VedantuResponse getUnsubscriptions(GetUserEmailSubscriptionReq getUserEmailSubscriptionReq);

    VedantuResponse uploadFile(MultipartFile file, UploadProfilePicReq request) throws IOException;

    Boolean generateEmailVerificationEvent(User user, String orgId, String callingAppId);

    String generatePasswordUpdate(String id, String getStringId, String callingAppId);
}
