package com.lms.service;


import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.models.AddOrgMemberMappingReq;
import com.lms.pojo.AddOrgMemberReq;
import com.lms.pojo.request.*;
import com.lms.user.vedantu.user.pojo.TestUserDataReq;
import com.lms.user.vedantu.user.requests.UserAuthReq;
import com.lms.user.vedantu.user.requests.UserExistenceReq;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface MemberService {
    VedantuResponse authenticateMember(MemberAuthReq memberAuthReq);

    VedantuResponse sendOTP(SendOTPReq sendOTPReq);

    VedantuResponse validateOTP(ValidateOTPReq validateOTPReq);

    VedantuResponse validateContactNumber(SendOTPReq sendOTPReq);

    VedantuResponse verifyContactNumber(SendOTPReq sendOTPReq);

    VedantuResponse getAllUserData(GetAllUserDataReq getAllUserDataReq);

    VedantuResponse unsetEmail(UnsetEmailReq unsetEmailReq);

    VedantuResponse activateUser(UserActivationUpdateReq userActivationUpdateReq);

    VedantuResponse activationPeriod(MemberActivationPeriodsReq memberActivationPeriodsReq);

    VedantuResponse getSection(GetOrgSectionReq getOrgSectionReq);

    VedantuResponse getPaymentInfo(GetPaymentInfoReq getPaymentInfoReq);

    VedantuResponse updateEndDateMapping(UpdateEndTimeMappingReq updateEndTimeMappingReq);

    VedantuResponse getSaleDetails(GetSaleDetailsReq getSaleDetailsReq);

    VedantuResponse authenticateOtpMember(UserAuthReq userAuthReq);

    VedantuResponse authenticateMember2(MemberAuthReq memberAuthReq);

    VedantuResponse getMemberProfile(GetOrgMemberProfileReq getOrgMemberProfileReq);

    VedantuResponse getOrgMemberProfile(GetOrgMemberReq getOrgMemberReq);

    VedantuResponse updateSaleDetails(UpdateSaleDetailsReq updateSaleDetailsReq);

    VedantuResponse getOrganizationMemberWithEmail(GetOrgMemberWithEmailReq getOrgMemberWithEmailReq);

    VedantuResponse getMembers(GetOrgMembersReq getOrgMembersReq);

    VedantuResponse addMember(AddOrgMemberReq addOrgMemberReq);

    VedantuResponse addMmberMapping(AddOrgMemberMappingReq addOrgMemberMappingReq);

    VedantuResponse updateMemberMapping(UpdateOrgMemberMappingReq updateOrgMemberMappingReq);

    VedantuResponse removeMemberMapping(RemoveOrgMemberMappingReq removeOrgMemberMappingReq);

    VedantuResponse updateOrgMemberReq(UpdateOrgMemberReq updateOrgMemberReq);

    VedantuResponse updateMemberEmail(UpdateOrgMemberReq updateOrgMemberReq);

    VedantuResponse resetUsername(ResetUsernameReq resetUsernameReq);

    VedantuResponse uploadProfilePic(MultipartFile file, UploadProfilePicReq uploadProfilePicReq);

    VedantuResponse bulkUploadProfilePics(MultipartFile file, BulkUploadProfilePicsReq bulkUploadProfilePicsReq);

    VedantuResponse sendForgotPasswordMail(SendForgotPasswordEmailReq sendForgotPasswordEmailReq);

    VedantuResponse bulkUpdateStudentsInSection(BulkUpdateStudentInSectionReq bulkUpdateStudentInSectionReq);


    VedantuResponse uploadStudents(MultipartFile file, UploadOrgStudentsReq uploadOrgStudentsReq);

    VedantuResponse saveTestUserData(TestUserDataReq testUserDataReq);

    File getTestUsersData(GetTestUsersDataReq getTestUsersDataReq);

    File getReferredUsersData(String referrer);

    File getStudentsData(GetStudentsDataReq getStudentsDataReq);

    VedantuResponse doesContactNumberExists(UserExistenceReq userExistenceReq);

    VedantuResponse isValidReferralCode(UserExistenceReq userExistenceReq);

    VedantuResponse getStudentsCount(GetCountOfStudentsReq getCountOfStudentsReq);

    VedantuResponse getwalletBalance(GetWalletBalanceReq getWalletBalanceReq);

    VedantuResponse getreferralData(GetReferralDataReq getReferralDataReq);

    VedantuResponse sendEmailsToStudents(SendEmailsToStudentsReq sendEmailsToStudentsReq);

    VedantuResponse addMemberWithAccessCode(AddOrgMemberWithRequestCodeReq addOrgMemberWithRequestCodeReq);
}
