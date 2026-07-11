package com.lms.user.vedantu.controllers;


import com.lms.user.vedantu.service.UserService;
import com.lms.user.vedantu.user.requests.*;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping( "/users")
public class Users {


    @Autowired
    UserService userServiceImpl;

    @PostMapping("/authenticateUser")
    public ResponseEntity<VedantuResponse> authenticateUser(UserAuthReq userAuthReq) {
         return ResponseEntity.ok(userServiceImpl.authenticateUser(userAuthReq));
    }
    @PostMapping("/doesUserExists")
    public ResponseEntity<VedantuResponse> doesUserExists(@Valid UserExistenceReq userExistenceReq) {
        return ResponseEntity.ok(userServiceImpl.doesUserExists(userExistenceReq));
    }
    @PostMapping("/updateUsername")
    public ResponseEntity<VedantuResponse> updateUsername(@Valid UpdateUsernameReq updateUsernameReq) throws VedantuException {
        return ResponseEntity.ok(userServiceImpl.updateUsername(updateUsernameReq));
    }

    @PostMapping("/updateUserPassword")
    public ResponseEntity<VedantuResponse> updateUserPassword(@Valid UpdateUserPasswordReq updateUserPasswordReq) throws VedantuException {
        return ResponseEntity.ok(userServiceImpl.updateUserPassword(updateUserPasswordReq));
    }
    @PostMapping("/changeUserPassword")
    public ResponseEntity<VedantuResponse> changeUserPassword(@Valid ChangeUserPasswordReq changeUserPasswordReq) throws VedantuException {
        return ResponseEntity.ok(userServiceImpl.changeUserPassword(changeUserPasswordReq));
    }

    @PostMapping("/addUser")
    public ResponseEntity<VedantuResponse> addUser(AddUserReq addUserReq) throws VedantuException {
        return ResponseEntity.ok(userServiceImpl.addUser(addUserReq));
    }
    @PostMapping("/updateUser")
    public ResponseEntity<VedantuResponse> updateUser(UpdateUserReq updateUserReq) throws VedantuException {
        return ResponseEntity.ok(userServiceImpl.updateUser(updateUserReq));
    }
    @PostMapping("/getUserSelfFullProfile")
    public ResponseEntity<VedantuResponse> getUserSelfFullProfile(GetUserSelfFullProfileReq getUserSelfFullProfileReq) throws VedantuException {
        return ResponseEntity.ok(userServiceImpl.getUserSelfFullProfile(getUserSelfFullProfileReq));
    }

    @PostMapping("/validateEmail")
    public ResponseEntity<VedantuResponse> validateEmail(@Valid  ValidateEmailReq validateEmailReq) throws VedantuException {
        return ResponseEntity.ok(userServiceImpl.validateEmail(validateEmailReq));
    }

    @PostMapping("/resendEmailVerification")
    public ResponseEntity<VedantuResponse> resendEmailVerification(ResendEmailVerificationReq resendEmailVerificationReq) throws VedantuException {
        return ResponseEntity.ok(userServiceImpl.resendEmailVerification(resendEmailVerificationReq));
    }

    @PostMapping("/unsetEmail")
    public ResponseEntity<VedantuResponse> unsetEmail(UnsetEmailReq unsetEmailReq) throws VedantuException {
        return ResponseEntity.ok(userServiceImpl.unsetEmail(unsetEmailReq));
    }

    @PostMapping("/sendForgotPasswordMail")
    public ResponseEntity<VedantuResponse> sendForgotPasswordMail(SendForgotPasswordEmailReq sendForgotPasswordEmailReq) throws VedantuException {
        return ResponseEntity.ok(userServiceImpl.sendForgotPasswordMail(sendForgotPasswordEmailReq));
    }
    @PostMapping("/updateUserForgottenPassword")
    public ResponseEntity<VedantuResponse> updateUserForgottenPassword(UpdateUserForgottenPasswordReq updateUserForgottenPasswordReq) throws VedantuException {
        return ResponseEntity.ok(userServiceImpl.updateUserForgottenPassword(updateUserForgottenPasswordReq));
    }

    @PostMapping("/acceptTnC")
    public ResponseEntity<VedantuResponse> acceptTnC(AcceptTnCReq acceptTnCReq) throws VedantuException {
        return ResponseEntity.ok(userServiceImpl.acceptTnC(acceptTnCReq));
    }

    @PostMapping("/unsubscribeEmail")
    public ResponseEntity<VedantuResponse> unsubscribeEmail(UnsubscribeReq unsubscribeReq) throws VedantuException {
        return ResponseEntity.ok(userServiceImpl.unsubscribeEmail(unsubscribeReq));
    }

    @PostMapping("/getDirectURL")
    public ResponseEntity<VedantuResponse> getDirectURL(UserDirectLoginReq userDirectLoginReq) throws VedantuException {
        return ResponseEntity.ok(userServiceImpl.getDirectURL(userDirectLoginReq));
    }

    @PostMapping("/subscribeEmail")
    public ResponseEntity<VedantuResponse> subscribeEmail(EmailSubscribeReq emailSubscribeReq) throws VedantuException {
        return ResponseEntity.ok(userServiceImpl.subscribeEmail(emailSubscribeReq));
    }

    @PostMapping("/getUnsubscriptions")
    public ResponseEntity<VedantuResponse> getUnsubscriptions(@Valid GetUserEmailSubscriptionReq getUserEmailSubscriptionReq) throws VedantuException {
        return ResponseEntity.ok(userServiceImpl.getUnsubscriptions(getUserEmailSubscriptionReq));
    }
    @PostMapping("/uploadProfilePic")
    public  ResponseEntity<VedantuResponse> uploadFile(@RequestParam("file")MultipartFile file, UploadProfilePicReq request) throws IOException {
        return ResponseEntity.ok(userServiceImpl.uploadFile(file,request));
    }

    }
