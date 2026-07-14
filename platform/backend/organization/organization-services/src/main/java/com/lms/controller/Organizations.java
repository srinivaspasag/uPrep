package com.lms.controller;

import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.commons.pojos.requests.AddOrgCenterReq;
import com.lms.common.vedantu.commons.pojos.requests.StateChangeOrgCenterReq;
import com.lms.common.vedantu.commons.pojos.requests.UpdateOrgCenterReq;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojo.GetOrgReq;
import com.lms.pojo.GetSharedOrgsReq;
import com.lms.pojo.request.*;
import com.lms.service.CategoryService;
import com.lms.service.DepartmentService;
import com.lms.service.OrganizationService;
import com.lms.service.ProgramService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

@Controller
@RequestMapping("/organizations")
public class Organizations {
    private static final Logger logger = LoggerFactory.getLogger(Organizations.class);
    public static final String server = "1";
    @Autowired
    private OrganizationService organizationServiceImpl;
    @Autowired
    private DepartmentService departmentServiceImpl;
    @Autowired
    private ProgramService programService;
    @Autowired
    private CategoryService categoryService;

    @PostMapping("/getOrganization")
    public ResponseEntity<VedantuResponse> getOrganization(GetOrgReq getOrgReq) throws VedantuException {
        return ResponseEntity.ok(organizationServiceImpl.getOrganization(getOrgReq));
    }

    @PostMapping("/getShowSharedSubjects")
    public ResponseEntity<VedantuResponse> getShowSharedSubjects(GetOrgReq getOrgReq) throws VedantuException {
        return ResponseEntity.ok(organizationServiceImpl.getShowSharedSubjects(getOrgReq));
    }

    @PostMapping("/getOrganizationInfoForInvoice")
    public ResponseEntity<VedantuResponse> getOrganizationInfoForInvoice(GetOrgReq getOrgReq) throws VedantuException {
        return ResponseEntity.ok(organizationServiceImpl.getOrganizationInfoForInvoice(getOrgReq));
    }

    @PostMapping("/checkWebsite")
    public ResponseEntity<VedantuResponse> checkWebsite(CheckWebsiteReq checkWebsiteReq) throws VedantuException {
        return ResponseEntity.ok(organizationServiceImpl.checkWebsite(checkWebsiteReq));
    }

    @PostMapping("/checkSlug")
    public ResponseEntity<VedantuResponse> checkSlug(CheckSlugReq checkSlugReq) throws VedantuException {
        return ResponseEntity.ok(organizationServiceImpl.checkSlug(checkSlugReq));
    }

    @PostMapping("/checkAppVersion")
    public ResponseEntity<VedantuResponse> checkAppVersion(CheckAppVersionReq checkAppVersionReq) throws VedantuException {
        return ResponseEntity.ok(organizationServiceImpl.checkAppVersion(checkAppVersionReq));
    }

    @PostMapping("/updateOrganizationSlug")
    public ResponseEntity<VedantuResponse> updateOrganizationSlug(UpdateOrgSlugReq updateOrgSlugReq) throws VedantuException {
        return ResponseEntity.ok(organizationServiceImpl.updateOrganizationSlug(updateOrgSlugReq));
    }

    @PostMapping("/updateOrganizationStatus")
    public ResponseEntity<VedantuResponse> updateOrganizationStatus(UpdateOrgStatusReq updateOrgStatusReq) throws VedantuException {
        return ResponseEntity.ok(organizationServiceImpl.updateOrganizationStatus(updateOrgStatusReq));
    }

    @PostMapping("/updateOrganizationDownloadStatus")
    public ResponseEntity<VedantuResponse> updateOrganizationDownloadStatus(@Valid UpdateOrganizationDownloadStatusReq updateOrgStatusReq) throws VedantuException {
        return ResponseEntity.ok(organizationServiceImpl.updateOrganizationDownloadStatus(updateOrgStatusReq));
    }

