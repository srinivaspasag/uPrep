package controllers;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Http.MultipartFormData;
import play.mvc.Result;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.user.managers.UserManager;
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

public class Users extends AbstractVedantuController {

    private static final ALogger LOGGER = Logger.of(Users.class);

    public static Result authenticateUser() {

        Form<UserAuthReq> userAuthForm = Form.form(UserAuthReq.class).bindFromRequest();
        if (userAuthForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        UserAuthReq userAuthReq = userAuthForm.get();
        UserAuthRes userAuthRes = null;
        try {
            userAuthRes = UserManager.authenticateUser(userAuthReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(userAuthRes).toObjectNode());
    }


    public static Result doesUserExists() {

        LOGGER.debug(" User request params" + request().queryString() + "  " + request().body());

        Form<UserExistenceReq> userExistenceForm = Form.form(UserExistenceReq.class).bindFromRequest();
        if (userExistenceForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        UserExistenceReq userExistenceReq = userExistenceForm.get();
        UserExistenceRes userAuthRes = null;
        try {
            userAuthRes = UserManager.doesUserExists(userExistenceReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(userAuthRes).toObjectNode());
    }


    public static Result addUser() {

        Form<AddUserReq> addUserForm = Form.form(AddUserReq.class).bindFromRequest();
        if (addUserForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        AddUserReq addUserReq = addUserForm.get();
        AddUserRes addUserRes = null;
        try {
            addUserRes = UserManager.addUser(addUserReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(addUserRes).toObjectNode());
    }

    public static Result updateUsername() {

        Form<UpdateUsernameReq> updateUsernameForm = Form.form(UpdateUsernameReq.class)
                .bindFromRequest();
        if (updateUsernameForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        UpdateUsernameReq updateUsernameReq = updateUsernameForm.get();
        UpdateUsernameRes updateUsernameRes = null;
        try {
            updateUsernameRes = UserManager.updateUsername(updateUsernameReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(updateUsernameRes).toObjectNode());
    }

    public static Result updateUserPassword() {

        Form<UpdateUserPasswordReq> updateUserPasswordForm = Form.form(UpdateUserPasswordReq.class)
                .bindFromRequest();
        if (updateUserPasswordForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        UpdateUserPasswordReq updateUserPasswordReq = updateUserPasswordForm.get();
        UpdateUserPasswordRes updateUserPasswordRes = null;
        try {
            updateUserPasswordRes = UserManager.updateUserPassword(updateUserPasswordReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(updateUserPasswordRes).toObjectNode());
    }

    public static Result changeUserPassword() {

        Form<ChangeUserPasswordReq> changeUserPasswordForm = Form.form(ChangeUserPasswordReq.class)
                .bindFromRequest();
        if (changeUserPasswordForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        ChangeUserPasswordReq changeUserPasswordReq = changeUserPasswordForm.get();
        ChangeUserPasswordRes changeUserPasswordRes = null;
        try {
            changeUserPasswordRes = UserManager.changeUserPassword(changeUserPasswordReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(changeUserPasswordRes).toObjectNode());
    }

    public static Result updateUser() {

        Form<UpdateUserReq> updateUserForm = Form.form(UpdateUserReq.class).bindFromRequest();
        if (updateUserForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        UpdateUserReq updateUserReq = updateUserForm.get();
        UpdateUserRes updateUserRes = null;
        try {
            updateUserRes = UserManager.updateUser(updateUserReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(updateUserRes).toObjectNode());
    }

    public static Result getUserSelfFullProfile() {

        Form<GetUserSelfFullProfileReq> getUserSelfFullProfileForm = Form.form(
                GetUserSelfFullProfileReq.class).bindFromRequest();
        if (getUserSelfFullProfileForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetUserSelfFullProfileReq getUserSelfFullProfileReq = getUserSelfFullProfileForm.get();
        GetUserSelfFullProfileRes getUserSelfFullProfileRes = null;
        try {
            getUserSelfFullProfileRes = UserManager
                    .getUserSelfFullProfile(getUserSelfFullProfileReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getUserSelfFullProfileRes).toObjectNode());
    }

    public static Result validateEmail() {

        Form<ValidateEmailReq> validateEmailForm = Form.form(ValidateEmailReq.class)
                .bindFromRequest();
        if (validateEmailForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        ValidateEmailReq validateEmailReq = validateEmailForm.get();
        ValidateEmailRes validateEmailRes = null;
        try {
            validateEmailRes = UserManager.validateEmail(validateEmailReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(validateEmailRes).toObjectNode());
    }

    public static Result resendEmailVerification() {

        Form<ResendEmailVerificationReq> resendEmailVerificationForm = Form.form(
                ResendEmailVerificationReq.class).bindFromRequest();
        if (resendEmailVerificationForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        ResendEmailVerificationReq resendEmailVerificationReq = resendEmailVerificationForm.get();
        ResendEmailVerificationRes resendEmailVerificationRes = null;
        try {
            resendEmailVerificationRes = UserManager
                    .resendEmailVerification(resendEmailVerificationReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(resendEmailVerificationRes).toObjectNode());
    }

    public static Result unsetEmail() {

        Form<UnsetEmailReq> unsetEmailForm = Form.form(UnsetEmailReq.class).bindFromRequest();
        if (unsetEmailForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        UnsetEmailReq unsetEmailReq = unsetEmailForm.get();
        UnsetEmailRes unsetEmailRes = null;
        try {
            unsetEmailRes = UserManager.unsetEmail(unsetEmailReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(unsetEmailRes).toObjectNode());
    }

    public static Result sendForgotPasswordMail() {

        Form<SendForgotPasswordEmailReq> sendForgotPasswordEmailForm = Form.form(
                SendForgotPasswordEmailReq.class).bindFromRequest();
        if (sendForgotPasswordEmailForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        SendForgotPasswordEmailReq sendForgotPasswordEmailReq = sendForgotPasswordEmailForm.get();
        SendForgotPasswordEmailRes sendForgotPasswordEmailRes = null;
        try {
            sendForgotPasswordEmailRes = UserManager
                    .sendForgotPasswordEmail(sendForgotPasswordEmailReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(sendForgotPasswordEmailRes).toObjectNode());
    }

    public static Result updateUserForgottenPassword() {

        Form<UpdateUserForgottenPasswordReq> updateUserForgottenPasswordForm = Form.form(
                UpdateUserForgottenPasswordReq.class).bindFromRequest();
        if (updateUserForgottenPasswordForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        UpdateUserForgottenPasswordReq updateUserForgottenPasswordReq = updateUserForgottenPasswordForm
                .get();
        UpdateUserForgottenPasswordRes updateUserForgottenPasswordRes = null;
        try {
            updateUserForgottenPasswordRes = UserManager
                    .updateUserForgottenPassword(updateUserForgottenPasswordReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(updateUserForgottenPasswordRes).toObjectNode());
    }

    public static Result acceptTnC() {

        Form<AcceptTnCReq> acceptTnCForm = Form.form(AcceptTnCReq.class).bindFromRequest();
        if (acceptTnCForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        AcceptTnCReq acceptTnCReq = acceptTnCForm.get();
        AcceptTnCRes acceptTnCRes = null;
        try {
            acceptTnCRes = UserManager.acceptTnC(acceptTnCReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(acceptTnCRes).toObjectNode());
    }

    public static Result uploadProfilePic() {

        MultipartFormData body = request().body().asMultipartFormData();
        UploadProfilePicReq uploadProfilePicReq = new UploadProfilePicReq(body);
        String validation = uploadProfilePicReq.validate();
        Result result = null;
        if (StringUtils.isNotEmpty(validation)) {
            result = ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, validation))
                    .toObjectNode());
        } else if (null == uploadProfilePicReq.inputFile) {
            result = ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_FILE))
                    .toObjectNode());
        } else {

            try {
                UploadProfilePicRes uploadProfilePicRes = UserManager
                        .uploadProfilePic(uploadProfilePicReq);
                result = ok(getResultResponse(uploadProfilePicRes).toObjectNode());
            } catch (VedantuException e) {
                result = ok(getErrorResponse(e).toObjectNode());
            }
        }

        deleteFile(uploadProfilePicReq.fileName, uploadProfilePicReq.inputFile);
        return result;
    }

    public static Result getDirectURL() {

        Form<UserDirectLoginReq> requestForm = Form.form(UserDirectLoginReq.class)
                .bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        UserDirectLoginReq request = requestForm.get();
        UserDirectLoginRes response = null;
        try {
            response = UserManager.getUserDirectLogin(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result unsubscribeEmail() {

        Form<UnsubscribeReq> requestForm = Form.form(UnsubscribeReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            LOGGER.error(getErrorMessege(requestForm));
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        UnsubscribeReq request = requestForm.get();
        UnsetEmailRes response = null;

        try {
            LOGGER.debug(" Unsubscribing now");
            response = UserManager.unsubscribe(request);

        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result subscribeEmail() {

        Form<EmailSubscribeReq> requestForm = Form.form(EmailSubscribeReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            LOGGER.error(getErrorMessege(requestForm));
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        EmailSubscribeReq request = requestForm.get();
        UnsetEmailRes response = null;

        try {
            LOGGER.debug(" Subscribing now");

            response = UserManager.subscribe(request);

        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result getUnsubscriptions() {

        Form<GetUserEmailSubscriptionReq> requestForm = Form
                .form(GetUserEmailSubscriptionReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            LOGGER.error(getErrorMessege(requestForm));
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetUserEmailSubscriptionReq request = requestForm.get();
        UserEmailUnsubscriptionRes response = null;

        try {
            LOGGER.debug(" Subscribing now");

            response = UserManager.getUserEmailSubscriptions(request);

        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result removeUser() {

        return TODO;
    }

}
