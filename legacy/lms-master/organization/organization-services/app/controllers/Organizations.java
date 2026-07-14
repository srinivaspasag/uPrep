package controllers;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Logger.ALogger;
import play.Play;
import play.data.Form;
import play.mvc.Http.MultipartFormData;
import play.mvc.Result;

import com.vedantu.billing.managers.InstaMojoTokenManager;
import com.vedantu.commons.JSONResponse;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.organization.managers.CategoryManager;
import com.vedantu.organization.managers.OrgCenterManager;
import com.vedantu.organization.managers.OrgDepartmentManager;
import com.vedantu.organization.managers.OrgProgramManager;
import com.vedantu.organization.managers.OrgSectionManager;
import com.vedantu.organization.managers.OrganizationManager;
import com.vedantu.organization.managers.UserStatusManager;
import com.vedantu.organization.pojos.OrgProgramBasicInfo;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;
import com.vedantu.organization.pojos.requests.CustomizeCategoryReq;
import com.vedantu.organization.pojos.requests.GetCategoryReq;
import com.vedantu.organization.pojos.requests.organizations.AcceptTncOrgReq;
import com.vedantu.organization.pojos.requests.organizations.ActivateOrgCenterReq;
import com.vedantu.organization.pojos.requests.organizations.ActivateOrgDepartmentReq;
import com.vedantu.organization.pojos.requests.organizations.ActivateOrgProgramReq;
import com.vedantu.organization.pojos.requests.organizations.ActivateOrgSectionReq;
import com.vedantu.organization.pojos.requests.organizations.AddCategoryReq;
import com.vedantu.organization.pojos.requests.organizations.AddOrgCenterReq;
import com.vedantu.organization.pojos.requests.organizations.AddOrgDepartmentReq;
import com.vedantu.organization.pojos.requests.organizations.AddOrgProgramCentersReq;
import com.vedantu.organization.pojos.requests.organizations.AddOrgProgramCoursesReq;
import com.vedantu.organization.pojos.requests.organizations.AddOrgProgramReq;
import com.vedantu.organization.pojos.requests.organizations.AddOrgReq;
import com.vedantu.organization.pojos.requests.organizations.AddOrgSectionReq;
import com.vedantu.organization.pojos.requests.organizations.ApproveOrgReq;
import com.vedantu.organization.pojos.requests.organizations.CheckIfSuperAdminReq;
import com.vedantu.organization.pojos.requests.organizations.CheckRefererReq;
import com.vedantu.organization.pojos.requests.organizations.CheckSlugReq;
import com.vedantu.organization.pojos.requests.organizations.CheckWebsiteReq;
import com.vedantu.organization.pojos.requests.organizations.EditCategoriesReq;
import com.vedantu.organization.pojos.requests.organizations.EditCategoryReq;
import com.vedantu.organization.pojos.requests.organizations.GenerateAppCredentialsReq;
import com.vedantu.organization.pojos.requests.organizations.GetAssociatedOrgsReq;
import com.vedantu.organization.pojos.requests.organizations.GetCategoriesReq;
import com.vedantu.organization.pojos.requests.organizations.GetCategorySectionReq;
import com.vedantu.organization.pojos.requests.organizations.GetCategorySectionsReq;
import com.vedantu.organization.pojos.requests.organizations.GetDigitalLibraryFieldsReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgByRefererReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgBySlugReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgCentersReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgCourseProgramsReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgDepartmentsReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgMemberExtraInfoInputFieldsReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgPointsOfSaleReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgProgramCentersReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgProgramCoursesReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgProgramsReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgSectionInfoByAccessCodeReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgSectionReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgSectionsReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgsReq;
import com.vedantu.organization.pojos.requests.organizations.GetProgramInfoReq;
import com.vedantu.organization.pojos.requests.organizations.GetSelfCategorySectionsReq;
import com.vedantu.organization.pojos.requests.organizations.GetSharedOrgsReq;
import com.vedantu.organization.pojos.requests.organizations.RemoveCategoryReq;
import com.vedantu.organization.pojos.requests.organizations.RemoveOrgCenterReq;
import com.vedantu.organization.pojos.requests.organizations.RemoveOrgDepartmentReq;
import com.vedantu.organization.pojos.requests.organizations.RemoveOrgProgramCentersReq;
import com.vedantu.organization.pojos.requests.organizations.RemoveOrgProgramCoursesReq;
import com.vedantu.organization.pojos.requests.organizations.RemoveOrgProgramReq;
import com.vedantu.organization.pojos.requests.organizations.RemoveOrgSectionReq;
import com.vedantu.organization.pojos.requests.organizations.RemoveProgramSharingReq;
import com.vedantu.organization.pojos.requests.organizations.UpdateDigitalLibraryFieldsReq;
import com.vedantu.organization.pojos.requests.organizations.UpdateMaxDiscountReq;
import com.vedantu.organization.pojos.requests.organizations.UpdateOrgCenterReq;
import com.vedantu.organization.pojos.requests.organizations.UpdateOrgDepartmentReq;
import com.vedantu.organization.pojos.requests.organizations.UpdateOrgMemberExtraInfoInputFieldsReq;
import com.vedantu.organization.pojos.requests.organizations.UpdateOrgProgramReq;
import com.vedantu.organization.pojos.requests.organizations.UpdateOrgRefererReq;
import com.vedantu.organization.pojos.requests.organizations.UpdateOrgReq;
import com.vedantu.organization.pojos.requests.organizations.UpdateOrgSectionAccessReq;
import com.vedantu.organization.pojos.requests.organizations.UpdateOrgSectionReq;
import com.vedantu.organization.pojos.requests.organizations.UpdateOrgSlugReq;
import com.vedantu.organization.pojos.requests.organizations.UpdateOrgStatusReq;
import com.vedantu.organization.pojos.requests.organizations.UpdateOrganizationClassroomConnectStatus;
import com.vedantu.organization.pojos.requests.organizations.UpdateOrganizationDownloadStatus;
import com.vedantu.organization.pojos.requests.organizations.UpdateOrganizationSharedSubjects;
import com.vedantu.organization.pojos.requests.organizations.UpdatePackageInfoReq;
import com.vedantu.organization.pojos.requests.organizations.UploadOrgPicReq;
import com.vedantu.organization.pojos.requests.organizations.VerifyAppCredentialsReq;
import com.vedantu.organization.pojos.requests.organizations.CheckAppVersionReq;
import com.vedantu.organization.pojos.responses.CustomizeCategoryRes;
import com.vedantu.organization.pojos.responses.GetCategoryRes;
import com.vedantu.organization.pojos.responses.organizations.ActivateOrgCenterRes;
import com.vedantu.organization.pojos.responses.organizations.ActivateOrgDepartmentRes;
import com.vedantu.organization.pojos.responses.organizations.ActivateOrgProgramRes;
import com.vedantu.organization.pojos.responses.organizations.ActivateOrgSectionRes;
import com.vedantu.organization.pojos.responses.organizations.AddCategoryRes;
import com.vedantu.organization.pojos.responses.organizations.AddOrgCenterRes;
import com.vedantu.organization.pojos.responses.organizations.AddOrgDepartmentRes;
import com.vedantu.organization.pojos.responses.organizations.AddOrgProgramCentersRes;
import com.vedantu.organization.pojos.responses.organizations.AddOrgProgramCoursesRes;
import com.vedantu.organization.pojos.responses.organizations.AddOrgProgramRes;
import com.vedantu.organization.pojos.responses.organizations.AddOrgRes;
import com.vedantu.organization.pojos.responses.organizations.AddOrgSectionRes;
import com.vedantu.organization.pojos.responses.organizations.ApproveOrgRes;
import com.vedantu.organization.pojos.responses.organizations.CheckAppVersionRes;
import com.vedantu.organization.pojos.responses.organizations.CheckIfSuperAdminRes;
import com.vedantu.organization.pojos.responses.organizations.CheckSlugRes;
import com.vedantu.organization.pojos.responses.organizations.EditCategoriesRes;
import com.vedantu.organization.pojos.responses.organizations.EditCategoryRes;
import com.vedantu.organization.pojos.responses.organizations.GenerateAppCredentialsRes;
import com.vedantu.organization.pojos.responses.organizations.GetAssociatedOrgsRes;
import com.vedantu.organization.pojos.responses.organizations.GetCategoriesRes;
import com.vedantu.organization.pojos.responses.organizations.GetCategorySectionRes;
import com.vedantu.organization.pojos.responses.organizations.GetCategorySectionsRes;
import com.vedantu.organization.pojos.responses.organizations.GetDigitalLibraryFieldsRes;
import com.vedantu.organization.pojos.responses.organizations.GetLatestTncRes;
import com.vedantu.organization.pojos.responses.organizations.GetOrgCentersRes;
import com.vedantu.organization.pojos.responses.organizations.GetOrgCourseProgramsRes;
import com.vedantu.organization.pojos.responses.organizations.GetOrgDepartmentsRes;
import com.vedantu.organization.pojos.responses.organizations.GetOrgMemberExtraInfoInputFieldsRes;
import com.vedantu.organization.pojos.responses.organizations.GetOrgPointsOfSaleRes;
import com.vedantu.organization.pojos.responses.organizations.GetOrgProgramCentersRes;
import com.vedantu.organization.pojos.responses.organizations.GetOrgProgramCoursesRes;
import com.vedantu.organization.pojos.responses.organizations.GetOrgProgramsRes;
import com.vedantu.organization.pojos.responses.organizations.GetOrgRes;
import com.vedantu.organization.pojos.responses.organizations.GetOrgResForInvoice;
import com.vedantu.organization.pojos.responses.organizations.GetOrgSectionInfoByAccessCodeRes;
import com.vedantu.organization.pojos.responses.organizations.GetOrgSectionRes;
import com.vedantu.organization.pojos.responses.organizations.GetOrgSectionsRes;
import com.vedantu.organization.pojos.responses.organizations.GetOrgsRes;
import com.vedantu.organization.pojos.responses.organizations.GetSharedOrgsRes;
import com.vedantu.organization.pojos.responses.organizations.GetShowSharedSubjectsRes;
import com.vedantu.organization.pojos.responses.organizations.InstaMojoAccessTokenResp;
import com.vedantu.organization.pojos.responses.organizations.RemoveCategoryRes;
import com.vedantu.organization.pojos.responses.organizations.RemoveOrgCenterRes;
import com.vedantu.organization.pojos.responses.organizations.RemoveOrgDepartmentRes;
import com.vedantu.organization.pojos.responses.organizations.RemoveOrgProgramCentersRes;
import com.vedantu.organization.pojos.responses.organizations.RemoveOrgProgramCoursesRes;
import com.vedantu.organization.pojos.responses.organizations.RemoveOrgProgramRes;
import com.vedantu.organization.pojos.responses.organizations.RemoveOrgSectionRes;
import com.vedantu.organization.pojos.responses.organizations.RemoveProgramSharingRes;
import com.vedantu.organization.pojos.responses.organizations.UpdateDigitalLibraryFieldsRes;
import com.vedantu.organization.pojos.responses.organizations.UpdateMaxDiscountRes;
import com.vedantu.organization.pojos.responses.organizations.UpdateOrgCenterRes;
import com.vedantu.organization.pojos.responses.organizations.UpdateOrgDepartmentRes;
import com.vedantu.organization.pojos.responses.organizations.UpdateOrgMemberExtraInfoInputFieldsRes;
import com.vedantu.organization.pojos.responses.organizations.UpdateOrgProgramRes;
import com.vedantu.organization.pojos.responses.organizations.UpdateOrgRes;
import com.vedantu.organization.pojos.responses.organizations.UpdateOrgSectionAccessRes;
import com.vedantu.organization.pojos.responses.organizations.UpdateOrgSectionRes;
import com.vedantu.organization.pojos.responses.organizations.UpdatePackageInfoRes;
import com.vedantu.organization.pojos.responses.organizations.UploadOrgPicRes;
import com.vedantu.organization.pojos.responses.organizations.VerifyAppCredentialsRes;
import com.vedantu.user.pojos.responses.AcceptTnCRes;