    @PostMapping("/UpdateOrganizationSharedSubjects")
    public ResponseEntity<VedantuResponse> UpdateOrganizationSharedSubjects(@Valid UpdateOrganizationSharedSubjectsReq updateOrganizationSharedSubjectsReq) throws VedantuException {
        return ResponseEntity.ok(organizationServiceImpl.UpdateOrganizationSharedSubjects(updateOrganizationSharedSubjectsReq));
    }

    @PostMapping("/updateOrganizationClassroomConnectStatus")
    public ResponseEntity<VedantuResponse> updateOrganizationClassroomConnectStatus(@Valid UpdateOrganizationClassroomConnectStatusReq updateOrganizationClassroomConnectStatusReq) throws VedantuException {
        return ResponseEntity.ok(organizationServiceImpl.updateOrganizationClassroomConnectStatus(updateOrganizationClassroomConnectStatusReq));
    }

    @PostMapping("/getOrganizationBySlug")
    public ResponseEntity<VedantuResponse> getOrganizationBySlug(@Valid GetOrgBySlugReq getOrgBySlugReq) throws VedantuException {
        return ResponseEntity.ok(organizationServiceImpl.getOrganizationBySlug(getOrgBySlugReq));
    }

    @PostMapping("/updateOrganizationReferer")
    public ResponseEntity<VedantuResponse> updateOrganizationReferer(@Valid UpdateOrgRefererReq updateOrgRefererReq) throws VedantuException {
        return ResponseEntity.ok(organizationServiceImpl.updateOrganizationReferer(updateOrgRefererReq));
    }

    @PostMapping("/getOrganizationByReferer")
    public ResponseEntity<VedantuResponse> getOrganizationByReferer(@RequestHeader("REFERER") String referer, GetOrgByRefererReq getOrgByRefererReq) throws VedantuException, MalformedURLException {
        logger.info("referer " + referer);
        return ResponseEntity.ok(organizationServiceImpl.getOrganizationByReferer(getOrgByRefererReq.getKey, referer));
    }

    @PostMapping("/checkReferer")
    public ResponseEntity<VedantuResponse> checkReferer(@Valid CheckRefererReq checkRefererReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(organizationServiceImpl.checkReferer(checkRefererReq));
    }

    @PostMapping("/getOrganizations")
    public ResponseEntity<VedantuResponse> getOrganizations(@Valid GetOrgsReq getOrgsReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(organizationServiceImpl.getOrganizations(getOrgsReq));
    }

    @PostMapping("/addOrganization")
    public ResponseEntity<VedantuResponse> addOrganization(AddOrgReq addOrgReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(organizationServiceImpl.addOrganization(addOrgReq));
    }

    @PostMapping("/updateOrganization")
    public ResponseEntity<VedantuResponse> updateOrganization(UpdateOrgReq updateOrgReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(organizationServiceImpl.updateOrganization(updateOrgReq));
    }

    @PostMapping("/approveOrganization")
    public ResponseEntity<VedantuResponse> approveOrganization(ApproveOrgReq approveOrgReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(organizationServiceImpl.approveOrganization(approveOrgReq));
    }


    @PostMapping("/updateOrgMemberExtraInputFields")
    public ResponseEntity<VedantuResponse> updateOrgMemberExtraInputFields(@Valid UpdateOrgMemberExtraInfoInputFieldsReq updateOrgMemberExtraInfoInputFieldsReq) throws VedantuException {
        return ResponseEntity.ok(organizationServiceImpl.updateOrgMemberExtraInputFields(updateOrgMemberExtraInfoInputFieldsReq));
    }

    @PostMapping("/updateDigitalLibraryFields")
    public ResponseEntity<VedantuResponse> updateDigitalLibraryFields(@Valid UpdateDigitalLibraryFieldsReq updateDigitalLibraryFieldsReq) throws VedantuException {
        return ResponseEntity.ok(organizationServiceImpl.updateDigitalLibraryFields(updateDigitalLibraryFieldsReq));
    }

    @PostMapping("/getDigitalLibraryFields")
    public ResponseEntity<VedantuResponse> getDigitalLibraryFields(@Valid GetDigitalLibraryFieldsReq getDigitalLibraryFieldsReq) throws VedantuException {
        return ResponseEntity.ok(organizationServiceImpl.getDigitalLibraryFields(getDigitalLibraryFieldsReq));
    }

