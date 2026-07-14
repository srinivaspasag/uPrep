package com.lms.controller;

import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.models.AddOrgMemberMappingReq;
import com.lms.pojo.AddOrgMemberReq;
import com.lms.pojo.request.*;
import com.lms.service.MemberService;
import com.lms.user.vedantu.user.pojo.TestUserDataReq;
import com.lms.user.vedantu.user.requests.UserAuthReq;
import com.lms.user.vedantu.user.requests.UserExistenceReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.websocket.server.PathParam;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@RestController
@RequestMapping("/members")
public class Members {
    @Autowired
    private MemberService memberServiceImpl;

    @PostMapping("/authenticateMember")
    public ResponseEntity<VedantuResponse> authenticateMember(@Valid MemberAuthReq memberAuthReq) throws VedantuException {

        return ResponseEntity.ok(memberServiceImpl.authenticateMember(memberAuthReq));
    }

    @PostMapping("/sendOTP")
    public ResponseEntity<VedantuResponse> sendOTP(@Valid SendOTPReq sendOTPReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.sendOTP(sendOTPReq));
    }

    @PostMapping("/validateOTP")
    public ResponseEntity<VedantuResponse> validateOTP(@Valid ValidateOTPReq validateOTPReq) {
        return ResponseEntity.ok(memberServiceImpl.validateOTP(validateOTPReq));
    }

    @PostMapping("/validateContactNumber")
    public ResponseEntity<VedantuResponse> validateContactNumber(@Valid SendOTPReq sendOTPReq) {
        return ResponseEntity.ok(memberServiceImpl.validateContactNumber(sendOTPReq));
    }

    @PostMapping("/verifyContactNumber")
    public ResponseEntity<VedantuResponse> verifyContactNumber(@Valid SendOTPReq sendOTPReq) {
        return ResponseEntity.ok(memberServiceImpl.verifyContactNumber(sendOTPReq));
    }

    @PostMapping("/getAllUserData")
    public ResponseEntity<VedantuResponse> getAllUserData(@Valid GetAllUserDataReq getAllUserDataReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.getAllUserData(getAllUserDataReq));
    }

    @PostMapping("/unsetEmail")
    public ResponseEntity<VedantuResponse> unsetEmail(@Valid UnsetEmailReq unsetEmailReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.unsetEmail(unsetEmailReq));
    }

    @PostMapping("/activate")
    public ResponseEntity<VedantuResponse> activateUser(@Valid UserActivationUpdateReq userActivationUpdateReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.activateUser(userActivationUpdateReq));
    }

    @PostMapping("/activationPeriod")
    public ResponseEntity<VedantuResponse> activationPeriod(@Valid MemberActivationPeriodsReq memberActivationPeriodsReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.activationPeriod(memberActivationPeriodsReq));
    }

    @PostMapping("/getSection")
    public ResponseEntity<VedantuResponse> getSection(@Valid GetOrgSectionReq getOrgSectionReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.getSection(getOrgSectionReq));
    }

    @PostMapping("/getPaymentInfo")
    public ResponseEntity<VedantuResponse> getPaymentInfo(@Valid GetPaymentInfoReq getPaymentInfoReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.getPaymentInfo(getPaymentInfoReq));
    }

    @PostMapping("/updateEndDateMapping")
    public ResponseEntity<VedantuResponse> updateEndDateMapping(@Valid UpdateEndTimeMappingReq updateEndTimeMappingReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.updateEndDateMapping(updateEndTimeMappingReq));
    }

    @PostMapping("/getSaleDetails")
    public ResponseEntity<VedantuResponse> getSaleDetails(@Valid GetSaleDetailsReq getSaleDetailsReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.getSaleDetails(getSaleDetailsReq));
    }

    @PostMapping("/authenticateOTPMember")
    public ResponseEntity<VedantuResponse> authenticateOTPMember(@Valid UserAuthReq userAuthReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.authenticateOtpMember(userAuthReq));
    }

    @PostMapping("/authenticateMember2")
    public ResponseEntity<VedantuResponse> authenticateMember2(@Valid MemberAuthReq memberAuthReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.authenticateMember2(memberAuthReq));
    }

    @PostMapping("/getMemberProfile")
    public ResponseEntity<VedantuResponse> getMemberProfile(@Valid GetOrgMemberProfileReq getOrgMemberProfileReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.getMemberProfile(getOrgMemberProfileReq));
    }

    @PostMapping("/getOrgMemberProfile")
    public ResponseEntity<VedantuResponse> getOrgMemberProfile(@Valid GetOrgMemberReq getOrgMemberReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.getOrgMemberProfile(getOrgMemberReq));
    }

    @PostMapping("/updateSaleDetails")
    public ResponseEntity<VedantuResponse> updateSaleDetails(@Valid UpdateSaleDetailsReq updateSaleDetailsReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.updateSaleDetails(updateSaleDetailsReq));
    }

    @PostMapping("/getOrgMemberWithEmail")
    public ResponseEntity<VedantuResponse> getOrgMemberWithEmail(@Valid GetOrgMemberWithEmailReq getOrgMemberWithEmailReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.getOrganizationMemberWithEmail(getOrgMemberWithEmailReq));
    }

    @PostMapping("/getMembers")
    public ResponseEntity<VedantuResponse> getMembers(@Valid GetOrgMembersReq getOrgMembersReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.getMembers(getOrgMembersReq));
    }

    @PostMapping("/addMember")
    public ResponseEntity<VedantuResponse> addMember(@Valid AddOrgMemberReq addOrgMemberReq) throws VedantuException {
        System.out.println("request>>>>>>>>>>>>:" + addOrgMemberReq);
        return ResponseEntity.ok(memberServiceImpl.addMember(addOrgMemberReq));
    }

    @PostMapping("/addMemberMapping")
    public ResponseEntity<VedantuResponse> addMemberMapping(@Valid AddOrgMemberMappingReq addOrgMemberMappingReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.addMmberMapping(addOrgMemberMappingReq));
    }

    @PostMapping("/updateMemberMapping")
    public ResponseEntity<VedantuResponse> updateMemberMapping(@Valid UpdateOrgMemberMappingReq updateOrgMemberMappingReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.updateMemberMapping(updateOrgMemberMappingReq));
    }

    @PostMapping("/removeMemberMapping")
    public ResponseEntity<VedantuResponse> removeMemberMapping(@Valid RemoveOrgMemberMappingReq removeOrgMemberMappingReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.removeMemberMapping(removeOrgMemberMappingReq));
    }

    @PostMapping("/setMemberRole")
    public ResponseEntity<VedantuResponse> setMemberRole() throws VedantuException {
        //TODO
        return null;
    }

    @PostMapping("/addMemberPic")
    public ResponseEntity<VedantuResponse> addMemberPic() throws VedantuException {
        //TODO
        return null;
    }

    @PostMapping("/updateMember")
    public ResponseEntity<VedantuResponse> updateMember(@Valid UpdateOrgMemberReq updateOrgMemberReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.updateOrgMemberReq(updateOrgMemberReq));
    }

    @PostMapping("/updateMemberEmail")
    public ResponseEntity<VedantuResponse> updateMemberEmail(@Valid UpdateOrgMemberReq updateOrgMemberReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.updateMemberEmail(updateOrgMemberReq));
    }

    @PostMapping("/resetUsername")
    public ResponseEntity<VedantuResponse> resetUsername(@Valid ResetUsernameReq resetUsernameReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.resetUsername(resetUsernameReq));
    }

    @PostMapping("/uploadProfilePic")
    public ResponseEntity<VedantuResponse> uploadProfilePic(@RequestParam("file") MultipartFile file, UploadProfilePicReq uploadProfilePicReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.uploadProfilePic(file, uploadProfilePicReq));
    }

    @PostMapping("/bulkUploadProfilePics")
    public ResponseEntity<VedantuResponse> bulkUploadProfilePics(@RequestParam("file") MultipartFile file, BulkUploadProfilePicsReq bulkUploadProfilePicsReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.bulkUploadProfilePics(file, bulkUploadProfilePicsReq));
    }

    @PostMapping("/sendForgotPasswordMail")
    public ResponseEntity<VedantuResponse> sendForgotPasswordMail(@Valid SendForgotPasswordEmailReq sendForgotPasswordEmailReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.sendForgotPasswordMail(sendForgotPasswordEmailReq));
    }

    @PostMapping("/bulkUpdateStudentsInSection")
    public ResponseEntity<VedantuResponse> bulkUpdateStudentsInSection(@Valid BulkUpdateStudentInSectionReq bulkUpdateStudentInSectionReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.bulkUpdateStudentsInSection(bulkUpdateStudentInSectionReq));
    }

    @PostMapping("/uploadStudents")
    public ResponseEntity<VedantuResponse> uploadStudents(@RequestParam("file") MultipartFile file, UploadOrgStudentsReq uploadOrgStudentsReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.uploadStudents(file, uploadOrgStudentsReq));
    }


    @PostMapping("/doesContactNumberExists")
    public ResponseEntity<VedantuResponse> doesContactNumberExists(@Valid UserExistenceReq userExistenceReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.doesContactNumberExists(userExistenceReq));
    }

    @PostMapping("/isValidReferralCode")
    public ResponseEntity<VedantuResponse> isValidReferralCode(@Valid UserExistenceReq userExistenceReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.isValidReferralCode(userExistenceReq));
    }

    @PostMapping("/getStudentsCount")
    public ResponseEntity<VedantuResponse> getStudentsCount(@Valid GetCountOfStudentsReq getCountOfStudentsReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.getStudentsCount(getCountOfStudentsReq));
    }

    @PostMapping("/getWalletBalance")
    public ResponseEntity<VedantuResponse> getWalletBalance(@Valid GetWalletBalanceReq getWalletBalanceReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.getwalletBalance(getWalletBalanceReq));
    }

    @PostMapping("/getReferralData")
    public ResponseEntity<VedantuResponse> getReferralData(@Valid GetReferralDataReq getReferralDataReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.getreferralData(getReferralDataReq));
    }

    @PostMapping("/saveTestUserData")
    public ResponseEntity<VedantuResponse> saveTestUserData(@Valid TestUserDataReq testUserDataReq) {
        return ResponseEntity.ok(memberServiceImpl.saveTestUserData(testUserDataReq));
    }

    @PostMapping("/sendEmailsToStudents")
    public ResponseEntity<VedantuResponse> sendEmailsToStudents(@Valid SendEmailsToStudentsReq sendEmailsToStudentsReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.sendEmailsToStudents(sendEmailsToStudentsReq));
    }

    @PostMapping("/addMemberWithAccessCode")
    public ResponseEntity<VedantuResponse> addMemberWithAccessCode(@Valid AddOrgMemberWithRequestCodeReq addOrgMemberWithRequestCodeReq) throws VedantuException {
        return ResponseEntity.ok(memberServiceImpl.addMemberWithAccessCode(addOrgMemberWithRequestCodeReq));
    }

    // for csv file generation need to confirm
    @PostMapping("/getTestUsersData")
    public ResponseEntity getTestUsersData(@Valid GetTestUsersDataReq getTestUsersDataReq) throws FileNotFoundException, VedantuException {
        File file = memberServiceImpl.getTestUsersData(getTestUsersDataReq);
        InputStreamResource inputStreamResource = new InputStreamResource(new FileInputStream(file));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Disposition", String.format("attachment;filename=\"%s\"", file.getName()));
        httpHeaders.add("Cache-Control", "no-cache,no-store,must-revalidate");
        httpHeaders.add("Pragma", "no-cache");
        httpHeaders.add("Expires", "0");
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().headers(httpHeaders).contentLength(file.length()).contentType(MediaType.parseMediaType("application/octet-stream")).body(inputStreamResource);
        return responseEntity;
    }


    // for csv file generation need to confirm
    @PostMapping("/getReferredUsersData")
    public ResponseEntity getReferredUsersData(@PathParam("referrer") String referrer) throws VedantuException, FileNotFoundException {
        File file = memberServiceImpl.getReferredUsersData(referrer);
        InputStreamResource inputStreamResource = new InputStreamResource(new FileInputStream(file));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Disposition", String.format("attachment;filename=\"%s\"", file.getName()));
        httpHeaders.add("Cache-Control", "no-cache,no-store,must-revalidate");
        httpHeaders.add("Pragma", "no-cache");
        httpHeaders.add("Expires", "0");
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().headers(httpHeaders).contentLength(file.length()).contentType(MediaType.parseMediaType("application/octet-stream")).body(inputStreamResource);
        return responseEntity;
    }


    // for csv file generation need to confirm
    @PostMapping("/getStudentsData")
    public ResponseEntity getStudentsData(@Valid GetStudentsDataReq getStudentsDataReq) throws VedantuException, FileNotFoundException {
        File file = memberServiceImpl.getStudentsData(getStudentsDataReq);
        InputStreamResource inputStreamResource = new InputStreamResource(new FileInputStream(file));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Disposition", String.format("attachment;filename=\"%s\"", file.getName()));
        httpHeaders.add("Cache-Control", "no-cache,no-store,must-revalidate");
        httpHeaders.add("Pragma", "no-cache");
        httpHeaders.add("Expires", "0");
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().headers(httpHeaders).contentLength(file.length()).contentType(MediaType.parseMediaType("application/octet-stream")).body(inputStreamResource);
        return responseEntity;
    }
}
