package controllers;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.organization.managers.OrgMemberManager;
import com.vedantu.organization.managers.OrgProgramManager;
import com.vedantu.organization.pojos.requests.GetAllUserDataReq;
import com.vedantu.organization.pojos.requests.GetCountOfStudentsReq;
import com.vedantu.organization.pojos.requests.members.*;
import com.vedantu.organization.pojos.requests.organizations.GetOrgSectionReq;
import com.vedantu.organization.pojos.requests.organizations.GetPaymentInfoReq;
import com.vedantu.organization.pojos.requests.organizations.UserActivationUpdateReq;
import com.vedantu.organization.pojos.responses.GetAllUserDataRes;
import com.vedantu.organization.pojos.responses.GetCountOfStudentsRes;
import com.vedantu.organization.pojos.responses.members.*;
import com.vedantu.organization.pojos.responses.members.UnsetEmailRes;
import com.vedantu.organization.pojos.responses.organizations.GetOrgSectionRes;
import com.vedantu.organization.pojos.responses.organizations.GetPaymentInfoRes;
import com.vedantu.organization.pojos.responses.organizations.UserActivationRes;
import com.vedantu.organizations.auth.ExtAuthHandler;
import com.vedantu.user.pojos.requests.TestUserDataReq;
import com.vedantu.user.pojos.requests.UserAuthReq;
import com.vedantu.user.pojos.requests.UserExistenceReq;
import com.vedantu.user.pojos.responses.*;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Http.MultipartFormData;
import play.mvc.Result;

import java.io.File;


public class Members extends AbstractVedantuController {

    private static final Logger.ALogger LOGGER = Logger.of(Members.class);