    @PostMapping("/checkIfSuperAdmin")
    public ResponseEntity<VedantuResponse> checkIfSuperAdmin(@Valid CheckIfSuperAdminReq checkIfSuperAdminReq) throws VedantuException {
        return ResponseEntity.ok(organizationServiceImpl.checkIfSuperAdmin(checkIfSuperAdminReq));
    }

    @PostMapping("/getOrgPointsOfSale")
    public ResponseEntity<VedantuResponse> getOrgPointsOfSale(@Valid AbstractOrgScopeReq abstractOrgScopeReq) throws VedantuException {
        return ResponseEntity.ok(organizationServiceImpl.getOrgPointsOfSale(abstractOrgScopeReq));
    }

    @PostMapping("/getOrgMemberExtraInputFields")
    public ResponseEntity<VedantuResponse> getOrgMemberExtraInputFields(@Valid GetOrgMemberExtraInfoInputFieldsReq getOrgMemberExtraInfoInputFieldsReq) throws VedantuException {
        return ResponseEntity.ok(organizationServiceImpl.getOrgMemberExtraInputFields(getOrgMemberExtraInfoInputFieldsReq));
    }

    @PostMapping("/getCenters")
    public ResponseEntity<VedantuResponse> getCenters(@Valid GetOrgCentersReq getOrgCentersReq) throws VedantuException {
        return ResponseEntity.ok(organizationServiceImpl.getCenters(getOrgCentersReq));
    }

    @PostMapping("/addCenter")
    public ResponseEntity<VedantuResponse> addCenter(AddOrgCenterReq addOrgCenterReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(organizationServiceImpl.addCenter(addOrgCenterReq));
    }

    @PostMapping("/updateCenter")
    public ResponseEntity<VedantuResponse> updateCenter(UpdateOrgCenterReq updateOrgCenterReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(organizationServiceImpl.updateCenter(updateOrgCenterReq));
    }

    @PostMapping("/removeCenter")
    public ResponseEntity<VedantuResponse> removeCenter(@Valid StateChangeOrgCenterReq stateChangeOrgCenterReq) throws VedantuException {
        return ResponseEntity.ok(organizationServiceImpl.removeCenter(stateChangeOrgCenterReq));
    }

    @PostMapping("/activateCenter")
    public ResponseEntity<VedantuResponse> activateCenter(@Valid StateChangeOrgCenterReq stateChangeOrgCenterReq) throws VedantuException {
        return ResponseEntity.ok(organizationServiceImpl.activateCenter(stateChangeOrgCenterReq));
    }

    @PostMapping("/getInstaMojoAccessToken")
    public ResponseEntity<VedantuResponse> getInstaMojoAccessToken(@Valid AbstractOrgScopeReq abstractOrgScopeReq) throws VedantuException {
        return ResponseEntity.ok(organizationServiceImpl.getInstaMojoAccessToken(abstractOrgScopeReq));
    }

    @GetMapping("/ping")
    public ResponseEntity<VedantuResponse> ping() {

        return ResponseEntity.ok(new VedantuResponse(server,"",""));
    }

    @PostMapping("/getSharedOrgsByProgId")
    public ResponseEntity<VedantuResponse> getSharedOrgsByProgId(GetSharedOrgsReq getSharedOrgsReq) throws VedantuException {
        return ResponseEntity.ok(organizationServiceImpl.getSharedOrgsByProgId(getSharedOrgsReq));
    }

    @PostMapping("/uploadOrgPic")
    public ResponseEntity<VedantuResponse> uploadOrgPic(@RequestParam("file") MultipartFile file, UploadOrgPicReq request) throws IOException {

        return ResponseEntity.ok(organizationServiceImpl.uploadOrgPic(file, request));
    }

    @PostMapping("/shareProgToOrg")
    public ResponseEntity<VedantuResponse> shareProgToOrg(GetSharedOrgsReq getSharedOrgsReq) throws IOException {

        return ResponseEntity.ok(organizationServiceImpl.shareProgToOrg(getSharedOrgsReq));
    }

