package com.lms.service.serviceImpl;

import com.lms.board.model.GranteeOrgProgram;
import com.lms.board.repo.GranteeOrgProgramRepo;
import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.fs.handlers.LocalFileSystemHandler;
import com.lms.common.fs.handlers.S3Handler;
import com.lms.common.utils.*;
import com.lms.common.validation.Validation;
import com.lms.common.vedantu.commons.pojos.requests.*;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelExtendedInfo;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.constants.EmailConfigurationConstants;
import com.lms.common.vedantu.constants.HardCodedConstants;
import com.lms.common.vedantu.constants.config.Configurations;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.entity.storage.FileCategory;
import com.lms.common.vedantu.entity.storage.OrganizationEntityFileStorage;
import com.lms.common.vedantu.entity.storage.StorageResult;
import com.lms.common.vedantu.enums.*;
import com.lms.common.vedantu.http.URLGenerator;
import com.lms.common.vedantu.mongo.FileMetaInfo;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.*;
import com.lms.models.*;
import com.lms.pojo.*;
import com.lms.pojo.request.*;
import com.lms.pojo.responce.*;
import com.lms.repository.*;
import com.lms.service.OrganizationService;
import com.lms.user.vedantu.user.enums.Gender;
import com.lms.user.vedantu.user.events.EmailVerificationDetails;
import com.lms.user.vedantu.user.model.User;
import com.lms.user.vedantu.user.model.UserSalt;
import com.lms.user.vedantu.user.pojo.*;
import com.lms.user.vedantu.user.pojo.responce.AddUserRes;
import com.lms.user.vedantu.user.repository.UserRepo;
import com.lms.user.vedantu.user.repository.UserSaltrepo;
import com.lms.user.vedantu.user.requests.AddUserReq;
import org.bson.internal.Base64;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class OrganizationsImpl implements OrganizationService {
    private static final Logger logger = LoggerFactory.getLogger(OrganizationsImpl.class);
    public static final String SUPER_ADMIN_MEMBER_ID = "SUPER_ADMIN";
    public static final int      NO_START       = 0;
    public static final int      NO_LIMIT       = 0;

    @Value("${org.tnc.version}")
    private  String TNC_VERSION;
    @Value("${check.referer.enabled}")
    private String checkReferEnable;
    @Value("${deployment.domain}")
    private String depolymentDomain;
    @Value("${instamojo.env}")
    private String instamoEnv;
    @Value("${instamojo.url}")
    private String instamoUrl;
    @Value("${instamojo.clientid}")
    private String instamoClientid;
    @Value("${instamojo.clientsecretkey}")
    private String instamoClientsecretkey;

    private static final String SYSTEM_SALT = "/vdntu/";
    @Autowired
    private OrganizationRepo organizationRepo;
    @Autowired
    private BoardMappingRepo boardMappingRepo;
    @Autowired
    private LicensingPlanRepo licensingPlanRepo;
    @Autowired
    private ImageDisplayURLUtil imageDisplayURLUtil;
    @Autowired
    private EncryptionUtils encryptionUtils;
    @Autowired
    private OrgSectionRepo orgSectionRepo;
    @Autowired
    private GranteeOrgProgramRepo granteeOrgProgramRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private PasswordUtils passwordUtils;

    @Autowired
    private UserSaltrepo userSaltrepo;
    public static final String UNKNOWN_DOB = "1970-01-01";

    @Autowired
    private GridFsOperations gridFsOperations;

    @Autowired
    private OrgMemberRepo orgMemberRepo;
    @Autowired
    private OrgCenterRepo orgCenterRepo;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private OrgProgramRepo orgProgramRepo;
    @Autowired
    private OrgDepartmentRepo orgDepartmentRepo;

    @Autowired
    private OrganizationEntityFileStorage picStorage;
    @Autowired
    private S3Handler s3Handler;
    @Autowired
    private MemberServiceImpl memberServiceImpl;
    @Autowired
    private LocalFileSystemHandler localFileSystemHandler;
    @Value("${bucket.name}")
    private String bucketName;
    @Autowired
    private EventUtil eventUtil;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public VedantuResponse getOrganization(GetOrgReq getOrgReq) throws VedantuException {

        if (getOrgReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        if (ObjectIdUtils.hasInvalidId(getOrgReq.getOrgId().trim())) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }
       GetOrgRes getOrgRes = null;
        getOrgRes = getorganization(getOrgReq);
        return new VedantuResponse(getOrgRes);

    }

    @Override
    public VedantuResponse getShowSharedSubjects(GetOrgReq getOrgReq) {

        if (getOrgReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        if (ObjectIdUtils.hasInvalidId(getOrgReq.orgId)) {
           throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }
        GetShowSharedSubjectsRes getOrgRes = null;

        getOrgRes = getShowSharedSubject(getOrgReq);

        return new VedantuResponse(getOrgRes);
    }

    @Override
    public VedantuResponse getOrganizationInfoForInvoice(GetOrgReq getOrgReq) throws VedantuException {

        if (getOrgReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        if (ObjectIdUtils.hasInvalidId(getOrgReq.getOrgId())) {
           throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }
        GetOrgResForInvoice getOrgRes = null;

        getOrgRes = getOrganizationInfoFormationInvoice(getOrgReq);

        return new VedantuResponse(getOrgRes);
    }

    @Override
    public VedantuResponse checkWebsite(CheckWebsiteReq checkWebsiteReq) throws VedantuException {

        if (checkWebsiteReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        CheckSlugRes checkOrgSlugRes = null;

            checkOrgSlugRes = getCheckWebsite(checkWebsiteReq);


            return new VedantuResponse(checkOrgSlugRes);
    }

    @Override
    public VedantuResponse checkSlug(CheckSlugReq checkSlugReq) throws VedantuException {

        if (checkSlugReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        CheckSlugRes checkOrgSlugRes = null;

            checkOrgSlugRes = getCheckSlug(checkSlugReq);


        return new VedantuResponse(checkOrgSlugRes);
    }

    @Override
    public VedantuResponse checkAppVersion(CheckAppVersionReq checkAppVersionReq) throws VedantuException {

        if (checkAppVersionReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        CheckAppVersionRes checkAppVersionRes = null;

        checkAppVersionRes = getcheckAppVersion(checkAppVersionReq);


        return new VedantuResponse(checkAppVersionRes);
    }

    @Override
    public VedantuResponse updateOrganizationSlug(UpdateOrgSlugReq updateOrgSlugReq) throws VedantuException {


        if (updateOrgSlugReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        GetOrgRes getOrgRes = updateSlugOfOrganization(updateOrgSlugReq);


        return new VedantuResponse(getOrgRes);


    }

    @Override
    public VedantuResponse updateOrganizationStatus(UpdateOrgStatusReq updateOrgStatusReq) throws VedantuException {
        if (updateOrgStatusReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        GetOrgRes getOrgRes = updateOrgStatus(updateOrgStatusReq);


        return new VedantuResponse(getOrgRes);
    }

    @Override
    public VedantuResponse updateOrganizationDownloadStatus(UpdateOrganizationDownloadStatusReq updateOrgStatusReq) {
        if (updateOrgStatusReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        GetOrgRes getOrgRes = updateOrgDownloadStatus(updateOrgStatusReq);

        return new VedantuResponse(getOrgRes);
    }

    @Override
    public VedantuResponse UpdateOrganizationSharedSubjects(UpdateOrganizationSharedSubjectsReq updateOrganizationSharedSubjectsReq) {


        if (updateOrganizationSharedSubjectsReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        GetOrgRes getOrgRes = updateOrgSharedSubjects(updateOrganizationSharedSubjectsReq);

        return new VedantuResponse(getOrgRes);
    }

    @Override
    public VedantuResponse updateOrganizationClassroomConnectStatus(UpdateOrganizationClassroomConnectStatusReq updateOrganizationClassroomConnectStatusReq) {
        if (updateOrganizationClassroomConnectStatusReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        GetOrgRes getOrgRes = updateOrgClassroomConnectStatus(updateOrganizationClassroomConnectStatusReq);

        return new VedantuResponse(getOrgRes);
    }

    @Override
    public VedantuResponse getOrganizationBySlug(GetOrgBySlugReq getOrgBySlugReq) throws VedantuException {
        if (getOrgBySlugReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        GetOrgRes getOrgRes = getOrgBySlug(getOrgBySlugReq);

        return new VedantuResponse(getOrgRes);
    }

    @Override
    public VedantuResponse updateOrganizationReferer(UpdateOrgRefererReq updateOrgRefererReq) throws VedantuException {
        if (updateOrgRefererReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        GetOrgRes getOrgRes = updateOrganizationReferrer(updateOrgRefererReq);


        return new VedantuResponse(getOrgRes);
    }

    @Override
    public VedantuResponse getOrganizationByReferer(boolean getKey,String referer) throws MalformedURLException, VedantuException {
        GetOrgRes getOrgRes = getOrgByReferer(referer, getKey);
        return new VedantuResponse(getOrgRes);
    }

    @Override
    public VedantuResponse checkReferer(CheckRefererReq checkRefererReq) throws VedantuException {
        if (checkRefererReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        CheckSlugRes checkOrgSlugRes = getCheckReferer(checkRefererReq);
        return new VedantuResponse(checkOrgSlugRes);
    }

    @Override
    public VedantuResponse getOrganizations(GetOrgsReq getOrgsReq) {
        if (getOrgsReq==null)
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        GetOrgsInfo getOrgsRes= getOrganizationsByOrgsReq(getOrgsReq);

        return new VedantuResponse(getOrgsRes);
    }

    @Override
    public VedantuResponse addOrganization(AddOrgReq addOrgReq) throws VedantuException {

        if (addOrgReq == null)
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        AddOrgRes addOrgRes = addOrganizationtoRepo(addOrgReq);

            return new VedantuResponse(addOrgRes);

    }

    @Override
    public VedantuResponse updateOrganization(UpdateOrgReq updateOrgReq) {
        if (updateOrgReq == null)
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        if (ObjectIdUtils.hasInvalidId(updateOrgReq.orgId)) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }
        UpdateOrgRes updateOrgRes = updateOrg(updateOrgReq);


        return new VedantuResponse(updateOrgRes);
    }

    @Override
    public VedantuResponse approveOrganization(ApproveOrgReq approveOrgReq) {
        if (approveOrgReq == null)  throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);

        if (ObjectIdUtils.hasInvalidId(approveOrgReq.getOrgId())) {
             throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }
        ApproveOrgRes apprOrgRes = organizationApproval(approveOrgReq);

        return new VedantuResponse(apprOrgRes);
    }
    public Organization approveOrganization(String orgId, String adminUserId,
                                            String adminOrgMemberId) throws VedantuException {

        logger.debug("approveOrganization orgId: " + orgId + ", adminUserId: " + adminUserId
                + ", adminOrgMemberId: " + adminOrgMemberId);

        Optional<Organization> organization = organizationRepo.findById(orgId);
        if (!organization.isPresent()) {
            logger.error("cannot approve organization as organization not found for _id: " + orgId);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
        }
        if (OrganizationStatus.APPROVED == organization.get().getStatus()) {
            logger.error("cannot approve organization as organization already approved for _id: "
                    + orgId);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_ALREADY_APPROVED);
        }
        if (organization.get().tncAcceptance != null) {
            organization.get().tncAcceptance.acceptedBy = adminUserId;
        }
        organization.get().adminUserId = adminUserId;
        organization.get().adminOrgMemberId = adminOrgMemberId;
        organization.get().status = OrganizationStatus.APPROVED;
        organization.get().studentPageStatus = OrganizationStatus.APPROVED;
        organizationRepo.save(organization.get());
        logger.info("approveOrganization approved orgId: " + orgId);

        return organization.get();

    }



    @Override
    public VedantuResponse getSharedOrgsByProgId(GetSharedOrgsReq getSharedOrgsReq) {

        if (getSharedOrgsReq == null)  throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);

        GetSharedOrgsRes getOrgsRes = getSharedOrgsByProg(getSharedOrgsReq);

        return new VedantuResponse(getOrgsRes);
    }

    @Override
    public VedantuResponse uploadOrgPic(MultipartFile file, UploadOrgPicReq request) throws IOException {
        validateFile(file);
        if (ObjectIdUtils.hasInvalidId(request.getOrgId().trim(), request.getOrgMemberId().trim())) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }
      
        FileMetaInfo fileDocument = new FileMetaInfo();
        fileDocument.setName(file.getOriginalFilename());
        fileDocument.setType(EntityType.FILE);
        request.inputFile = picStorage.convertMultiPartToFile(file);
        UploadOrgPicRes uploadOrgPicRes = uploadOrganizationPic(fileDocument,request);
        FileUtils.deleteFile(request.fileName, request.inputFile);
        
        logger.info("File uploaded successfully");
        return new VedantuResponse(uploadOrgPicRes);

    }

    @Override
    public VedantuResponse shareProgToOrg(GetSharedOrgsReq getSharedOrgsReq) {

        if (getSharedOrgsReq==null) {

            throw  new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        GetSharedOrgsRes getOrgsRes = sharedProgToOrganization(getSharedOrgsReq);

        return new VedantuResponse(getOrgsRes);

    }

    @Override
    public VedantuResponse removeSharedProgramFromOrg(RemoveProgramSharingReq removeProgramSharingReq) {
    
        if (removeProgramSharingReq==null) {
          
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
       
        RemoveProgramSharingRes removeSharingRes = removeSharedProgramFromOrganization(removeProgramSharingReq);
        return new VedantuResponse(removeSharingRes);
    }

    @Override
    public VedantuResponse acceptTnC(AcceptTncOrgReq acceptTncOrgReq) {

        if (acceptTncOrgReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        AcceptTnCRes acceptTnCRes = acceptTnCOrg(acceptTncOrgReq);

        return new VedantuResponse(acceptTnCRes);
    }

    @Override
    public VedantuResponse getSection(GetOrgSectionReq getOrgSectionReq) {
        if (getOrgSectionReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
       
        if (ObjectIdUtils.hasInvalidId(getOrgSectionReq.getOrgId().trim(), getOrgSectionReq.getSectionId().trim()
        )) {
           throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }
        GetOrgSectionRes getOrgSectionsRes = getProgramSection(getOrgSectionReq);
        

        return new VedantuResponse(getOrgSectionsRes);
        
    }

    @Override
    public VedantuResponse getSections(GetOrgSectionsReq getOrgSectionsReq) {
        if (getOrgSectionsReq==null) {
           throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        if (ObjectIdUtils.hasInvalidId(getOrgSectionsReq.getOrgId().trim(), getOrgSectionsReq.getProgramId().trim(),
                getOrgSectionsReq.getCenterId().trim())) {
          throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }
        GetOrgSectionsRes getOrgSectionsRes = getProgramSections(getOrgSectionsReq);


        return new VedantuResponse(getOrgSectionsRes);
    }

    @Override
    public VedantuResponse addSection(AddOrgSectionReq addOrgSectionReq) throws FileNotFoundException {
      
        if (addOrgSectionReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        if (ObjectIdUtils.hasInvalidId(addOrgSectionReq.getOrgId(), addOrgSectionReq.getProgramId(),
                addOrgSectionReq.getCenterId())) {
           throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }
        AddOrgSectionRes addOrgSectionsRes = addProgramSection(addOrgSectionReq);
   

        return new VedantuResponse(addOrgSectionsRes); 
        
        
        
        
        
    }

    @Override
    public VedantuResponse updateSection(UpdateOrgSectionReq updateOrgSectionReq) throws FileNotFoundException {
        if (updateOrgSectionReq==null) {
           throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
       
        if (ObjectIdUtils.hasInvalidId(updateOrgSectionReq.getOrgId(), updateOrgSectionReq.getProgramId(),
                updateOrgSectionReq.getSectionId())) {
           throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }
        UpdateOrgSectionRes updateOrgSectionRes = updateOrgSection(updateOrgSectionReq);
        

        return new VedantuResponse(updateOrgSectionRes);
    }

    @Override
    public VedantuResponse removeSection(RemoveOrgSectionReq removeOrgSectionReq) {
        if (removeOrgSectionReq==null) {
           throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        if (ObjectIdUtils.hasInvalidId(removeOrgSectionReq.getOrgId().trim(), removeOrgSectionReq.getSectionId().trim())) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }
        RemoveOrgSectionRes removeOrgSectionsRes = removeProgramSection(removeOrgSectionReq);
       
        return new VedantuResponse(removeOrgSectionsRes);
    
    }

    @Override
    public VedantuResponse activateSection(ActivateOrgSectionReq activateOrgSectionReq) {
        if (activateOrgSectionReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
      
        if (ObjectIdUtils
                .hasInvalidId(activateOrgSectionReq.getOrgId().trim(), activateOrgSectionReq.getSectionId().trim())) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID);
                   
        }
        ActivateOrgSectionRes activateOrgSectionsRes = activateOrgSection(activateOrgSectionReq);
       

        return new VedantuResponse(activateOrgSectionsRes);   
    }

    @Override
    public VedantuResponse getSectionByAccessCode(GetOrgSectionInfoByAccessCodeReq req) {
        if (req==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        GetOrgSectionInfoByAccessCodeRes res = getOrgSectionInfoByAccessCode(req);

        return new VedantuResponse(res);
        }

    @Override
    public VedantuResponse generateOrgSectionAccessCode() {
        List<OrgSection> orgSections = orgSectionRepo.findAll();
        for (OrgSection orgSection : orgSections) {
            orgSection.setAccessCode(UniqueCodeUtils.generateUniqueCode(EntityType.SECTION.name()));
          //  OrgSectionDAO.INSTANCE.save(orgSection);
            orgSectionRepo.save(orgSection);
        }
        return new VedantuResponse(orgSections.size());
    }

    @Override
    public VedantuResponse updateOrgSectionAccessReq(UpdateOrgSectionAccessReq updateOrgSectionAccessReq) {
        if (updateOrgSectionAccessReq==null) {
         throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        UpdateOrgSectionAccessRes updateOrgSectionAccessRes = updateSectionAccess(updateOrgSectionAccessReq);

        return new VedantuResponse(updateOrgSectionAccessRes);
    }

    @Override
    public VedantuResponse getSectionPackageInfo(GetOrgSectionReq getOrgSectionReq) {
        if (getOrgSectionReq==null) {
           throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        if (ObjectIdUtils.hasInvalidId(getOrgSectionReq.getOrgId().trim(), getOrgSectionReq.getSectionId().trim())) {
         throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }
        GetOrgSectionRes getOrgSectionsRes = getOrgSectionPackageInfo(getOrgSectionReq);

        return new VedantuResponse(getOrgSectionsRes);
    }

    @Override
    public VedantuResponse updateORgPackageInfo(UpdatePackageInfoReq updatePackageInfoReq) {
        if (updatePackageInfoReq==null)
           throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        if(updatePackageInfoReq.getPackagesList().isEmpty()){
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,"packagesList should not be null");
        }
        UpdatePackageInfoRes res = updateOrgPackageInfo(updatePackageInfoReq);

        return new VedantuResponse(res);
    }

    @Override
    public VedantuResponse updateSectionMaxDiscount(UpdateMaxDiscountReq req) {
        if (req==null)
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        UpdateMaxDiscountRes res = updateMaxDiscount(req);
        return new VedantuResponse(res);
    }

    @Override
    public VedantuResponse getLatestTNC() {
        GetLatestTncRes response = new GetLatestTncRes();
        response.setTncVersion(TNC_VERSION);
        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse getAssociatedOrganizationsOfUser(GetAssociatedOrgsReq getAssociatedOrgsReq) {
        if (getAssociatedOrgsReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        GetAssociatedOrgsRes getAssociatedOrgsRes = getAssociatedOrgsOfUser(getAssociatedOrgsReq);
        return new VedantuResponse(getAssociatedOrgsRes);
    }

    @Override
    public VedantuResponse generateAppCredes(GenerateAppCredentialsReq generateAppCredentialsReq) {
        if (generateAppCredentialsReq==null)
           throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);

        GenerateAppCredentialsRes res = generateAppCredentials(generateAppCredentialsReq);

        return new VedantuResponse(res);
    }

    @Override
    public VedantuResponse verifyAppCreds(VerifyAppCredentialsReq verifyAppCredentialsReq) {

        if (verifyAppCredentialsReq==null)
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        VerifyAppCredentialsRes res = generateAppCredentialsForVerify(verifyAppCredentialsReq);

        return new VedantuResponse(res);
    }

    private VerifyAppCredentialsRes generateAppCredentialsForVerify(VerifyAppCredentialsReq verifyAppCredentialsReq) {

        Organization org = getOrganizationById(verifyAppCredentialsReq.getOrgId());
        AppSecurityCredentials appSecurityCredentials = org.__addOrGetAppCredentials(verifyAppCredentialsReq.getAppId(),
                new AtomicBoolean(false));

        VerifyAppCredentialsRes res = new VerifyAppCredentialsRes();
        res.setValid(appSecurityCredentials != null
                && appSecurityCredentials.getAuthToken().equals(verifyAppCredentialsReq.getAuthToken())
                && appSecurityCredentials.getSecretKey().equals(verifyAppCredentialsReq.getSecretKey()));
        return res;
    }

    private GenerateAppCredentialsRes generateAppCredentials(GenerateAppCredentialsReq generateAppCredentialsReq) {
        Organization org = getOrganizationById(generateAppCredentialsReq.getOrgId());
        AtomicBoolean update = new AtomicBoolean(false);
        AppSecurityCredentials appSecurityCredentials = org.__addOrGetAppCredentials(generateAppCredentialsReq.getAppId(),
                update);
        if (update.get()) {
            org.appCredentials.add(appSecurityCredentials);
            organizationRepo.save(org);
        }

        return new GenerateAppCredentialsRes(appSecurityCredentials.getAppId(),
                appSecurityCredentials.getAuthToken(), appSecurityCredentials.getSecretKey());
    }

    public Organization getOrganizationById(String orgId) {

        Optional<Organization> organization = organizationRepo.findById(orgId);

            if (!organization.isPresent()) {
                logger.error("cannot find organization for id: " + orgId);
                throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND,
                        "no org found with id: " + orgId);
            }

            return organization.get();

    }

    private GetAssociatedOrgsRes getAssociatedOrgsOfUser(GetAssociatedOrgsReq getAssociatedOrgsReq) {

        List<OrgMember> memberships = getAssociatedOrgs(getAssociatedOrgsReq.getUserId());
        GetAssociatedOrgsRes getAssociatedOrgsRes = new GetAssociatedOrgsRes();
        if (CollectionUtils.isEmpty(memberships)) {
            return getAssociatedOrgsRes;
        }
        Map<String, Organization> orgs = new HashMap<String, Organization>();
        for (OrgMember membership : memberships) {
            orgs.put(membership.getOrgId(), null);
        }
        List<ObjectId> orgIds = ObjectIdUtils.toObjectIds(new ArrayList<String>(orgs.keySet()));
        if (CollectionUtils.isEmpty(orgIds)) {
            return getAssociatedOrgsRes;
        }
        List<Organization> organizations = organizationRepo.findAllByIdIn(orgIds);

        if (CollectionUtils.isEmpty(organizations)) {
            return getAssociatedOrgsRes;
        }
        for (Organization organization : organizations) {
            if (organization==null) {
                continue;
            }
            orgs.put(organization._getStringId(), organization);
        }
        for (OrgMember membership : memberships) {
            Organization org = orgs.get(membership.getOrgId());
            if (null == org) {
                continue;
            }

            long currentTime = new Date().getTime();
            // OrgMemberState userState = OrgMemberState.ACTIVE;
            // if(membership._getMemberState(currentTime) == OrgMemberState.BLOCKED)
            // {
            // userState = OrgMemberState.BLOCKED;
            // }

            OrgAssociatedInfo info = new OrgAssociatedInfo(membership.getOrgId(), org.getName(),
                    org.getFullName(), org.getType(), org.getScope(), org.getStatus(), org.getThumbnail(),
                    membership.getTimeCreated(), membership.getLastUpdated(), membership.getRecordState(),
                    membership._getStringId(), membership.getMemberId(), membership.getFirstName(),
                    membership.getLastName(), membership.getProfile(), membership.getThumbnail(),
                    membership._getMemberState(currentTime), org.getAuthType(), org.getReferer(), org.getSlug(), org.isShowClassroomConnect());
            if(membership.profile == OrgMemberProfile.STUDENT){
                if(org.getStudentPageStatus() != null){
                    if(org.getStudentPageStatus() == OrganizationStatus.APPROVED){
                        info.setOrgStudentPageStatus(OrgMemberState.ACTIVE);
                    }else{
                        info.setOrgStudentPageStatus(OrgMemberState.BLOCKED);
                    }
                }
                else{
                    info.setOrgStudentPageStatus(OrgMemberState.ACTIVE);
                }
            }else{
                if(org.getStatus() == OrganizationStatus.APPROVED){
                    info.setOrgStudentPageStatus(OrgMemberState.ACTIVE);
                }else{
                    info.setOrgStudentPageStatus(OrgMemberState.ACTIVE);
                }
            }
//            info.orgStudentPageStatus = membership.profile == OrgMemberProfile.STUDENT ? (org.studentPageStatus == OrganizationStatus.APPROVED ? OrgMemberState.ACTIVE: OrgMemberState.BLOCKED):();
            getAssociatedOrgsRes.getList().add(info);
            getAssociatedOrgsRes.totalHits++;
        }

        return getAssociatedOrgsRes;
    }

    private List<OrgMember> getAssociatedOrgs(String userId) {

        logger.debug("getAssociatedOrgs userId: " + userId);

        OrgMember orgMembers1 = orgMemberRepo.findByUserId(userId);
        List<OrgMember> orgMembers =new ArrayList<>();
       orgMembers.add(orgMembers1);
       return  orgMembers;
    }

    private UpdateMaxDiscountRes updateMaxDiscount(UpdateMaxDiscountReq req) {
        Optional<OrgSection> orgSection = orgSectionRepo.findById(req.getSectionId());
        if (!orgSection.isPresent()) {
            logger.error("updateMaxDiscount::cannot find orgSection for _id: " + req.getSectionId());
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_PROGRAM_SECTION_NOT_FOUND);
        }
        if (req.getMaxDiscount() < 0 || req.getMaxDiscount() >= 100) {
            throw new VedantuException(VedantuErrorCode.INVALID_INPUT_DATA);
        }
        orgSection.get().setMaxDiscount(req.getMaxDiscount());
        orgSectionRepo.save(orgSection.get());
        UpdateMaxDiscountRes res = new UpdateMaxDiscountRes();
        res.setEdited(true);
        return res;
    }

    private UpdatePackageInfoRes updateOrgPackageInfo(UpdatePackageInfoReq req) throws VedantuException {

            OrgSection orgSection = updatePackageInfo(req.getOrgId(),
                    req.getSectionId(), req.getPackagesList());
            UpdatePackageInfoRes res = new UpdatePackageInfoRes();
            res.edited = true;
            return res;
    }

    private OrgSection updatePackageInfo(String orgId, String sectionId,
                                         List<PackageInfo> packagesList) throws VedantuException {
        Optional<OrgSection> orgSection = orgSectionRepo.findById(sectionId);

        if (!orgSection.isPresent()) {
            logger.error("updatePackageInfo::cannot find orgSection for _id: " + sectionId);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_PROGRAM_SECTION_NOT_FOUND);
        }
        Map<String, List<PackageInfo>> packagesMap = orgSection.get().getPackagesMap();
        if (packagesMap == null) {
            packagesMap = new HashMap<String, List<PackageInfo>>();
        }
        List<PackageInfo> packages = new ArrayList<PackageInfo>();
        for (PackageInfo packageInfo : packagesList) {
            if (packageInfo.getNumDays() <= 0 || packageInfo.getCostRate().getValue() <= 0) {
                throw new VedantuException(VedantuErrorCode.INVALID_PACKAGES_PRICING);
            }
            PackageInfo pInfo = new PackageInfo(packageInfo.numDays, packageInfo.costRate);
            packages.add(pInfo);
        }
        updateSavingsTxt(packages);
        Collections.sort(packages);
        if (!packages.isEmpty()) {
            packagesMap.put(orgId, packages);
        } else {
            packagesMap.remove(orgId);
        }
        orgSection.get().setPackagesMap(packagesMap);

        // Update startingRates by iterating over packages.
        Map<String, CostRate> startingRates = orgSection.get().getStartingRates();
        if (startingRates == null) {
            startingRates = new HashMap<String, CostRate>();
        }
        startingRates.remove(orgId);
        if (!packages.isEmpty()) {
            CostRate minCostRate = packages.get(0).costRate;
            for (PackageInfo packageInfo : packages) {
                if (packageInfo.getCostRate().getValue() < minCostRate.getValue()) {
                    minCostRate = packageInfo.costRate;
                }
            }
            startingRates.put(orgId, minCostRate);
        }
        orgSection.get().setStartingRates(startingRates);
        orgSectionRepo.save(orgSection.get());
        return orgSection.get();
    }

    public void updateSavingsTxt(List<PackageInfo> packages) {
        boolean monthlyPkgFound = false;
        CostRate monthlyRate = null;
        for (PackageInfo pkgInfo : packages) {
            if (pkgInfo.numDays == 30) {
                monthlyPkgFound = true;
                monthlyRate = pkgInfo.costRate;
                break;
            }
        }

        if (!monthlyPkgFound) return;
        for (PackageInfo pkgInfo : packages) {
            if (pkgInfo.numDays > 30) {
                double discountPercentage = calculateDiscountPercentage(pkgInfo, monthlyRate);
                if (discountPercentage >= 1.0) {
                    pkgInfo.savingsTxt = "SAVE " + Math.round(discountPercentage) + "%";
                }
            }
        }
    }
    private double calculateDiscountPercentage(PackageInfo pkgInfo, CostRate monthlyRate) {
        if (monthlyRate == null || monthlyRate.getValue() == 0) return 0;
        int days = pkgInfo.numDays;
        int months = 0;
        if (days % 365 == 0) {
            months = (days / 365) * 12;
        } else if (days % 30 == 0) {
            months = (days / 30);
        }
        if (months == 0) return 0;

        int normalRate = months * monthlyRate.getValue();
        int actualRate = pkgInfo.getCostRate().getValue();
        if (actualRate >= normalRate) return 0;
        int discount = normalRate - actualRate;
        double discountPercentage = (discount * 100.0) / normalRate;
        return discountPercentage;
    }

    private GetOrgSectionRes getOrgSectionPackageInfo(GetOrgSectionReq request) {
        OrgMember member = getMemberByUserId(request.getOrgId(), request.getUserId());
        if (member == null) {
            throw new VedantuException(
                    VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }

        Optional<OrgSection> section = orgSectionRepo.findById(request.getSectionId());
        if (!section.isPresent()) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_PROGRAM_SECTION_NOT_FOUND);
        }
        OrgProgramSectionBasicInfo sectionInfo = new OrgProgramSectionBasicInfo(
                section.get()._getStringId(), section.get().getRecordState(), section.get().getName(),
                section.get().getCode(), EntityType.SECTION);
        sectionInfo.addSectionExtraInfo(section.get());
        GetOrgSectionRes response = new GetOrgSectionRes();
        response.setInfo(sectionInfo);
        return response;
    }

    private UpdateOrgSectionAccessRes updateSectionAccess(UpdateOrgSectionAccessReq updateOrgSectionAccessReq) {

        UpdateOrgSectionAccessRes updateOrgSectionAccessRes = new UpdateOrgSectionAccessRes();

        if (updateOrgSectionAccessReq.getSectionAccessInfos()==null) {
            throw new VedantuException(VedantuErrorCode.SECTION_LIST_NOT_SPECIFIED);
        }

        logger.debug("......section array is not null.......");

        for (OrgSectionAccessInfo sectionAccessInfo : updateOrgSectionAccessReq.getSectionAccessInfos()) {
            sectionAccessInfo.validate();
        }

        logger.debug("........section array is valid...........");

        for (OrgSectionAccessInfo sectionAccessInfo : updateOrgSectionAccessReq.getSectionAccessInfos())
        {
            updateSectionAccess(sectionAccessInfo.getId(),
                    sectionAccessInfo.getAccessScope(), sectionAccessInfo.getRevenueModel(),
                    sectionAccessInfo.getCostRate());
        }
        updateOrgSectionAccessRes.setEdited(true);
        return updateOrgSectionAccessRes;




    }
    public OrgSection updateSectionAccess(String id, AccessScope accessScope,
                                          RevenueModel revenueModel, CostRate costRate) throws VedantuException {

        Optional<OrgSection> orgSection = orgSectionRepo.findById(id);
        if (!orgSection.isPresent()) {
            return null;
        }
        orgSection.get().setAccessScope(accessScope);

        orgSection.get().setRevenueModel(revenueModel);

        orgSection.get().setCostRate(costRate);

        orgSectionRepo.save(orgSection.get());

        return orgSection.get();
    }

    private GetOrgSectionInfoByAccessCodeRes getOrgSectionInfoByAccessCode(GetOrgSectionInfoByAccessCodeReq req)throws VedantuException {

        GetOrgSectionInfoByAccessCodeRes res = new GetOrgSectionInfoByAccessCodeRes();
        OrgSection orgSection = getOrgSectionByAccessCode(req.getAccessCode());
        res.setSection((OrgStructureBasicInfo) orgSection.toBasicInfo());

        Optional<OrgProgram> orgProgram = orgProgramRepo.findById(orgSection.getProgramId());
        if (!orgProgram.isPresent()) {
            logger.error("orgProgram not found for the accessCode: " + req.accessCode);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_PROGRAM_NOT_FOUND);
        }

        res.program = (OrgProgramBasicInfo) toBasicInfo(orgProgram.get());

        GetOrgReq getOrgReq = new GetOrgReq();
        getOrgReq.setOrgId(orgSection.orgId);
        getOrgReq.setGetKey(req.getOrgKey);

        res.setOrg(getorganization(getOrgReq));

        Optional<OrgCenter> orgCenter = orgCenterRepo.findById(orgSection.getCenterId());

        if (!orgCenter.isPresent()) {
            logger.error("orgCenter not found for the access: " + req.accessCode);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_CENTER_NOT_FOUND);
        }

        res.setCenter((OrgStructureBasicInfo) orgCenter.get().toBasicInfo());

        return res;
    }
    public ModelBasicInfo toBasicInfo(OrgProgram orgProgram) {

        Optional<OrgDepartment> department = orgDepartmentRepo.findById(orgProgram.getDepartmentId().trim());
        if(!department.isPresent())
            throw new VedantuException(VedantuErrorCode.INVALID_ACCESS_CODE,"Department is not found");

        return new OrgProgramBasicInfo(orgProgram._getStringId(), orgProgram.getRecordState(), orgProgram.getcName(), orgProgram.getCode(),
                orgProgram._getEntityType(), orgProgram.getDepartmentId(), department.get().getName(), department.get().getCode(), orgProgram.getCourseIds(), orgProgram.isOffline);
    }

    private OrgSection getOrgSectionByAccessCode(String accessCode) {
        OrgSection orgSection = orgSectionRepo.findByAccessCodeAndRecordState(accessCode,VedantuRecordState.ACTIVE);
        if (orgSection == null) {
            logger.error("cannot find orgSection for accessCode: " + accessCode);
            throw new VedantuException(VedantuErrorCode.INVALID_ACCESS_CODE, "invalid access code");
        }
        return orgSection;
    }

    private ActivateOrgSectionRes activateOrgSection(ActivateOrgSectionReq activateOrgSectionReq) {

        OrgSection orgSection = getSectionById(activateOrgSectionReq.getOrgId(), activateOrgSectionReq.getSectionId());

            orgSection.setRecordState(VedantuRecordState.ACTIVE);
            orgSectionRepo.save(orgSection);
            ActivateOrgSectionRes activateOrgSectionRes = new ActivateOrgSectionRes();
        activateOrgSectionRes.setId(orgSection._getStringId());
        activateOrgSectionRes.setRecordState(orgSection.getRecordState());

        return activateOrgSectionRes;
    }


    private RemoveOrgSectionRes removeProgramSection(RemoveOrgSectionReq removeOrgSectionReq) {
        

            OrgSection orgSection =getSectionById(removeOrgSectionReq.getOrgId(), removeOrgSectionReq.getSectionId());

            OrgProgram program = getProgramById(Arrays.asList(removeOrgSectionReq.getOrgId()), orgSection.getProgramId());
            logger.debug("found program: " + program._getStringId());

            RemoveOrgSectionRes removeOrgSectionRes = removeOrgSection(removeOrgSectionReq);

            boolean result = removeCenterSection(
                    removeOrgSectionReq.getOrgId(), orgSection.getProgramId(),
                    orgSection.getCenterId(), removeOrgSectionReq.getSectionId());
            logger.debug("section removal result: " + result);

            return removeOrgSectionRes;
        
    }

    private boolean removeCenterSection(String orgId, String programId, String centerId, String sectionId) {


            logger.debug("removeCenterSection orgId: " + orgId + ", programId: "
                    + programId + ", centerId: " + centerId + ", sectionId: "
                    + sectionId);

            OrgProgram orgProgram = getProgramById(Arrays.asList(orgId), programId);
            OrgProgramCenterSections centerSections = orgProgram
                    ._getOrgProgramCenterSections(centerId);
            if (null == centerSections) {
                logger.debug("removeCenterSection no center found in programId: "
                        + programId + " for centerId: " + centerId);
                return false;
            }
            if (centerSections.getSectionIds().contains(sectionId)) {
                centerSections.getSectionIds().remove(sectionId);
                orgProgramRepo.save(orgProgram);
                logger.info("removeCenterSection removed section from programId: "
                        + programId + " for sectionId: " + sectionId);
                return true;
            }

            logger.info("removeCenterSection section not found in programId: "
                    + programId + " for sectionId: " + sectionId);
            return false;

    }

    private RemoveOrgSectionRes removeOrgSection(RemoveOrgSectionReq removeOrgSectionReq) {

        OrgSection orgSection = getSectionById(removeOrgSectionReq.getOrgId(), removeOrgSectionReq.getSectionId());
        orgSection.setRecordState(VedantuRecordState.DELETED);
        orgSectionRepo.save(orgSection);
        RemoveOrgSectionRes removeOrgSectionRes = new RemoveOrgSectionRes();
        removeOrgSectionRes.setId(orgSection._getStringId());
        removeOrgSectionRes.setRecordState(orgSection.getRecordState());

        return removeOrgSectionRes;
    }

    private UpdateOrgSectionRes updateOrgSection(UpdateOrgSectionReq updateOrgSectionReq) throws FileNotFoundException {
       if (updateOrgSectionReq.thumbnail != null && !updateOrgSectionReq.thumbnail.isEmpty()) {
            if (!updateOrgSectionReq.thumbnail.contains("https://s3.amazonaws.com/")) {
                updateOrgSectionReq.thumbnail = getAWSFileUrl(updateOrgSectionReq.imageNameWithExtension);
            }
        }

        OrgSection orgSection = updateOrganizationSection(updateOrgSectionReq.getOrgId(),
                updateOrgSectionReq.getSectionId(), updateOrgSectionReq.getCode(), updateOrgSectionReq.getName(),
                updateOrgSectionReq.getProgramId(), updateOrgSectionReq.getAccessScope(),
                updateOrgSectionReq.getRevenueModel(), updateOrgSectionReq.getDesc(),
                updateOrgSectionReq.getSdOnly(), updateOrgSectionReq.getDescriptionPoints(),
                updateOrgSectionReq.getThumbnail());

        UpdateOrgSectionRes updateOrgSectionRes = new UpdateOrgSectionRes();
        updateOrgSectionRes.setRecordState(orgSection.getRecordState());
        updateOrgSectionRes.setEdited(true);
        return updateOrgSectionRes;
    }

    private OrgSection updateOrganizationSection(String orgId, String sectionId, String code, String name, String programId, AccessScope accessScope, RevenueModel revenueModel, String desc, Boolean sdOnly, List<String> descriptionPoints, String thumbnail) {

            OrgSection orgSection = getSectionById(orgId, sectionId);

                if (!orgSection.getProgramId().equals(programId)) {
                    logger.error("mismatch in programId for section _id: " + sectionId
                            + ", expected programId: " + orgSection.programId + ", found programId: "
                            + programId);
                    throw new VedantuException(VedantuErrorCode.INVALID_ID);
                }
                orgSection.setCode(code);
                orgSection.setName(name);
                if (accessScope != null) {
                    orgSection.setAccessScope(accessScope);
                }
                if (revenueModel != null) {
                    orgSection.setRevenueModel(revenueModel);
                }
                if (!StringUtils.isEmpty(desc)) {
                    orgSection.desc = desc;
                }
                if (sdOnly != null) {
                    orgSection.extSupported = sdOnly.booleanValue();
                }
                if(descriptionPoints != null && !descriptionPoints.isEmpty()){
                    orgSection.descriptionPoints = descriptionPoints;
                }
                if(thumbnail != null && !thumbnail.isEmpty()){
                    orgSection.thumbnail = thumbnail;
                }
              orgSectionRepo.save(orgSection);


            return orgSection;





    }
    public OrgSection getSectionById(String orgId, String sectionId) throws VedantuException {

        Optional<OrgSection> orgSection = orgSectionRepo.findById(sectionId);
        if ( !orgSection.isPresent()) {
            logger.error("cannot find orgSection for _id: " + sectionId);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_PROGRAM_SECTION_NOT_FOUND);
        }
        if (!orgSection.get().getOrgId().equals(orgId)) {
            logger.error("mismatch in orgId for section _id: " + sectionId + ", expected orgId: "
                    + orgSection.get().getOrgId() + ", found orgId: " + orgId);
            throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }
        return orgSection.get();
    }

    private AddOrgSectionRes addProgramSection(AddOrgSectionReq addOrgSectionReq) throws FileNotFoundException {
        
           logger.info("......entering addSection function......");
            OrgProgram program = getProgramById(Arrays.asList(addOrgSectionReq.getOrgId()), addOrgSectionReq.programId);
            logger.debug("found program: " + program._getStringId());

            OrgCenter center = getCenterById(
                    addOrgSectionReq.getOrgId(), addOrgSectionReq.getCenterId());
            logger.debug("found center: " + center._getStringId());

            OrgProgramCenterSections c = program
                    ._getOrgProgramCenterSections(addOrgSectionReq.centerId);
            if (null == c) {
                logger.debug("could not find center for adding section orgId: "
                        + addOrgSectionReq.getOrgId() + ", programId: "
                        + addOrgSectionReq.getProgramId() + ", centerId: "
                        + addOrgSectionReq.getCenterId());
                throw new VedantuException(
                        VedantuErrorCode.ORGANIZATION_PROGRAM_CENTER_NOT_FOUND);
            }

            final boolean returnExisting = false;
            AddOrgSectionRes addOrgSectionRes = addOrgSection(addOrgSectionReq, returnExisting);

            boolean result = addCenterSection(
                    addOrgSectionReq.orgId, addOrgSectionReq.programId,
                    addOrgSectionReq.centerId, addOrgSectionRes.id);
            logger.debug("section addition result: " + result);

            return addOrgSectionRes;

        
    }
    public boolean addCenterSection(String orgId, String programId,
                                    String centerId, String sectionId) throws VedantuException {

        logger.debug("addCenterSection orgId: " + orgId + ", programId: "
                + programId + ", centerId: " + centerId + ", sectionId: "
                + sectionId);

        OrgProgram orgProgram = getProgramById(Arrays.asList(orgId), programId);
        OrgProgramCenterSections centerSections = orgProgram
                ._getOrgProgramCenterSections(centerId);

            if (null == centerSections) {
                centerSections = new OrgProgramCenterSections(centerId);
                orgProgram.centersSections.add(centerSections);
                logger.debug("addCenterSection added center to programId: "
                        + programId + "for centerId: " + centerId);
            }
            if (centerSections.sectionIds.contains(sectionId)) {
                logger.debug("addCenterSection program already contains sectionId: "
                        + sectionId);
                return false;
            }
            centerSections.sectionIds.add(sectionId);
            orgProgramRepo.save(orgProgram);

            logger.info("addCenterSection added sectionId: " + sectionId);

        return true;
    }

    private AddOrgSectionRes addOrgSection(AddOrgSectionReq addOrgSectionReq, boolean returnExisting) throws FileNotFoundException {
        logger.debug("request orgId" + addOrgSectionReq.orgId);
        if (addOrgSectionReq.thumbnail != null && !addOrgSectionReq.thumbnail.isEmpty()){
            addOrgSectionReq.thumbnail = getAWSFileUrl(addOrgSectionReq.imageNameWithExtension);
        }

        OrgSection orgSection =  addSection(addOrgSectionReq.orgId,
                addOrgSectionReq.code, addOrgSectionReq.name, addOrgSectionReq.desc,
                addOrgSectionReq.programId, addOrgSectionReq.centerId, returnExisting,
                addOrgSectionReq.descriptionPoints, addOrgSectionReq.thumbnail);

        AddOrgSectionRes addOrgSectionRes = new AddOrgSectionRes();
        addOrgSectionRes.id = orgSection._getStringId();
        addOrgSectionRes.recordState = orgSection.recordState;

        return addOrgSectionRes;

    }
    public OrgSection addSection(String orgId, String code, String name, String desc,
                                 String programId, String centerId, boolean returnExisting, List<String> descriptionPoints, String thumbnail) throws VedantuException {

        logger.debug("......entering addSection function......");
        OrgSection orgSection = orgSectionRepo.findByOrgIdAndCode(orgId,code);
        if (null != orgSection) {
            if (returnExisting) {
                logger.debug("section already exists and will return the same for orgId: " + orgId
                        + ", code: " + code);
                return orgSection;
            }

            if (VedantuRecordState.ACTIVE == orgSection.recordState) {
                logger.error("cannot add orgSection as orgSection already exists for orgId: "
                        + orgId + ", programId: " + programId + ", centerId: " + centerId
                        + ", code: " + code);
                throw new VedantuException(
                        VedantuErrorCode.ORGANIZATION_PROGRAM_SECTION_ALREADY_EXISTS,
                        "a section with code:" + code + " already exists for orgId:" + orgId);
            } else {
                logger.error("changing orgSection recordState for orgId: " + orgId + ", code: "
                        + code + ", _id: " + orgSection._getStringId() + ", from: "
                        + orgSection.recordState + ", to: " + VedantuRecordState.ACTIVE);
                orgSection.setName(name);
                orgSection.setRecordState(VedantuRecordState.ACTIVE);
                orgSectionRepo.save(orgSection);
                return orgSection;
            }
        }

        orgSection = new OrgSection(orgId, code, name, desc, programId, centerId, descriptionPoints, thumbnail);
        orgSectionRepo.save(orgSection);

        return orgSection;
    }

    private String getAWSFileUrl(String imageNameWithExtension) throws FileNotFoundException {
       // LocalFileSystemHandler localFileSystemHandler =
                localFileSystemHandler.localFileSystemHandlerTempDirectory(true);
                //FileSystemFactory.INSTANCE.getTempFS();
        String filePath = localFileSystemHandler.getFilePath("organization", imageNameWithExtension);
        File file = new File(filePath);
        // move to s3 public bucket and get s3 url

            s3Handler.store(file, bucketName,
                    imageNameWithExtension, new HashMap<String, String>());

        FileUtils.deleteFile(imageNameWithExtension, file);
        String AWSFileUrl = "https://"+bucketName+".s3.amazonaws.com/"
                + imageNameWithExtension;
        return AWSFileUrl;
    }

    private OrgProgram getProgramById(List<String> asList, String programId) {
        Optional<OrgProgram> orgProgram = orgProgramRepo.findById(programId);
        if (!orgProgram.isPresent()) {
            String errorMsg = "cannot find orgProgram for _id: " + programId;
            logger.error(errorMsg);
            throw new VedantuException(
                    VedantuErrorCode.ORGANIZATION_PROGRAM_NOT_FOUND, errorMsg);
        }
        logger.info("getProgramById orgProgram: " + orgProgram);

        return orgProgram.get();
    }


    private GetOrgSectionsRes getProgramSections(GetOrgSectionsReq getOrgSectionsReq) {
        logger.debug("......entering getProgramSections function......");

            AtomicLong totalProgramHits = new AtomicLong(0L);
            List<GranteeOrgProgram> granteeOrgPrograms = getGranteeOrgPrograms(getOrgSectionsReq.getOrgId(), null, totalProgramHits);
            List<String> allOrgIds = new ArrayList<String>();
            logger.debug("OrgProgramManager getPrograms"+granteeOrgPrograms+" size="+granteeOrgPrograms.size());

            for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
                allOrgIds.add(granteeOrgProgram.getProviderOrgId());
            }
            allOrgIds.add(getOrgSectionsReq.orgId);


            Optional<OrgProgram> program = orgProgramRepo.findById(getOrgSectionsReq.getProgramId());
                  if(!program.isPresent()) {
                      String errorMsg = "cannot find orgProgram for _id: " + getOrgSectionsReq.getProgramId();
                      logger.error(errorMsg);
                      throw new VedantuException(
                              VedantuErrorCode.ORGANIZATION_PROGRAM_NOT_FOUND, errorMsg);
                  }
           logger.debug("found program: " + program.get()._getStringId());

            GetOrgSectionsRes getOrgSectionsRes = new GetOrgSectionsRes();

            OrgProgramCenterSections c = program.get()
                    ._getOrgProgramCenterSections(getOrgSectionsReq.centerId);

            if (c==null) {
                logger.debug("could not find center for orgId: "
                        + getOrgSectionsReq.orgId + ", programId: "
                        + getOrgSectionsReq.programId + ", centerId: "
                        + getOrgSectionsReq.centerId);
                return getOrgSectionsRes;
            }
            if (CollectionUtils.isEmpty(c.sectionIds)) {
                logger.debug("could not find sections for orgId: "
                        + getOrgSectionsReq.orgId + ", programId: "
                        + getOrgSectionsReq.programId + ", centerId: "
                        + getOrgSectionsReq.centerId);
                return getOrgSectionsRes;
            }

            List<ObjectId> sectionIds = ObjectIdUtils.toObjectIds(c.sectionIds);
            List<OrgSection> sections = getSectionsByIds(
                    getOrgSectionsReq.getOrgId(), getOrgSectionsReq.getProgramId(),
                    sectionIds, getOrgSectionsReq.getAccessScope(),
                    getOrgSectionsReq.revenueModel, null, NO_START,
                    NO_LIMIT, new AtomicLong());
            if (CollectionUtils.isEmpty(sections)) {
                logger.debug("could not convert sectionIds to sections for orgId: "
                        + getOrgSectionsReq.orgId + ", programId: "
                        + getOrgSectionsReq.programId + ", centerId: "
                        + getOrgSectionsReq.centerId + ", sectionIds: ["
                        + c.sectionIds.stream().collect(Collectors.joining(","))+ "]");
                return getOrgSectionsRes;
            }
            List<OrgSectionInfo> sectionInfos = toOrgSectionInfo(sections);

            getOrgSectionsRes.list.addAll(sectionInfos);
            getOrgSectionsRes.totalHits = sectionInfos.size();
            return getOrgSectionsRes;
        }

    public static List<OrgSectionInfo> toOrgSectionInfo(List<OrgSection> sections) {

        List<OrgSectionInfo> sectionInfos = new ArrayList<OrgSectionInfo>();
        if (!sections.isEmpty()) {
            for (OrgSection section : sections) {
                if (null == section) {
                    continue;
                }
                sectionInfos.add(new OrgSectionInfo(section._getStringId(), section.getName(), section.code,
                        section.getRecordState(), section.getAccessScope(), section.getRevenueModel(), section.getDesc(),
                        section.getCostRate(),section.getSize(),section.extSupported, section.getOrgId(),
                        section.getStartingRates(), section.getPackagesMap(), section.getDescriptionPoints(),section.getThumbnail()));
            }
           // Collections.sort(sectionInfos, OrgStructureInfoNameComparator.INSTANCE);
        }
        return sectionInfos;
    }

    public List<OrgSection> getSectionsByIds(String orgId, String programId,
                                             List<ObjectId> sectionsIds, AccessScope accessScope, RevenueModel revenueModel,
                                             VedantuRecordState recordState, int start, int size, AtomicLong totalHits) {
        return getAllSectionsByIds(Arrays.asList(orgId),programId,sectionsIds,accessScope,revenueModel,recordState,start,size,totalHits);
    }

    public List<OrgSection> getAllSectionsByIds(List<String> asList, String programId, List<ObjectId> sectionsIds, AccessScope accessScope, RevenueModel revenueModel, VedantuRecordState recordState, int start, int size, AtomicLong totalHits) {


      /*  logger.info("..........entered function getSectionsByIds......." + revenueModel);
        logger.info("........" + programId + " " + sectionsIds + " " + asList + "" + accessScope + "" + recordState + "" + revenueModel);
        List<OrgSection> sections = null;

        if (!asList.isEmpty() && programId != null && !sectionsIds.isEmpty() && accessScope != null && revenueModel != null && recordState != null) {
            sections = orgSectionRepo.findAllByIdInAndProgramIdAndRecordStateAndRevenueModelAndAccessScopeAndAndOrgIdIn(sectionsIds, programId, recordState, revenueModel, accessScope, asList);
        }
        if (!asList.isEmpty() && programId == null && !sectionsIds.isEmpty() && accessScope != null && revenueModel != null && recordState != null) {
            sections = orgSectionRepo.findAllByIdInAndRecordStateAndRevenueModelAndAccessScopeAndOrgIdIn(sectionsIds, recordState, revenueModel, accessScope, asList);
        }
        if (!asList.isEmpty() && programId != null && sectionsIds.isEmpty() && accessScope != null && revenueModel != null && recordState != null) {
            sections = orgSectionRepo.findAllByProgramIdAndRecordStateAndRevenueModelAndAccessScopeAndOrgIdIn(programId, recordState, revenueModel, accessScope, asList);
        }
        if(!asList.isEmpty()&&programId!=null&&!sectionsIds.isEmpty()&&accessScope==null&&revenueModel!=null&&recordState!=null) {
            sections=orgSectionRepo.findAllByIdInAndProgramIdAndRecordStateAndRevenueModelAndOrgIdIn(sectionsIds,programId,recordState,revenueModel,asList);
        }
        if(!asList.isEmpty()&&programId!=null&&!sectionsIds.isEmpty()&&accessScope!=null&&revenueModel==null&&recordState!=null) {
            sections=orgSectionRepo.findAllByIdInAndProgramIdAndRecordStateAndAccessScopeAndOrgIdIn(sectionsIds,programId,recordState,accessScope,asList);
        }
        if(!asList.isEmpty()&&programId!=null&&!sectionsIds.isEmpty()&&accessScope!=null&&revenueModel!=null&&recordState==null) {
            sections=orgSectionRepo.findAllByIdInAndProgramIdAndRevenueModelAndAccessScopeAndOrgIdIn(sectionsIds,programId,revenueModel,accessScope,asList);
        }
        if(!asList.isEmpty()&&programId!=null&&!sectionsIds.isEmpty()&&accessScope==null&&revenueModel==null&&recordState==null) {
            sections=orgSectionRepo.findAllByIdInAndProgramIdAndOrgIdIn(sectionsIds,programId,asList);
        }

        if (!asList.isEmpty() && programId == null && !sectionsIds.isEmpty() && accessScope != null && revenueModel == null && recordState != null) {
            sections = orgSectionRepo.findAllByIdInAndRecordStateAndAccessScopeAndOrgIdIn(sectionsIds, recordState, accessScope, asList);
        }
        if(!sections.isEmpty())
        totalHits.set(sections.size());

        logger.info("..........about to return from function getSectionsByIds......."
                + sections.size());*/
        logger.debug("..........entered function getSectionsByIds......." + revenueModel);
        Query query = new Query();
        Criteria criteria = new Criteria();

        //Query<OrgSection> sectionsQuery = getQuery().filter("orgId", orgId);
        // Query<OrgSection> sectionsQuery = getQuery().field(FIELD_ID).hasAnyOf(orgIds);
        criteria.and("orgId").in(asList);
        if (!StringUtils.isEmpty(programId)) {
            criteria.and("programId").is(programId);
        }

        // sectionsQuery.filter(OrgSection.FIELD_ACCESS_SCOPE, accessScope);

        if (!CollectionUtils.isEmpty(sectionsIds)) {
            criteria.and("id").in(sectionsIds);
        }
        if (accessScope != null) {
            criteria.and("accessScope").is(accessScope);
            ///  sectionsQuery.filter(OrgSection.FIELD_ACCESS_SCOPE, accessScope);
        }
        if (revenueModel != null) {
            criteria.and("revenueModel").is(revenueModel);
            //sectionsQuery.filter(OrgSection.FIELD_REVENUE_MODEL, revenueModel);
        }

        if (recordState != null) {
            criteria.and(ConstantsGlobal.RECORD_STATE).is(recordState);
        }
        // sectionsQuery.order("cName");
        List<OrgSection> sections = mongoTemplate.find(query.addCriteria(criteria), OrgSection.class);
        if (!sections.isEmpty())
            totalHits.set(sections.size());

        logger.debug("..........about to return from function getSectionsByIds......."
                + sections.size());
        return sections;

    }

    private List<GranteeOrgProgram> getGranteeOrgPrograms(String providerOrgId, String departmentId, AtomicLong totalProgramHits) {
        logger.debug("getGrateeOrgPrograms orgId: " + providerOrgId
                + ", departmentId: " + departmentId);
        List<GranteeOrgProgram> granteeOrgPrograms=null;

        if ( departmentId!=null) {
         //    granteeOrgPrograms=granteeOrgProgramRepo.findAllBySubscriberOrgIdAndRecordStateAndDepartmentId(providerOrgId,VedantuRecordState.ACTIVE,departmentId);
        }

            granteeOrgPrograms=  granteeOrgProgramRepo.findAllBySubscriberOrgIdAndRecordState(providerOrgId, VedantuRecordState.ACTIVE);


        totalProgramHits.set(granteeOrgPrograms.stream().count());

        logger.info("getGrateeOrgPrograms"+granteeOrgPrograms.size()+" totalHits: " + totalProgramHits.get());

        return granteeOrgPrograms;
    }


    public GetOrgSectionRes getProgramSection(GetOrgSectionReq getOrgSectionReq) {

        OrgMember member = getMemberByUserId(getOrgSectionReq.getOrgId().trim(), getOrgSectionReq.getUserId());


        if (member == null) {
            throw new VedantuException(
                    VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }

        Optional<OrgSection> section = orgSectionRepo.findById(getOrgSectionReq.getSectionId());
        if (!section.isPresent()) {
            throw new VedantuException(
                    VedantuErrorCode.ORGANIZATION_PROGRAM_SECTION_NOT_FOUND);
        }
        OrgProgramSectionBasicInfo sectionInfo = new OrgProgramSectionBasicInfo(
                section.get()._getStringId(), section.get().getRecordState(), section.get().getcName(),
                section.get().getCode(), EntityType.SECTION);
        sectionInfo.thumbnail = section.get().getThumbnail();
        sectionInfo.addSectionExtraInfo(section.get());
        boolean found = false;
        for (OrgMemberMappingInfo info : member.mappings) {
            if (info.endTime <= 0 || info.endTime > System.currentTimeMillis()) {
                if (sectionInfo.id.equals(info.sectionId)) {
                    sectionInfo.setTimeJoined(info.getTimeJoined());
                    sectionInfo.setEndTime(info.getEndTime());
                    sectionInfo.setOrderId(info.getOrderId());
                    found = true;
                    break;
                }
            }

        }
        if (!found) {
            throw new VedantuException(
                    VedantuErrorCode.ORGANIZATION_MEMBER_MAPPING_NOT_FOUND);
        }

        GetOrgSectionRes response = new GetOrgSectionRes();
        response.setInfo(sectionInfo);
        return response;
    }

    public OrgMember getMemberByUserId(String orgId, String userId) {
        logger.debug("getMemberByUserId orgId: " + orgId + ", userId: " + userId);

        OrgMember orgMember = orgMemberRepo.findByOrgIdAndUserId(orgId,userId);


        if (orgMember==null) {
            logger.error("cannot find orgMember for orgId: " + orgId + ", userId: " + userId);
        }
        return orgMember;
    }

    private AcceptTnCRes acceptTnCOrg(AcceptTncOrgReq acceptTncOrgReq) {
        if (!acceptTncOrgReq.agrees) {
            throw new VedantuException(VedantuErrorCode.TNC_NOT_ACCEPTED);
        }

        TnCAcceptance acceptance = new TnCAcceptance(acceptTncOrgReq.agrees, acceptTncOrgReq.getVersion(),
                System.currentTimeMillis());

        acceptance.acceptedBy = acceptTncOrgReq.getUserId();
        Optional<Organization> organization1 = organizationRepo.findById(acceptTncOrgReq.getOrgId());
        if(!organization1.isPresent()){
            throw new VedantuException(VedantuErrorCode.INVALID_CODE,"the given orgId not found");
        }
        Organization organization=organization1.get();
        if (StringUtils.isEmpty(acceptTncOrgReq.getUserId())
                || !organization.adminUserId.equals(acceptTncOrgReq.getUserId())) {
            throw new VedantuException(VedantuErrorCode.ACCESS_DENIED);
        }

        organization.setTncAcceptance(acceptance);
        organizationRepo.save(organization);

        AcceptTnCRes acceptTnCRes = new AcceptTnCRes(acceptance.agrees);
        return acceptTnCRes;
    }

    private RemoveProgramSharingRes removeSharedProgramFromOrganization(RemoveProgramSharingReq removeProgramSharingReq) {

        RemoveProgramSharingRes removeProgramSharingRes = new RemoveProgramSharingRes();
        removeProgramSharing(
                removeProgramSharingReq.getProviderOrgId(),removeProgramSharingReq.getProgramId(),
                removeProgramSharingReq.getSubscriberOrgId());
        removeSharedProgramMapping(removeProgramSharingReq.getSubscriberOrgId(),
                removeProgramSharingReq.getProgramId());
        removeProgramSharingRes.done = true;
        return removeProgramSharingRes;
        
        
        
    }

    private void removeSharedProgramMapping(String subscriberOrgId, String programId) {

        List<OrgMember> orgMembers=orgMemberRepo.findByOrgIdAndMappingsProgramId(subscriberOrgId,programId);


            for (OrgMember member : orgMembers) {
                boolean modified = false;
                if (member.getMappings() != null && !member.getMappings().isEmpty()) {
                    Iterator<OrgMemberMappingInfo> mappingIterator = member.getMappings().iterator();
                    while (mappingIterator.hasNext()) {
                        OrgMemberMappingInfo mapping = mappingIterator.next();
                        if (mapping.getProgramId().equals(programId)) {
                            logger.warn("Removing mapping because of shared organization deletion for userId : "
                                    + member.userId);
                            // Other organization stopped sharing so moving it to
                            // expired mappings
                            if (member.getExpiredMappings() == null) {
                                member.setExpiredMappings( new ArrayList<OrgMemberMappingInfo>());
                            }
                            member.getMappings().add(mapping);
                            mappingIterator.remove();
                            modified = true;
                        }
                    }
                }
                if (modified) {
                    orgMemberRepo.save(member);
                }
            }

    }

    private GranteeOrgProgram removeProgramSharing(String providerOrgId, String programId, String subscriberOrgId) {
        GranteeOrgProgram granteeOrgProgram=granteeOrgProgramRepo.findByProviderOrgIdAndSubscriberOrgIdAndProgramId(providerOrgId,subscriberOrgId,programId);
        if (granteeOrgProgram != null) {
            granteeOrgProgram.setRecordState(VedantuRecordState.DELETED);
        }
        granteeOrgProgramRepo.save(granteeOrgProgram);
        return granteeOrgProgram;
    }

    private GetSharedOrgsRes sharedProgToOrganization(GetSharedOrgsReq getSharedOrgsReq) {


            AtomicLong totalHits = new AtomicLong(0L);
            GetSharedOrgsRes getOrgsRes = new GetSharedOrgsRes();
             AtomicLong totalProgramHits = new AtomicLong(0L);
            List<GranteeOrgProgram> granteeOrgPrograms = getGranteeOrgByProgId(getSharedOrgsReq.getProviderOrgId(), getSharedOrgsReq.getProgramId(), totalProgramHits);
            if(hasSharedProgramAccess(getSharedOrgsReq.getProgramId().trim())){
                // Check limit, Should not share this program to not more than one organisation.
                if(granteeOrgPrograms.size() >= 1){
                    throw new VedantuException(VedantuErrorCode.CAN_NOT_SHARE_THIS_PROGRAM);
                }
            }
            GranteeOrgProgram addedGranteeOrgProg = null;
            try {
                addedGranteeOrgProg = addGranteeOrgProgram(
                        getSharedOrgsReq.getProviderOrgId(), getSharedOrgsReq.getSubscriberOrgId(),
                        getSharedOrgsReq.getProgramId());
                logger.debug("Shared granteeOrgProg " + addedGranteeOrgProg);
            } catch (VedantuException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            granteeOrgPrograms = getGranteeOrgByProgId(getSharedOrgsReq.providerOrgId, getSharedOrgsReq.programId, totalProgramHits);
            logger.debug("OrganizationManager sharedProgToOrg "+granteeOrgPrograms+" size="+granteeOrgPrograms.size());
            List<String> alreadyExistingOrgIds = new ArrayList<String>();
            if (!granteeOrgPrograms.isEmpty()) {
                getOrgsRes.setTotalHits(totalProgramHits.get());
                for (GranteeOrgProgram granteeOrg : granteeOrgPrograms) {
                    getOrgsRes.list.add(granteeOrg);
                    alreadyExistingOrgIds.add(granteeOrg.subscriberOrgId);
                }
            }
            List<Organization> organizations = getAllOrganizations(
                    null, totalHits);
            for (Organization organization : organizations) {
                String id = organization._getStringId();
                if(alreadyExistingOrgIds.contains(id)||id.equals(getSharedOrgsReq.subscriberOrgId)){
                    getOrgsRes.subscriberOrgsInfos.add((OrgBasicInfo) organization.toBasicInfo());
                }
                else{
                    getOrgsRes.orgsKeyValue.put(id,organization.fullName);
                }
            }
            return getOrgsRes;

    }

    private List<Organization> getAllOrganizations(OrganizationStatus status , AtomicLong totalHits) {
        logger.debug("getAllOrganizations status: " + status + ", totalHits: " + totalHits.get());
        List<Organization> organizations = null;


        if (null != status) {
            organizations=organizationRepo.findAllByStatus(status);

        }else{
            organizations=organizationRepo.findAll();
        }

        totalHits.set(organizations.size());

        logger.info("getAllOrganizations status: " + status + ", totalHits: " + totalHits
                + ", organizations.size: " +organizations.size());
        return organizations;
    }

    private GranteeOrgProgram addGranteeOrgProgram(String providerOrgId, String subscriberOrgId, String programId) {

        logger.debug("addGranteeOrgProgram providerOrgId: " + providerOrgId + ",subscriberOrgId: "
                + subscriberOrgId + ", programId: " + programId);

        GranteeOrgProgram orgProgram = null;
        orgProgram=granteeOrgProgramRepo.findByProviderOrgIdAndSubscriberOrgIdAndProgramId(providerOrgId,subscriberOrgId,programId);


        if (orgProgram!=null) {
            if (orgProgram.getRecordState().equals(VedantuRecordState.ACTIVE)) {
                logger.error("cannot add orgProgram as orgProgram already exists for orgId: "
                        + providerOrgId + ", subscriberOrgId: " + subscriberOrgId + ", programId: "
                        + programId);
                throw new VedantuException(VedantuErrorCode.ORGANIZATION_PROGRAM_ALREADY_EXISTS);
            }
            orgProgram.setRecordState(VedantuRecordState.ACTIVE);
        } else {
            orgProgram = new GranteeOrgProgram(providerOrgId, subscriberOrgId, programId);
        }
        granteeOrgProgramRepo.save(orgProgram);
        return orgProgram;



    }

    private  boolean hasSharedProgramAccess(String programId) {
        // TODO Auto-generated method stub
        Optional<OrgProgram> prog = orgProgramRepo.findById(programId);
        return prog.isPresent() && prog.get().sharedProgramAccess;
    }

    private UploadOrgPicRes uploadOrganizationPic(FileMetaInfo fileDocument, UploadOrgPicReq request) {

        ImageFilter filter = new ImageFilter();
        boolean isImg = filter.accept(new File(fileDocument.getName()));
        if (!isImg) {
            throw new VedantuException(VedantuErrorCode.UNSUPPORTED_FILE_TYPE, "not an image file");
        }

        Optional<Organization> organization = organizationRepo.findById(request.getOrgId().trim());

        OrgMember member = orgMemberRepo.findByOrgIdAndUserId(request.getOrgId().trim(),
                request.getUserId());
        if (member == null || !member._getStringId().equals(request.orgMemberId)) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }
        if (!OrgMemberProfile.MANAGER.equals(member.profile)) {
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED);
        }

        final String imageName = organization.get().getStringId();

        //OrganizationEntityFileStorage picStorage = new OrganizationEntityFileStorage();
        picStorage.AbstractEntityFileStorageEntity(EntityType.ORGANIZATION);
        try {
        StorageResult picStorageResult = picStorage.storeImage(imageName,
                request.inputFile, FileCategory.ORIGINAL, ImageSize.ORIGINAL, null);
        for (ImageSize imageSize : new ImageSize[] { ImageSize.MEDIUM, ImageSize.SMALL,
                ImageSize.EXTRA_SMALL }) {
            File convertedFile;
			
				convertedFile = picStorage.createImage(request.inputFile,
				        imageSize, request.fileName);
			
            picStorageResult = picStorage.storeImage(imageName, convertedFile,
                    FileCategory.CONVERTED, imageSize, null);
            logger.debug(picStorageResult.toString());

            FileUtils.deleteFile(convertedFile.getName(), convertedFile);
        }
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        organization.get().setThumbnail(imageName);
        organizationRepo.save(organization.get());

        //String thumbnailUrl =organization.get()._getThumbnailUrl(organization.get().getThumbnail());
        String thumbnailUrl;
        if(!StringUtils.isEmpty(imageName) ) {
         thumbnailUrl = picStorage.getSecuredURL(imageName, EntityType.ORGANIZATION,
                FileUtils.JPG_EXTENTION_WITHOUT_DOT, com.lms.common.vedantu.entity.storage.MediaType.IMAGE,
                FileCategory.CONVERTED, ImageSize.SMALL).getSecuredURL();
        }else {
        	thumbnailUrl = ImageDisplayURLUtil.getEntityStaticThumbnail(
                    EntityType.ORGANIZATION, Arrays.asList("default"));
        }
        
        UploadOrgPicRes uploadOrgPicRes = new UploadOrgPicRes(true, thumbnailUrl);

        return uploadOrgPicRes;
    }
    private void validateFile(MultipartFile multipartFile) throws InputMismatchException {
        if(multipartFile.isEmpty() ||multipartFile.getOriginalFilename().isEmpty() || multipartFile.getContentType().isEmpty()){
            throw new InputMismatchException("Uploaded File is not valid");
        }
    }

    private GetSharedOrgsRes getSharedOrgsByProg(GetSharedOrgsReq getSharedOrgsReq) {
        List<Organization> organizations = organizationRepo.findAll();
        AtomicLong totalHits =new AtomicLong(organizations.stream().count());
        GetSharedOrgsRes getOrgsRes = new GetSharedOrgsRes();
        AtomicLong totalProgramHits = null;
        List<GranteeOrgProgram> granteeOrgPrograms = getGranteeOrgByProgId(getSharedOrgsReq.providerOrgId, getSharedOrgsReq.programId, totalProgramHits);
         totalProgramHits=new AtomicLong(granteeOrgPrograms.stream().count());
        logger.debug("OrganizationManager getSharedOrgsByProg "+granteeOrgPrograms+" size="+granteeOrgPrograms.size());
        List<String> alreadyExistingOrgIds = new ArrayList<String>();
        if (!granteeOrgPrograms.isEmpty()) {
            getOrgsRes.setTotalHits( granteeOrgPrograms.stream().count());
            for (GranteeOrgProgram granteeOrg : granteeOrgPrograms) {
                getOrgsRes.getList().add(granteeOrg);
                alreadyExistingOrgIds.add(granteeOrg.subscriberOrgId);
            }
        }
        for (Organization organization : organizations) {
            String id = organization._getStringId();
            if(alreadyExistingOrgIds.contains(id)||id.equals(getSharedOrgsReq.subscriberOrgId)){
                getOrgsRes.subscriberOrgsInfos.add((OrgBasicInfo) organization.toBasicInfo());
            }
            else{
                getOrgsRes.orgsKeyValue.put(id,organization.fullName);
            }
        }
        return getOrgsRes;
    }

    private List<GranteeOrgProgram> getGranteeOrgByProgId(String providerOrgId, String programId, AtomicLong totalProgramHits) {
        logger.debug("getGranteeOrgByProgId orgId: " + providerOrgId
                + ", programId: " + programId);
        List<GranteeOrgProgram> granteeOrgProgram =null;
       // granteeOrgProgram= granteeOrgProgramRepo.findALLByProviderOrgIdAndRecordState(providerOrgId,VedantuRecordState.ACTIVE);


        if (programId!=null) {
            granteeOrgProgram= granteeOrgProgramRepo.findAllByProviderOrgIdAndRecordStateAndProgramId(providerOrgId,VedantuRecordState.ACTIVE,programId);
        }
        else{
            granteeOrgProgram= granteeOrgProgramRepo.findALLByProviderOrgIdAndRecordState(providerOrgId,VedantuRecordState.ACTIVE);

        }
        List<GranteeOrgProgram> programs = granteeOrgProgram;
        return granteeOrgProgram;


}

    private ApproveOrgRes organizationApproval(ApproveOrgReq approveOrgReq) {


        Optional<Organization> organization1 = organizationRepo.findById(approveOrgReq.getOrgId());
        if (!organization1.isPresent()) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
        }
        Organization organization = organization1.get();
        if (OrganizationStatus.APPROVED == organization.getStatus()) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_ALREADY_APPROVED);
        }
        if (OrganizationStatus.APPROVED == organization.getStudentPageStatus()) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_ALREADY_APPROVED);
        }
        if (organization != null
                && (organization.getSubscription() == null || (organization.getSubscription().getPlanId().isEmpty()))) {
            throw new VedantuException(VedantuErrorCode.INVALID_LICENSING_PLAN);
        }

        AvailablePlansRes plans = getPlans(organization.getSubscription().getPlanId(), PlanState.ACTIVE);
        if (plans.getList().isEmpty()) {
            throw new VedantuException(VedantuErrorCode.INVALID_LICENSING_PLAN);
        }

        Optional<User> superAdminUser1 = userRepo.findByUsername(organization.getRepresentative().getEmail());
        if (!superAdminUser1.isPresent()) {
            throw new VedantuException(VedantuErrorCode.INVALID_CODE, "the user not found");
        }
        User superAdminUser = superAdminUser1.get();
        String password = HardCodedConstants.emptyString;

        String userId = null;
        String adminPasswordResetURL = null;
        boolean isNewUserAdded = false;
        if (superAdminUser == null) {
            password = PasswordUtils.generateRandomPassword();
            AddUserReq addUserReq = new AddUserReq(organization.getRepresentative(), password,
                    UNKNOWN_DOB, Gender.UNKNOWN);
            AddUserRes addUserRes = addUser(addUserReq);

            userId = addUserRes.id;
            isNewUserAdded = true;
            superAdminUser1 = userRepo.findById(userId);
            if (!superAdminUser1.isPresent()) {
                throw new VedantuException(VedantuErrorCode.INVALID_CODE, "the user not found");
            }
            adminPasswordResetURL = generatePasswordUpdate(addUserRes.getId(),
                    organization._getStringId(), approveOrgReq.getCallingAppId());
        } else {
            userId = superAdminUser._getStringId();
        }

            OrgMember superAdminOrgMember = orgMemberRepo.findByOrgIdAndMemberId(approveOrgReq.getOrgId(),SUPER_ADMIN_MEMBER_ID);

            String orgMemberId = null;

            boolean isNewOrgMemberAdded = false;
          if ( superAdminOrgMember==null) {

                AddOrgMemberReq addOrgMemberReq = new AddOrgMemberReq(approveOrgReq.orgId,
                        SUPER_ADMIN_MEMBER_ID, organization.representative.firstName,
                        organization.representative.lastName, UNKNOWN_DOB, Gender.UNKNOWN,
                        organization.representative.getEmail(), OrgMemberProfile.MANAGER,
                        organization.contactNumber);
                AddOrgMemberRes addOrgMemberRes = memberServiceImpl.addOrgMember(addOrgMemberReq);

                orgMemberId = addOrgMemberRes.id;
                isNewOrgMemberAdded = true;
            } else {
                orgMemberId = superAdminOrgMember._getStringId();
            }

            organization = approveOrganization(approveOrgReq.orgId, userId,
                    orgMemberId);

        ApproveOrgRes approveOrgRes = new ApproveOrgRes();
        approveOrgRes.orgId = organization._getStringId();

        approveOrgRes.isNewUserAdded = isNewUserAdded;
        approveOrgRes.adminUserId = userId;
        approveOrgRes.adminPassword = password;

         approveOrgRes.isNewOrgMemberAdded = isNewOrgMemberAdded;
          approveOrgRes.adminOrgMemberId = orgMemberId;

        approveOrgRes.status = organization.status;
        UserEmailInfo info = new UserEmailInfo();
       //   info.fromUserExtendedInfo((UserExtendedInfo) superAdminUser.toExtendedInfo());

        //   generateApproveOrganizationEmailEvent(organization, info, adminPasswordResetURL);

        return approveOrgRes;

    }

    public AddUserRes addUser(AddUserReq addUserReq) {

        if (!VedantuStringUtils.isValidDOB(addUserReq.getDob())) {
            throw new VedantuException(VedantuErrorCode.INVALID_DATE_FORMAT);
        }
        AtomicBoolean isEmailVerificationNeeded = new AtomicBoolean(false);

        // check if encryption keys are need to be generated

        SecurityCredentials credentials = EncryptionUtils.generateKeys();
        SocialInfo socialInfo = null;
        if (addUserReq.twitterHandle != null) {
            socialInfo = new SocialInfo();
            socialInfo.twitter = addUserReq.twitterHandle;
        }
        User user = addUserToRepo(addUserReq, isEmailVerificationNeeded, credentials, socialInfo);
        AddUserRes addUserRes = new AddUserRes();
        if (null != user) {
            addUserRes.setId(user.getId().toString());
            logger.debug("user created with id: " + addUserRes.getId());
            if (isEmailVerificationNeeded.get()) {
                generateEmailVerificationEvent(user, addUserReq.getOrgId(), addUserReq.getCallingAppId());
            }
        }


        return addUserRes;

    }


    private AvailablePlansRes getPlans(String id, PlanState active) {
        List<LicensingPlan> plans = licensingPlanRepo.findByIdAndState(id, PlanState.ACTIVE);
        // .INSTANCE.getAllPlans(ids, state);

        AvailablePlansRes response = new AvailablePlansRes();

        if (!plans.isEmpty()) {
            for (LicensingPlan plan : plans) {
                response.getList().add((LicensingInfo) plan.toBasicInfo());

            }
            response.totalHits = plans.size();
        }

        return response;
    }

    private UpdateOrgRes updateOrg(UpdateOrgReq updateOrgReq) {
        Set<String> updateList = new HashSet<String>();

        setUpdateList(updateList, updateOrgReq);

        OrganizationType type = OrganizationType.valueOfKey(updateOrgReq.type);
        Scope scope = Scope.valueOfKey(updateOrgReq.getScope());

        Organization organization = updateOrganizationToRepo(
                updateOrgReq.getOrgId(),
                updateOrgReq.getName(),
                updateOrgReq.getFullName(),
                updateOrgReq.getWebsite(),
                updateOrgReq.getEmailDomain(),
                updateOrgReq.getContactNumber(),
                type,
                updateOrgReq.getLocations(),
                updateOrgReq.getAddress(),
                updateOrgReq.getDescription(),
                scope,
                updateOrgReq.getEncLevel(),
                updateOrgReq.getAuthType(),
                updateOrgReq.getEndPoint(),
                updateOrgReq.getSocialMedia(),
                updateOrgReq.getAppInfos(),
                updateOrgReq.pointsOfSale == null ? null : Arrays.asList(updateOrgReq.getPointsOfSale().split(",")),
                updateOrgReq.getDoubtsForumMode(), updateList,
                updateOrgReq.getSmtpHost(), updateOrgReq.getSmtpUser(),
                updateOrgReq.getSmtpPassword(),
                updateOrgReq.getInstaMojoClientId(),
                updateOrgReq.getInstaMojoClientSecret(),
                updateOrgReq.getInstaMojoApiKey(),
                updateOrgReq.getInstaMojoAuthToken(),
                updateOrgReq.getVersionCode(),
                updateOrgReq.disableSignup,
                updateOrgReq.getDisableSignupMessage());
        organization.setCommunicationMail(updateOrgReq.getCommunicationMail());
        organizationRepo.save(organization);
        UpdateOrgRes updateOrgRes = new UpdateOrgRes();
        updateOrgRes.id = organization._getStringId();
        updateOrgRes.recordState = organization.recordState;

        return updateOrgRes;

    }

    private Organization updateOrganizationToRepo(String orgId, String name,
                                                  String fullName, String website, String emailDomain,
                                                  String contactNumber, OrganizationType type,
                                                  List<Location> locations, String address, String description,
                                                  Scope scope, EncryptionLevel encLevel, AuthType authType,
                                                  ExternalOrganizationEndpoints endPoint, SocialInfo socialMedia,
                                                  List<AppInfo> appInfos, List<String> pointsOfSale,
                                                  DoubtsForumMode doubtsForumMode, Set<String> updateList,
                                                  String smtpHost, String smtpUser, String smtpPassword,
                                                  String instaMojoClientId, String instaMojoClientSecret, String instaMojoApiKey, String instaMojoAuthToken, int versionCode, boolean disableSignup, String disableSignupMessage) throws VedantuException {

        logger.debug("updateOrganization orgId: " + orgId + ", name: " + name
                + ", fullName: " + fullName + ", website: " + website
                + ", emailDomain: " + emailDomain + ", contactNumber: "
                + contactNumber + ", type: " + type + ", locations: {"
                + locations + "}" + ", address: "
                + address + ", description: " + description + ", scope: "
                + scope + " encLevel " + encLevel + " smtpHost " + smtpHost + " smtpUser " + smtpUser
                + " smtpPassword " + smtpPassword + " instaMojoClientId "
                + instaMojoClientId + " instaMojoClientSecret "
                + instaMojoClientSecret + "instaMojoApiKey " + instaMojoApiKey + "instaMojoAuthToken " + instaMojoAuthToken);
        Optional<Organization> organization1 = organizationRepo.findById(orgId);
        if (!organization1.isPresent()) {
            throw new VedantuException(VedantuErrorCode.INVALID_CODE, "the given orgId is invalid");
        }
        Organization organization = organization1.get();

        if (!(organization.getWebsite().equalsIgnoreCase(website))) {
            Organization orgByWebsite = organizationRepo.findByWebsite(website);

            if (orgByWebsite != null) {
                logger.error("cannot update organization as organization website: " + website
                        + " already exists for another organization: "
                        + orgByWebsite._getStringId());
                throw new VedantuException(VedantuErrorCode.ORGANIZATION_ALREADY_EXISTS,
                        "another organization already has a website: " + website);
            }
        }


        //organization = new Organization();
        organization.setName(name);
        organization.setFullName(fullName);
        organization.setWebsite(website);
        organization.setEmailDomain(emailDomain);
        organization.setContactNumber(contactNumber);
        organization.setType(type);
        organization.setLocations(locations);
        organization.setAddress(address);
        organization.setDescription(description);
        organization.setScope(scope);
        organization.setEndPoint(endPoint);
        organization.setAuthType(authType == null ? AuthType.VEDANTU : authType);
        organization.setSocialMedia(socialMedia);
        organization.setAppInfos(appInfos);
        organization.setDisableSignup(disableSignup);
        organization.setDisableSignupMessage(disableSignupMessage);
        organization.setDoubtsForumMode(DoubtsForumMode.PUBLIC);
        organization.setEncLevel(encLevel);
        if (!smtpUser.isEmpty()
                && !smtpPassword.isEmpty()
                && !smtpHost.isEmpty()) {
            organization.setSmtpHost(smtpHost);
            organization.setSmtpUser(smtpUser);
            organization.setSmtpPassword(smtpPassword);
        }
        if (!instaMojoClientId.isEmpty() && !instaMojoClientSecret.isEmpty() && instaMojoApiKey.isEmpty() && !instaMojoAuthToken.isEmpty()) {
            organization.setInstaMojoClientId(instaMojoClientId);
            organization.setInstaMojoClientSecret(instaMojoClientSecret);
            organization.setInstaMojoApiKey(instaMojoApiKey);
            organization.setInstaMojoAuthToken(instaMojoAuthToken);
        }
        if (versionCode > 0) {
            organization.versionCode = versionCode;
        }

        if (!updateList.isEmpty()) {
            // update pointsOfSale only in partial update query(caller should specifically, specify
            // it in updateList)
            if (pointsOfSale == null) {
                pointsOfSale = new ArrayList<String>();
            }
            organization.setPointsOfSale(pointsOfSale);
            organizationRepo.save(organization);
        } else {
            organizationRepo.save(organization);
        }

        logger.info("updateOrganization updated organization: " + organization._getStringId());

        return organization;
    }

    private static void setUpdateList(Set<String> updateList, UpdateOrgReq request)
            throws VedantuException {

        boolean matched = false;

        if (!request.updateList.isEmpty()) {
            for (String key : request.updateList) {
                matched = false;
                if (key.equals(AbstractAddOrgReq.NAME)) {
                    matched |= updateList.add(Organization.FIELD_NAME);
                } else if (key.equals(AbstractAddOrgReq.CONTACT_NUMBER)) {
                    matched |= updateList.add(Organization.FIELD_CONTACT_NUMBER);
                } else if (key.equals(AbstractAddOrgReq.FULL_NAME)) {
                    matched |= updateList.add(Organization.FIELD_FULL_NAME);
                } else if (key.equals(AbstractAddOrgReq.WEBSITE)) {
                    matched |= updateList.add(Organization.FIELD_WEBSITE);
                } else if (key.equals(AbstractAddOrgReq.TYPE)) {
                    matched |= updateList.add(Organization.FIELD_TYPE);
                } else if (key.equals(AbstractAddOrgReq.LOCATIONS)) {
                    matched |= updateList.add(Organization.FIELD_LOCATIONS);
                } else if (key.equals(AbstractAddOrgReq.ADDRESS)) {
                    matched |= updateList.add(Organization.FIELD_ADDRESS);
                } else if (key.equals(AbstractAddOrgReq.DESCRIPTION)) {
                    matched |= updateList.add(Organization.FIELD_DESCRIPTION);
                } else if (key.equals(AbstractAddOrgReq.SCOPE)) {
                    matched |= matched |= updateList.add(Organization.FIELD_SCOPE);
                } else if (key.equals(AbstractAddOrgReq.REPRESENTATIVE)) {
                    matched |= updateList.add(Organization.FIELD_REPRESENTATIVE);
                } else if (key.equals(AbstractAddOrgReq.SLUG)) {
                    matched |= updateList.add(Organization.FIELD_SLUG);
                } else if (key.equals(AbstractAddOrgReq.SOCIAL_MEDIA)) {
                    matched |= updateList.add(Organization.FIELD_SOCIAL_MEDIA);
                } else if (key.equals(AbstractAddOrgReq.ENC_LEVEL)) {
                    matched |= updateList.add(Organization.FIELD_ENC_LEVEL);
                } else if (key.equals(AbstractAddOrgReq.AUTH_TYPE)) {
                    matched |= updateList.add(Organization.FIELD_AUTH_TYPE);
                } else if (key.equals(AbstractAddOrgReq.END_POINT)) {
                    matched |= updateList.add(Organization.FIELD_END_POINT);
                } else if (key.equals(AbstractAddOrgReq.APP_INFO)) {
                    matched |= updateList.add(Organization.FIELD_APP_INFOS);
                } else if (key.equals(AbstractAddOrgReq.POINTS_OF_SALE)) {
                    matched |= updateList.add(Organization.FIELD_POINTS_OF_SALE);
                } else if (key.equals(AbstractAddOrgReq.DOUBTS_FORUM_MODE)) {
                    matched |= updateList.add(Organization.FIELD_DOUBTS_FORUM_MODE);
                } else if (key.equals(AbstractAddOrgReq.DISABLE_SIGNUP)) {
                    matched |= updateList.add(Organization.FIELD_DISABLE_SIGNUP);
                } else if (key.equals(AbstractAddOrgReq.DISABLE_SIGNUP_MESSAGE)) {
                    matched |= updateList.add(Organization.FIELD_DISABLE_SIGNUP_MESSAGE);
                } else if (key.equals(AbstractAddOrgReq.COMMUNICATION_MAIL)) {
                    matched |= updateList.add(Organization.COMMUNICATION_MAIL);
                } else if (key.equals(AbstractAddOrgReq.SMTP_HOST)) {
                    matched |= updateList.add(Organization.SMTP_HOST);
                } else if (key.equals(AbstractAddOrgReq.SMTP_USER)) {
                    matched |= updateList.add(Organization.SMTP_USER);
                } else if (key.equals(AbstractAddOrgReq.SMTP_PASSWORD)) {
                    matched |= updateList.add(Organization.SMTP_PASSWORD);
                } else if (key.equals(AbstractAddOrgReq.INSTAMOJO_CLIENT_ID)) {
                    matched |= updateList.add(Organization.INSTAMOJO_CLIENT_ID);
                } else if (key.equals(AbstractAddOrgReq.INSTAMOJO_CLIENT_SECRET)) {
                    matched |= updateList.add(Organization.INSTAMOJO_CLIENT_SECRET);
                } else if (key.equals(AbstractAddOrgReq.INSTAMOJO_API_KEY)) {
                    matched |= updateList.add(Organization.INSTAMOJO_API_KEY);
                } else if (key.equals(AbstractAddOrgReq.INSTAMOJO_AUTH_TOKEN)) {
                    matched |= updateList.add(Organization.INSTAMOJO_AUTH_TOKEN);
                } else if (key.equals(AbstractAddOrgReq.VERSION_CODE)) {
                    matched |= updateList.add(Organization.VERSION_CODE);
                }
                if (!matched) {
                    logger.debug("Key didnt match for update " + key);
                }
            }
        }

        if (updateList.size() != request.updateList.size()) {
            throw new VedantuException(VedantuErrorCode.INCORRECT_UPDATE_DATA_PROVIDED);
        }

    }

    private AddOrgRes addOrganizationtoRepo(AddOrgReq addOrgReq) throws VedantuException {
        OrganizationType type = OrganizationType.valueOfKey(addOrgReq.getType());
        Scope scope = Scope.valueOfKey(addOrgReq.getScope());
        OrganizationStatus status = OrganizationStatus.REQUESTED;
        OrganizationStatus studentPageStatus = OrganizationStatus.REQUESTED;
        // check if encryption keys are need to be generated
        if (addOrgReq.getEncLevel() == null) {
            addOrgReq.setEncLevel(EncryptionLevel.NA);
        }
        SecurityCredentials credentials = EncryptionUtils.generateKeys();
        long requestTime = System.currentTimeMillis();

        TnCAcceptance acceptance = new TnCAcceptance(true, addOrgReq.getTncVersion(), requestTime,
                addOrgReq.getRepresentative().getEmail());

        Optional<LicensingPlan> plan = licensingPlanRepo.findById(addOrgReq.planId);
        if (!plan.isPresent()) {
            throw new VedantuException(VedantuErrorCode.INVALID_CODE, "the give planId is not available");
        }

        Subscription subscription = new Subscription();
        subscription.setPlanId(addOrgReq.getPlanId());
        Calendar validity = setValidity(requestTime);

        Interval interval=new Interval(requestTime, (plan.get().peruser? -1 : validity.getTimeInMillis()));
        subscription.setValidity(interval);


        Organization organization = addOrg(addOrgReq.getName(),
                addOrgReq.getFullName(), addOrgReq.getWebsite(), addOrgReq.getEmailDomain(),
                addOrgReq.getContactNumber(), type, addOrgReq.getLocations(), addOrgReq.getAddress(),
                addOrgReq.getDescription(), scope, addOrgReq.getSlug(), addOrgReq.getRepresentative(), status, studentPageStatus,
                credentials, addOrgReq.getEncLevel(), subscription, acceptance, addOrgReq.getTheme(), addOrgReq.isNewUI, addOrgReq.showSharedSubjects);

        AddOrgRes addOrgRes = new AddOrgRes();
        if (null != organization) {
            addOrgRes.id = organization._getStringId();
            logger.debug("organization created with id: " + addOrgRes.id);
        }

        UserEmailInfo userEmailInfo = new UserEmailInfo();
        userEmailInfo.setEmail(addOrgReq.getRepresentative().getEmail());
        userEmailInfo.setFirstName(addOrgReq.getRepresentative().firstName);
        userEmailInfo.setLastName(addOrgReq.representative.lastName);
        userEmailInfo.setLastName(organization._getStringId());
       // generateAddOrganizationEmailEvent(organization, userEmailInfo);
        return addOrgRes;


    }

    private Organization addOrg(String name, String fullName, String website, String emailDomain, String contactNumber, OrganizationType type, List<Location> locations, String address, String description, Scope scope, String slug, UserBasicInfo representative, OrganizationStatus status, OrganizationStatus studentPageStatus, SecurityCredentials credentials, EncryptionLevel encLevel, Subscription subscription, TnCAcceptance acceptance, String theme, boolean isNewUI, boolean showSharedSubjects) throws VedantuException {
        logger.debug("addOrganization name: " + name + ", fullName: " + fullName + ", website: "
                + website + ", emailDomain: " + emailDomain + ", contactNumber: " + contactNumber
                + ", type: " + type
                + ", address: " + address + ", description: " + description + ", scope: " + scope
                + ", representative: " + representative + ", status: " + status + ", slug:" + slug
                + ", level:" + encLevel + " , planId:" + subscription.getPlanId());
        Organization organization=organizationRepo.findBySlugAndWebsite(slug.trim(),website.trim());
        if (organization!=null) {
            logger.error("cannot add organization as organization already exists for website: "
                    + website + " or slug: " + slug);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_ALREADY_EXISTS);
        }

        organization = new Organization();
        organization.setName(name);
        organization.setFullName(fullName);
        organization.setWebsite( website);
        organization.setEmailDomain(emailDomain);
        organization.setContactNumber(contactNumber);
        organization.setType(type);
        organization.setLocations(locations);
        organization.setAddress(address);
        organization.setSlug(slug);
        organization.setDescription(description);
        organization.setScope(scope);
        organization.setRepresentative(representative);
        organization.setCredentials(credentials);
        organization.setStatus(status);
        organization.setStudentPageStatus(studentPageStatus);
        organization.setSubscription(subscription);
        organization.setDoubtsForumMode(DoubtsForumMode.PUBLIC);
        organization.setEncLevel(encLevel);
        organization.setTncAcceptance(acceptance);
        if (!theme.isEmpty()) {
            organization.setTheme(theme);
        }
        organization.isNewUI = isNewUI;
        organization.showSharedSubjects = showSharedSubjects;
        organizationRepo.save(organization);

        logger.info("addOrganization saved organization: " + organization._getStringId());

        return organization;
    }

    private Calendar setValidity(long requestTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(requestTime));
        c.add(Calendar.YEAR, 1);

        return c;
    }

    private GetOrgsInfo getOrganizationsByOrgsReq(GetOrgsReq getOrgsReq) {
        GetOrgsInfo getOrgsRes = new GetOrgsInfo();
        AtomicLong totalHits = new AtomicLong(0L);
        List<Organization> organizations = null;
        if (getOrgsReq.getStatus() != null && getOrgsReq.getQuery()!=null) {
            organizations = organizationRepo.findALLByStatusAndFullName(getOrgsReq.getStatus(), getOrgsReq.getQuery());
        }
        if (getOrgsReq.getStatus() != null && getOrgsReq.getQuery() == null) {
            organizations = organizationRepo.findAllByStatus(getOrgsReq.getStatus());
        }
        if (getOrgsReq.getStatus() == null && getOrgsReq.getQuery() != null) {
            organizations = organizationRepo.findAllByFullName(getOrgsReq.getQuery());
        }
        if (getOrgsReq.getStatus() == null && getOrgsReq.getQuery() == null) {
            organizations = organizationRepo.findAll();
        }
        if (organizations != null) {
            getOrgsRes.setTotalHits(organizations.stream().count());
            for (Organization organization : organizations) {
                OrgInfo orgInfo = (OrgInfo) getOrgInfo(organization);
                orgInfo.setAuthType(organization.getAuthType());
                getOrgsRes.getList().add(orgInfo);
            }
        }
        return getOrgsRes;


    }

    public ModelExtendedInfo getOrgInfo(Organization organization) {


        OrgInfo orgInfo = new OrgInfo(organization.getId().toString(), organization.getName(), organization.getFullName(), organization.getType(), organization.getScope(), organization.getStatus(),
                organization.getThumbnail(), organization.getTimeCreated(), organization.getLastUpdated(), organization.getReferer(), organization.getSlug(), organization.getRecordState());
        orgInfo.setAuthType(organization.getAuthType());
        LicensingInfo licensingInfo = null;
        if (organization.getSubscription() != null && !(organization.getSubscription().getPlanId().isEmpty())) {
            Optional<LicensingPlan> licensingPlan = licensingPlanRepo.findById(organization.getSubscription().getPlanId());
            orgInfo.setPlanInfo(licensingPlan.isPresent() ? licensingInfo = new LicensingInfo(licensingPlan.get()) : null);

        }
        orgInfo.setSlug(organization.getSlug());

        return orgInfo;
    }


    private CheckSlugRes getCheckReferer(CheckRefererReq checkRefererReq) throws VedantuException {
        if (!isValidReferer(checkRefererReq.getReferer())) {
            logger.error("invalid referer: " + checkRefererReq.getReferer());
            throw new VedantuException(VedantuErrorCode.INVALID_REFERER);
        }
        CheckSlugRes res = new CheckSlugRes();
        Organization organization = organizationRepo.findByReferer(checkRefererReq.getReferer().trim());
        if (organization == null) {
            logger.error("cannot find organization for referer: " + checkRefererReq.getReferer());
            res.setAvailable(false);
        } else res.setAvailable(true);

        return res;


    }

    private GetOrgRes getOrgByReferer(String referer, boolean getKey) throws VedantuException, MalformedURLException {

        logger.info("Referer asked for" + referer);
        String refererHost = null;

        URL refererURL = new URL(referer);
        if (isValidReferer(refererURL.getHost())) {
            refererHost = refererURL.getHost();
        } else {
            throw new VedantuException(VedantuErrorCode.INVALID_REFERER);
        }
        logger.info("finding Organization by referehost" + refererHost);
        logger.info("finding Organization by refere");
        Organization organization = organizationRepo.findByReferer(refererHost);
        if (organization == null) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
        }

        GetOrgRes getOrgRes = getOrgRes(organization, getKey);

        getOrgRes.setAuthType(organization.getAuthType());
        getOrgRes.setReferer(organization.referer);
        logger.info("ended");
        return getOrgRes;
    }

    private GetOrgRes updateOrganizationReferrer(UpdateOrgRefererReq updateOrgRefererReq) throws VedantuException {
        if (!updateOrgRefererReq.remove) {

            if (!updateOrgRefererReq.getReferer().isEmpty() || !isValidReferer(updateOrgRefererReq.getReferer())) {
                logger.error("invalid slug: " + updateOrgRefererReq.getReferer());
                throw new VedantuException(VedantuErrorCode.INVALID_REFERER);
            }
        }
        // TODO: make check for permission of the user to update the organization referer

        Optional<Organization> organization = organizationRepo.findById(updateOrgRefererReq.getOrgId());
        if (!organization.isPresent()) {
            throw new VedantuException(VedantuErrorCode.INVALID_CODE, "the given orgId Not Found");
        }
        organization.get().setReferer(updateOrgRefererReq.getReferer());
        organizationRepo.save(organization.get());
        GetOrgRes getOrgRes = getOrgRes(organization.get(), false);
        getOrgRes.setReferer(organization.get().getReferer());
        return getOrgRes;
    }

    private boolean isValidReferer(String referer) {
        try {
            boolean checkReferrerEnabled = Boolean.valueOf(checkReferEnable);
            if (!checkReferrerEnabled) {
                return true;
            }
            DigExecutor executor = new DigExecutor();
            executor.setMatchCNAME(referer);
            String localSetup = depolymentDomain;
            return executor.match(localSetup);
        } catch (VedantuException e) {
            logger.error("Failed to resolve referer", e);
            return false;
        }
    }

    private GetOrgRes getOrgBySlug(GetOrgBySlugReq getOrgBySlugReq) throws VedantuException {

        Organization organization = getOrganizationBySlug(getOrgBySlugReq.getSlug());

        if (null == organization) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
        }
        GetOrgRes getOrgRes = getOrgRes(organization, getOrgBySlugReq.getKey);
        getOrgRes.setAuthType(organization.getAuthType());

        return getOrgRes;
    }

    private GetOrgRes updateOrgClassroomConnectStatus(UpdateOrganizationClassroomConnectStatusReq updateOrganizationClassroomConnectStatusReq) {
        Optional<Organization> organization = organizationRepo.findById(updateOrganizationClassroomConnectStatusReq.getOrgId());
        if (organization.isPresent()) {
            organization.get().setShowClassroomConnect(updateOrganizationClassroomConnectStatusReq.isShowClassroomConnect());
            organizationRepo.save(organization.get());
        }
        GetOrgRes getOrgRes = getOrgRes(organization.get(), false);
        return getOrgRes;
    }

    private GetOrgRes updateOrgSharedSubjects(UpdateOrganizationSharedSubjectsReq updateOrganizationSharedSubjectsReq) {
        Optional<Organization> organization = organizationRepo.findById(updateOrganizationSharedSubjectsReq.getOrgId());
        if (organization.isPresent()&&organization.get()!=null) {
            organization.get().setShowSharedSubjects(updateOrganizationSharedSubjectsReq.isShowSharedSubjects());
            organizationRepo.save(organization.get());
        } else{
            throw new VedantuException(VedantuErrorCode.INVALID_CODE,"the given orgId is invalid");
        }
        GetOrgRes getOrgRes = getOrgRes(organization.get(), false);
        return getOrgRes;

    }

    private GetOrgRes updateOrgDownloadStatus(UpdateOrganizationDownloadStatusReq updateOrgStatusReq) {
        Optional<Organization> organization = organizationRepo.findById(updateOrgStatusReq.getOrgId());
        if (organization.isPresent()) {
            organization.get().setDisableDownload(updateOrgStatusReq.disableDownload);
            organizationRepo.save(organization.get());
        }
        else{
            throw new VedantuException(VedantuErrorCode.INVALID_CODE,"the given orgId is in valid");
        }
        GetOrgRes getOrgRes = getOrgRes(organization.get(), false);
        return getOrgRes;


    }

    private GetOrgRes updateOrgStatus(UpdateOrgStatusReq updateOrgStatusReq) throws VedantuException {

        if (OrganizationStatus.valueOfKey(updateOrgStatusReq.getStatus()) == OrganizationStatus.UNKNOWN) {
            logger.error("invalid status: " + updateOrgStatusReq.status);
            throw new VedantuException(VedantuErrorCode.INVALID_STATUS);
        }
        Optional<Organization> organization = organizationRepo.findById(updateOrgStatusReq.getOrgId());
        if (organization.isPresent()) {
            if(updateOrgStatusReq.getType().equals("STUDENT")){
                organization.get().setStudentPageStatus(OrganizationStatus.valueOfKey(updateOrgStatusReq.getStatus()));
            }
            else{
                organization.get().setStatus(OrganizationStatus.valueOfKey(updateOrgStatusReq.getStatus()));
            }
            organizationRepo.save(organization.get());
        }else{
            throw new VedantuException(VedantuErrorCode.INVALID_CODE,"the given orgId is invalid");
        }

        GetOrgRes getOrgRes = getOrgRes(organization.get(), false);
        return getOrgRes;

    }

    private GetOrgRes updateSlugOfOrganization(UpdateOrgSlugReq updateOrgSlugReq) throws VedantuException {

        if (!isValidSlug(updateOrgSlugReq.getSlug())) {
            logger.error("invalid slug: " + updateOrgSlugReq.getSlug());
            throw new VedantuException(VedantuErrorCode.INVALID_SLUG);
        }
        Optional<Organization> organization = organizationRepo.findById(updateOrgSlugReq.getOrgId());


        Organization slugOrg = getOrganizationBySlug(updateOrgSlugReq.getSlug());
        if (slugOrg != null) {
            // given slug is already taken
            logger.error("slug[" + updateOrgSlugReq.getSlug() + "] already taken");
            throw new VedantuException(VedantuErrorCode.SLUG_NOT_AVAILABLE);
        }
        if(!organization.isPresent()){
            throw new VedantuException(VedantuErrorCode.INVALID_CODE,"the given orgId is not found");

        }
        // TODO: make check for permission of the user to update the organization slug
        organization.get().setSlug(updateOrgSlugReq.getSlug());
        organizationRepo.save(organization.get());
        GetOrgRes getOrgRes = getOrgRes(organization.get(), false);
        return getOrgRes;
    }

    private Organization getOrganizationBySlug(String slug) {


        Organization organization = organizationRepo.findBySlug(slug.trim());

        if (null == organization) {
            logger.error("cannot find organization for slug: " + slug);
        }

        return organization;
    }

    private CheckAppVersionRes getcheckAppVersion(CheckAppVersionReq req) throws VedantuException {
        Boolean b = true;
        CheckAppVersionRes resp = new CheckAppVersionRes();
        logger.debug("..... Inside checkAppVersion function..... orgId : "
                + req.getOrgId() + "  reqVersionCode  " + req.getAppVersion());

        if (ObjectIdUtils.hasInvalidId(req.getOrgId())) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }
        Optional<Organization> organization = organizationRepo.findById(req.getOrgId());
        if (!organization.isPresent()) {
            throw new VedantuException(VedantuErrorCode.INVALID_CODE, "org not found");
        }
        b = req.getAppVersion()<=organization.get().getVersionCode();
        resp.setAppVersion(b);
        return resp;

    }

    private CheckSlugRes getCheckSlug(CheckSlugReq checkSlugReq) throws VedantuException {
        if (!isValidSlug(checkSlugReq.getSlug())) {
            logger.error("invalid slug: " + checkSlugReq.getSlug());
            throw new VedantuException(VedantuErrorCode.INVALID_SLUG);
        }
        Organization organization = organizationRepo.findBySlug(checkSlugReq.getSlug().trim());
        CheckSlugRes res = new CheckSlugRes();
        res.available = organization == null;
        return res;
    }

    public static boolean isValidSlug(String slug) {

        return !slug.matches(".*([^a-zA-Z0-9.]).*");
    }

    private CheckSlugRes getCheckWebsite(CheckWebsiteReq checkWebsiteReq) throws VedantuException {

        if (!isValidWebsite(checkWebsiteReq.getWebsite())) {
            logger.error("invalid website: " + checkWebsiteReq.website);
            throw new VedantuException(VedantuErrorCode.INVALID_WEBSITE);
        }
        Organization organization = organizationRepo.findByWebsite(checkWebsiteReq.getWebsite().trim());
        CheckSlugRes res = new CheckSlugRes();
        res.available = organization == null;
        return res;

    }

    public static boolean isValidWebsite(String website) {

        try {
            @SuppressWarnings("unused")
            URL url = new URL(website);
        } catch (MalformedURLException e) {
            return false;
        }
        return true;
    }

    private GetOrgResForInvoice getOrganizationInfoFormationInvoice(GetOrgReq getOrgReq) throws VedantuException {
        Optional<Organization> getorganization = organizationRepo.findById(getOrgReq.getOrgId());
        if (!getorganization.isPresent()) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID,"the given orgId is invalid");
        }
        Organization organization = getorganization.get();
        GetOrgResForInvoice getOrgResForInvoice = new GetOrgResForInvoice();
        getOrgResForInvoice.setAddress(organization.getAddress());
        getOrgResForInvoice.setName(organization.getName());
        getOrgResForInvoice.setRepresentative(organization.getRepresentative());
        getOrgResForInvoice.setOrgThumbnail(organization.getThumbnail());
        getOrgResForInvoice.contactNumber = organization.contactNumber;
        getOrgResForInvoice.locations = organization.locations;
        return getOrgResForInvoice;



    }

    private GetShowSharedSubjectsRes getShowSharedSubject(GetOrgReq getOrgReq) {
        Optional<Organization> getShowSharedSubjects = organizationRepo.findById(getOrgReq.getOrgId());
        if(!getShowSharedSubjects.isPresent()){
            throw new VedantuException(VedantuErrorCode.INVALID_CODE,"orgId is invalid");
        }
        GetShowSharedSubjectsRes res = new GetShowSharedSubjectsRes();
        res.showSharedSubjects = getShowSharedSubjects.get().showSharedSubjects;
        return res;
    }


    private GetOrgRes getorganization(GetOrgReq getOrgReq) throws VedantuException {

        Optional<Organization> organization = organizationRepo.findById(getOrgReq.getOrgId());
        Optional<BoardMapping> boardMapping = boardMappingRepo.findBySharedToOrgId(getOrgReq.getOrgId());
        if (!organization.isPresent()) {
            logger.error("cannot find organization for id: " + getOrgReq.orgId);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND,
                    "no org found with id: " + getOrgReq.orgId);
        }
        if (!boardMapping.isPresent()) {
           // throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND,"no boardmapping found with id: " + getOrgReq.orgId);
        }
        Organization organization1 = organization.get();
        GetOrgRes getOrgRes = getOrgRes(organization1, getOrgReq.getKey);
        getOrgRes.setShowSharedSubjects(organization1.showSharedSubjects);
        getOrgRes.setShowClassroomConnect(organization1.showClassroomConnect);
        getOrgRes.setDisableDownload(organization1.disableDownload);
        getOrgRes.setAuthType(organization1.getAuthType());
        getOrgRes.setPointsOfSale(organization1.getPointsOfSale());
        getOrgRes.setDoubtsForumMode(organization1.getDoubtsForumMode());
        getOrgRes.setDisableSignup(organization1.disableSignup);
        getOrgRes.setDisableSignupMessage(organization1.getDisableSignupMessage());
        getOrgRes.setNewUI(organization1.isNewUI);
        getOrgRes.setTheme(organization1.getTheme());
        getOrgRes.setStatus(organization1.getStatus());
        getOrgRes.setStudentPageStatus(organization1.getStudentPageStatus());
        if (!boardMapping.isEmpty()) {
            getOrgRes.setSharedQuestionsState((boardMapping.get().publish) ? "ENABLED" : "DISABLED");
        } else {
            getOrgRes.setSharedQuestionsState("N.A");
        }

        if ((getOrgReq.getUserId() != null)
                && getOrgReq.getUserId().equalsIgnoreCase(organization1.getAdminUserId())) {
            getOrgRes.setNeedsTnCAcceptance(true);
            getOrgRes.setEndPoint(organization1.getEndPoint());
            getOrgRes.setSocialMedia(organization1.getSocialMedia());
            getOrgRes.setReferer(organization1.getReferer());
            if (getOrgRes.getEndPoint() != null) {
                getOrgRes.endPoint.convertNullToEmptyValues();
            }
            getOrgRes.setLatestTnCVersion(TNC_VERSION);
            getOrgRes.setAppInfos(organization1.getAppInfos());
            if (organization1.getTncAcceptance() != null) {

                getOrgRes.setAcceptedTNCVersion(organization1.getTncAcceptance().getVersion());
                if (organization1.getTncAcceptance().getVersion().equalsIgnoreCase(getOrgRes.latestTnCVersion)) {
                    getOrgRes.setNeedsTnCAcceptance(false);
                }
            }
        }

        return getOrgRes;
    }

    private GetOrgRes getOrgRes(Organization organization, boolean addKey) {
        LicensingInfo licensingPlan = null;
        LicensingPlan licensingPlan1 = null;
        if (organization.getSubscription() != null) {
            if (!organization.getSubscription().getPlanId().isEmpty())
                licensingPlan1 = getBasicInfo(organization.getSubscription().getPlanId());
            licensingPlan = new LicensingInfo(licensingPlan1);

        }

        GetOrgRes getOrgRes = new GetOrgRes(
                organization._getStringId(),
                organization.getName(),
                organization.getFullName(),
                organization.getWebsite(),
                organization.getEmailDomain(),
                organization.getContactNumber(),
                organization.getType(),
                organization.getLocations(),
                organization.getAddress(),
                organization.getDescription(),
                organization.getScope(),
                organization.getRepresentative(),
                organization.getThumbnail(),
                organization.getSlug(),
                organization.getEncLevel(),
                licensingPlan,
                organization.getTncAcceptance(), organization.getSocialMedia(), organization.getAppInfos());


        getOrgRes.setAdminUserId(organization.getAdminUserId());
        getOrgRes.setReferer(organization.getReferer());
        getOrgRes.setDoubtsForumMode(organization.getDoubtsForumMode());
        getOrgRes.setShowClassroomConnect(organization.showClassroomConnect);
        getOrgRes.setDisableDownload(organization.disableDownload);
        getOrgRes.setDisableSignup(organization.disableSignup);
        getOrgRes.setDisableSignupMessage((organization.getDisableSignupMessage() == null) ? "SignUp is Temporarily Disabled" : organization.getDisableSignupMessage());
        getOrgRes.setCommunicationMail(organization.getCommunicationMail());
        getOrgRes.setSmtpHost(organization.getSmtpHost());
        getOrgRes.setSmtpUser(organization.getSmtpUser());
        getOrgRes.setSmtpPassword(organization.getSmtpPassword());
        getOrgRes.setInstaMojoClientId(organization.getInstaMojoClientId());
        getOrgRes.setInstaMojoClientSecret(organization.getInstaMojoClientSecret());
        getOrgRes.setInstaMojoApiKey(organization.getInstaMojoApiKey());
        getOrgRes.setInstaMojoAuthToken(organization.getInstaMojoAuthToken());
        getOrgRes.setVersionCode(organization.getVersionCode());
        getOrgRes.setStatus(organization.getStatus());
        getOrgRes.setStudentPageStatus(organization.getStudentPageStatus());
        getOrgRes.setShowSharedSubjects(organization.showSharedSubjects);
        if (addKey) {
            try {
                getOrgRes.setKey(getPublicKey(organization));
            } catch (VedantuException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return getOrgRes;
    }

    private String getPublicKey(Organization organization) throws VedantuException {
         if (organization == null) {
                    return null;
                }
                SecurityCredentials credentials = organization.credentials;
                if (credentials == null) {
                    credentials = setCredentials(organization);
    }
                 return Base64.encode(credentials.getPublicKey());
    }

    private SecurityCredentials setCredentials(Organization organization) throws VedantuException {

        if (organization.credentials != null) {
            return organization.credentials;

                    }
                    organization.credentials = EncryptionUtils.generateKeys();
                    organizationRepo.save(organization);
                    return organization.getCredentials();

    }

    private LicensingPlan getBasicInfo(String planId) {
        Optional<LicensingPlan> licensingPlan = licensingPlanRepo.findById(planId);
        return licensingPlan.get();
    }
    @Override
	public VedantuResponse updateOrgMemberExtraInputFields(
			UpdateOrgMemberExtraInfoInputFieldsReq updateOrgMemberExtraInfoInputFieldsReq) throws VedantuException {
		if (ObjectIdUtils.hasInvalidId(updateOrgMemberExtraInfoInputFieldsReq.orgId)) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID);

        }

        UpdateOrgMemberExtraInfoInputFieldsRes updateOrgMemberExtraInputFieldsRes = updateOrganizationMemberExtraInputFields(updateOrgMemberExtraInfoInputFieldsReq);

		return new VedantuResponse(updateOrgMemberExtraInputFieldsRes);
	}


    public  UpdateOrgMemberExtraInfoInputFieldsRes updateOrganizationMemberExtraInputFields(
            UpdateOrgMemberExtraInfoInputFieldsReq req) throws VedantuException {


       Organization organization = organizationRepo.findById(req.orgId).get();


        // this operation will only be allowed to SUPER_ADMIN (ADMIN who created this organization)
        if (organization.adminUserId == null || !organization.adminUserId.equals(req.userId)) {
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED,
                    "this operation is only allowed to super admin");
        }

        // only update for provided OrgMemberProfile
        if (organization.extraMemberInfoFields == null) {
            organization.extraMemberInfoFields = new HashMap<OrgMemberProfile, List<InputFieldInfo>>();
        }

        if (!(CollectionUtils.isEmpty(req.fields))) {
            for (InputFieldInfo info : req.fields) {
            	if (!info.validate()) {
                    throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
                }
            }
        }
        organization.extraMemberInfoFields.put(req.targetOrgMemberProfile, req.fields);
        organizationRepo.save(organization);


        UpdateOrgMemberExtraInfoInputFieldsRes res = new UpdateOrgMemberExtraInfoInputFieldsRes();
        res.targetOrgMemberProfile = req.targetOrgMemberProfile;
        res.fields = organization.extraMemberInfoFields.get(req.targetOrgMemberProfile);
        return res;
    }
    @Override
	public VedantuResponse updateDigitalLibraryFields(UpdateDigitalLibraryFieldsReq updateDigitalLibraryFieldsReq)
			throws VedantuException {
		if (ObjectIdUtils.hasInvalidId(updateDigitalLibraryFieldsReq.getOrgId())) {

          throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }
        UpdateDigitalLibraryFieldsRes res = new UpdateDigitalLibraryFieldsRes();

        	Optional<Organization> organizationOptional = organizationRepo.findById(updateDigitalLibraryFieldsReq.orgId) ;//getOrganizationById(req.orgId);
            Organization organization =  organizationOptional.get();
            if(organizationOptional.isPresent()) {

            // this operation will only be allowed to SUPER_ADMIN (ADMIN who created this organization)
            if (organization.adminUserId == null || !organization.adminUserId.equals(updateDigitalLibraryFieldsReq.userId)) {
                throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED,
                        "this operation is only allowed to super admin");
            }

            // only update for provided OrgMemberProfile
            if (organization.digitalLibraryHiddenFields == null) {
                organization.digitalLibraryHiddenFields = new HashSet<String>();
            }
            if (updateDigitalLibraryFieldsReq.fields == null){
                organization.digitalLibraryHiddenFields.clear();
            }else{
                organization.digitalLibraryHiddenFields.clear();
                for (String field : updateDigitalLibraryFieldsReq.fields) {
                    organization.digitalLibraryHiddenFields.add(field);
                }
            }
            organizationRepo.save(organization);
            }else {
            	throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND,
                        "no org found with id: ");
            }
            res.fields = organization.digitalLibraryHiddenFields;

        return new VedantuResponse(res);
    }
        @Override
    	public VedantuResponse getDigitalLibraryFields(GetDigitalLibraryFieldsReq getDigitalLibraryFieldsReq)
    			throws VedantuException {
    		if (ObjectIdUtils.hasInvalidId(getDigitalLibraryFieldsReq.getOrgId())) {

                throw new VedantuException(VedantuErrorCode.INVALID_ID);
    	        }

            GetDigitalLibraryFieldsRes res = new GetDigitalLibraryFieldsRes();
            Optional<Organization> organizationOptional = organizationRepo.findById(getDigitalLibraryFieldsReq.getOrgId());
    		Organization organization = organizationOptional.get();
    		if(organizationOptional.isPresent()) {
    		if (organization.digitalLibraryHiddenFields == null) {
    		    organization.digitalLibraryHiddenFields = new HashSet<String>();
    		}
    		res.fields = organization.digitalLibraryHiddenFields;
    		if (res.fields == null) {
    		    res.fields = new HashSet<String>();
    		}
    		}else {
    			throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND,
                        "no org found with id: ");
    		}

            return new VedantuResponse(res);

    	}
        @Override
    	public VedantuResponse checkIfSuperAdmin(CheckIfSuperAdminReq checkIfSuperAdminReq) throws VedantuException{
    		CheckIfSuperAdminRes response = new CheckIfSuperAdminRes();
            boolean result = false;
    		Organization organization = organizationRepo.findByIdAndAdminUserId(checkIfSuperAdminReq.getOrgId(), checkIfSuperAdminReq.getUserId());
    		if(organization!=null) {
    			result = true;
    		}

    		response.isSuperAdmin = result;

            return new VedantuResponse(response);
    	}

    	@Override
    	public VedantuResponse getOrgPointsOfSale(AbstractOrgScopeReq abstractOrgScopeReq) throws VedantuException{

    		if(abstractOrgScopeReq == null) {
    			throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                        "parameters are missing");
    		}
    		GetOrgPointsOfSaleRes res = new GetOrgPointsOfSaleRes();
            Optional<Organization> orgOptional = organizationRepo.findById(abstractOrgScopeReq.getOrgId());
            if (orgOptional.isPresent()) {
                Organization org = orgOptional.get();
                res.pointsOfSale = org.pointsOfSale == null ? new ArrayList<String>() : org.pointsOfSale;
            } else {
                throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND,
                        "no org found with id: ");
            }
            return new VedantuResponse(res);
        }

    @Override
    public VedantuResponse getOrgMemberExtraInputFields(
            GetOrgMemberExtraInfoInputFieldsReq getOrgMemberExtraInfoInputFieldsReq)
            throws VedantuException {
        if (ObjectIdUtils.hasInvalidId(getOrgMemberExtraInfoInputFieldsReq.getOrgId())) {

            throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }
        if (getOrgMemberExtraInfoInputFieldsReq.checkIfSignupAllowed) {

            long openedSectionCount = orgSectionRepo.countByAccessScopeAndOrgId(AccessScope.OPEN, getOrgMemberExtraInfoInputFieldsReq.orgId);

            Long totalHits = granteeOrgProgramRepo.countBySubscriberOrgIdAndRecordState(getOrgMemberExtraInfoInputFieldsReq.getOrgId(), VedantuRecordState.ACTIVE);
            long totalSections = openedSectionCount + totalHits.longValue();
            if (totalSections < 1) {
                String errorMsg = "no program section is opened for signup for orgId: " + getOrgMemberExtraInfoInputFieldsReq.getOrgId();
                throw new VedantuException(VedantuErrorCode.ORG_SIGNUP_NOT_SUPPORTED, errorMsg);
            }
        }
        Optional<Organization> organizationOptional = organizationRepo.findById(getOrgMemberExtraInfoInputFieldsReq.getOrgId());

        GetOrgMemberExtraInfoInputFieldsRes res = new GetOrgMemberExtraInfoInputFieldsRes();

        if (organizationOptional.isPresent()) {
            Organization organization = organizationOptional.get();
            if (organization.disableSignup) {
                String errorMsg = StringUtils.isEmpty(organization.disableSignupMessage) ? "SignUp is Temporarily Disabled" : organization.disableSignupMessage;
                throw new VedantuException(VedantuErrorCode.ORG_SIGNUP_NOT_SUPPORTED, errorMsg);
            }
            res.targetOrgMemberProfile = getOrgMemberExtraInfoInputFieldsReq.targetOrgMemberProfile;

            if (organization.extraMemberInfoFields == null) {
                organization.extraMemberInfoFields = new HashMap<OrgMemberProfile, List<InputFieldInfo>>();
            }
            res.enableOTP = organization.enableOTP;
            res.fields = organization.extraMemberInfoFields.get(getOrgMemberExtraInfoInputFieldsReq.targetOrgMemberProfile);
            if (res.fields == null) {
                res.fields = new ArrayList<InputFieldInfo>();
            }
        } else {

        }
        return new VedantuResponse(res);
    }

    public User addUserToRepo(AddUserReq addUserReq, AtomicBoolean isEmailVerificationNeeded,
                              SecurityCredentials credentials, SocialInfo socialIn
    ) throws VedantuException {
        {

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
    public String getUserPassHash(String username, String password, boolean isOnlyCheck) {

        logger.error("getUserPassHash username: " + username);
        String pass= getSaltedPassword(username,password,isOnlyCheck);
        String hashedPass = getHashed(pass, "SHA-256");
        logger.error("Hashed password for username: " + username + " : " + hashedPass);
        return hashedPass;
    }
    public static String getHashed(String input, String hashType) {
        try {
            MessageDigest m = MessageDigest.getInstance(hashType);
            byte[] out = m.digest(input.getBytes());
            return Base64.encode(out);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    public String getSaltedPassword(String username, String password, boolean isOnlyCheck) {


        UserSalt userSalt =userSaltrepo.findByUsername(username);

        if(userSalt==null) {
            logger.debug("user-salt not found for username: " + username);

            if (isOnlyCheck) {
                logger.debug("will not create new user-salt for username: "
                        + username);
                return HardCodedConstants.emptyString;
            }

            userSalt = new UserSalt(username, UUID.randomUUID().toString());
            userSaltrepo.save(userSalt);
        }

        String saltedPassword = userSalt.getSalt() + SYSTEM_SALT + password;
        return saltedPassword;

    }
    public String generatePasswordUpdate(String id, String getStringId, String callingAppId) {
        Optional<User> user = userRepo.findById(id);
        if (!user.isPresent()) {
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }

        if (user.get().getForgotPasswordReq()== null) {
            user.get().setForgotPasswordReq( new ForgotPasswordReqInfo(UUID.randomUUID().toString()));
            userRepo.save(user.get());
            logger.debug("updated password saved user: " + user);
        } else {
            logger.debug("generateForgotPasswordReq user already has a forgotPasswordReq for user: "
                    + user);
        }

        return generatePasswordResetURL(user.get(), id, callingAppId );
    }
    private String generatePasswordResetURL(User user, String orgId, String callingAppId) throws VedantuException {
        final String emailVerifyHost = Configurations.getAppLearnHost(callingAppId);

        final String emailVerifyEndPoint = EmailConfigurationConstants.EMAIL_FORGOTPASSWORD_ENDPOINT;

        URLGenerator generator = new URLGenerator();
        generator.host = emailVerifyHost;
        generator.endPoint = emailVerifyEndPoint;
        generator.protocol = Configurations.APP_PROTOCOL;

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("code", URLEncoder.encode(user.forgotPasswordReq.verificationCode, StandardCharsets.UTF_8));
        params.put("userId", URLEncoder.encode(user._getStringId(), StandardCharsets.UTF_8));
        params.put("email", URLEncoder.encode(user.email, StandardCharsets.UTF_8));
        if (!orgId.isEmpty()) {
            logger.debug("Update happen in organization" + orgId);
            params.put("orgId", URLEncoder.encode(orgId, StandardCharsets.UTF_8));
        }

        generator.params = params;
        return generator.generate();

    }
    public Boolean generateEmailVerificationEvent(User user, String orgId, String callingAppId) throws VedantuException {

        // TODO: verification of user email needs to be done through event
        final String emailVerifyHost = Configurations.getAppLearnHost(callingAppId);
        final String emailVerifyEndPoint = EmailConfigurationConstants.EMAIL_VERIFICATION_ENDPOINT;

        URLGenerator generator = new URLGenerator();
        generator.host = emailVerifyHost;
        generator.endPoint = emailVerifyEndPoint;
        generator.protocol = Configurations.APP_PROTOCOL;

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("code", URLEncoder.encode(user.emailChangeReq.verificationCode, StandardCharsets.UTF_8));
        params.put("userId", URLEncoder.encode(user.getUsername(), StandardCharsets.UTF_8));
        params.put("email", URLEncoder.encode(user.getEmailChangeReq().getEmail(), StandardCharsets.UTF_8));
        if (orgId.isEmpty()) {
            params.put("orgId", URLEncoder.encode(orgId, StandardCharsets.UTF_8));
        }

        generator.params = params;

        EmailVerificationDetails details;
        try {
            details = new EmailVerificationDetails();
        } catch (Exception e) {
            logger.debug(" Not found email details", e);
            throw new VedantuException(VedantuErrorCode.EMAIL_CAN_NOT_BE_SEND);
        }

        details.user = new UserEmailInfo();
        //  details.user.fromUserExtendedInfo((UserExtendedInfo) user.toExtendedInfo());
        details.user.fromUserExtendedInfo(user);
        details.verificationLink = generator.generate();
        details.orgId = orgId;
        details.addRecepient(details.user.getFullName(), user.emailChangeReq.email);
        generateEventAysc(user._getStringId(), details, EventType.SEND_INSTANT_EMAIL);

        return true;
    }

    private void generateEventAysc(String userId, EmailVerificationDetails details, EventType eventType) {
        CompletableFuture.runAsync(() -> {
            eventUtil.generateEvent(eventType, null, userId, details,
                    details.__getSrcEntity(), UserActionType.EventActionType.ADD, 0);
        });

    }

    @Override
    public VedantuResponse getCenters(GetOrgCentersReq getOrgCentersReq) throws VedantuException {
        if (ObjectIdUtils.hasInvalidId(getOrgCentersReq.getOrgId())) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }
        GetOrgCentersRes getOrgCentersRes = new GetOrgCentersRes();
        try {


            List<OrgCenter> centers = orgCenterRepo.findByOrgIdOrderByCNameAsc(getOrgCentersReq.getOrgId());

            getOrgCentersRes.totalHits = centers.size();
            List<OrgCenterInfo> centerInfos = toOrgCenterInfo(centers);
            getOrgCentersRes.list.addAll(centerInfos);
        } catch (VedantuException e) {
            throw e;
        }

        return new VedantuResponse(getOrgCentersRes);

    }

    public List<OrgCenterInfo> toOrgCenterInfo(List<OrgCenter> centers) {
        List<OrgCenterInfo> centerInfos = new ArrayList<OrgCenterInfo>();
        if (!CollectionUtils.isEmpty(centers)) {
            for (OrgCenter center : centers) {
                if (null == center) {
                    continue;
                }
                OrgCenterInfo centerInfo = new OrgCenterInfo(
                        center._getStringId(), center.getName(), center.code,
                        center.recordState);
                centerInfos.add(centerInfo);
            }
            //Collections.sort(centerInfos,OrgStructureInfoNameComparator.INSTANCE);
            centerInfos.sort((OrgCenterInfo orgCenterInfo1, OrgCenterInfo orgCenterInfo2) -> orgCenterInfo1.getName().compareTo(orgCenterInfo2.getName()));
        }
        return centerInfos;
    }

    @Override
    public VedantuResponse addCenter(AddOrgCenterReq addOrgCenterReq) throws VedantuException {
        if (ObjectIdUtils.hasInvalidId(addOrgCenterReq.orgId)) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID);

        }
        AddOrgCenterRes addOrgCenterRes = new AddOrgCenterRes();

        try {
            OrgCenter orgCenter = addCenter(
                    addOrgCenterReq.orgId, addOrgCenterReq.code,
                    addOrgCenterReq.name);

            addOrgCenterRes.id = orgCenter._getStringId();
            addOrgCenterRes.recordState = orgCenter.recordState;
        } catch (VedantuException e) {
            throw e;
        }

        return new VedantuResponse(addOrgCenterRes);
    }

    public OrgCenter addCenter(String orgId, String code, String name)
            throws VedantuException {
        OrgCenter orgCenter = orgCenterRepo.findByOrgIdAndCodeOrderByCNameAsc(orgId, code);
        try {
            if (null != orgCenter) {
                if (VedantuRecordState.ACTIVE == orgCenter.recordState) {

                    throw new VedantuException(
                            VedantuErrorCode.ORGANIZATION_CENTER_ALREADY_EXISTS);
                } else {

                    orgCenter.setName(name);
                    orgCenter.recordState = VedantuRecordState.ACTIVE;
                    orgCenterRepo.save(orgCenter);
                    return orgCenter;
                }
            }

            orgCenter = new OrgCenter(orgId, code, name);
            orgCenterRepo.save(orgCenter);
        } catch (Exception exception) {

            throw new VedantuException(
                    VedantuErrorCode.ORGANIZATION_CENTER_ALREADY_EXISTS);
        }
        return orgCenter;
    }

    @Override
    public VedantuResponse updateCenter(UpdateOrgCenterReq updateOrgCenterReq) throws VedantuException {
        if (ObjectIdUtils.hasInvalidId(updateOrgCenterReq.getOrgId(), updateOrgCenterReq.getCenterId())) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID);

        }
        UpdateOrgCenterRes updateOrgCenterRes = new UpdateOrgCenterRes();
        try {
            OrgCenter orgCenter = updateCenter(updateOrgCenterReq.getOrgId(), updateOrgCenterReq.getCenterId(),
                    updateOrgCenterReq.code, updateOrgCenterReq.name);

            updateOrgCenterRes.id = orgCenter._getStringId();
            updateOrgCenterRes.recordState = orgCenter.recordState;

        } catch (VedantuException e) {
            throw e;
        }

        return new VedantuResponse(updateOrgCenterRes);
    }

    public OrgCenter updateCenter(String orgId, String centerId, String code, String name) throws VedantuException {
        OrgCenter orgCenter = null;
        try {
            Optional<OrgCenter> orgCenterOptional = orgCenterRepo.findById(centerId);
            if (orgCenterOptional.isPresent()) {
                orgCenter = orgCenterOptional.get();
                if (!(orgCenter.orgId.equals(orgId))) {

                    throw new VedantuException(VedantuErrorCode.INVALID_ID);
                }
            } else {
                throw new VedantuException(VedantuErrorCode.ORGANIZATION_CENTER_NOT_FOUND);
            }
            orgCenter.code = code;
            orgCenter.setName(name);
            orgCenterRepo.save(orgCenter);
        } catch (Exception exception) {

            throw new VedantuException(VedantuErrorCode.ORGANIZATION_CENTER_ALREADY_EXISTS);
        }
        return orgCenter;
    }

    @Override
    public VedantuResponse removeCenter(StateChangeOrgCenterReq stateChangeOrgCenterReq) throws VedantuException {
        if (ObjectIdUtils.hasInvalidId(stateChangeOrgCenterReq.getOrgId(), stateChangeOrgCenterReq.getCenterId())) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID);

        }
        StateChangeOrgCenterRes stateChangeOrgCenterRes = null;
        try {
            OrgCenter orgCenter = getCenterById(stateChangeOrgCenterReq.getOrgId(), stateChangeOrgCenterReq.getCenterId());
            orgCenter.setRecordState(VedantuRecordState.DELETED);
            orgCenterRepo.save(orgCenter);
            stateChangeOrgCenterRes = new StateChangeOrgCenterRes();
            stateChangeOrgCenterRes.id = orgCenter._getStringId();
            stateChangeOrgCenterRes.recordState = orgCenter.recordState;


        } catch (VedantuException e) {
            throw e;
        }

        return new VedantuResponse(stateChangeOrgCenterRes);
    }

    public OrgCenter getCenterById(String orgId, String centerId) throws VedantuException {
        OrgCenter orgCenter = null;
        Optional<OrgCenter> orgCenterOptional = orgCenterRepo.findById(centerId);
        if (orgCenterOptional.isPresent()) {
            orgCenter = orgCenterOptional.get();
            if (!(orgCenter.orgId.equals(orgId))) {
                throw new VedantuException(VedantuErrorCode.INVALID_ID);
            }
        } else {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_CENTER_NOT_FOUND);
        }
        return orgCenter;
    }

    @Override
    public VedantuResponse activateCenter(StateChangeOrgCenterReq stateChangeOrgCenterReq) {
        if (ObjectIdUtils.hasInvalidId(stateChangeOrgCenterReq.getOrgId(), stateChangeOrgCenterReq.getCenterId())) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID);

        }
        StateChangeOrgCenterRes stateChangeOrgCenterRes = null;

        try {

            OrgCenter orgCenter = getCenterById(stateChangeOrgCenterReq.getOrgId(),
                    stateChangeOrgCenterReq.getCenterId());
            try {
                orgCenter.setRecordState(VedantuRecordState.ACTIVE);
                orgCenterRepo.save(orgCenter);
            } catch (Exception exception) {
                throw new VedantuException(VedantuErrorCode.ORGANIZATION_CENTER_ALREADY_EXISTS);
            }

            stateChangeOrgCenterRes = new StateChangeOrgCenterRes();
            stateChangeOrgCenterRes.id = orgCenter._getStringId();
            stateChangeOrgCenterRes.recordState = orgCenter.recordState;

        } catch (VedantuException e) {
            throw e;
        }

        return new VedantuResponse(stateChangeOrgCenterRes);

    }

    @Override
    public VedantuResponse getInstaMojoAccessToken(AbstractOrgScopeReq abstractOrgScopeReq) throws VedantuException {
        abstractOrgScopeReq.orgId = abstractOrgScopeReq.orgId.substring(1, abstractOrgScopeReq.orgId.length() - 1);
        InstaMojoAccessTokenResp getInstaMojoAccessTokenResp = getInstaMojoAccessTokenFromPHP(abstractOrgScopeReq);
        if(getInstaMojoAccessTokenResp.access_token!=null){
        	getInstaMojoAccessTokenResp.access_token = instamoEnv+getInstaMojoAccessTokenResp.access_token;
    }
        return new VedantuResponse(getInstaMojoAccessTokenResp);
    }

    public InstaMojoAccessTokenResp getInstaMojoAccessTokenFromPHP(AbstractOrgScopeReq getOrganizationReq) {
        String INSTAMOJOCLIENTID = instamoClientid;
        String INSTAMOJOCLIENTSECRETKEY = instamoClientsecretkey;
        // GETTING ANIL NAIR'S CREDENTIALS for anc and skillmithra
        Optional<Organization> orgOptional = organizationRepo.findById(getOrganizationReq.orgId);
        if (orgOptional.isPresent()) {
            Organization org = orgOptional.get();
            if (!StringUtils.isEmpty(org.instaMojoClientId) && !StringUtils.isEmpty(org.instaMojoClientSecret)) {

                INSTAMOJOCLIENTID = org.instaMojoClientId;
                INSTAMOJOCLIENTSECRETKEY = org.instaMojoClientSecret;
            }
        }
        InstaMojoAccessTokenResp resp = new InstaMojoAccessTokenResp();
        HttpEntity<MultiValueMap<String, String>> request = null;
        try {

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
            map.add("grant_type", "client_credentials");
            map.add("client_id", INSTAMOJOCLIENTID);
            map.add("client_secret", INSTAMOJOCLIENTSECRETKEY);
            map.add("instamojo_url", instamoUrl);
            request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
        } catch (Exception e1) {
        }

        return restTemplate
                .postForEntity(instamoUrl, request, InstaMojoAccessTokenResp.class)
                .getBody();
    }

}