public class Organizations extends AbstractVedantuController {

    private static final ALogger LOGGER = Logger.of(Organizations.class);

    public static Result getOrganization() {

        Form<GetOrgReq> getOrgForm = Form.form(GetOrgReq.class).bindFromRequest();
        if (getOrgForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getOrgForm))).toObjectNode());
        }
        GetOrgReq getOrgReq = getOrgForm.get();
        if (ObjectIdUtils.hasInvalidId(getOrgReq.orgId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        GetOrgRes getOrgRes = null;
        try {
            getOrgRes = OrganizationManager.getOrganization(getOrgReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getOrgRes).toObjectNode());
    }

    public static Result getShowSharedSubjects() {

        Form<GetOrgReq> getOrgForm = Form.form(GetOrgReq.class).bindFromRequest();
        if (getOrgForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getOrgForm))).toObjectNode());
        }
        GetOrgReq getOrgReq = getOrgForm.get();
        if (ObjectIdUtils.hasInvalidId(getOrgReq.orgId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        GetShowSharedSubjectsRes getOrgRes = null;

        getOrgRes = OrganizationManager.getShowSharedSubjects(getOrgReq);

        return ok(getResultResponse(getOrgRes).toObjectNode());
    }

    public static Result getOrganizationInfoForInvoice() {

        Form<GetOrgReq> getOrgForm = Form.form(GetOrgReq.class).bindFromRequest();
        if (getOrgForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getOrgForm))).toObjectNode());
        }
        GetOrgReq getOrgReq = getOrgForm.get();
        if (ObjectIdUtils.hasInvalidId(getOrgReq.orgId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        GetOrgResForInvoice getOrgRes = null;
        try {
            getOrgRes = OrganizationManager.getOrganizationInfoForInvoice(getOrgReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getOrgRes).toObjectNode());
    }

    public static Result checkIfSuperAdmin() {

        Form<CheckIfSuperAdminReq> checkIfSuperAdminForm = Form.form(CheckIfSuperAdminReq.class)
                .bindFromRequest();
        if (checkIfSuperAdminForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(checkIfSuperAdminForm))).toObjectNode());
        }
        CheckIfSuperAdminReq checkIfSuperAdminReq = checkIfSuperAdminForm.get();
        CheckIfSuperAdminRes checkIfSuperAdminRes = null;
        try {
            checkIfSuperAdminRes = UserStatusManager.checkIfSuperAdmin(checkIfSuperAdminReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(checkIfSuperAdminRes).toObjectNode());
    }

	public static Result checkAppVersion() {

		Form<CheckAppVersionReq> checkAppVersionReqForm = Form.form(
				CheckAppVersionReq.class).bindFromRequest();
		if (checkAppVersionReqForm.hasErrors()) {
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
							getErrorMessege(checkAppVersionReqForm)))
					.toObjectNode());
		}
		CheckAppVersionReq checkAppVersionReq = checkAppVersionReqForm.get();
		CheckAppVersionRes checkAppVersionRes = null;
		try {
			checkAppVersionRes = OrganizationManager
					.checkAppVersion(checkAppVersionReq);
		} catch (VedantuException e) {
			return ok(getErrorResponse(e).toObjectNode());
		}

		return ok(getResultResponse(checkAppVersionRes).toObjectNode());
	}

    public static Result checkWebsite() {

        Form<CheckWebsiteReq> checkOrgWebsiteForm = Form.form(CheckWebsiteReq.class)
                .bindFromRequest();
        if (checkOrgWebsiteForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(checkOrgWebsiteForm))).toObjectNode());
        }
        CheckWebsiteReq checkOrgWebsiteReq = checkOrgWebsiteForm.get();
        CheckSlugRes checkOrgSlugRes = null;
        try {
            checkOrgSlugRes = OrganizationManager.checkWebsite(checkOrgWebsiteReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(checkOrgSlugRes).toObjectNode());
    }

    public static Result checkSlug() {

        Form<CheckSlugReq> updateOrgSlugForm = Form.form(CheckSlugReq.class).bindFromRequest();
        if (updateOrgSlugForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(updateOrgSlugForm))).toObjectNode());
        }
        CheckSlugReq checkOrgSlugReq = updateOrgSlugForm.get();
        CheckSlugRes checkOrgSlugRes = null;
        try {
            checkOrgSlugRes = OrganizationManager.checkSlug(checkOrgSlugReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(checkOrgSlugRes).toObjectNode());
    }

    public static Result checkReferer() {

        Form<CheckRefererReq> requestForm = Form.form(CheckRefererReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        CheckRefererReq request = requestForm.get();
        CheckSlugRes checkOrgSlugRes = null;
        try {
            checkOrgSlugRes = OrganizationManager.checkReferer(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(checkOrgSlugRes).toObjectNode());
    }

    public static Result getOrganizationBySlug() {

        Form<GetOrgBySlugReq> getOrgForm = Form.form(GetOrgBySlugReq.class).bindFromRequest();
        if (getOrgForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getOrgForm))).toObjectNode());
        }
        GetOrgBySlugReq getOrgReq = getOrgForm.get();
        GetOrgRes getOrgRes = null;
        try {
            getOrgRes = OrganizationManager.getOrganizationBySlug(getOrgReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getOrgRes).toObjectNode());
    }

    public static Result updateOrganizationSlug() {

        Form<UpdateOrgSlugReq> updateOrgSlugForm = Form.form(UpdateOrgSlugReq.class)
                .bindFromRequest();
        if (updateOrgSlugForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(updateOrgSlugForm))).toObjectNode());
        }
        UpdateOrgSlugReq updateOrgSlugReq = updateOrgSlugForm.get();
        GetOrgRes getOrgRes = null;
        try {
            getOrgRes = OrganizationManager.updateOrganizationSlug(updateOrgSlugReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getOrgRes).toObjectNode());
    }
    public static Result updateOrganizationStatus() {

        Form<UpdateOrgStatusReq> updateOrgStatusForm = Form.form(UpdateOrgStatusReq.class)
                .bindFromRequest();
        if (updateOrgStatusForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(updateOrgStatusForm))).toObjectNode());
        }
        UpdateOrgStatusReq updateOrgStatusReq = updateOrgStatusForm.get();
        GetOrgRes getOrgRes = null;
        try {
            getOrgRes = OrganizationManager.updateOrganizationStatus(updateOrgStatusReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getOrgRes).toObjectNode());
    }

    public static Result updateOrganizationSharedSubjects() {

        Form<UpdateOrganizationSharedSubjects> updateOrgStatusForm = Form.form(UpdateOrganizationSharedSubjects.class)
                .bindFromRequest();
        if (updateOrgStatusForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(updateOrgStatusForm))).toObjectNode());
        }
        UpdateOrganizationSharedSubjects updateOrgStatusReq = updateOrgStatusForm.get();
        GetOrgRes getOrgRes = null;
        try {
            getOrgRes = OrganizationManager.updateOrganizationSharedSubjects(updateOrgStatusReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getOrgRes).toObjectNode());
    }

    public static Result updateOrganizationClassroomConnectStatus(){
        Form<UpdateOrganizationClassroomConnectStatus> updateOrgStatusForm = Form.form(UpdateOrganizationClassroomConnectStatus.class)
                .bindFromRequest();
        if (updateOrgStatusForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(updateOrgStatusForm))).toObjectNode());
        }
        UpdateOrganizationClassroomConnectStatus updateOrgStatusReq = updateOrgStatusForm.get();
        GetOrgRes getOrgRes = null;
        try {
            getOrgRes = OrganizationManager.updateOrganizationClassroomConnectStatus(updateOrgStatusReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getOrgRes).toObjectNode());
    }

    public static Result updateOrganizationDownloadStatus(){
        Form<UpdateOrganizationDownloadStatus> updateOrgStatusForm = Form.form(UpdateOrganizationDownloadStatus.class)
                .bindFromRequest();
        if (updateOrgStatusForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(updateOrgStatusForm))).toObjectNode());
        }
        UpdateOrganizationDownloadStatus updateOrgStatusReq = updateOrgStatusForm.get();
        GetOrgRes getOrgRes = null;
        try {
            getOrgRes = OrganizationManager.updateOrganizationDownloadStatus(updateOrgStatusReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getOrgRes).toObjectNode());
    }

    public static Result getOrganizationByReferer() {

        Form<GetOrgByRefererReq> getOrgForm = Form.form(GetOrgByRefererReq.class).bindFromRequest();
        if (getOrgForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getOrgForm))).toObjectNode());
        }
        GetOrgByRefererReq getOrgReq = getOrgForm.get();
        GetOrgRes getOrgRes = null;
        try {
            String referer = request().getHeader(REFERER);
            LOGGER.debug(" Referer from header " + referer);
            getOrgRes = OrganizationManager.getOrganizationByReferer(referer, getOrgReq.getKey);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getOrgRes).toObjectNode());
    }

    public static Result updateOrganizationReferer() {

        Form<UpdateOrgRefererReq> updateOrgSlugForm = Form.form(UpdateOrgRefererReq.class)
                .bindFromRequest();
        if (updateOrgSlugForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(updateOrgSlugForm))).toObjectNode());
        }
        UpdateOrgRefererReq updateOrgSlugReq = updateOrgSlugForm.get();
        GetOrgRes getOrgRes = null;
        try {
            getOrgRes = OrganizationManager.updateOrganizationReferrer(updateOrgSlugReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getOrgRes).toObjectNode());
    }

    public static Result getOrganizations() {

        Form<GetOrgsReq> getOrgsForm = Form.form(GetOrgsReq.class).bindFromRequest();
        if (getOrgsForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getOrgsForm))).toObjectNode());
        }
        GetOrgsReq getOrgsReq = getOrgsForm.get();
        LOGGER.debug("......entering addSection ......"+getOrgsReq);
        GetOrgsRes getOrgsRes = OrganizationManager.getOrganizations(getOrgsReq);

        return ok(getResultResponse(getOrgsRes).toObjectNode());
    }

    public static Result getSharedOrgsByProgId() {

        Form<GetSharedOrgsReq> getOrgsForm = Form.form(GetSharedOrgsReq.class).bindFromRequest();
        if (getOrgsForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getOrgsForm))).toObjectNode());
        }
        GetSharedOrgsReq getOrgsReq = getOrgsForm.get();
        LOGGER.debug("......entering addSection ......"+getOrgsReq);
        GetSharedOrgsRes getOrgsRes = OrganizationManager.getSharedOrgsByProg(getOrgsReq);

        return ok(getResultResponse(getOrgsRes).toObjectNode());
    }

    public static Result shareProgToOrg() {

        Form<GetSharedOrgsReq> getOrgsForm = Form.form(GetSharedOrgsReq.class).bindFromRequest();
        if (getOrgsForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getOrgsForm))).toObjectNode());
        }
        GetSharedOrgsReq getOrgsReq = getOrgsForm.get();
        LOGGER.debug(getOrgsReq.programId+" prov "+getOrgsReq.providerOrgId+" subs "+getOrgsReq.subscriberOrgId);
        GetSharedOrgsRes getOrgsRes;
        try {
            getOrgsRes = OrganizationManager.sharedProgToOrg(getOrgsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getOrgsRes).toObjectNode());
    }

    public static Result removeSharedProgramFromOrg() {

        Form<RemoveProgramSharingReq> removeSharingForm = Form.form(RemoveProgramSharingReq.class)
                .bindFromRequest();
        if (removeSharingForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(removeSharingForm))).toObjectNode());
        }
        RemoveProgramSharingReq removeSharingReq = removeSharingForm.get();
        RemoveProgramSharingRes removeSharingRes = OrganizationManager
                .removeSharedProgramFromOrg(removeSharingReq);
        return ok(getResultResponse(removeSharingRes).toObjectNode());
    }

    public static Result addOrganization() {

        Form<AddOrgReq> addOrgForm = Form.form(AddOrgReq.class).bindFromRequest();
        if (addOrgForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        AddOrgReq addOrgReq = addOrgForm.get();
        AddOrgRes addOrgRes = null;

        try {
            addOrgRes = OrganizationManager.addOrganization(addOrgReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(addOrgRes).toObjectNode());
    }

    public static Result approveOrganization() {

        Form<ApproveOrgReq> approveOrgForm = Form.form(ApproveOrgReq.class).bindFromRequest();
        if (approveOrgForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        ApproveOrgReq approveOrgReq = approveOrgForm.get();
        if (ObjectIdUtils.hasInvalidId(approveOrgReq.orgId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        ApproveOrgRes apprOrgRes = null;

        try {
            apprOrgRes = OrganizationManager.approveOrganization(approveOrgReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(apprOrgRes).toObjectNode());
    }

    public static Result updateOrganization() {

        Form<UpdateOrgReq> updateOrgForm = Form.form(UpdateOrgReq.class).bindFromRequest();
        if (updateOrgForm.hasErrors()) {

            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS),
                    getErrorMessege(updateOrgForm)).toObjectNode());
        }

        UpdateOrgReq updateOrgReq = updateOrgForm.get();
        if (ObjectIdUtils.hasInvalidId(updateOrgReq.orgId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        UpdateOrgRes updateOrgRes = null;

        try {
            updateOrgRes = OrganizationManager.updateOrganization(updateOrgReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(updateOrgRes).toObjectNode());
    }

    public static Result updateOrgMemberExtraInputFields() {

        Form<UpdateOrgMemberExtraInfoInputFieldsReq> updateOrgMemberExtraInputFieldsForm = Form
                .form(UpdateOrgMemberExtraInfoInputFieldsReq.class).bindFromRequest();
        if (updateOrgMemberExtraInputFieldsForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }

        UpdateOrgMemberExtraInfoInputFieldsReq updateOrgMemberExtraInputFieldsReq = updateOrgMemberExtraInputFieldsForm
                .get();
        if (ObjectIdUtils.hasInvalidId(updateOrgMemberExtraInputFieldsReq.orgId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }

        UpdateOrgMemberExtraInfoInputFieldsRes updateOrgMemberExtraInputFieldsRes = null;

        try {
            updateOrgMemberExtraInputFieldsRes = OrganizationManager
                    .updateOrganizationMemberExtraInputFields(updateOrgMemberExtraInputFieldsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(updateOrgMemberExtraInputFieldsRes).toObjectNode());
    }

    public static Result getOrgMemberExtraInputFields() {

        Form<GetOrgMemberExtraInfoInputFieldsReq> getOrgMemberExtraInputFieldsForm = Form.form(
                GetOrgMemberExtraInfoInputFieldsReq.class).bindFromRequest();
        if (getOrgMemberExtraInputFieldsForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }

        GetOrgMemberExtraInfoInputFieldsReq getOrgMemberExtraInputFieldsReq = getOrgMemberExtraInputFieldsForm
                .get();
        if (ObjectIdUtils.hasInvalidId(getOrgMemberExtraInputFieldsReq.orgId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }

        GetOrgMemberExtraInfoInputFieldsRes getOrgMemberExtraInputFieldsRes = null;

        try {
            getOrgMemberExtraInputFieldsRes = OrganizationManager
                    .getOrganizationMemberExtraInputFields(getOrgMemberExtraInputFieldsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getOrgMemberExtraInputFieldsRes).toObjectNode());
    }

    public static Result updateDigitalLibraryFields() {

        Form<UpdateDigitalLibraryFieldsReq> updateDigitalLibraryFieldsForm = Form
                .form(UpdateDigitalLibraryFieldsReq.class).bindFromRequest();
        if (updateDigitalLibraryFieldsForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }

        UpdateDigitalLibraryFieldsReq updateDigitalLibraryFieldsReq = updateDigitalLibraryFieldsForm
                .get();
        if (ObjectIdUtils.hasInvalidId(updateDigitalLibraryFieldsReq.orgId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }

        UpdateDigitalLibraryFieldsRes updateDigitalLibraryFieldsRes = null;

        try {
            updateDigitalLibraryFieldsRes = OrganizationManager
                    .updateDigitalLibraryFields(updateDigitalLibraryFieldsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(updateDigitalLibraryFieldsRes).toObjectNode());
    }

    public static Result getDigitalLibraryFields() {

        Form<GetDigitalLibraryFieldsReq> getDigitalLibraryFieldsForm = Form.form(
                GetDigitalLibraryFieldsReq.class).bindFromRequest();
        if (getDigitalLibraryFieldsForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }

        GetDigitalLibraryFieldsReq getDigitalLibraryFieldsReq = getDigitalLibraryFieldsForm
                .get();
        if (ObjectIdUtils.hasInvalidId(getDigitalLibraryFieldsReq.orgId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }

        GetDigitalLibraryFieldsRes getDigitalLibraryFieldsRes = null;

        try {
            getDigitalLibraryFieldsRes = OrganizationManager
                    .getDigitalLibraryFields(getDigitalLibraryFieldsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getDigitalLibraryFieldsRes).toObjectNode());
    }

    public static Result getCenters() {

        Form<GetOrgCentersReq> getOrgCentersForm = Form.form(GetOrgCentersReq.class)
                .bindFromRequest();
        if (getOrgCentersForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetOrgCentersReq getOrgCentersReq = getOrgCentersForm.get();
        if (ObjectIdUtils.hasInvalidId(getOrgCentersReq.orgId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        GetOrgCentersRes getOrgCentersRes = null;

        try {
            getOrgCentersRes = OrgCenterManager.getCenters(getOrgCentersReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getOrgCentersRes).toObjectNode());
    }

    public static Result addCenter() {

        Form<AddOrgCenterReq> addOrgCentersForm = Form.form(AddOrgCenterReq.class)
                .bindFromRequest();
        if (addOrgCentersForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        AddOrgCenterReq addOrgCenterReq = addOrgCentersForm.get();
        if (ObjectIdUtils.hasInvalidId(addOrgCenterReq.orgId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        AddOrgCenterRes addOrgCenterRes = null;

        try {
            addOrgCenterRes = OrgCenterManager.addCenter(addOrgCenterReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(addOrgCenterRes).toObjectNode());
    }

    public static Result updateCenter() {

        Form<UpdateOrgCenterReq> updateOrgCenterForm = Form.form(UpdateOrgCenterReq.class)
                .bindFromRequest();
        if (updateOrgCenterForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        UpdateOrgCenterReq updateOrgCenterReq = updateOrgCenterForm.get();
        if (ObjectIdUtils.hasInvalidId(updateOrgCenterReq.orgId, updateOrgCenterReq.centerId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        UpdateOrgCenterRes updateOrgCenterRes = null;

        try {
            updateOrgCenterRes = OrgCenterManager.updateCenter(updateOrgCenterReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(updateOrgCenterRes).toObjectNode());
    }

    public static Result removeCenter() {

        Form<RemoveOrgCenterReq> removeOrgCenterForm = Form.form(RemoveOrgCenterReq.class)
                .bindFromRequest();
        if (removeOrgCenterForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        RemoveOrgCenterReq removeOrgCenterReq = removeOrgCenterForm.get();
        if (ObjectIdUtils.hasInvalidId(removeOrgCenterReq.orgId, removeOrgCenterReq.centerId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        RemoveOrgCenterRes removeOrgCenterRes = null;

        try {
            removeOrgCenterRes = OrgCenterManager.removeCenter(removeOrgCenterReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(removeOrgCenterRes).toObjectNode());
    }

    public static Result activateCenter() {

        Form<ActivateOrgCenterReq> activateOrgCenterForm = Form.form(ActivateOrgCenterReq.class)
                .bindFromRequest();
        if (activateOrgCenterForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        ActivateOrgCenterReq activateOrgCenterReq = activateOrgCenterForm.get();
        if (ObjectIdUtils.hasInvalidId(activateOrgCenterReq.orgId, activateOrgCenterReq.centerId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        ActivateOrgCenterRes activateOrgCenterRes = null;

        try {
            activateOrgCenterRes = OrgCenterManager.activateCenter(activateOrgCenterReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(activateOrgCenterRes).toObjectNode());
    }

    public static Result getDepartments() {

        Form<GetOrgDepartmentsReq> getOrgDepartmentsForm = Form.form(GetOrgDepartmentsReq.class)
                .bindFromRequest();
        if (getOrgDepartmentsForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetOrgDepartmentsReq getOrgDepartmentsReq = getOrgDepartmentsForm.get();
        if (ObjectIdUtils.hasInvalidId(getOrgDepartmentsReq.orgId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        GetOrgDepartmentsRes getOrgDepartmentsRes = null;

        try {
            getOrgDepartmentsRes = OrgDepartmentManager.getDepartments(getOrgDepartmentsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getOrgDepartmentsRes).toObjectNode());
    }

    public static Result addDepartment() {

        Form<AddOrgDepartmentReq> addOrgDepartmentsForm = Form.form(AddOrgDepartmentReq.class)
                .bindFromRequest();
        if (addOrgDepartmentsForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        AddOrgDepartmentReq addOrgDepartmentReq = addOrgDepartmentsForm.get();
        if (ObjectIdUtils.hasInvalidId(addOrgDepartmentReq.orgId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        AddOrgDepartmentRes addOrgDepartmentRes = null;

        try {
            addOrgDepartmentRes = OrgDepartmentManager.addDepartment(addOrgDepartmentReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(addOrgDepartmentRes).toObjectNode());
    }

    public static Result updateDepartment() {

        Form<UpdateOrgDepartmentReq> updateOrgDepartmentForm = Form.form(
                UpdateOrgDepartmentReq.class).bindFromRequest();
        if (updateOrgDepartmentForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        UpdateOrgDepartmentReq updateOrgDepartmentReq = updateOrgDepartmentForm.get();
        if (ObjectIdUtils.hasInvalidId(updateOrgDepartmentReq.orgId,
                updateOrgDepartmentReq.departmentId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        UpdateOrgDepartmentRes updateOrgDepartmentRes = null;

        try {
            updateOrgDepartmentRes = OrgDepartmentManager.updateDepartment(updateOrgDepartmentReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(updateOrgDepartmentRes).toObjectNode());
    }

    public static Result removeDepartment() {

        Form<RemoveOrgDepartmentReq> removeOrgDepartmentForm = Form.form(
                RemoveOrgDepartmentReq.class).bindFromRequest();
        if (removeOrgDepartmentForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        RemoveOrgDepartmentReq removeOrgDepartmentReq = removeOrgDepartmentForm.get();
        if (ObjectIdUtils.hasInvalidId(removeOrgDepartmentReq.orgId,
                removeOrgDepartmentReq.departmentId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        RemoveOrgDepartmentRes removeOrgDepartmentRes = null;

        try {
            removeOrgDepartmentRes = OrgDepartmentManager.removeDepartment(removeOrgDepartmentReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(removeOrgDepartmentRes).toObjectNode());
    }

    public static Result activateDepartment() {

        Form<ActivateOrgDepartmentReq> activateOrgDepartmentForm = Form.form(
                ActivateOrgDepartmentReq.class).bindFromRequest();
        if (activateOrgDepartmentForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        ActivateOrgDepartmentReq activateOrgDepartmentReq = activateOrgDepartmentForm.get();
        if (ObjectIdUtils.hasInvalidId(activateOrgDepartmentReq.orgId,
                activateOrgDepartmentReq.departmentId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        ActivateOrgDepartmentRes activateOrgDepartmentRes = null;

        try {
            activateOrgDepartmentRes = OrgDepartmentManager
                    .activateDepartment(activateOrgDepartmentReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(activateOrgDepartmentRes).toObjectNode());
    }

    public static Result getPrograms() {

        Form<GetOrgProgramsReq> getOrgProgramsForm = Form.form(GetOrgProgramsReq.class)
                .bindFromRequest();
        if (getOrgProgramsForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetOrgProgramsReq getOrgProgramsReq = getOrgProgramsForm.get();
        if (ObjectIdUtils.hasInvalidId(getOrgProgramsReq.orgId, getOrgProgramsReq.departmentId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        GetOrgProgramsRes getOrgProgramsRes = null;

        try {
            getOrgProgramsRes = OrgProgramManager.getPrograms(getOrgProgramsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getOrgProgramsRes).toObjectNode());
    }

    public static Result addProgram() {

        Form<AddOrgProgramReq> addOrgProgramForm = Form.form(AddOrgProgramReq.class)
                .bindFromRequest();
        if (addOrgProgramForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        AddOrgProgramReq addOrgProgramReq = addOrgProgramForm.get();
        if (ObjectIdUtils.hasInvalidId(addOrgProgramReq.orgId, addOrgProgramReq.departmentId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        AddOrgProgramRes addOrgProgramRes = null;

        try {
            addOrgProgramRes = OrgProgramManager.addProgram(addOrgProgramReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(addOrgProgramRes).toObjectNode());
    }

    public static Result updateProgram() {

        Form<UpdateOrgProgramReq> updateOrgProgramForm = Form.form(UpdateOrgProgramReq.class)
                .bindFromRequest();
        if (updateOrgProgramForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        UpdateOrgProgramReq updateOrgProgramReq = updateOrgProgramForm.get();
        if (ObjectIdUtils.hasInvalidId(updateOrgProgramReq.orgId, updateOrgProgramReq.departmentId,
                updateOrgProgramReq.programId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        UpdateOrgProgramRes updateOrgProgramRes = null;

        try {
            updateOrgProgramRes = OrgProgramManager.updateProgram(updateOrgProgramReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(updateOrgProgramRes).toObjectNode());
    }

    public static Result removeProgram() {

        Form<RemoveOrgProgramReq> removeOrgProgramForm = Form.form(RemoveOrgProgramReq.class)
                .bindFromRequest();
        if (removeOrgProgramForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        RemoveOrgProgramReq removeOrgProgramReq = removeOrgProgramForm.get();
        if (ObjectIdUtils.hasInvalidId(removeOrgProgramReq.orgId, removeOrgProgramReq.programId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        RemoveOrgProgramRes removeOrgProgramRes = null;

        try {
            removeOrgProgramRes = OrgProgramManager.removeProgram(removeOrgProgramReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(removeOrgProgramRes).toObjectNode());
    }

    public static Result activateProgram() {

        Form<ActivateOrgProgramReq> activateOrgProgramForm = Form.form(ActivateOrgProgramReq.class)
                .bindFromRequest();
        if (activateOrgProgramForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        ActivateOrgProgramReq activateOrgProgramReq = activateOrgProgramForm.get();
        if (ObjectIdUtils
                .hasInvalidId(activateOrgProgramReq.orgId, activateOrgProgramReq.programId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        ActivateOrgProgramRes activateOrgProgramRes = null;

        try {
            activateOrgProgramRes = OrgProgramManager.activateProgram(activateOrgProgramReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(activateOrgProgramRes).toObjectNode());
    }

    public static Result getProgramCenters() {

        Form<GetOrgProgramCentersReq> getOrgProgramCentersForm = Form.form(
                GetOrgProgramCentersReq.class).bindFromRequest();
        if (getOrgProgramCentersForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetOrgProgramCentersReq getOrgProgramCentersReq = getOrgProgramCentersForm.get();
        if (ObjectIdUtils.hasInvalidId(getOrgProgramCentersReq.orgId,
                getOrgProgramCentersReq.programId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        GetOrgProgramCentersRes getOrgProgramCentersRes = null;

        try {
            getOrgProgramCentersRes = OrgProgramManager.getProgramCenters(getOrgProgramCentersReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getOrgProgramCentersRes).toObjectNode());
    }

    public static Result addProgramCenters() {

        Form<AddOrgProgramCentersReq> addOrgProgramCentersForm = Form.form(
                AddOrgProgramCentersReq.class).bindFromRequest();
        if (addOrgProgramCentersForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        AddOrgProgramCentersReq addOrgProgramCentersReq = addOrgProgramCentersForm.get();
        String[] tCenterIds = addOrgProgramCentersReq.centerIds.toArray(new String[0]);
        if (ObjectIdUtils.hasInvalidId(addOrgProgramCentersReq.orgId,
                addOrgProgramCentersReq.programId) || ObjectIdUtils.hasInvalidId(tCenterIds)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        AddOrgProgramCentersRes addOrgProgramCentersRes = null;

        try {
            addOrgProgramCentersRes = OrgProgramManager.addProgramCenters(addOrgProgramCentersReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(addOrgProgramCentersRes).toObjectNode());
    }

    public static Result removeProgramCenters() {

        Form<RemoveOrgProgramCentersReq> removeOrgProgramCentersForm = Form.form(
                RemoveOrgProgramCentersReq.class).bindFromRequest();
        if (removeOrgProgramCentersForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        RemoveOrgProgramCentersReq removeOrgProgramCentersReq = removeOrgProgramCentersForm.get();
        String[] tCenterIds = removeOrgProgramCentersReq.centerIds.toArray(new String[0]);
        if (ObjectIdUtils.hasInvalidId(removeOrgProgramCentersReq.orgId,
                removeOrgProgramCentersReq.programId) || ObjectIdUtils.hasInvalidId(tCenterIds)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        RemoveOrgProgramCentersRes removeOrgProgramCentersRes = null;

        try {
            removeOrgProgramCentersRes = OrgProgramManager
                    .removeProgramCenters(removeOrgProgramCentersReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(removeOrgProgramCentersRes).toObjectNode());
    }

    public static Result getSections() {

        Form<GetOrgSectionsReq> getOrgSectionsForm = Form.form(GetOrgSectionsReq.class)
                .bindFromRequest();
        if (getOrgSectionsForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetOrgSectionsReq getOrgSectionsReq = getOrgSectionsForm.get();
        if (ObjectIdUtils.hasInvalidId(getOrgSectionsReq.orgId, getOrgSectionsReq.programId,
                getOrgSectionsReq.centerId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        GetOrgSectionsRes getOrgSectionsRes = null;

        try {
            getOrgSectionsRes = OrgProgramManager.getProgramSections(getOrgSectionsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getOrgSectionsRes).toObjectNode());
    }

    public static Result getSection() {

        Form<GetOrgSectionReq> getOrgSectionsForm = Form.form(GetOrgSectionReq.class)
                .bindFromRequest();
        if (getOrgSectionsForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetOrgSectionReq getOrgSectionsReq = getOrgSectionsForm.get();
        if (ObjectIdUtils.hasInvalidId(getOrgSectionsReq.orgId, getOrgSectionsReq.sectionId
               )) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        GetOrgSectionRes getOrgSectionsRes = null;

        try {
            getOrgSectionsRes = OrgProgramManager.getProgramSection(getOrgSectionsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getOrgSectionsRes).toObjectNode());
    }

    public static Result addSection() {

        // LOGGER.debug("......entering addSection ......");
        Form<AddOrgSectionReq> addOrgSectionForm = Form.form(AddOrgSectionReq.class)
                .bindFromRequest();
        if (addOrgSectionForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        AddOrgSectionReq addOrgSectionReq = addOrgSectionForm.get();
        if (ObjectIdUtils.hasInvalidId(addOrgSectionReq.orgId, addOrgSectionReq.programId,
                addOrgSectionReq.centerId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        AddOrgSectionRes addOrgSectionsRes = null;

        try {
            addOrgSectionsRes = OrgProgramManager.addProgramSection(addOrgSectionReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(addOrgSectionsRes).toObjectNode());
    }

    public static Result addCategory() throws VedantuException {

        // LOGGER.debug("......entering addSection ......");
        Form<AddCategoryReq> addcategoryForm = Form.form(AddCategoryReq.class).bindFromRequest();
        if (addcategoryForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        AddCategoryReq addCategoryReq = addcategoryForm.get();
        if (ObjectIdUtils.hasInvalidId(addCategoryReq.orgId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        AddCategoryRes addCategoryRes = null;

        try {
            addCategoryRes = CategoryManager.addCategory(addCategoryReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(addCategoryRes).toObjectNode());
    }

    public static Result getCategories() throws VedantuException {

        // LOGGER.debug("......entering addSection ......");
        Form<GetCategoriesReq> getcategoriesForm = Form.form(GetCategoriesReq.class)
                .bindFromRequest();
        if (getcategoriesForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetCategoriesReq getCategoriesReq = getcategoriesForm.get();
        if (ObjectIdUtils.hasInvalidId(getCategoriesReq.orgId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        GetCategoriesRes getCategoriesRes = null;

        try {
            getCategoriesRes = CategoryManager.getCategories(getCategoriesReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getCategoriesRes).toObjectNode());
    }

    public static Result getCategory() throws VedantuException {

        LOGGER.debug("......entering getCategory ......");
        Form<GetCategoryReq> getcategoryForm = Form.form(GetCategoryReq.class)
                .bindFromRequest();
        if (getcategoryForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetCategoryReq getCategoryReq = getcategoryForm.get();
        if (ObjectIdUtils.hasInvalidId(getCategoryReq.orgId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        GetCategoryRes getCategoryRes = null;

        try {
            getCategoryRes = CategoryManager.getCategory(getCategoryReq);
            LOGGER.debug("......getCategory Organization......" + getCategoryRes);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getCategoryRes).toObjectNode());
    }

    public static Result editCategory() throws VedantuException {

        // LOGGER.debug("......entering addSection ......");
        Form<EditCategoryReq> editcategoryForm = Form.form(EditCategoryReq.class).bindFromRequest();
        if (editcategoryForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        EditCategoryReq editCategoryReq = editcategoryForm.get();
        if (ObjectIdUtils.hasInvalidId(editCategoryReq.id)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        EditCategoryRes editCategoryRes = null;

        try {
            editCategoryRes = CategoryManager.editCategory(editCategoryReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(editCategoryRes).toObjectNode());
    }

    public static Result editCategories() throws VedantuException {

        // LOGGER.debug("......entering addSection ......");
        Form<EditCategoriesReq> editcategoriesForm = Form.form(EditCategoriesReq.class)
                .bindFromRequest();
        if (editcategoriesForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        EditCategoriesReq editCategoriesReq = editcategoriesForm.get();

        EditCategoriesRes editCategoriesRes = null;

        try {
            editCategoriesRes = CategoryManager.editCategories(editCategoriesReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(editCategoriesRes).toObjectNode());
    }

    public static Result customizeCategory() throws VedantuException {
        Form<CustomizeCategoryReq> customizeCategoryForm = Form.form(CustomizeCategoryReq.class)
                .bindFromRequest();
        if (customizeCategoryForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        CustomizeCategoryReq customizeCategoryReq = customizeCategoryForm.get();
        CustomizeCategoryRes customizeCategoryRes = null;

        try {
            customizeCategoryRes = CategoryManager.customizeCategory(customizeCategoryReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(customizeCategoryRes).toObjectNode());
    }

    public static Result removeCategory() throws VedantuException {

        // LOGGER.debug("......entering addSection ......");
        Form<RemoveCategoryReq> removecategoryForm = Form.form(RemoveCategoryReq.class)
                .bindFromRequest();
        if (removecategoryForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        RemoveCategoryReq removeCategoryReq = removecategoryForm.get();
        if (ObjectIdUtils.hasInvalidId(removeCategoryReq.id)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        RemoveCategoryRes removeCategoryRes = null;

        try {
            removeCategoryRes = CategoryManager.removeCategory(removeCategoryReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(removeCategoryRes).toObjectNode());
    }

    public static Result getCategorySections() {

        Form<GetCategorySectionsReq> getCategorySectionsForm = Form.form(
                GetCategorySectionsReq.class).bindFromRequest();
        if (getCategorySectionsForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS),
                    getErrorMessege(getCategorySectionsForm)).toObjectNode());
        }
        GetCategorySectionsReq getCategorySectionsReq = getCategorySectionsForm.get();

        GetCategorySectionsRes getCategorySectionsRes = null;

        try {
            getCategorySectionsRes = CategoryManager.getCategorySections(getCategorySectionsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        if(getCategorySectionsReq.orgId.equals(Play.application().configuration().getString("learnpedia.id"))){
            try {
                return ok(addCustomDescription(getResultResponse(getCategorySectionsRes).toObjectNode().toString()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ok(getResultResponse(getCategorySectionsRes).toObjectNode());
    }

    private static String addCustomDescription(String resultResponse) throws JSONException {
        JSONObject response = new JSONObject(resultResponse);
        JSONObject result = new JSONObject(resultResponse);
        result = result.getJSONObject("result");
        JSONArray list = new JSONArray();
        list = result.getJSONArray("list");
        for(int i = 0; i < list.length(); i++){
            String secId = list.getJSONObject(i).getJSONObject("sectionInfo").getString("id");
            JSONObject description = new JSONObject();
            description.put("desc", Play.application().configuration().getString("\""+secId+".desc"+"\""));
            description.put("firstPoint", Play.application().configuration().getString("\""+secId+".firstPoint"+"\""));
            description.put("secondPoint", Play.application().configuration().getString("\""+secId+".secondPoint"+"\""));
            description.put("thirdPoint", Play.application().configuration().getString("\""+secId+".thirdPoint"+"\""));
            description.put("fourthPoint", Play.application().configuration().getString("\""+secId+".fourthPoint"+"\""));
            description.put("imageUrl", Play.application().configuration().getString("\""+secId+".imageUrl"+"\""));
            response.getJSONObject("result").getJSONArray("list").getJSONObject(i).put("description", description);
        }
        LOGGER.debug("Final result "+response.toString());
        return response.toString();
    }

    public static Result getCategorySection() {

        Form<GetCategorySectionReq> getCategorySectionsForm = Form
                .form(GetCategorySectionReq.class).bindFromRequest();
        if (getCategorySectionsForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS),
                    getErrorMessege(getCategorySectionsForm)).toObjectNode());
        }
        GetCategorySectionReq request = getCategorySectionsForm.get();

        GetCategorySectionRes response = null;

        try {
            response = CategoryManager.getCategorySection(request);
            String secId = request.sectionId;
            String temp = Play.application().configuration().getString("\""+secId+".desc"+"\"");
            if(null == temp){
                response.isB2C = false;
            }else{
                response.isB2C = true;
            }
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result getMemberCategorySections() {

        Form<GetSelfCategorySectionsReq> getCategorySectionsForm = Form.form(
                GetSelfCategorySectionsReq.class).bindFromRequest();
        if (getCategorySectionsForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS),
                    getErrorMessege(getCategorySectionsForm)).toObjectNode());
        }
        GetSelfCategorySectionsReq getCategorySectionsReq = getCategorySectionsForm.get();

        GetCategorySectionsRes getCategorySectionsRes = null;

        try {
            getCategorySectionsRes = CategoryManager
                    .getMemberCategorySections(getCategorySectionsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        if(getCategorySectionsReq.orgId.equals(Play.application().configuration().getString("learnpedia.id"))){
            try {
                return ok(addCustomDescription(getResultResponse(getCategorySectionsRes).toObjectNode().toString()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ok(getResultResponse(getCategorySectionsRes).toObjectNode());
    }

    public static Result updateSection() {

        Form<UpdateOrgSectionReq> updateOrgSectionForm = Form.form(UpdateOrgSectionReq.class)
                .bindFromRequest();
        if (updateOrgSectionForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        UpdateOrgSectionReq updateOrgSectionReq = updateOrgSectionForm.get();
        if (ObjectIdUtils.hasInvalidId(updateOrgSectionReq.orgId, updateOrgSectionReq.programId,
                updateOrgSectionReq.sectionId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        UpdateOrgSectionRes updateOrgSectionRes = null;

        try {
            updateOrgSectionRes = OrgSectionManager.updateSection(updateOrgSectionReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(updateOrgSectionRes).toObjectNode());
    }

    public static Result updateSectionAccess() {

        Form<UpdateOrgSectionAccessReq> updateOrgSectionAccessForm = Form.form(
                UpdateOrgSectionAccessReq.class).bindFromRequest();
        if (updateOrgSectionAccessForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS),
                    getErrorMessege(updateOrgSectionAccessForm)).toObjectNode());
        }
        UpdateOrgSectionAccessReq updateOrgSectionAccessReq = updateOrgSectionAccessForm.get();

        UpdateOrgSectionAccessRes updateOrgSectionAccessRes = null;

        try {
            updateOrgSectionAccessRes = OrgSectionManager
                    .updateSectionAccess(updateOrgSectionAccessReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(updateOrgSectionAccessRes).toObjectNode());
    }

    public static Result removeSection() {

        Form<RemoveOrgSectionReq> removeOrgSectionForm = Form.form(RemoveOrgSectionReq.class)
                .bindFromRequest();
        if (removeOrgSectionForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        RemoveOrgSectionReq removeOrgSectionReq = removeOrgSectionForm.get();
        if (ObjectIdUtils.hasInvalidId(removeOrgSectionReq.orgId, removeOrgSectionReq.sectionId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        RemoveOrgSectionRes removeOrgSectionsRes = null;

        try {
            removeOrgSectionsRes = OrgProgramManager.removeProgramSection(removeOrgSectionReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(removeOrgSectionsRes).toObjectNode());
    }

    public static Result activateSection() {

        Form<ActivateOrgSectionReq> activateOrgSectionForm = Form.form(ActivateOrgSectionReq.class)
                .bindFromRequest();
        if (activateOrgSectionForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        ActivateOrgSectionReq activateOrgSectionReq = activateOrgSectionForm.get();
        if (ObjectIdUtils
                .hasInvalidId(activateOrgSectionReq.orgId, activateOrgSectionReq.sectionId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        ActivateOrgSectionRes activateOrgSectionsRes = null;

        try {
            activateOrgSectionsRes = OrgSectionManager.activateSection(activateOrgSectionReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(activateOrgSectionsRes).toObjectNode());
    }

    public static Result getAssociatedOrgsOfUser() {

        Form<GetAssociatedOrgsReq> getAssociatedOrgsForm = Form.form(GetAssociatedOrgsReq.class)
                .bindFromRequest();
        if (getAssociatedOrgsForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetAssociatedOrgsReq getAssociatedOrgsReq = getAssociatedOrgsForm.get();

        GetAssociatedOrgsRes getAssociatedOrgsRes = null;

        try {
            getAssociatedOrgsRes = OrganizationManager
                    .getAssociatedOrgsOfUser(getAssociatedOrgsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getAssociatedOrgsRes).toObjectNode());
    }

    public static Result getProgramCourses() {

        Form<GetOrgProgramCoursesReq> getOrgProgramCoursesForm = Form.form(
                GetOrgProgramCoursesReq.class).bindFromRequest();
        if (getOrgProgramCoursesForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetOrgProgramCoursesReq getOrgProgramCoursesReq = getOrgProgramCoursesForm.get();
        if (ObjectIdUtils.hasInvalidId(getOrgProgramCoursesReq.orgId,
                getOrgProgramCoursesReq.programId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }

        GetOrgProgramCoursesRes getOrgProgramCoursesRes = null;

        try {
            getOrgProgramCoursesRes = OrgProgramManager.getProgramCourses(getOrgProgramCoursesReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getOrgProgramCoursesRes).toObjectNode());
    }

    public static Result addProgramCourses() {

        Form<AddOrgProgramCoursesReq> addOrgProgramCoursesForm = Form.form(
                AddOrgProgramCoursesReq.class).bindFromRequest();
        if (addOrgProgramCoursesForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        AddOrgProgramCoursesReq addOrgProgramCoursesReq = addOrgProgramCoursesForm.get();
        String[] tCourseIds = addOrgProgramCoursesReq.courseIds.toArray(new String[0]);
        if (ObjectIdUtils.hasInvalidId(addOrgProgramCoursesReq.orgId,
                addOrgProgramCoursesReq.programId) || ObjectIdUtils.hasInvalidId(tCourseIds)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }

        AddOrgProgramCoursesRes addOrgProgramCoursesRes = null;

        try {
            addOrgProgramCoursesRes = OrgProgramManager.addProgramCourses(addOrgProgramCoursesReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(addOrgProgramCoursesRes).toObjectNode());
    }

    public static Result removeProgramCourses() {

        Form<RemoveOrgProgramCoursesReq> removeOrgProgramCoursesForm = Form.form(
                RemoveOrgProgramCoursesReq.class).bindFromRequest();
        if (removeOrgProgramCoursesForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        RemoveOrgProgramCoursesReq removeOrgProgramCoursesReq = removeOrgProgramCoursesForm.get();
        String[] tCourseIds = removeOrgProgramCoursesReq.courseIds.toArray(new String[0]);
        if (ObjectIdUtils.hasInvalidId(removeOrgProgramCoursesReq.orgId,
                removeOrgProgramCoursesReq.programId) || ObjectIdUtils.hasInvalidId(tCourseIds)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }

        RemoveOrgProgramCoursesRes removeOrgProgramCoursesRes = null;

        try {
            removeOrgProgramCoursesRes = OrgProgramManager
                    .removeProgramCourses(removeOrgProgramCoursesReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(removeOrgProgramCoursesRes).toObjectNode());
    }

    public static Result getCoursePrograms() {

        Form<GetOrgCourseProgramsReq> getOrgCourseProgramsForm = Form.form(
                GetOrgCourseProgramsReq.class).bindFromRequest();
        if (getOrgCourseProgramsForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetOrgCourseProgramsReq getOrgCourseProgramsReq = getOrgCourseProgramsForm.get();
        if (ObjectIdUtils.hasInvalidId(getOrgCourseProgramsReq.orgId,
                getOrgCourseProgramsReq.courseId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }

        GetOrgCourseProgramsRes getOrgCourseProgramsRes = null;

        try {
            getOrgCourseProgramsRes = OrgProgramManager.getCoursePrograms(getOrgCourseProgramsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getOrgCourseProgramsRes).toObjectNode());
    }

    public static Result uploadOrgPic() {

        MultipartFormData body = request().body().asMultipartFormData();
        UploadOrgPicReq uploadOrgPicReq = new UploadOrgPicReq(body);
        String validation = uploadOrgPicReq.validate();
        Result result = null;
        if (StringUtils.isNotEmpty(validation)) {
            result = ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, validation))
                    .toObjectNode());
        } else if (null == uploadOrgPicReq.inputFile) {
            result = ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_FILE))
                    .toObjectNode());
        } else {

            if (ObjectIdUtils.hasInvalidId(uploadOrgPicReq.orgId, uploadOrgPicReq.orgMemberId)) {
                return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                        .toObjectNode());
            }

            try {
                UploadOrgPicRes uploadOrgPicRes = OrganizationManager.uploadOrgPic(uploadOrgPicReq);
                result = ok(getResultResponse(uploadOrgPicRes).toObjectNode());
            } catch (VedantuException e) {
                result = ok(getErrorResponse(e).toObjectNode());
            }
        }

        deleteFile(uploadOrgPicReq.fileName, uploadOrgPicReq.inputFile);
        return result;
    }

    public static Result getProgramInfo() {

        Form<GetProgramInfoReq> form = Form.form(GetProgramInfoReq.class).bindFromRequest();
        if (form.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, getErrorMessege(form)))
                    .toObjectNode());
        }
        GetProgramInfoReq req = form.get();
        OrgProgramBasicInfo res = null;
        try {
            res = OrgProgramManager.getOrgProgramBasicInfo(req.programId, true, true, null);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(res).toObjectNode());
    }

    public static Result getOrgPointsOfSale() {

        Form<GetOrgPointsOfSaleReq> form = Form.form(GetOrgPointsOfSaleReq.class).bindFromRequest();
        if (form.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, getErrorMessege(form)))
                    .toObjectNode());
        }
        GetOrgPointsOfSaleReq req = form.get();
        GetOrgPointsOfSaleRes res = null;
        try {
            res = OrganizationManager.getOrganizationPoinsOfSale(req);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(res).toObjectNode());
    }

    public static Result getSectionByAccessCode() {

        Form<GetOrgSectionInfoByAccessCodeReq> form = Form.form(
                GetOrgSectionInfoByAccessCodeReq.class).bindFromRequest();
        if (form.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, getErrorMessege(form)))
                    .toObjectNode());
        }
        GetOrgSectionInfoByAccessCodeReq req = form.get();

        GetOrgSectionInfoByAccessCodeRes res = null;

        try {
            res = OrgSectionManager.getOrgSectionInfoByAccessCode(req);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(res).toObjectNode());
    }

    // TODO: remove this function agter v.3.7.5 deployment, also remove it's mapping in routes
    public static Result generateOrgSectionAccessCode() {

        return ok(getResultResponse(
                new JSONResponse(OrgSectionManager.generateOrgSectionAccessCode())).toObjectNode());
    }

    public static Result getLatestTNC() {

        GetLatestTncRes response = new GetLatestTncRes();
        response.tncVersion = OrganizationManager.getLatestTnC();
        return ok(getResultResponse(response).toObjectNode());

    }

    public static Result acceptTnC() {

        Form<AcceptTncOrgReq> acceptTnCForm = Form.form(AcceptTncOrgReq.class).bindFromRequest();
        if (acceptTnCForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(acceptTnCForm))).toObjectNode());
        }
        AcceptTncOrgReq acceptTnCReq = acceptTnCForm.get();
        AcceptTnCRes acceptTnCRes = null;
        try {
            acceptTnCRes = OrganizationManager.acceptTnC(acceptTnCReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(acceptTnCRes).toObjectNode());
    }

    public static Result generateAppCredentials() {

        Form<GenerateAppCredentialsReq> form = Form.form(GenerateAppCredentialsReq.class)
                .bindFromRequest();
        if (form.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, getErrorMessege(form)))
                    .toObjectNode());
        }
        GenerateAppCredentialsReq req = form.get();
        GenerateAppCredentialsRes res = null;
        try {
            res = OrganizationManager.generateAppCredentials(req);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(res).toObjectNode());
    }

    public static Result verifyAppCredentials() {

        Form<VerifyAppCredentialsReq> form = Form.form(VerifyAppCredentialsReq.class)
                .bindFromRequest();
        if (form.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, getErrorMessege(form)))
                    .toObjectNode());
        }
        VerifyAppCredentialsReq req = form.get();
        VerifyAppCredentialsRes res = null;
        try {
            res = OrganizationManager.generateAppCredentials(req);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(res).toObjectNode());
    }

    public static Result getSectionPackageInfo() {

        Form<GetOrgSectionReq> getOrgSectionsForm = Form.form(GetOrgSectionReq.class)
                .bindFromRequest();
        if (getOrgSectionsForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetOrgSectionReq getOrgSectionsReq = getOrgSectionsForm.get();
        if (ObjectIdUtils.hasInvalidId(getOrgSectionsReq.orgId, getOrgSectionsReq.sectionId
               )) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        GetOrgSectionRes getOrgSectionsRes = null;

        try {
            getOrgSectionsRes = OrgProgramManager.getSectionPackageInfo(getOrgSectionsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getOrgSectionsRes).toObjectNode());
    }

    public static Result updatePackageInfo() {
        Form<UpdatePackageInfoReq> form = Form.form(UpdatePackageInfoReq.class)
                .bindFromRequest();
        if (form.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, getErrorMessege(form)))
                    .toObjectNode());
        }
        UpdatePackageInfoReq req = form.get();
        UpdatePackageInfoRes res = null;
        try {
            res = OrgSectionManager.updatePackageInfo(req);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(res).toObjectNode());
    }

    public static Result updateSectionMaxDiscount() {
        Form<UpdateMaxDiscountReq> form = Form.form(UpdateMaxDiscountReq.class)
                .bindFromRequest();
        if (form.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, getErrorMessege(form)))
                    .toObjectNode());
        }
        UpdateMaxDiscountReq req = form.get();
        UpdateMaxDiscountRes res = null;
        try {
            res = OrgSectionManager.updateMaxDiscount(req);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(res).toObjectNode());
    }
    public static Result getInstaMojoAccessToken() throws VedantuException {
//		USING SAME REQUEST FOR ORGID REQIRED IN GETIING ACCESS TOKEN
//		TIME BEING NOT SENDING ORGID... IN FUTURE USE ORGID FOR DIFFERENT ORGS
        Form<AbstractOrgScopeReq> getOrganizationForm = Form.form(AbstractOrgScopeReq.class)
                .bindFromRequest();
        if (getOrganizationForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        AbstractOrgScopeReq getOrganizationReq = getOrganizationForm.get();
        LOGGER.debug("ORGID INSTA TOKEN :" + getOrganizationReq.orgId);
        //TO REMOVE SINGLE QUOTES
        getOrganizationReq.orgId = getOrganizationReq.orgId.substring(1, getOrganizationReq.orgId.length()-1);
        InstaMojoAccessTokenResp getInstaMojoAccessTokenResp = InstaMojoTokenManager.getInstaMojoAccessTokenFromPHP(getOrganizationReq);
        return ok(getResultResponse(getInstaMojoAccessTokenResp).toObjectNode());
    }

	public static Result ping() {
		InstaMojoAccessTokenResp getInstaMojoAccessTokenResp = new InstaMojoAccessTokenResp();
		return ok(getResultResponse(getInstaMojoAccessTokenResp).toObjectNode());
	}
}