    @PostMapping("/removeSharedProgramFromOrg")
    public ResponseEntity<VedantuResponse> removeSharedProgramFromOrg(@Valid RemoveProgramSharingReq removeProgramSharingReq) throws IOException {

        return ResponseEntity.ok(organizationServiceImpl.removeSharedProgramFromOrg(removeProgramSharingReq));
    }

    @PostMapping("/acceptTnC")
    public ResponseEntity<VedantuResponse> acceptTnC(@Valid AcceptTncOrgReq acceptTncOrgReq) throws IOException {

        return ResponseEntity.ok(organizationServiceImpl.acceptTnC(acceptTncOrgReq));
    }

    @PostMapping("/getDepartments")
    public ResponseEntity<VedantuResponse> getDepartments(@Valid GetOrgDepartmentsReq getOrgDepartmentsReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(departmentServiceImpl.getDepartments(getOrgDepartmentsReq));
    }

    @PostMapping("/getPrograms")
    public ResponseEntity<VedantuResponse> getPrograms(@Valid GetOrgProgramsReq getOrgProgramsReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(programService.getPrograms(getOrgProgramsReq));
    }

    @PostMapping("/getProgramInfo")
    public ResponseEntity<VedantuResponse> getProgramInfo(@Valid GetProgramInfoReq getProgramInfoReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(programService.getProgramInfo(getProgramInfoReq));
    }

    @PostMapping("/addProgram")
    public ResponseEntity<VedantuResponse> addProgram(@Valid AddOrgProgramReq addOrgProgramReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(programService.addProgram(addOrgProgramReq));
    }

    @PostMapping("/updateProgram")
    public ResponseEntity<VedantuResponse> updateProgram(@Valid UpdateOrgProgramReq updateOrgProgramReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(programService.updateProgram(updateOrgProgramReq));
    }

    @PostMapping("/removeProgram")
    public ResponseEntity<VedantuResponse> removeProgram(@Valid StateChangeOrgProgramReq stateChangeOrgProgramReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(programService.removeProgram(stateChangeOrgProgramReq));
    }

    @PostMapping("/activateProgram")
    public ResponseEntity<VedantuResponse> activateProgram(@Valid StateChangeOrgProgramReq stateChangeOrgProgramReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(programService.activateProgram(stateChangeOrgProgramReq));
    }

    @PostMapping("/getProgramCenters")
    public ResponseEntity<VedantuResponse> getProgramCenters(@Valid GetOrgProgramCentersReq getOrgProgramCentersReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(programService.getProgramCenters(getOrgProgramCentersReq));
    }

    @PostMapping("/addProgramCenters")
    public ResponseEntity<VedantuResponse> addProgramCenters(@Valid AddOrgProgramCentersReq addOrgProgramCentersReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(programService.addProgramCenters(addOrgProgramCentersReq));
    }

    @PostMapping("/removeProgramCenters")
    public ResponseEntity<VedantuResponse> removeProgramCenters(@Valid AddOrgProgramCentersReq addOrgProgramCentersReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(programService.removeProgramCenters(addOrgProgramCentersReq));
    }

    @PostMapping("/addCategory")
    public ResponseEntity<VedantuResponse> addCategory(@Valid AddCategoryReq addCategoryReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(categoryService.addCategory(addCategoryReq));
    }

    @PostMapping("/editCategory")
    public ResponseEntity<VedantuResponse> editCategory(@Valid EditCategoryReq editCategoryReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(categoryService.editCategory(editCategoryReq));
    }

    @PostMapping("/removeCategory")
    public ResponseEntity<VedantuResponse> removeCategory(@Valid RemoveCategoryReq removeCategoryReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(categoryService.removeCategory(removeCategoryReq));
    }

    @PostMapping("/addDepartment")
    public ResponseEntity<VedantuResponse> addDepartment(@Valid AddOrgDepartmentReq addOrgDepartmentReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(departmentServiceImpl.addDepartment(addOrgDepartmentReq));
    }