    public static Result authenticateMember() {

        Form<MemberAuthReq> memberAuthForm = Form.form(MemberAuthReq.class).bindFromRequest();
        if (memberAuthForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(memberAuthForm))).toObjectNode());
        }
        MemberAuthReq memberAuthReq = memberAuthForm.get();
        UserAuthRes userAuthRes;
        try {
            userAuthRes = OrgMemberManager.authenticateOrgMember(memberAuthReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(userAuthRes).toObjectNode());
    }

    public static Result authenticateOTPMember() {

        Form<UserAuthReq> memberAuthForm = Form.form(UserAuthReq.class).bindFromRequest();
        if (memberAuthForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(memberAuthForm))).toObjectNode());
        }
        UserAuthReq memberAuthReq = memberAuthForm.get();
        UserAuthRes userAuthRes = null;
        try {
            userAuthRes = OrgMemberManager.authenticateOTPMember(memberAuthReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(userAuthRes).toObjectNode());
    }

    public static Result authenticateMember2() {

        Form<MemberAuthReq> memberAuthForm = Form.form(MemberAuthReq.class).bindFromRequest();
        if (memberAuthForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(memberAuthForm))).toObjectNode());
        }
        MemberAuthReq memberAuthReq = memberAuthForm.get();
        UserAuthRes userAuthRes = null;
        try {

            ExtAuthHandler authHandler = new ExtAuthHandler(
                    OrganizationDAO.INSTANCE.getById(memberAuthReq.orgId));

            userAuthRes = authHandler.authenticate(memberAuthReq.getMemberId(),
                    memberAuthReq.password);

        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(userAuthRes).toObjectNode());
    }

    /**
     * getOrgMemberProfile by memberId
     *
     * @return
     */
    public static Result getOrgMemberProfile() {

        Form<GetOrgMemberReq> getOrgMemberForm = Form.form(GetOrgMemberReq.class).bindFromRequest();
        if (getOrgMemberForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getOrgMemberForm))).toObjectNode());
        }
        GetOrgMemberReq getOrgMemberReq = getOrgMemberForm.get();
        GetOrgMemberProfileRes getOrgMemberRes = null;

        try {
            getOrgMemberRes = OrgMemberManager.getOrgMemberByMemberId(getOrgMemberReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getOrgMemberRes).toObjectNode());
    }

    public static Result getOrgMemberWithEmail() {

        Form<GetOrgMemberWithEmailReq> getOrgMemberForm = Form.form(GetOrgMemberWithEmailReq.class).bindFromRequest();
        if (getOrgMemberForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getOrgMemberForm))).toObjectNode());
        }
        GetOrgMemberWithEmailReq getOrgMemberWithEmailReq = getOrgMemberForm.get();
        GetOrgMemberProfileRes getOrgMemberRes = null;

        try {
            getOrgMemberRes = OrgMemberManager.getOrgMemberWithEmail(getOrgMemberWithEmailReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getOrgMemberRes).toObjectNode());
    }

    /**
     * * getOrgMemberProfile by userId
     *
     * @return
     */
    public static Result getMemberProfile() {

        Form<GetOrgMemberProfileReq> getOrgMemberProfileForm = Form.form(
                GetOrgMemberProfileReq.class).bindFromRequest();
        if (getOrgMemberProfileForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getOrgMemberProfileForm))).toObjectNode());
        }
        GetOrgMemberProfileReq getOrgMemberProfileReq = getOrgMemberProfileForm.get();
        GetOrgMemberProfileRes getOrgMemberProfileRes = null;

        try {
            getOrgMemberProfileRes = OrgMemberManager.getOrgMember(getOrgMemberProfileReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getOrgMemberProfileRes).toObjectNode());
    }

    public static Result activationPeriod() {

        Form<MemberActivationPeriodsReq> memberActivationPeriodsReqFrom = Form.form(
                MemberActivationPeriodsReq.class).bindFromRequest();
        if (memberActivationPeriodsReqFrom.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(memberActivationPeriodsReqFrom))).toObjectNode());
        }
        MemberActivationPeriodsReq memberActivationPeriodsReq = memberActivationPeriodsReqFrom
                .get();
        MemberActivationPeriodsRes memberActivationPeriodsRes = null;
        try {
            memberActivationPeriodsRes = OrgMemberManager
                    .getMemberActivationPeriods(memberActivationPeriodsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(memberActivationPeriodsRes).toObjectNode());
    }

    public static Result activateUser() {

        Form<UserActivationUpdateReq> userActivationRequestForm = Form.form(
                UserActivationUpdateReq.class).bindFromRequest();
        if (userActivationRequestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(userActivationRequestForm))).toObjectNode());
        }
        UserActivationUpdateReq userActivationReq = userActivationRequestForm.get();
        UserActivationRes userActivationRes = null;
        try {
            userActivationRes = OrgMemberManager.recordChange(userActivationReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(userActivationRes).toObjectNode());
    }

    public static Result getMembers() {

        Form<GetOrgMembersReq> getOrgMembersForm = Form.form(GetOrgMembersReq.class)
                .bindFromRequest();
        if (getOrgMembersForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getOrgMembersForm))).toObjectNode());
        }
        GetOrgMembersReq getOrgMembersReq = getOrgMembersForm.get();
        GetOrgMembersRes getOrgMemberRes = null;

        try {
            getOrgMemberRes = OrgMemberManager.getOrgMembers(getOrgMembersReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getOrgMemberRes).toObjectNode());
    }

    public static Result verifyContactNumber() {
        Form<SendOTPReq> sendOTP = Form.form(SendOTPReq.class).bindFromRequest();
        if (sendOTP.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(sendOTP))).toObjectNode());
        }
        SendOTPReq sendOTPReq = sendOTP.get();
        SendOTPRes sendOTPRes = null;

        try {
            sendOTPRes = OrgMemberManager.verifyContactNumber(sendOTPReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(sendOTPRes).toObjectNode());
    }

    public static Result sendOTP() {
        Form<SendOTPReq> sendOTP = Form.form(SendOTPReq.class).bindFromRequest();
        if (sendOTP.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(sendOTP))).toObjectNode());
        }
        SendOTPReq sendOTPReq = sendOTP.get();
        SendOTPRes sendOTPRes = null;

        try {
            sendOTPRes = OrgMemberManager.sendOTP(sendOTPReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(sendOTPRes).toObjectNode());
    }

    public static Result sendOTPApp() {
        Form<SendOTPReq> sendOTP = Form.form(SendOTPReq.class).bindFromRequest();
        if (sendOTP.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(sendOTP))).toObjectNode());
        }
        SendOTPReq sendOTPReq = sendOTP.get();
        SendOTPRes sendOTPRes = null;

        try {
            sendOTPRes = OrgMemberManager.sendOTPApp(sendOTPReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(sendOTPRes).toObjectNode());
    }

    public static Result validateOTP() {
        Form<ValidateOTPReq> validateOTP = Form.form(ValidateOTPReq.class).bindFromRequest();
        if (validateOTP.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(validateOTP))).toObjectNode());
        }
        ValidateOTPReq validateOTPReq = validateOTP.get();
        ValidateOTPRes validateOTPRes = null;

        try {
            validateOTPRes = OrgMemberManager.validateOTP(validateOTPReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(validateOTPRes).toObjectNode());
    }

    public static Result validateContactNumber() {

        Form<SendOTPReq> validateContactForm = Form.form(SendOTPReq.class)
                .bindFromRequest();
        if (validateContactForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        SendOTPReq validateContactReq = validateContactForm.get();
        SendOTPRes validateContactRes = null;
        try {
            validateContactRes = OrgMemberManager.validateContactNumber(validateContactReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(validateContactRes).toObjectNode());
    }

    public static Result doesContactNumberExists() {

        Form<UserExistenceReq> userExistenceForm = Form.form(UserExistenceReq.class).bindFromRequest();
        if (userExistenceForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        UserExistenceReq userExistenceReq = userExistenceForm.get();
        UserExistenceRes userAuthRes = null;
        try {
            userAuthRes = OrgMemberManager.doesContactNumberExists(userExistenceReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(userAuthRes).toObjectNode());
    }

    public static Result getReferredUsersData() {
        DynamicForm sampleForm = Form.form().bindFromRequest();
        String referralCode = sampleForm.get("referrer");
        File resultFile = null;
        try {
            resultFile = OrgMemberManager.getReferralUsersData(referralCode);
        } catch (VedantuException e) {
            LOGGER.error(e.getMessage(), e);
            if(e.errorCode == VedantuErrorCode.INVALID_REFERER) {
                return ok("Invalid Referer");
            }
        }
        if(resultFile == null) {
            return ok("No data found");
        }
        return ok(resultFile);
    }

    public static Result getTestUsersData() {
        DynamicForm sampleForm = Form.form().bindFromRequest();
        Long startDate = Long.parseLong(sampleForm.get("startDate"));
        Long endDate = Long.parseLong(sampleForm.get("endDate"));
        String orgId = sampleForm.get("orgId");
        File resultFile = null;
        try {
            resultFile = OrgMemberManager.getTestUsersData(orgId,startDate,endDate);
        } catch (VedantuException e) {
            LOGGER.error(e.getMessage(), e);
            if(e.errorCode == VedantuErrorCode.CANNOT_WRITE_FILE) {
                return ok("Cannot download file");
            }
        }
        if(resultFile == null) {
            return ok("There are no users matching your criteria.Please change criteria and try again");
        }
        return ok(resultFile);
    }

    public static Result getStudentsData() {
        DynamicForm sampleForm = Form.form().bindFromRequest();
        Long startDate = Long.parseLong(sampleForm.get("startDate"));
        Long endDate = Long.parseLong(sampleForm.get("endDate"));
        String orgId = sampleForm.get("orgId");
        File resultFile = null;
        try {
            resultFile = OrgMemberManager.getStudentsData(orgId,startDate,endDate);
        } catch (VedantuException e) {
            LOGGER.error(e.getMessage(), e);
            if(e.errorCode == VedantuErrorCode.CANNOT_WRITE_FILE) {
                return ok("Cannot download file");
            }
        }
        if(resultFile == null) {
            return ok("There are no users matching your criteria.Please change criteria and try again");
        }
        return ok(resultFile);
    }

    public static Result isValidReferralCode() {

        Form<UserExistenceReq> userExistenceForm = Form.form(UserExistenceReq.class).bindFromRequest();
        if (userExistenceForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        UserExistenceReq userExistenceReq = userExistenceForm.get();
        UserExistenceRes userAuthRes = null;
        userAuthRes = OrgMemberManager.isValidReferralCode(userExistenceReq);
        return ok(getResultResponse(userAuthRes).toObjectNode());
    }

    public static Result addMember() {

        Form<AddOrgMemberReq> addOrgMemberForm = Form.form(AddOrgMemberReq.class).bindFromRequest();
        if (addOrgMemberForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(addOrgMemberForm))).toObjectNode());
        }
        AddOrgMemberReq addOrgMemberReq = addOrgMemberForm.get();
        AddOrgMemberRes addOrgMemberRes = null;

        try {
            addOrgMemberRes = OrgMemberManager.addOrgMember(addOrgMemberReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(addOrgMemberRes).toObjectNode());
    }

    public static Result getReferralData() {
        Form<GetReferralDataReq> getReferralDataForm = Form.form(GetReferralDataReq.class)
                .bindFromRequest();
        if (getReferralDataForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getReferralDataForm))).toObjectNode());
        }
        GetReferralDataReq getReferralDataReq = getReferralDataForm.get();
        GetReferralDataRes getReferralDataRes = null;

        getReferralDataRes = OrgMemberManager.getReferralData(getReferralDataReq);

        return ok(getResultResponse(getReferralDataRes).toObjectNode());
    }

    public static Result getWalletBalance() {
        Form<GetWalletBalanceReq> getWalletBalanceDataForm = Form.form(GetWalletBalanceReq.class)
                .bindFromRequest();
        if (getWalletBalanceDataForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getWalletBalanceDataForm))).toObjectNode());
        }
        GetWalletBalanceReq getWalletBalanceReq = getWalletBalanceDataForm.get();
        GetWalletBalanceRes getWalletBalanceRes = null;

        getWalletBalanceRes = OrgMemberManager.getWalletBalance(getWalletBalanceReq);

        return ok(getResultResponse(getWalletBalanceRes).toObjectNode());
    }

    public static Result addMemberMapping() {

        Form<AddOrgMemberMappingReq> addOrgMemberMappingForm = Form.form(
                AddOrgMemberMappingReq.class).bindFromRequest();
        if (addOrgMemberMappingForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(addOrgMemberMappingForm))).toObjectNode());
        }
        AddOrgMemberMappingReq addOrgMemberMappingReq = addOrgMemberMappingForm.get();
        AddOrgMemberMappingRes addOrgMemberMappingRes = null;

        try {
            final boolean noExceptionOnExistingMapping = false;
            addOrgMemberMappingRes = OrgMemberManager.addOrgMemberMapping(addOrgMemberMappingReq,
                    noExceptionOnExistingMapping);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(addOrgMemberMappingRes).toObjectNode());
    }

    public static Result updateMemberMapping() {

        Form<UpdateOrgMemberMappingReq> updateOrgMemberMappingForm = Form.form(
                UpdateOrgMemberMappingReq.class).bindFromRequest();
        if (updateOrgMemberMappingForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(updateOrgMemberMappingForm))).toObjectNode());
        }
        UpdateOrgMemberMappingReq updateOrgMemberMappingReq = updateOrgMemberMappingForm.get();
        UpdateOrgMemberMappingRes updateOrgMemberMappingRes = null;

        try {
            updateOrgMemberMappingRes = OrgMemberManager
                    .updateOrgMemberMapping(updateOrgMemberMappingReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(updateOrgMemberMappingRes).toObjectNode());
    }

    public static Result removeMemberMapping() {

        Form<RemoveOrgMemberMappingReq> removeOrgMemberMappingForm = Form.form(
                RemoveOrgMemberMappingReq.class).bindFromRequest();
        if (removeOrgMemberMappingForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(removeOrgMemberMappingForm))).toObjectNode());
        }
        RemoveOrgMemberMappingReq removeOrgMemberMappingReq = removeOrgMemberMappingForm.get();
        RemoveOrgMemberMappingRes removeOrgMemberMappingRes = null;

        try {
            removeOrgMemberMappingRes = OrgMemberManager
                    .removeOrgMemberMapping(removeOrgMemberMappingReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(removeOrgMemberMappingRes).toObjectNode());
    }

    public static Result setMemberRole() {

        return TODO;
    }

    public static Result addMemberPic() {

        return TODO;
    }

    public static Result updateMember() {

        Form<UpdateOrgMemberReq> updateOrgMemberForm = Form.form(UpdateOrgMemberReq.class)
                .bindFromRequest();
        if (updateOrgMemberForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(updateOrgMemberForm))).toObjectNode());
        }
        UpdateOrgMemberReq updateOrgMemberReq = updateOrgMemberForm.get();
        UpdateOrgMemberRes updateOrgMemberRes = null;

        try {
            updateOrgMemberRes = OrgMemberManager.updateOrgMember(updateOrgMemberReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(updateOrgMemberRes).toObjectNode());
    }

    public static Result updateMemberEmail() {

        Form<UpdateOrgMemberReq> updateOrgMemberForm = Form.form(UpdateOrgMemberReq.class)
                .bindFromRequest();
        if (updateOrgMemberForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(updateOrgMemberForm))).toObjectNode());
        }
        UpdateOrgMemberReq updateOrgMemberReq = updateOrgMemberForm.get();
        UpdateOrgMemberRes updateOrgMemberRes = null;

        try {
            updateOrgMemberRes = OrgMemberManager.updateOrgMemberEmail(updateOrgMemberReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(updateOrgMemberRes).toObjectNode());
    }

    public static Result uploadStudents() {

        MultipartFormData body = request().body().asMultipartFormData();
        UploadOrgStudentsReq uploadOrgStudentsReq = new UploadOrgStudentsReq(body);
        String validation = uploadOrgStudentsReq.validate();
        Result result = null;
        if (StringUtils.isNotEmpty(validation)) {
            result = ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, validation))
                    .toObjectNode());
        } else if (null == uploadOrgStudentsReq.inputFile) {
            result = ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_FILE))
                    .toObjectNode());
        } else if (ObjectIdUtils.hasInvalidId(uploadOrgStudentsReq.orgId,
                uploadOrgStudentsReq.orgMemberId, uploadOrgStudentsReq.programId)) {
            result = ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        } else {

            try {
                UploadOrgStudentsRes uploadOrgStudentsRes = OrgMemberManager
                        .uploadOrgStudents(uploadOrgStudentsReq);
                result = ok(getResultResponse(uploadOrgStudentsRes).toObjectNode());
            } catch (VedantuException e) {
                result = ok(getErrorResponse(e).toObjectNode());
            }
        }

        deleteFile(uploadOrgStudentsReq.fileName, uploadOrgStudentsReq.inputFile);
        return result;
    }

    public static Result resetUsername() {

        Form<ResetUsernameReq> resetUsernameForm = Form.form(ResetUsernameReq.class)
                .bindFromRequest();
        if (resetUsernameForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        ResetUsernameReq resetUsernameReq = resetUsernameForm.get();
        if (ObjectIdUtils.hasInvalidId(resetUsernameReq.orgId, resetUsernameReq.targetUserId,
                resetUsernameReq.targetOrgMemberId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        ResetUsernameRes resetUsernameRes = null;

        try {
            resetUsernameRes = OrgMemberManager.resetUsername(resetUsernameReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(resetUsernameRes).toObjectNode());
    }

    public static Result unsetEmail() {

        Form<UnsetEmailReq> unsetEmailForm = Form.form(UnsetEmailReq.class).bindFromRequest();
        if (unsetEmailForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        UnsetEmailReq unsetEmailReq = unsetEmailForm.get();
        if (ObjectIdUtils.hasInvalidId(unsetEmailReq.orgId, unsetEmailReq.targetUserId,
                unsetEmailReq.targetOrgMemberId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }

        UnsetEmailRes unsetEmailRes = null;
        try {
            unsetEmailRes = OrgMemberManager.unsetEmail(unsetEmailReq);
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
        if (ObjectIdUtils.hasInvalidId(sendForgotPasswordEmailReq.orgId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        SendForgotPasswordEmailRes sendForgotPasswordEmailRes = null;
        try {
            sendForgotPasswordEmailRes = OrgMemberManager
                    .sendForgotPasswordEmail(sendForgotPasswordEmailReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(sendForgotPasswordEmailRes).toObjectNode());
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
            if (ObjectIdUtils.hasInvalidId(uploadProfilePicReq.orgId,
                    uploadProfilePicReq.targetUserId, uploadProfilePicReq.targetOrgMemberId)) {
                return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                        .toObjectNode());
            }

            try {
                UploadProfilePicRes uploadProfilePicRes = OrgMemberManager
                        .uploadProfilePic(uploadProfilePicReq);
                result = ok(getResultResponse(uploadProfilePicRes).toObjectNode());
            } catch (VedantuException e) {
                result = ok(getErrorResponse(e).toObjectNode());
            }
        }

        deleteFile(uploadProfilePicReq.fileName, uploadProfilePicReq.inputFile);
        return result;
    }

    public static Result bulkUploadProfilePics() {

        MultipartFormData body = request().body().asMultipartFormData();
        BulkUploadProfilePicsReq bulkUploadProfilePicReq = new BulkUploadProfilePicsReq(body);
        String validation = bulkUploadProfilePicReq.validate();
        Result result = null;
        if (StringUtils.isNotEmpty(validation)) {
            result = ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, validation))
                    .toObjectNode());
        } else if (null == bulkUploadProfilePicReq.inputFile) {
            result = ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_FILE))
                    .toObjectNode());
        }
        try {
            BulkUploadOrgMembersProfilePicRes bulkUploadProfilePicRes = OrgMemberManager
                    .bulkUploadMembersProfilePic(bulkUploadProfilePicReq);
            result = ok(getResultResponse(bulkUploadProfilePicRes).toObjectNode());
        } catch (VedantuException e) {
            result = ok(getErrorResponse(e).toObjectNode());
        }

        deleteFile(bulkUploadProfilePicReq.fileName, bulkUploadProfilePicReq.inputFile);
        return result;
    }

    public static Result removeMember() {

        return TODO;
    }

    public static Result sendVerificationLink() {

        return TODO;
    }

    public static Result removeEmail() {

        return TODO;
    }

    public static Result addMemberWithAccessCode() {

        Form<AddOrgMemberWithRequestCodeReq> form = Form.form(AddOrgMemberWithRequestCodeReq.class)
                .bindFromRequest();
        if (form.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, getErrorMessege(form)))
                    .toObjectNode());
        }
        AddOrgMemberWithRequestCodeReq req = form.get();
        AddOrgMemberRes res = null;

        try {
            res = OrgMemberManager.addOrgMemberWithAccessCode(req);

        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(res).toObjectNode());
    }

    public static Result bulkUpdateStudentsInSection() {

        Form<BulkUpdateStudentInSectionReq> form = Form.form(BulkUpdateStudentInSectionReq.class)
                .bindFromRequest();
        if (form.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, getErrorMessege(form)))
                    .toObjectNode());
        }

        BulkUpdateStudentInSectionReq req = form.get();
        BulkUpdateStudentsInSectionRes res = null;

        try {
            res = OrgMemberManager.updateStudentsInSection(req);

        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(res).toObjectNode());
    }

    public static Result getSection() {

        Form<GetOrgSectionReq> requestForm = Form.form(GetOrgSectionReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS),
                    getErrorMessege(requestForm)).toObjectNode());
        }
        GetOrgSectionReq request = requestForm.get();

        GetOrgSectionRes response = null;

        try {

            response = OrgProgramManager.getProgramSection(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result getPaymentInfo() {

        Form<GetPaymentInfoReq> requestForm = Form.form(GetPaymentInfoReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS),
                    getErrorMessege(requestForm)).toObjectNode());
        }
        GetPaymentInfoReq request = requestForm.get();

        GetPaymentInfoRes response = null;

        try {

            response = OrgProgramManager.getProgramPaymentInfo(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result updateEndDateMapping(){
        Form<UpdateEndTimeMappingReq> requestForm =
                Form.form(UpdateEndTimeMappingReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS),
                    getErrorMessege(requestForm)).toObjectNode());
        }
        UpdateEndTimeMappingReq request = requestForm.get();

        UpdateEndTimeMappingRes response = null;

        try {
            response = OrgMemberManager.updateEndDateMapping(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result sendEmailsToStudents() throws VedantuException{
        Form<SendEmailsToStudentsReq> requestForm =
                Form.form(SendEmailsToStudentsReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS),
                    getErrorMessege(requestForm)).toObjectNode());
        }
        SendEmailsToStudentsReq request = requestForm.get();
        SendEmailsToStudentsRes response = null;

        response = OrgMemberManager.sendEmailsToStudents(request);
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result getSaleDetails() throws VedantuException {
        Form<GetSaleDetailsReq> requestForm =
                Form.form(GetSaleDetailsReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS),
                    getErrorMessege(requestForm)).toObjectNode());
        }
        GetSaleDetailsReq request = requestForm.get();
        GetSaleDetailsRes response = null;

        response = OrgMemberManager.getSaleDetails(request);
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result updateSaleDetails() throws VedantuException {
        Form<UpdateSaleDetailsReq> requestForm =
                Form.form(UpdateSaleDetailsReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS),
                    getErrorMessege(requestForm)).toObjectNode());
        }
        UpdateSaleDetailsReq request = requestForm.get();
        UpdateSaleDetailsRes response = null;

        response = OrgMemberManager.updateSaleDetails(request);
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result getStudentsCount() {
        Form<GetCountOfStudentsReq> requestForm =
                Form.form(GetCountOfStudentsReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS),
                    getErrorMessege(requestForm)).toObjectNode());
        }
        GetCountOfStudentsReq request = requestForm.get();
        GetCountOfStudentsRes response = null;

        response = OrgMemberManager.getStudentsCount(request);
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result saveTestUserData() {
        Form<TestUserDataReq> requestForm = Form
                .form(TestUserDataReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            LOGGER.error(getErrorMessege(requestForm));
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        TestUserDataReq request = requestForm.get();
        TestUserDataRes response = null;
        try {
            LOGGER.debug(" Subscribing now");

            response = OrgMemberManager.saveTestUserData(request);

        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result getAllUserData() {
        Form<GetAllUserDataReq> requestForm = Form
                .form(GetAllUserDataReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            LOGGER.error(getErrorMessege(requestForm));
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetAllUserDataReq request = requestForm.get();
        GetAllUserDataRes response = null;
        try {
            response = OrgMemberManager.getAllUserData(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }
}
