package com.lms.service;

import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.commons.pojos.requests.AddOrgCenterReq;
import com.lms.common.vedantu.commons.pojos.requests.StateChangeOrgCenterReq;
import com.lms.common.vedantu.commons.pojos.requests.UpdateOrgCenterReq;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojo.GetOrgReq;
import com.lms.pojo.GetSharedOrgsReq;
import com.lms.pojo.request.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

public interface OrganizationService {
    VedantuResponse getOrganization(GetOrgReq getOrgReq) throws VedantuException;

    VedantuResponse getShowSharedSubjects(GetOrgReq getOrgReq);

    VedantuResponse getOrganizationInfoForInvoice(GetOrgReq getOrgReq) throws VedantuException;

    VedantuResponse checkWebsite(CheckWebsiteReq checkWebsiteReq) throws VedantuException;

    VedantuResponse checkSlug(CheckSlugReq checkSlugReq) throws VedantuException;

    VedantuResponse checkAppVersion(CheckAppVersionReq checkAppVersionReq) throws VedantuException;

    VedantuResponse updateOrganizationSlug(UpdateOrgSlugReq updateOrgSlugReq) throws VedantuException;

    VedantuResponse updateOrganizationStatus(UpdateOrgStatusReq updateOrgStatusReq) throws VedantuException;

    VedantuResponse updateOrganizationDownloadStatus(UpdateOrganizationDownloadStatusReq updateOrgStatusReq);

    VedantuResponse UpdateOrganizationSharedSubjects(UpdateOrganizationSharedSubjectsReq updateOrganizationSharedSubjectsReq);

    VedantuResponse updateOrganizationClassroomConnectStatus(UpdateOrganizationClassroomConnectStatusReq updateOrganizationClassroomConnectStatusReq);

    VedantuResponse getOrganizationBySlug(GetOrgBySlugReq getOrgBySlugReq) throws VedantuException;

    VedantuResponse updateOrganizationReferer(UpdateOrgRefererReq updateOrgRefererReq) throws VedantuException;

    VedantuResponse getOrganizationByReferer(boolean getKey,String referer) throws MalformedURLException, VedantuException;

    VedantuResponse checkReferer(CheckRefererReq checkRefererReq) throws VedantuException;

    VedantuResponse getOrganizations(GetOrgsReq getOrgsReq);

    VedantuResponse addOrganization(AddOrgReq addOrgReq) throws VedantuException;

    VedantuResponse updateOrgMemberExtraInputFields(UpdateOrgMemberExtraInfoInputFieldsReq updateOrgMemberExtraInfoInputFieldsReq) throws VedantuException;

    VedantuResponse updateDigitalLibraryFields(UpdateDigitalLibraryFieldsReq updateDigitalLibraryFieldsReq) throws VedantuException;

    VedantuResponse getDigitalLibraryFields(GetDigitalLibraryFieldsReq getDigitalLibraryFieldsReq) throws VedantuException;

    VedantuResponse checkIfSuperAdmin(CheckIfSuperAdminReq checkIfSuperAdminReq) throws VedantuException;

    VedantuResponse getOrgPointsOfSale(AbstractOrgScopeReq abstractOrgScopeReq) throws VedantuException;

    VedantuResponse getOrgMemberExtraInputFields(GetOrgMemberExtraInfoInputFieldsReq getOrgMemberExtraInfoInputFieldsReq) throws VedantuException;


    VedantuResponse updateOrganization(UpdateOrgReq updateOrgReq);

    VedantuResponse approveOrganization(ApproveOrgReq approveOrgReq);

    VedantuResponse getCenters(GetOrgCentersReq getOrgCentersReq) throws VedantuException;

    VedantuResponse addCenter(AddOrgCenterReq addOrgCenterReq) throws VedantuException;

    VedantuResponse updateCenter(UpdateOrgCenterReq updateOrgCenterReq) throws VedantuException;

    VedantuResponse removeCenter(StateChangeOrgCenterReq activateOrgCenterReq) throws VedantuException;

    VedantuResponse activateCenter(StateChangeOrgCenterReq stateChangeOrgCenterReq);

    VedantuResponse getInstaMojoAccessToken(AbstractOrgScopeReq abstractOrgScopeReq) throws VedantuException;

    VedantuResponse getSharedOrgsByProgId(GetSharedOrgsReq getSharedOrgsReq);

    VedantuResponse uploadOrgPic(MultipartFile file, UploadOrgPicReq request) throws IOException;

    VedantuResponse shareProgToOrg(GetSharedOrgsReq getSharedOrgsReq);

    VedantuResponse removeSharedProgramFromOrg(RemoveProgramSharingReq removeProgramSharingReq);

    VedantuResponse acceptTnC(AcceptTncOrgReq acceptTncOrgReq);

    VedantuResponse getSection(GetOrgSectionReq getOrgSectionReq);

    VedantuResponse getSections(GetOrgSectionsReq getOrgSectionsReq);

    VedantuResponse addSection(AddOrgSectionReq addOrgSectionReq) throws FileNotFoundException;

    VedantuResponse updateSection(UpdateOrgSectionReq updateOrgSectionReq) throws FileNotFoundException;

    VedantuResponse removeSection(RemoveOrgSectionReq removeOrgSectionReq);

    VedantuResponse activateSection(ActivateOrgSectionReq activateOrgSectionReq);

    VedantuResponse getSectionByAccessCode(GetOrgSectionInfoByAccessCodeReq getOrgSectionInfoByAccessCodeReq);

    VedantuResponse generateOrgSectionAccessCode();

    VedantuResponse updateOrgSectionAccessReq(UpdateOrgSectionAccessReq updateOrgSectionAccessReq);

    VedantuResponse getSectionPackageInfo(GetOrgSectionReq getOrgSectionReq);

    VedantuResponse updateORgPackageInfo(UpdatePackageInfoReq updatePackageInfoReq);

    VedantuResponse updateSectionMaxDiscount(UpdateMaxDiscountReq updateMaxDiscountReq);

    VedantuResponse getLatestTNC();

    VedantuResponse getAssociatedOrganizationsOfUser(GetAssociatedOrgsReq getAssociatedOrgsReq);

    VedantuResponse generateAppCredes(GenerateAppCredentialsReq generateAppCredentialsReq);

    VedantuResponse verifyAppCreds(VerifyAppCredentialsReq verifyAppCredentialsReq);
}