    @PostMapping("/updateDepartment")
    public ResponseEntity<VedantuResponse> addDepartment(@Valid UpdateOrgDepartmentReq updateOrgDepartmentReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(departmentServiceImpl.updateDepartment(updateOrgDepartmentReq));
    }

    @PostMapping("/removeDepartment")
    public ResponseEntity<VedantuResponse> removeDepartment(@Valid RemoveOrgDepartmentReq removeOrgDepartmentReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(departmentServiceImpl.removeDepartment(removeOrgDepartmentReq));
    }

    @PostMapping("/activateDepartment")
    public ResponseEntity<VedantuResponse> activateDepartment(@Valid ActivateOrgDepartmentReq activateOrgDepartmentReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(departmentServiceImpl.activateDepartment(activateOrgDepartmentReq));
    }

    @PostMapping("/getSection")
    public ResponseEntity<VedantuResponse> getSection(@Valid GetOrgSectionReq getOrgSectionReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(organizationServiceImpl.getSection(getOrgSectionReq));
    }

    @PostMapping("/getSections")
    public ResponseEntity<VedantuResponse> getSections(@Valid GetOrgSectionsReq getOrgSectionsReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(organizationServiceImpl.getSections(getOrgSectionsReq));
    }

    @PostMapping("/addSection")
    public ResponseEntity<VedantuResponse> addSection(@Valid AddOrgSectionReq addOrgSectionReq) throws VedantuException, MalformedURLException, FileNotFoundException {
        return ResponseEntity.ok(organizationServiceImpl.addSection(addOrgSectionReq));
    }

    @PostMapping("/editCategories")
    public ResponseEntity<VedantuResponse> editCategories(@Valid EditCategoriesReq editCategoriesReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(categoryService.editCategories(editCategoriesReq));
    }

    @PostMapping("/getCategories")
    public ResponseEntity<VedantuResponse> getCategories(@Valid AbstractOrgScopeReq abstractOrgScopeReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(categoryService.getCategories(abstractOrgScopeReq));
    }

    @PostMapping("/getCategory")
    public ResponseEntity<VedantuResponse> getCategory(@Valid GetCategoryReq getCategoryReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(categoryService.getCategory(getCategoryReq));
    }

    @PostMapping("/customizeCategory")
    public ResponseEntity<VedantuResponse> customizeCategory(@Valid CustomizeCategoryReq customizeCategoryReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(categoryService.customizeCategory(customizeCategoryReq));
    }

    @PostMapping("/getCategorySections")
    public ResponseEntity<VedantuResponse> getCategorySections(@Valid GetCategorySectionsReq getCategorySectionsReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(categoryService.getCategorySections(getCategorySectionsReq));
    }

    @PostMapping("/getCategorySection")
    public ResponseEntity<VedantuResponse> getCategorySection(@Valid GetCategorySectionReq getCategorySectionReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(categoryService.getCategorySection(getCategorySectionReq));
    }

    @PostMapping("/getMemberCategorySections")
    public ResponseEntity<VedantuResponse> getMemberCategorySections(@Valid GetSelfCategorySectionsReq getSelfCategorySectionsReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(categoryService.getMemberCategorySections(getSelfCategorySectionsReq));
    }

    @PostMapping("/getProgramCourses")
    public ResponseEntity<VedantuResponse> getProgramCourses(@Valid GetOrgProgramCoursesReq getOrgProgramCoursesReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(programService.getProgramCourses(getOrgProgramCoursesReq));
    }

    @PostMapping("/addProgramCourses")
    public ResponseEntity<VedantuResponse> addProgramCourses(@Valid OrgProgramCoursesReq addOrgProgramCoursesReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(programService.addProgramCourses(addOrgProgramCoursesReq));
    }

    @PostMapping("/removeProgramCourses")
    public ResponseEntity<VedantuResponse> removeProgramCourses(@Valid OrgProgramCoursesReq removeOrgProgramCoursesReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(programService.removeProgramCourses(removeOrgProgramCoursesReq));
    }

    @PostMapping("/getCoursePrograms")
    public ResponseEntity<VedantuResponse> getCoursePrograms(@Valid GetOrgCourseProgramsReq getOrgCourseProgramsReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(programService.getCoursePrograms(getOrgCourseProgramsReq));
    }

    @PostMapping("/updateSection")
    public ResponseEntity<VedantuResponse> updateSection(@Valid UpdateOrgSectionReq updateOrgSectionReq) throws VedantuException, MalformedURLException, FileNotFoundException {
        return ResponseEntity.ok(organizationServiceImpl.updateSection(updateOrgSectionReq));
    }

    @PostMapping("/removeSection")
    public ResponseEntity<VedantuResponse> removeSection(@Valid RemoveOrgSectionReq removeOrgSectionReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(organizationServiceImpl.removeSection(removeOrgSectionReq));
    }

    @PostMapping("/activateSection")
    public ResponseEntity<VedantuResponse> activateSection(@Valid ActivateOrgSectionReq activateOrgSectionReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(organizationServiceImpl.activateSection(activateOrgSectionReq));
    }

    @PostMapping("/getSectionByAccessCode")
    public ResponseEntity<VedantuResponse> getSectionByAccessCode(@Valid GetOrgSectionInfoByAccessCodeReq getOrgSectionInfoByAccessCodeReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(organizationServiceImpl.getSectionByAccessCode(getOrgSectionInfoByAccessCodeReq));
    }

    @PostMapping("/generateOrgSectionAccessCode")
    public ResponseEntity<VedantuResponse> generateOrgSectionAccessCode() throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(organizationServiceImpl.generateOrgSectionAccessCode());
    }
    @PostMapping("/updateSectionAccess")
    public ResponseEntity<VedantuResponse> updateSectionAccess(@Valid UpdateOrgSectionAccessReq updateOrgSectionAccessReq) throws VedantuException, MalformedURLException {

        return ResponseEntity.ok(organizationServiceImpl.updateOrgSectionAccessReq(updateOrgSectionAccessReq));
    }
    @PostMapping("/getSectionPackageInfo")
    public ResponseEntity<VedantuResponse> getSectionPackageInfo(@Valid GetOrgSectionReq getOrgSectionReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(organizationServiceImpl.getSectionPackageInfo(getOrgSectionReq));
    }
    @PostMapping("/updatePackageInfo")
    public ResponseEntity<VedantuResponse> updatePackageInfo(@Valid UpdatePackageInfoReq updatePackageInfoReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(organizationServiceImpl.updateORgPackageInfo(updatePackageInfoReq));
    }

    @PostMapping("/updateSectionMaxDiscount")
    public ResponseEntity<VedantuResponse> updateSectionMaxDiscount(@Valid UpdateMaxDiscountReq updateMaxDiscountReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(organizationServiceImpl.updateSectionMaxDiscount(updateMaxDiscountReq));
    }
    @PostMapping("/getLatestTNC")
    public ResponseEntity<VedantuResponse> getLatestTNC() throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(organizationServiceImpl.getLatestTNC());
    }
    @PostMapping("/getAssociatedOrgsOfUser")
    public ResponseEntity<VedantuResponse> getAssociatedOrgsOfUser(@Valid GetAssociatedOrgsReq getAssociatedOrgsReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(organizationServiceImpl.getAssociatedOrganizationsOfUser(getAssociatedOrgsReq));
    }
    @PostMapping("/generateAppCredentials")
    public ResponseEntity<VedantuResponse> generateAppCredentials(@Valid GenerateAppCredentialsReq generateAppCredentialsReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(organizationServiceImpl.generateAppCredes(generateAppCredentialsReq));
    }
    @PostMapping("/verifyAppCredentials")
    public ResponseEntity<VedantuResponse> verifyAppCredentials(@Valid VerifyAppCredentialsReq verifyAppCredentialsReq) throws VedantuException, MalformedURLException {
        return ResponseEntity.ok(organizationServiceImpl.verifyAppCreds(verifyAppCredentialsReq));
    }
}







