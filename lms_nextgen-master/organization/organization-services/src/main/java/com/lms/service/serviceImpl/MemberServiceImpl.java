package com.lms.service.serviceImpl;

import com.lms.billing.enums.ItemCategory;
import com.lms.billing.enums.OrderState;
import com.lms.billing.enums.TransactionStatus;
import com.lms.billing.enums.TransactionType;
import com.lms.billing.model.*;
import com.lms.billing.pojo.*;
import com.lms.billing.repository.*;
import com.lms.board.model.Board;
import com.lms.board.model.GranteeOrgProgram;
import com.lms.board.pojos.test.BoardBasicInfo;
import com.lms.board.repo.BoardRepo;
import com.lms.board.repo.GranteeOrgProgramRepo;
import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.fs.exception.FileStoreException;
import com.lms.common.fs.handlers.LocalFileSystemHandler;
import com.lms.common.utils.*;
import com.lms.common.vedantu.Repo.CounterRepo;
import com.lms.common.vedantu.commons.pojos.requests.*;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.constants.EmailConfigurationConstants;
import com.lms.common.vedantu.constants.HardCodedConstants;
import com.lms.common.vedantu.constants.config.Configurations;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.entity.storage.FileCategory;
import com.lms.common.vedantu.entity.storage.StorageResult;
import com.lms.common.vedantu.entity.storage.UserProfilePicEntityFileStorage;
import com.lms.common.vedantu.enums.*;
import com.lms.common.vedantu.http.URLGenerator;
import com.lms.common.vedantu.model.Counter;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.components.OrgMemberInfo;
import com.lms.enums.CampaignType;
import com.lms.enums.OrgMappingBulkOperationType;
import com.lms.enums.OrgMemberProfile;
import com.lms.enums.OrgMemberState;
import com.lms.models.*;
import com.lms.organization.auth.AuthHandler;
import com.lms.organization.auth.AuthHandlerFactory;
import com.lms.organization.auth.ExtAuthHandler;
import com.lms.parsers.StudentsXLParser;
import com.lms.pojo.*;
import com.lms.pojo.request.SendForgotPasswordEmailReq;
import com.lms.pojo.request.UnsetEmailReq;
import com.lms.pojo.request.UploadProfilePicReq;
import com.lms.pojo.request.*;
import com.lms.pojo.responce.*;
import com.lms.repository.*;
import com.lms.service.MemberService;
import com.lms.user.vedantu.user.dto.UserDto;
import com.lms.user.vedantu.user.enums.Gender;
import com.lms.user.vedantu.user.events.ForgotPasswordDetails;
import com.lms.user.vedantu.user.model.User;
import com.lms.user.vedantu.user.model.UserSalt;
import com.lms.user.vedantu.user.pojo.*;
import com.lms.user.vedantu.user.pojo.responce.*;
import com.lms.user.vedantu.user.repository.UserRepo;
import com.lms.user.vedantu.user.repository.UserSaltrepo;
import com.lms.user.vedantu.user.requests.*;
import org.apache.commons.io.IOUtils;
import org.bson.internal.Base64;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class MemberServiceImpl implements MemberService {
    public static final String CENTER_SECTION_SEPARATOR = "/";
    private static final Logger logger = LoggerFactory.getLogger(MemberServiceImpl.class);
    private static final int EMAILS_BATCH_SIZE = 100;
    private static final int DEFAULT_SIZE_STUDENT_BULK_OPERATION = 50;
    public final String SUPER_ADMIN_MEMBER_ID = "SUPER_ADMIN";
    public final int NO_START = 0;
    public final int NO_LIMIT = 0;
    private final int SYSTEM_GENERATED_PASS_LENGTH = 6;
    private final int DEFAULT_CAMPAIGN_PACKAGE_DAYS = 365;
    private final long FREEZE_TIME = 600000;
    private final String SYSTEM_SALT = "/vdntu/";
    @Value("${org.default.memberid.prefix}")
    public String REGEX_SYSGEN_MEMBER_ID_PATTERN;
    @Value("${org.default.memberid.prefix}")
    public String prefex;
    public AuthType authType;
    @Autowired
    OrganizationsImpl organizationsImpl;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private OrgMemberRepo orgMemberRepo;
    @Autowired
    private OrganizationRepo organizationRepo;
    @Value("${fs.local.basedir}")
    private String fsLocalBaseUrl;
    @Value("${org.tnc.version}")
    private String TNC_VERSION;
    @Value("${otp.size}")
    private Integer OTPSize;
    @Autowired
    private TestUserRepo testUserRepo;
    @Value("${neetdemo.section.id}")
    private String neetdemoSectionId;
    @Value("${neetdemo.program.id}")
    private String neetdemoProgramId;
    @Value("${jeedemo.center.id}")
    private String jeedemoCenterId;
    @Value("${jeedemo.section.id}")
    private String jeedemoSectionId;
    @Value("${jeedemo.program.id}")
    private String jeedemoProgramId;
    @Value("${neetseries.program.id}")
    private String neetseriesProgramId;
    @Value("${neetseries.center.id}")
    private String neetseriesCenterId;
    @Value("${neetseries.section.id}")
    private String neetseriesSectionId;
    @Value("${jeesoln.center.id}")
    private String jeesolnCenterId;
    @Value("${jeesoln.program.id}")
    private String jeesolnProgramId;
    @Value("${jeesoln.section.id}")
    private String jeesolnSectionId;
    @Value("${emp.center.id}")
    private String empCenterId;
    @Value("${emp.program.id}")
    private String empProgramId;
    @Value("${emp.section.id}")
    private String empSectionId;
    @Value("${smscountry.url}")
    private String SMSCOUNTRYURL;
    @Value("${smscountry.user}")
    private String USER;
    @Value("${smscountry.sid}")
    private String SID;
    @Value("${smscountry.mtype}")
    private String MTYPE;
    @Value("${smscountry.dr}")
    private String DR;
    @Value("${campaign.referral}")
    private String CAMPAIGN_REFERRAL_CODE;
    @Value("${campaign.user.packagedays}")
    private Integer campaignUserPackagedays;
    @Value("${campaign.section.id}")
    private String campaignSectionId;
    @Value("${campaign.center.id}")
    private String campaignCenterId;
    @Value("${campaign.program.id}")
    private String campaignProgramId;
    @Value("${smscountry.passwd}")
    private String PSWD;
    @Autowired
    private GranteeOrgProgramRepo granteeOrgProgramRepo;
    @Autowired
    private CampaignRepo campaignRepo;
    @Autowired
    private CounterRepo counterRepo;
    @Autowired
    private TransactionRepo transactionRepo;
    @Autowired
    private TeacherAnalyticsRepo teacherAnalyticsRepo;
    @Autowired
    private CouponCodeRepo couponCodeRepo;
    @Autowired
    private SaleDetailsRepo saleDetailsRepo;
    @Autowired
    private CampaignCodesServicesImpl campaignCodesServicesImpl;
    @Autowired
    private SalesCampaignRepo salesCampaignRepo;
    @Value("${neetdemo.center.id}")
    private String neetdemoCenterId;
    @Autowired
    private CampaignCodeRepo campaignCodeRepo;
    @Autowired
    private ProgramServiceImpl programServiceImpl;
    @Autowired
    private OrderRepo orderRepo;
    @Autowired
    private OrgSectionRepo orgSectionRepo;
    @Autowired
    private OrgProgramRepo orgProgramRepo;
    @Autowired
    private BoardRepo boardRepo;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private OrgCenterRepo orgCenterRepo;
    @Autowired
    private UserStateLogRepo userStateLogRepo;
    @Autowired
    private UserSaltrepo userSaltrepo;
    @Autowired
    private SaleDetailsRepo salesDetailsRepo;
    @Autowired
    private UserProfilePicEntityFileStorage userProfilePicEntityFileStorage;
    @Autowired
    private LocalFileSystemHandler localFileSystemHandler;
    @Autowired
    private EventUtil eventUtil;
    @Autowired
    private OrgMemberInfo orgMemberInfo;

    public static String generateOTP(int otpsize) {
        int startValue = (int) Math.pow(10, otpsize - 1);
        int endValue = (int) Math.pow(10, otpsize);
        return showRandomInteger(startValue, endValue);
    }

    private static String showRandomInteger(int aStart, int aEnd) {
        Random random = new Random();
        //get the range, casting to long to avoid overflow problems
        long range = (long) aEnd - (long) aStart + 1;
        // compute a fraction of the range, 0 <= frac < range
        long fraction = (long) (range * random.nextDouble());
        int randomNumber = (int) (fraction + aStart);
        return randomNumber + "";
    }

    private static String escapeCommaForCsv(String text) {
        return "\"" + text + "\"";
    }

    public static int getDurationOfUserMapping(OrgMemberMappingInfo mapping) {
        if (mapping.endTime == 0) {
            return 24;
        }
        if (mapping.timeJoined > mapping.endTime) {
            return 0;
        }
        Date startDate = new Date(mapping.timeJoined);
        Date endDate = new Date(mapping.endTime);
        DateTime startDateTime = new DateTime(startDate);
        DateTime endDateTime = new DateTime(endDate);
        Days days = Days.daysBetween(startDateTime, endDateTime);
        int daysDifference = days.getDays();
        int monthsDifference = daysDifference / 30;
        if (daysDifference % 30 > 0) {
            monthsDifference++;
        }
        return monthsDifference;
    }

    public static String getMemberDefaultPassword(OrgMemberProfile orgMemberProfile, String memberId,
                                                  String dobYYYYMMDD) {

        boolean noSeparator = true;
        if (orgMemberProfile == OrgMemberProfile.OFFLINE_USER) {
            return HardCodedConstants.emptyString;
        }
        String defaultPassword = memberId;
        return defaultPassword;
    }

    private static String getOrderIdForAddedSection(OrgMember orgMember, String sectionId) {
        String orderId = "0";
        for (OrgMemberMappingInfo mapping : orgMember.mappings) {
            if (mapping.sectionId.equals(sectionId)) {
                orderId = mapping.orderId;
            }
        }
        return orderId;
    }

    public static List<Tax> getTaxes(ItemCategory category, Location shippedToLocation) {

        // category wise taxes
        // location wise taxes
        // TODO: omplete this
        List<Tax> taxes = new ArrayList<Tax>();
        if (category == ItemCategory.PLAN) {
            taxes.add(new Tax("VAT", "Value Added Tax", 12.36f, "VAT"));
            taxes.add(new Tax("Service Tax", "Service Tax", 5.50f, "ServiceTax"));
        } else if (category == ItemCategory.SECTION) {
            // taxes.add(new Tax("VAT", "Value Added Tax", 12.36f, "VAT"));
            // taxes.add(new Tax("Service Tax", "Service Tax", 5.50f, "ServiceTax"));
        }
        return taxes;

    }

    public  SendForgotPasswordEmailRes sendForgotPasswordEmail(
            SendForgotPasswordEmailReq sendForgotPasswordEmailReq) throws VedantuException {

        //
        AuthHandler authHandler = getAuthHandler(sendForgotPasswordEmailReq.orgId);

        com.lms.user.vedantu.user.requests.SendForgotPasswordEmailReq tSendForgotPasswordEmailReq = new com.lms.user.vedantu.user.requests.SendForgotPasswordEmailReq();
        tSendForgotPasswordEmailReq.setUsername(authHandler.getMemberUsername(sendForgotPasswordEmailReq.orgId,
                sendForgotPasswordEmailReq.getMemberId()));
        tSendForgotPasswordEmailReq.setOrgId(sendForgotPasswordEmailReq.orgId);
        SendForgotPasswordEmailRes sendForgotPasswordEmailRes = sendForgotPasswordMailRes(tSendForgotPasswordEmailReq);

        return sendForgotPasswordEmailRes;
    }

    public static String getCenterPart(String centerQualifiedSectionCode) {

        String[] tokens = StringUtils.split(centerQualifiedSectionCode, CENTER_SECTION_SEPARATOR);
        return null != tokens && tokens.length == 2 ? tokens[0] : null;
    }

    @Override
    public VedantuResponse authenticateMember(MemberAuthReq memberAuthReq) {
        logger.info("authenticateMember " + memberAuthReq);
        if (memberAuthReq == null)
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);

        UserAuthRes userAuthRes = authenticateOrgMember(memberAuthReq);

        return new VedantuResponse(userAuthRes);
    }

    @Override
    public VedantuResponse authenticateOtpMember(UserAuthReq userAuthReq) {
        if (userAuthReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        UserAuthRes userAuthRes = authenticateOTPMember(userAuthReq);
        return new VedantuResponse(userAuthRes);
    }

    @Override
    public VedantuResponse authenticateMember2(MemberAuthReq memberAuthReq) {
        logger.info("authenticateMember2" + memberAuthReq);
        if (memberAuthReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        Optional<Organization> organization = organizationRepo.findById(memberAuthReq.getOrgId());
        if (!organization.isPresent())
            throw new VedantuException(VedantuErrorCode.INVALID_CODE, "the given orgId should not be null");

        ExtAuthHandler authHandler = new ExtAuthHandler(organization.get());
        OrgMember orgMember = orgMemberRepo.findByIdAndOrgId(memberAuthReq.getMemberId(), memberAuthReq.getOrgId());
        UserAuthReq userAuthReq = authHandler.authenticate(memberAuthReq.getMemberId(), memberAuthReq.password, orgMember);
        UserAuthRes userAuthRes = authenticateUser(userAuthReq);

        return new VedantuResponse(userAuthRes);
    }

    @Override
    public VedantuResponse getMemberProfile(GetOrgMemberProfileReq getOrgMemberProfileReq) {
        if (getOrgMemberProfileReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        GetOrgMemberProfileRes getOrgMemberProfileRes = getOrgMember(getOrgMemberProfileReq);
        return new VedantuResponse(getOrgMemberProfileRes);
    }

    @Override
    public VedantuResponse getOrgMemberProfile(GetOrgMemberReq getOrgMemberReq) {
        if (getOrgMemberReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        GetOrgMemberProfileRes getOrgMemberRes = getOrgMemberByMemberId(getOrgMemberReq);

        return new VedantuResponse(getOrgMemberRes);
    }

    @Override
    public VedantuResponse getOrganizationMemberWithEmail(GetOrgMemberWithEmailReq getOrgMemberWithEmailReq) {
        if (getOrgMemberWithEmailReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        GetOrgMemberProfileRes getOrgMemberRes = getOrgMemberWithEmail(getOrgMemberWithEmailReq);
        return new VedantuResponse(getOrgMemberRes);

    }

    @Override
    public VedantuResponse getMembers(GetOrgMembersReq getOrgMembersReq) {
        if (getOrgMembersReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        GetOrgMembersRes getOrgMemberRes = getOrgantionMembers(getOrgMembersReq);
        return new VedantuResponse(getOrgMemberRes);
    }

    @Override
    public VedantuResponse addMember(AddOrgMemberReq addOrgMemberReq) {
        if (addOrgMemberReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        AddOrgMemberRes addOrgMemberRes = addOrgMember(addOrgMemberReq);
        logger.info("signup success");
        System.out.println("endeded");
        return new VedantuResponse(addOrgMemberRes);
    }

    @Override
    public VedantuResponse addMmberMapping(AddOrgMemberMappingReq addOrgMemberMappingReq) {
        if (addOrgMemberMappingReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        final boolean noExceptionOnExistingMapping = false;
        AddOrgMemberMappingRes addOrgMemberMappingRes = addOrgMemMapping(addOrgMemberMappingReq,
                noExceptionOnExistingMapping);

        return new VedantuResponse(addOrgMemberMappingRes);
    }

    @Override
    public VedantuResponse updateMemberMapping(UpdateOrgMemberMappingReq updateOrgMemberMappingReq) {
        if (updateOrgMemberMappingReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        UpdateOrgMemberMappingRes updateOrgMemberMappingRes = updateOrgMemberMapping(updateOrgMemberMappingReq);


        return new VedantuResponse(updateOrgMemberMappingRes);
    }

    @Override
    public VedantuResponse removeMemberMapping(RemoveOrgMemberMappingReq removeOrgMemberMappingReq) {
        if (removeOrgMemberMappingReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        RemoveOrgMemberMappingRes removeOrgMemberMappingRes = removeOrgMemberMapping(removeOrgMemberMappingReq);

        return new VedantuResponse(removeOrgMemberMappingRes);
    }

    @Override
    public VedantuResponse updateOrgMemberReq(UpdateOrgMemberReq updateOrgMemberReq) {
        if (updateOrgMemberReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        UpdateOrgMemberRes updateOrgMemberRes = updateOrgMember(updateOrgMemberReq);

        return new VedantuResponse(updateOrgMemberRes);
    }

    @Override
    public VedantuResponse updateMemberEmail(UpdateOrgMemberReq updateOrgMemberReq) {

        if (updateOrgMemberReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        UpdateOrgMemberRes updateOrgMemberRes = updateOrgMemberEmail(updateOrgMemberReq);


        return new VedantuResponse(updateOrgMemberRes);

    }

    @Override
    public VedantuResponse resetUsername(ResetUsernameReq resetUsernameReq) {
        if (resetUsernameReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        if (ObjectIdUtils.hasInvalidId(resetUsernameReq.orgId.trim(), resetUsernameReq.targetUserId.trim(),
                resetUsernameReq.targetOrgMemberId.trim())) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }
        ResetUsernameRes resetUsernameRes = resetUserName(resetUsernameReq);

        return new VedantuResponse(resetUsernameRes);
    }

    @Override
    public VedantuResponse saveTestUserData(TestUserDataReq request) {
        if (request == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        OrgMember admin = orgMemberRepo.findByOrgIdAndUserId(request.getOrgId(), request.getAdminUserId());
        if (admin == null || admin.profile != OrgMemberProfile.MANAGER) {
            throw new VedantuException(VedantuErrorCode.ACCESS_DENIED, "Access to this service is denied for you");
        }
        TestUser testUser = testUserRepo.findByMemberId(request.memberId);
        TestUserDataRes response = new TestUserDataRes();
        if (testUser == null) {
            try {
                testUser = new TestUser();
                testUser.createFromReq(request);
                testUserRepo.save(testUser);
                AddOrgMemberRes orgmemberRes = null;
                logger.debug("saveTestUserData : Checking wheather student exist with memberId");
                OrgMember member = orgMemberRepo.findByOrgIdAndMemberId(request.orgId, request.memberId);
                if (member == null) {
                    logger.debug("saveTestUserData : Checking wheather student exist with email");
                    member = orgMemberRepo.findByOrgIdAndEmail(request.orgId, request.email);
                }
                if (member == null) {
                    logger.debug("saveTestUserData : Checking wheather student exist with Mobile");
                    member = orgMemberRepo.findByCountryCodeAndContactNumber("+91", request.studentsMobile);
                }
                if (member == null) {
                    logger.debug("saveTestUserData : No user found. So creating one");
                    AddOrgMemberReq orgMemberReq = new AddOrgMemberReq();
                    orgMemberReq.fromTestDataReq(request);
                    orgmemberRes = addOrgMember(orgMemberReq);
                    if (!(orgmemberRes.userId).isEmpty()) {
                        testUser.userId = orgmemberRes.userId;
                    } else {
                        throw new VedantuException(VedantuErrorCode.USER_LOGIN_NOT_FOUND);
                    }
                } else {
                    logger.debug("saveTestUserData : User found");
                    testUser.userId = member.userId;
                }

                testUserRepo.save(testUser);
                response.userAdded = true;
                response.userId = testUser.userId;
                addMappingForTestUser(request, response);

            } catch (VedantuException ve) {
                logger.error("Error while saving testUserData", ve);
                throw ve;
            } catch (Exception e) {
                logger.error("Error while saving testUserData", e);
                throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
            }
        } else {
            response.userId = testUser.userId;
            response.userAlreadyExists = true;
            addMappingForTestUser(request, response);
        }
        return new VedantuResponse(response);
    }

    public void addMappingForTestUser(TestUserDataReq request, TestUserDataRes response) throws VedantuException {
        OrgSection section = orgSectionRepo.findById(request.sectionId).get();
        if (null == section) {
            logger.error("cannot find orgSection for _id: " + request.sectionId);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_PROGRAM_SECTION_NOT_FOUND);
        }
        if (!section.orgId.equals(request.getOrgId())) {
            logger.error("mismatch in orgId for section _id: " + request.sectionId + ", expected orgId: "
                    + section.orgId + ", found orgId: " + request.orgId);
            throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }
        if (section == null) {
            throw new VedantuException(VedantuErrorCode.INVALID_SECTION_ID);
        }
        OrgMember member = orgMemberRepo.findByOrgIdAndUserId(request.orgId, response.userId);
        if (member == null) {
            throw new VedantuException(VedantuErrorCode.USER_LOGIN_NOT_FOUND);
        }
        AddOrgMemberMappingReq addReq = new AddOrgMemberMappingReq();
        addReq.createFromTestUserReq(request, section.programId, section.centerId, member);
        AddOrgMemberMappingRes addRes = addOrgMemberMapping(addReq, true);
        if (addRes.done) {
            response.mappingAdded = true;
        } else {
            throw new VedantuException(VedantuErrorCode.INVALID_CODE);
        }
    }

    @Override
    public File getTestUsersData(GetTestUsersDataReq getTestUsersDataReq) {
        File resultFile = null;
        if (getTestUsersDataReq.getOrgId().isEmpty()) {
            throw new VedantuException(VedantuErrorCode.ACCESS_DENIED);
        }
        LocalFileSystemHandler tempFileSystemHandler = new LocalFileSystemHandler();
//        tempFileSystemHandler.localFileSystemHandlerDirectory(true, fsLocalBaseUrl);
        tempFileSystemHandler.localFileSystemHandlerBaseDirectory(true);
        File generatedFile = tempFileSystemHandler.getFileWithSpecifiedName("testUsersData",
                "studentsData" + UUID.randomUUID().toString(), FileUtils.CSV_EXTENTION_WITHOUT_DOT);

        BufferedWriter writer = null;
        final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            List<TestUser> testUsers = testUserRepo.findByOrgIdAndTimeCreatedGreaterThanEqualAndRecordStateIs(getTestUsersDataReq.getOrgId(), getTestUsersDataReq.getStartDate(), VedantuRecordState.ACTIVE);
            List<TestUser> fetchedtestusers = new ArrayList<>();
            for (TestUser tu : testUsers) {
                if (tu.getTimeCreated() <= getTestUsersDataReq.getEndDate()) {
                    fetchedtestusers.add(tu);
                }
            }
            if (testUsers == null || testUsers.isEmpty()) {
                return null;
            }
            writer = new BufferedWriter(new PrintWriter(generatedFile));
            writer.write("Date,Student Name,Student Email,Member Id,Father Mobile," +
                    "DOB,Address,Caste,School Name,School Place,Medium,Percentage,Course," +
                    "Maths Marks, Science Marks,English Marks,Qualified For,Ambition," +
                    "Father Name,Father Qualification,Subcaste,Mother Occupation");
            for (TestUser testUser : fetchedtestusers) {
                writer.newLine();
                writer.write(sdf.format(new Date(testUser.timeCreated)) + ",");
                writer.write((testUser.name == null ? "N.A" : escapeCommaForCsv(testUser.name)) + ",");
                writer.write((testUser.email == null ? "N.A" : escapeCommaForCsv(testUser.email)) + ",");
                writer.write((testUser.memberId == null ? "N.A" : escapeCommaForCsv(testUser.memberId)) + ",");
                writer.write((testUser.studentsMobile == null ? "N.A" : escapeCommaForCsv(testUser.studentsMobile)) + ",");
                writer.write((testUser.dob == null ? "N.A" : escapeCommaForCsv(testUser.dob)) + ",");
                writer.write((testUser.address == null ? "N.A" : escapeCommaForCsv(testUser.address)) + ",");
                writer.write((testUser.caste == null ? "N.A" : escapeCommaForCsv(testUser.caste)) + ",");
                writer.write((testUser.schoolName == null ? "N.A" : escapeCommaForCsv(testUser.schoolName)) + ",");
                writer.write((testUser.schoolPlace == null ? "N.A" : escapeCommaForCsv(testUser.schoolPlace)) + ",");
                writer.write((testUser.medium == null ? "N.A" : escapeCommaForCsv(testUser.medium)) + ",");
                writer.write(testUser.percentage + ",");
                writer.write((testUser.course == null ? "N.A" : escapeCommaForCsv(testUser.course)) + ",");
                writer.write(testUser.mathMarks + ",");
                writer.write(testUser.scienceMarks + ",");
                writer.write(testUser.englishMarks + ",");
                writer.write((testUser.qualifiedFor == null ? "N.A" : escapeCommaForCsv(testUser.qualifiedFor)) + ",");
                writer.write((testUser.ambition == null ? "N.A" : escapeCommaForCsv(testUser.ambition)) + ",");
                writer.write((testUser.fatherName == null ? "N.A" : escapeCommaForCsv(testUser.fatherName)) + ",");
                writer.write((testUser.fatherQualification == null ? "N.A" : escapeCommaForCsv(testUser.fatherQualification)) + ",");
                writer.write((testUser.subcaste == null ? "N.A" : escapeCommaForCsv(testUser.subcaste)) + ",");
                writer.write(testUser.motherQualification == null ? "N.A" : escapeCommaForCsv(testUser.motherQualification));
            }
            writer.flush();

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new VedantuException(VedantuErrorCode.CANNOT_WRITE_FILE);
        } finally {
            IOUtils.closeQuietly(writer);
        }
        generatedFile.deleteOnExit();
        return generatedFile;
    }

    @Override
    public File getReferredUsersData(String referrer) {
        if (referrer.isEmpty()) {
            throw new VedantuException(VedantuErrorCode.INVALID_REFERER);
        }
        LocalFileSystemHandler tempFileSystemHandler = new LocalFileSystemHandler();
        tempFileSystemHandler.localFileSystemHandlerBaseDirectory(true);
        File generatedFile = tempFileSystemHandler.getFileWithSpecifiedName("ReferralData",
                "studentsData" + UUID.randomUUID().toString(), FileUtils.CSV_EXTENTION_WITHOUT_DOT);
        BufferedWriter writer = null;
        final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            String referrerUserId = getUserIdFromReferralCode(referrer);
            if (referrerUserId == null) {
                throw new VedantuException(VedantuErrorCode.INVALID_REFERER);
            }
            List<OrgMember> referrerUsers = orgMemberRepo.findAllByUserId(referrerUserId);
            if (referrerUsers == null || referrerUsers.isEmpty()) {
                return null;
            }
            writer = new BufferedWriter(new PrintWriter(generatedFile));
            writer.write("Date,Student Name,Student Email,Student Mobile," +
                    "Referrer Code,Referrer Name,Referrer Mobile,ReferrerEmail");
            for (OrgMember referrerUser : referrerUsers) {
                List<OrgMember> referredUsers = orgMemberRepo.findAllByUserId(referrerUser.userId);
                if (referredUsers == null || referredUsers.isEmpty()) {
                    continue;
                }
                for (OrgMember referredUser : referredUsers) {
                    writer.newLine();
                    writer.write(sdf.format(new Date(referredUser.timeCreated)) + ",");
                    writer.write(referredUser.getFullName() + ",");
                    writer.write(referredUser.email + ",");
                    writer.write(referredUser.contactNumber + ",");
                    writer.write(referrerUser.referralCode + ",");
                    writer.write(referrerUser.getFullName() + ",");
                    writer.write(referrerUser.contactNumber + ",");
                    writer.write(referrerUser.email);
                }
            }
            writer.flush();

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            IOUtils.closeQuietly(writer);
        }
        generatedFile.deleteOnExit();
        return generatedFile;
    }

    @Override
    public File getStudentsData(GetStudentsDataReq getStudentsDataReq) {
        File resultFile = null;
        if (getStudentsDataReq.getOrgId().isEmpty()) {
            throw new VedantuException(VedantuErrorCode.ACCESS_DENIED);
        }
        LocalFileSystemHandler tempFileSystemHandler = new LocalFileSystemHandler();
        tempFileSystemHandler.localFileSystemHandlerBaseDirectory(true);
        File generatedFile = tempFileSystemHandler.getFileWithSpecifiedName("testUsersData",
                "studentsData" + UUID.randomUUID().toString(), FileUtils.CSV_EXTENTION_WITHOUT_DOT);
        BufferedWriter writer = null;
        final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            List<OrgMember> orgMembers = orgMemberRepo.findByOrgIdAndTimeCreatedGreaterThanEqualAndRecordStateIsAndProfileIs(getStudentsDataReq.getOrgId(), getStudentsDataReq.getStartDate(), VedantuRecordState.ACTIVE, OrgMemberProfile.STUDENT);
            if (orgMembers.isEmpty()) {
                return null;
            }
            List<OrgMember> fetchedmembers = new ArrayList<>();
            for (OrgMember orgMember : orgMembers) {
                if (orgMember.getTimeCreated() <= getStudentsDataReq.getEndDate()) {
                    fetchedmembers.add(orgMember);
                }
            }

            // Get Shared Programs of this organisation
            List<GranteeOrgProgram> sharedPrograms = granteeOrgProgramRepo.findBySubscriberOrgIdAndRecordState(getStudentsDataReq.getOrgId(), VedantuRecordState.ACTIVE);
            // Get all sections of this organisation
            List<OrgSection> sections = orgSectionRepo.findAllByOrgIdAndRecordState(getStudentsDataReq.getOrgId(), VedantuRecordState.ACTIVE);
            // Get all sections of shared program
            for (GranteeOrgProgram sharedProgram : sharedPrograms) {
                sections.addAll(orgSectionRepo.findAllByProgramId(sharedProgram.programId));
            }
            Map<String, String> programMap = new HashMap<String, String>();
            Map<String, String> centerMap = new HashMap<String, String>();

            Map<String, String> programsDataMap = new HashMap<String, String>();
            OrgProgram program = null;
            OrgCenter center = null;
            for (OrgSection section : sections) {
                if (!programMap.containsKey(section.programId)) {
                    //Need to test the below line.
                    program = orgProgramRepo.findByOrgIdAndId(getStudentsDataReq.getOrgId(), section.programId);
                    programMap.put(section.programId, program.getName());
                }
                if (!centerMap.containsKey(section.centerId)) {
                    center = orgCenterRepo.findById(section.centerId).get();
                    if (null == center) {
                        logger.error("cannot find orgCenter for _id: " + section.centerId);
                        throw new VedantuException(
                                VedantuErrorCode.ORGANIZATION_CENTER_NOT_FOUND);
                    }
                    centerMap.put(section.centerId, center.getName());
                }
                programsDataMap.put(section._getStringId(), programMap.get(section.programId) + "-" + centerMap.get(section.centerId) + "-" + section.getName());
                writer = new BufferedWriter(new PrintWriter(generatedFile));
                writer.write("Date,Name,MemberId,Email,Student Number,Program,Extra Info");
                for (OrgMember student : orgMembers) {
                    writer.newLine();
                    writer.write(sdf.format(new Date(student.timeCreated)) + ",");
                    writer.write(escapeCommaForCsv(student.firstName + " " + student.lastName) + ",");
                    writer.write((student.memberId == null || student.memberId.isEmpty() ? "N/A" : escapeCommaForCsv(student.memberId)) + ",");
                    writer.write((student.email == null || student.email.isEmpty() ? "N/A" : escapeCommaForCsv(student.email)) + ",");
                    writer.write((student.contactNumber == null || student.contactNumber.isEmpty() ? "N/A" : escapeCommaForCsv(student.contactNumber)) + ",");
                    if ((student.mappings) != null) {
                        String sb = "";
                        for (OrgMemberMappingInfo mapping : student.mappings) {
                            if (programsDataMap.containsKey(mapping.sectionId)) {
                                sb = sb + programsDataMap.get(mapping.sectionId) + ", ";
                            }
                        }
                        sb.trim();
                        if (sb.length() > 2) {
                            sb = sb.substring(0, sb.length() - 2);
                        }
                        writer.write(escapeCommaForCsv(sb) + ",");
                    } else {
                        writer.write("No Programs Found" + ",");
                    }
                    if ((student.extraInfo) != null) {
                        String value = "";
                        for (OrgMemberExtraInfo extra : student.extraInfo) {
                            value += extra.name + ": " + extra.value + "  & ";
                        }
                        value.trim();
                        if (value.length() > 2) {
                            value = value.substring(0, value.length() - 2);
                        }
                        writer.write(escapeCommaForCsv(value));
                    } else {
                        writer.write("N/A");
                    }
                }
            }

            writer.flush();

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new VedantuException(VedantuErrorCode.CANNOT_WRITE_FILE);
        } finally {
            IOUtils.closeQuietly(writer);
        }
        generatedFile.deleteOnExit();
        return generatedFile;
    }

    public String getUserIdFromReferralCode(String referralCode) {

        OrgMember member = orgMemberRepo.findByReferralCode(referralCode);
        String userId = null;
        if (member != null) {
            userId = member.userId;
        }
        return userId;
    }

    @Override
    public VedantuResponse doesContactNumberExists(UserExistenceReq userExistenceReq) {
        if (userExistenceReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        UserExistenceRes userAuthRes = doesContactNumExists(userExistenceReq);

        return new VedantuResponse(userAuthRes);

    }

    @Override
    public VedantuResponse isValidReferralCode(UserExistenceReq userExistenceReq) {
        if (userExistenceReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        UserExistenceRes userAuthRes = isValidReferrCode(userExistenceReq);
        return new VedantuResponse(userAuthRes);
    }

    @Override
    public VedantuResponse getStudentsCount(GetCountOfStudentsReq getCountOfStudentsReq) {
        if (getCountOfStudentsReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        GetCountOfStudentsRes response = getstudentsCount(getCountOfStudentsReq);
        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse getwalletBalance(GetWalletBalanceReq getWalletBalanceReq) {
        if (getWalletBalanceReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        GetWalletBalanceRes getWalletBalanceRes = null;

        getWalletBalanceRes = getWalletBalance(getWalletBalanceReq);

        return new VedantuResponse(getWalletBalanceRes);
    }

    @Override
    public VedantuResponse getreferralData(GetReferralDataReq getReferralDataReq) {

        if (getReferralDataReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        GetReferralDataRes getReferralDataRes = getReferralData(getReferralDataReq);

        return new VedantuResponse(getReferralDataRes);
    }

    private GetReferralDataRes getReferralData(GetReferralDataReq request) {
        GetReferralDataRes response = new GetReferralDataRes();
        Campaign campaign = getCampaignWithCampaignType(request.getCampaignType());
        if (campaign != null) {
            response.message = campaign.message;
            response.friendRewards = campaign.friendRewards;
            response.referrerRewards = campaign.referrerRewards;
        }
        OrgMember referrer = orgMemberRepo.findByUserId(request.getUserId());
        if (referrer == null)
            throw new VedantuException(VedantuErrorCode.INVALID_CODE, "orgMember not found");
        if (referrer.freezedRewardsOrderId != 0) {
            try {
                if (!isRewardsFreezed(referrer)) {
                    referrer = addBackRewards(request.userId);
                }
            } catch (VedantuException e) {
                logger.error("Exception at getReferralData function", e);
            }
        }
        response.existingRewardPoints = referrer.rewards;
        if (referrer.referralCode == null) {
            referrer.referralCode = generateReferralCode(referrer.firstName);
            orgMemberRepo.save(referrer);
            response.referralCode = referrer.referralCode;
        } else {
            response.referralCode = referrer.referralCode;
        }

        return response;
    }

    public Campaign getCampaignWithCampaignType(CampaignType campaignType) {
        Campaign campaign = null;
        if (campaignType != null) {
            campaign = campaignRepo.findByCampaignTypeAndRecordState(campaignType, VedantuRecordState.ACTIVE);
        } else {
            campaign = campaignRepo.findByRecordState(VedantuRecordState.ACTIVE);
        }
        return campaign;
    }

    public String generateReferralCode(String firstName) {
        int referralCodeNumber = (int) (Math.random() * 999 + 0);
        String referralString = String.format("%03d", referralCodeNumber);
        String firstNameWithoutSpaces = firstName.replaceAll("\\s+", "");
        String referralCode = firstNameWithoutSpaces.toLowerCase() + referralString;
        boolean isUniqueReferralCode = checkReferralCodeUniqueness(referralCode, firstName);
        if (!isUniqueReferralCode) {
            referralCode = generateReferralCode(firstName);
        }
        return referralCode;
    }

    public boolean checkReferralCodeUniqueness(String referralCode, String firstName) {
        OrgMember orgMember = orgMemberRepo.findByReferralCode(referralCode);

        return orgMember == null;

    }

    private GetWalletBalanceRes getWalletBalance(GetWalletBalanceReq request) {
        GetWalletBalanceRes response = new GetWalletBalanceRes();
        if (request.getUserId() == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "please provide userId");
        }
        OrgMember member = orgMemberRepo.findByUserId(request.getUserId());

        // If Freezed
        if (member.freezedRewardsOrderId != 0) {
            try {
                // If not Freezed, add back rewards
                if (!isRewardsFreezed(member)) {
                    member = addBackRewards(request.userId);
                }
            } catch (VedantuException e) {
                logger.error("Exception at getWalletBalance function", e);
            }
        }
        // Still Freezed
        if (member.freezedRewardsOrderId != 0) {
            response.existingRewardPoints = member.rewards;
            response.maxRewardPointsToRedeem = 0;
        } else {
            response.existingRewardPoints = member.rewards;
            response.maxRewardPointsToRedeem = member.rewards;
        }
        Order order = null;
        if (!StringUtils.isEmpty(request.getOrderId())) {

            order = getOrderById(request.orderId);
            if ((order.totalAmount / 100) < response.existingRewardPoints
                    && response.maxRewardPointsToRedeem != 0) {
                response.maxRewardPointsToRedeem = order.totalAmount / 100;

            }
        }

        return response;
    }

    public OrgMember addBackRewards(String userId) {
        OrgMember orgMember = orgMemberRepo.findByUserId(userId);
        if (orgMember == null)
            throw new VedantuException(VedantuErrorCode.INVALID_CODE, "OrgMemberNotFound");
        orgMember = addBackRewards(userId, orgMember.getFreezedRewards());
        return orgMember;
    }

    public OrgMember addBackRewards(String userId, int lpCreditsRedeemed) {
        OrgMember orgMember = orgMemberRepo.findByUserId(userId);
        orgMember.freezedRewards = orgMember.freezedRewards - lpCreditsRedeemed;
        orgMember.rewards = orgMember.rewards + lpCreditsRedeemed;
        orgMember.freezedRewardsOrderId = 0;
        orgMemberRepo.save(orgMember);
        return orgMember;
    }

    public boolean isRewardsFreezed(OrgMember referrer) throws VedantuException {
        if (referrer.freezedRewardsOrderId > 0) {
            Order order = getOrderById(referrer.freezedRewardsOrderId);
            if (order == null) {
                throw new VedantuException(VedantuErrorCode.INVALID_ORDER_ID, "Order is NOT valid");
            }
            if (order.orderState.equals(OrderState.AWAITING_PAYMENT)
                    || order.orderState.equals(OrderState.DRAFT)) {
                // Freezing time of 10 mins
                return System.currentTimeMillis() < (order.lastUpdated + FREEZE_TIME);
            }
            return false;
        }
        return false;
    }

    private GetCountOfStudentsRes getstudentsCount(GetCountOfStudentsReq request) {

        GetCountOfStudentsRes res = new GetCountOfStudentsRes();
        AtomicLong totalHits = new AtomicLong();
        List<GranteeOrgProgram> programs = getProgramsGrantedToMe(
                request.orgId, null, totalHits);
        Set<String> programIds = new HashSet<String>();
        String learnpediaOrgId = request.orgId;
        for (GranteeOrgProgram program : programs) {
            learnpediaOrgId = program.providerOrgId;
            programIds.add(program.programId);
        }
        Map<String, OrgProgram> programIdsInfo = new HashMap<String, OrgProgram>();
        if (programIds != null && !programIds.isEmpty()) {
            programIdsInfo = getProgramsMapByIds(programIds);
        }

        Map<String, RevenueModel> revenueModel = new HashMap<String, RevenueModel>();
        for (String programId : programIds) {
            AtomicLong totalHitsSections = new AtomicLong();
            List<OrgSection> sectionsOfaProgram = getSectionsById(
                    learnpediaOrgId, programId, null, null, null, VedantuRecordState.ACTIVE,
                    NO_START, NO_LIMIT, totalHitsSections);
            for (OrgSection section : sectionsOfaProgram) {
                revenueModel.put(section._getStringId(), section.revenueModel);
            }
        }

        List<OrgProgram> programsCreatedByOrganization = getPrograms(
                request.orgId, null, totalHits);

        Set<String> programIdsCreatedByOrg = new HashSet<String>();
        for (OrgProgram program : programsCreatedByOrganization) {
            programIdsCreatedByOrg.add(program._getStringId());
        }
        Map<String, OrgProgram> programIdsCreatedByOrgInfo = new HashMap<String, OrgProgram>();
        if (programIdsCreatedByOrg != null && !programIdsCreatedByOrg.isEmpty()) {
            programIdsCreatedByOrgInfo = getProgramsMapByIds(programIdsCreatedByOrg);
        }

        AtomicLong countOfStudents = new AtomicLong();

        List<OrgMember> orgMembers = getOrganizationMembers(request.orgId,
                OrgMemberProfile.STUDENT, null, null, null, null, null, null,
                NO_START, NO_LIMIT, null, false, countOfStudents);
        Map<String, StudentsProgramDuration> responseMap = new HashMap<String, StudentsProgramDuration>();
        for (String programId : programIds) {
            String programName = programIdsInfo.get(programId).getName();
            responseMap.put(programId, new StudentsProgramDuration(programName));
        }
        for (String programId : programIdsCreatedByOrg) {
            String programName = programIdsCreatedByOrgInfo.get(programId).getName();
            responseMap.put(programId, new StudentsProgramDuration(programName));
        }
        member:
        for (OrgMember member : orgMembers) {
            boolean isAppUser = false;
            boolean isFreeUser = false;
            if (member.mappings == null || member.mappings.size() == 0) {
                if (member.timeCreated > request.startDate && member.timeCreated < request.endDate) {
                    res.noProgramsUsers++;
                }
                continue;
            }
            isFreeUser = getIfUserIsFree(member, revenueModel, request.startDate, request.endDate);
            isAppUser = checkIfUserIsaAppUser(member, programIds, revenueModel, request.startDate,
                    request.endDate);
            if (isAppUser) {
                res.onlyAppUsers++;
                continue;
            }
            if (isFreeUser) {
                res.freeUsers++;
                continue;
            }
            List<OrgMemberMappingInfo> allUserMappings = new ArrayList<OrgMemberMappingInfo>();
            allUserMappings.addAll(member.mappings);
            if (member.expiredMappings != null) {
                allUserMappings.addAll(member.expiredMappings);
            }
            mapping:
            for (OrgMemberMappingInfo mapping : allUserMappings) {
                if (request.startDate >= mapping.timeJoined
                        || mapping.timeJoined >= request.endDate) {
                    continue;
                }
                if (revenueModel.containsKey(mapping.sectionId)
                        && revenueModel.get(mapping.sectionId).equals(RevenueModel.PAID)) {
                    int duration = getDurationOfUserMapping(mapping);
                    if (duration < 6) {
                        responseMap.get(mapping.programId).numberOfSixMonths += duration;
                    } else if (duration < 12) {
                        responseMap.get(mapping.programId).numberOfOneYear++;
                    } else {
                        responseMap.get(mapping.programId).numberOfTwoYears++;
                    }
                }
            }
        }

        Iterator<Map.Entry<String, StudentsProgramDuration>> iterator = responseMap.entrySet()
                .iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, StudentsProgramDuration> entry = iterator.next();
            StudentsProgramDuration studDuration = entry.getValue();
            if (studDuration.numberOfOneYear + studDuration.numberOfSixMonths
                    + studDuration.numberOfTwoYears == 0) {
                //iterator.remove();
            }
        }
        res.countResponse = responseMap;
        return res;

    }

    public boolean getIfUserIsFree(OrgMember member, Map<String, RevenueModel> revenueModel,
                                   long startDate, long endDate) {
        boolean isFreeUser = false;
        List<OrgMemberMappingInfo> allUserMappings = new ArrayList<OrgMemberMappingInfo>();
        allUserMappings.addAll(member.mappings);
        if (member.expiredMappings != null) {
            allUserMappings.addAll(member.expiredMappings);
        }

        for (OrgMemberMappingInfo mapping : allUserMappings) {
            if (startDate <= mapping.timeJoined && mapping.timeJoined <= endDate) {
                if (revenueModel.containsKey(mapping.sectionId)
                        && revenueModel.get(mapping.sectionId).equals(RevenueModel.FREE)) {
                    isFreeUser = true;
                    break;
                }
            }
        }
        return isFreeUser;
    }

    public boolean checkIfUserIsaAppUser(OrgMember member, Set<String> programIds,
                                         Map<String, RevenueModel> revenueModel, long startDate, long endDate) {
        boolean isAppUser = false;
        List<OrgMemberMappingInfo> allUserMappings = new ArrayList<OrgMemberMappingInfo>();
        allUserMappings.addAll(member.mappings);
        if (member.expiredMappings != null) {
            allUserMappings.addAll(member.expiredMappings);
        }
        for (OrgMemberMappingInfo mapping : allUserMappings) {
            if (startDate > mapping.timeJoined || mapping.timeJoined > endDate) {
                continue;
            }
            if (!programIds.contains(mapping.programId)) {
                isAppUser = true;
                continue;
            }
            if (revenueModel.containsKey(mapping.sectionId)) {
                RevenueModel revenue = revenueModel.get(mapping.sectionId);
                if (revenue == RevenueModel.PAID) {
                    isAppUser = false;
                    break;
                }
            }
        }
        return isAppUser;
    }

    public List<OrgSection> getSectionsById(String orgId, String programId,
                                            List<ObjectId> sectionsIds, AccessScope accessScope, RevenueModel revenueModel,
                                            VedantuRecordState recordState, int start, int size, AtomicLong totalHits) {

        logger.debug("..........entered function getSectionsByIds......." + revenueModel);


        Criteria criteria = new Criteria();
        Query query = new Query();
        //Query<OrgSection> sectionsQuery = getQuery().filter("orgId", orgId);
        criteria.and("orgId").is(orgId);
//        Query<OrgSection> sectionsQuery = getQuery().field(FIELD_ID).hasAnyOf(orgIds);
        if (!StringUtils.isEmpty(programId)) {
            //sectionsQuery.filter("programId", programId);
            criteria.and("programId").is(programId);
        }

        // sectionsQuery.filter(OrgSection.FIELD_ACCESS_SCOPE, accessScope);

        if (!CollectionUtils.isEmpty(sectionsIds)) {
            // sectionsQuery.field(FIELD_ID).hasAnyOf(sectionsIds);
            criteria.and("id").in(programId);
        }
        if (accessScope != null) {
            // sectionsQuery.filter(OrgSection.FIELD_ACCESS_SCOPE, accessScope);
            criteria.and("accessScope").is(accessScope);
        }
        if (revenueModel != null) {
            //sectionsQuery.filter(OrgSection.FIELD_REVENUE_MODEL, revenueModel);
            criteria.and("revenueModel").is(revenueModel);
        }

        if (recordState != null) {
            // sectionsQuery.filter(ConstantsGlobal.RECORD_STATE, recordState);
            criteria.and("recordState").is(recordState);
        }
        query.addCriteria(criteria);
        List<OrgSection> sections = mongoTemplate.find(query, OrgSection.class);
        totalHits.set(sections.size());
        logger.debug("..........about to return from function getSectionsByIds......."
                + sections.size());
        return sections;
    }

    public Map<String, OrgProgram> getProgramsMapByIds(Set<String> programIds) {
        List<OrgProgram> results = orgProgramRepo.findAllByIdIn(programIds);
        Map<String, OrgProgram> programsMap = new HashMap<String, OrgProgram>();
        for (OrgProgram result : results) {
            programsMap.put(result._getStringId(), result);
        }
        return programsMap;
    }

    public List<OrgProgram> getPrograms(String orgId, String departmentId,
                                        AtomicLong totalHits) {
        logger.debug("getPrograms orgId: " + orgId + ", departmentId: "
                + departmentId);


        List<OrgProgram> programs = null;
        if (null != departmentId) {
            programs = orgProgramRepo.findAllByOrgIdAndDepartmentId(orgId, departmentId);
        } else {
            programs = orgProgramRepo.findAllByOrgId(orgId);
        }
        totalHits.set(programs.size());

        logger.info("getPrograms totalHits: " + totalHits.get());

        return programs;
    }

    public List<GranteeOrgProgram> getProgramsGrantedToMe(String providerOrgId, String departmentId,
                                                          AtomicLong totalHits) {
        logger.debug("getGrateeOrgPrograms orgId: " + providerOrgId
                + ", departmentId: " + departmentId);
        List<GranteeOrgProgram> programs = null;
        if (null != departmentId) {
            //   programs=granteeOrgProgramRepo.findAllBySubscriberOrgIdAndRecordStateAndDepartmentId(providerOrgId,VedantuRecordState.ACTIVE,departmentId);


        } else {
            programs = granteeOrgProgramRepo.findAllBySubscriberOrgIdAndRecordState(providerOrgId, VedantuRecordState.ACTIVE);
        }

        totalHits.set(programs.size());

        logger.info("getGrateeOrgPrograms totalHits: " + totalHits.get());

        return programs;
    }

    private UserExistenceRes isValidReferrCode(UserExistenceReq userExistenceReq) {
        if (userExistenceReq.getCountryCode() == null)
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "Country code should not be null");
        boolean doesReferralCodeExists = isValidreferralCode(userExistenceReq.referralCode);
        UserExistenceRes userExistenceRes = new UserExistenceRes();
        userExistenceRes.doesReferralCodeExists = doesReferralCodeExists;
        return userExistenceRes;
    }

    private boolean isValidreferralCode(String referralCode) {

        OrgMember member = orgMemberRepo.findByReferralCode(referralCode.trim());
        if (member == null) {
            logger.info(referralCode + " is invalid referralCode");
            return false;
        }
        logger.info(referralCode + " is already existing referralCode");
        return true;


    }

    private UserExistenceRes doesContactNumExists(UserExistenceReq userExistenceReq) {
        if (userExistenceReq.contactNumber.isEmpty() || userExistenceReq.countryCode.isEmpty())
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "contactNumber or Code missing");
        boolean doesContactNumberExists = isNewPhone(userExistenceReq.contactNumber, userExistenceReq.countryCode);
        UserExistenceRes userExistenceRes = new UserExistenceRes();
        userExistenceRes.doesContactNumberExists = doesContactNumberExists;
        return userExistenceRes;
    }

    public ResetUsernameRes resetUserName(ResetUsernameReq resetUsernameReq)
            throws VedantuException {

        Optional<User> user1 = userRepo.findById(resetUsernameReq.targetUserId);
        if (!user1.isPresent()) {
            logger.error("user not found for targetUserId: " + resetUsernameReq.targetUserId);
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        User user = user1.get();

        OrgMember orgMember = organizationsImpl.getMemberByUserId(resetUsernameReq.orgId,
                resetUsernameReq.targetUserId);
        if (null == orgMember) {
            logger.error("orgMember not found for orgId: " + resetUsernameReq.orgId
                    + ", targetUserId: " + resetUsernameReq.targetUserId);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }

        AuthHandler authHandler = getAuthHandler(
                resetUsernameReq.orgId);

        if (!resetUsernameReq.targetOrgMemberId.equals(orgMember._getStringId())) {
            logger.error("orgMember._id: " + orgMember._getStringId()
                    + " does not match for orgId: " + resetUsernameReq.orgId + ", targetUserId: "
                    + resetUsernameReq.targetUserId + ", targetOrgMemberId: "
                    + resetUsernameReq.targetOrgMemberId);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }

        UpdateUsernameReq updateUsernameReq = new UpdateUsernameReq();
        updateUsernameReq.targetUserId = resetUsernameReq.targetUserId;
        updateUsernameReq.setNewUsername(authHandler.getMemberUsername(orgMember.orgId,
                orgMember.memberId));
        updateUsernameReq.newPassword = resetUsernameReq.newPassword;

        UpdateUsernameRes updateUsernameRes = updateUsername(updateUsernameReq);

        ResetUsernameRes resetUsernameRes = new ResetUsernameRes();
        resetUsernameRes.done = updateUsernameRes.done;

        return resetUsernameRes;
    }

    private UpdateOrgMemberRes updateOrgMemberEmail(UpdateOrgMemberReq updateOrgMemberReq) {
        OrgMember orgMember = organizationsImpl.getMemberByUserId(updateOrgMemberReq.orgId,
                updateOrgMemberReq.targetUserId);
        if (null == orgMember) {
            logger.error("orgMember not found for orgId: " + updateOrgMemberReq.orgId
                    + ", targetUserId: " + updateOrgMemberReq.targetUserId);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }
        orgMember = updateOrgMemberEmail(updateOrgMemberReq.orgId, updateOrgMemberReq.userId, updateOrgMemberReq.getEmail());
        UpdateOrgMemberRes updateOrgMemberRes = new UpdateOrgMemberRes(orgMember._getStringId(),
                orgMember.recordState, orgMember.orgId, orgMember.userId);

        return updateOrgMemberRes;
    }

    public OrgMember updateOrgMemberEmail(String orgId, String userId, String email) throws VedantuException {

        logger.debug("updateOrgMemberEmail orgId: " + orgId + ", userId: " + userId + ", email" + email);

        OrgMember orgMember = organizationsImpl.getMemberByUserId(orgId, userId);
        if (null == orgMember) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }
        orgMember.email = email;
        orgMemberRepo.save(orgMember);
        return organizationsImpl.getMemberByUserId(orgId, userId);
    }

	public boolean isUpdateValid(Set<String> updateList) throws VedantuException {

		boolean isUpdatable = true;
		isUpdatable &= !updateList.contains(OrgMember.FIELD_FIRST_NAME);
		isUpdatable &= !updateList.contains(OrgMember.FIELD_LAST_NAME);
		isUpdatable &= !updateList.contains(OrgMember.FIELD_MAPPINGS);
		isUpdatable &= !updateList.contains(OrgMember.FIELD_MEMBER_ID);
		isUpdatable &= !updateList.contains(OrgMember.FIELD_PROFILE);

		if (!isUpdatable) {
			throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_UPDATED);

		}
		return isUpdatable;
	}

	private UpdateOrgMemberRes updateOrgMember(UpdateOrgMemberReq updateOrgMemberReq) {

		Set<String> updateList = new HashSet<String>();

		setUpdateList(updateList, updateOrgMemberReq);

		AuthHandler authHandler = getAuthHandler(updateOrgMemberReq.orgId);

		boolean isValidUpdate = isUpdateValid(updateList);
		logger.debug("isValidUpdate: " + isValidUpdate);
		if ((CollectionUtils.isEmpty(updateList) || updateList.contains(OrgMember.FIELD_DOB))
				&& !VedantuStringUtils.isValidDOB(updateOrgMemberReq.dob)) {
			throw new VedantuException(VedantuErrorCode.INVALID_DATE_FORMAT);
		}

		Optional<User> user = userRepo.findById(updateOrgMemberReq.targetUserId);
		if (!user.isPresent()) {
			logger.error("user not found for targetUserId: " + updateOrgMemberReq.targetUserId);
			throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
		}

		OrgMember orgMember = organizationsImpl.getMemberByUserId(updateOrgMemberReq.orgId,
				updateOrgMemberReq.targetUserId);
		if (null == orgMember) {
			logger.error("orgMember not found for orgId: " + updateOrgMemberReq.orgId + ", targetUserId: "
					+ updateOrgMemberReq.targetUserId);
			throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
		}
		if (!updateOrgMemberReq.targetOrgMemberId.equals(orgMember._getStringId())) {
			logger.error("orgMember._id: " + orgMember._getStringId() + " does not match for orgId: "
					+ updateOrgMemberReq.orgId + ", targetUserId: " + updateOrgMemberReq.targetUserId
					+ ", targetOrgMemberId: " + updateOrgMemberReq.targetOrgMemberId);
			throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
		}

		boolean isMemberIdChanging = false;
		if ((CollectionUtils.isEmpty(updateList) || updateList.contains(OrgMember.FIELD_MEMBER_ID))
				&& !StringUtils.isEmpty(orgMember.memberId)) {
			isMemberIdChanging = !(updateOrgMemberReq.getTargetMemberId().equals(orgMember.memberId));
		}

		boolean doesUserUseOrgCredentials = user.get().username
				.equals(authHandler.getMemberUsername(orgMember.orgId, orgMember.memberId));

		if (isMemberIdChanging) {
			OrgMember otherOrgMember = getMemberByMemberId(updateOrgMemberReq.orgId,
					updateOrgMemberReq.getTargetMemberId());
			if (null != otherOrgMember) {
				logger.error("cannot create another orgMember with same targetMemberId: "
						+ updateOrgMemberReq.getTargetMemberId() + " for orgId: " + updateOrgMemberReq.orgId
						+ ", targetUserId: " + updateOrgMemberReq.targetUserId + ", targetOrgMemberId: "
						+ updateOrgMemberReq.targetOrgMemberId);
				throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_ALREADY_EXISTS);
			}
		}
		boolean isDOBChanging = false;
		if ((CollectionUtils.isEmpty(updateList) || updateList.contains(OrgMember.FIELD_DOB))
				&& !StringUtils.isEmpty(updateOrgMemberReq.dob)) {
			isDOBChanging = !updateOrgMemberReq.dob.equals(orgMember.dob);
		}

		if (OrgMemberProfile.STUDENT == updateOrgMemberReq.profile) {
			orgMember = updateMember(updateOrgMemberReq.orgId, updateOrgMemberReq.targetUserId,
					updateOrgMemberReq.targetOrgMemberId, updateOrgMemberReq.getTargetMemberId(),
					updateOrgMemberReq.firstName, updateOrgMemberReq.lastName, updateOrgMemberReq.dob,
					updateOrgMemberReq.gender, updateOrgMemberReq.getEmail(), updateOrgMemberReq.profile,
					updateOrgMemberReq.contactNumber, updateOrgMemberReq.father, updateOrgMemberReq.mother,
					updateOrgMemberReq.guardian, updateOrgMemberReq.getParentEmail(), updateOrgMemberReq.extraInfo,
					updateList);
		} else {
			orgMember = updateMember(updateOrgMemberReq.orgId, updateOrgMemberReq.targetUserId,
					updateOrgMemberReq.targetOrgMemberId, updateOrgMemberReq.getTargetMemberId(),
					updateOrgMemberReq.firstName, updateOrgMemberReq.lastName, updateOrgMemberReq.dob,
					updateOrgMemberReq.gender, updateOrgMemberReq.getEmail(), updateOrgMemberReq.profile,
					updateOrgMemberReq.contactNumber, updateOrgMemberReq.isCanImpersonate(),
					updateOrgMemberReq.extraInfo, updateList);
		}

		if (doesUserUseOrgCredentials) {
			if (isMemberIdChanging) {
				UpdateUsernameReq updateUsernameReq = new UpdateUsernameReq();
				updateUsernameReq.targetUserId = updateOrgMemberReq.targetUserId;
				updateUsernameReq.setNewUsername(authHandler.getMemberUsername(orgMember.orgId, orgMember.memberId));
				updateUsernameReq.newPassword = getMemberDefaultPassword(updateOrgMemberReq.profile, orgMember.memberId,
						orgMember.dob);
				UpdateUsernameRes updateUsernameRes = updateUsername(updateUsernameReq);
				logger.debug("update username response: " + updateUsernameRes.done);
			} else if (isDOBChanging) {
				logger.debug("News dob" + orgMember.dob);
				UpdateUserPasswordReq updateUserPasswordReq = new UpdateUserPasswordReq();
				updateUserPasswordReq.targetUserId = updateOrgMemberReq.targetUserId;
				updateUserPasswordReq.newPassword = getMemberDefaultPassword(updateOrgMemberReq.profile,
						orgMember.memberId, orgMember.dob);
				logger.debug("New password" + updateUserPasswordReq.newPassword);
				UpdateUserPasswordRes updateUserPasswordRes = updateUserPassword(
						updateUserPasswordReq.getTargetUserId(), updateUserPasswordReq.getNewPassword());
				user.get().setDob(orgMember.dob);
				// updateModel(user, Arrays.asList(User.FIELD_DOB));
				logger.debug("update user password response: " + updateUserPasswordRes.done);
			}
		}

		UpdateOrgMemberRes updateOrgMemberRes = new UpdateOrgMemberRes(orgMember._getStringId(), orgMember.recordState,
				orgMember.orgId, orgMember.userId);

		return updateOrgMemberRes;
	}

	

	public OrgMember updateMember(String orgId, String userId, String orgMemberId, String memberId, String firstName,
			String lastName, String dob, Gender gender, String email, OrgMemberProfile profile, String contactNumber,
			boolean canImpersonate, List<OrgMemberExtraInfo> extraInfo, Set<String> updateList)
			throws VedantuException {

		return updateMember(orgId, userId, orgMemberId, memberId, firstName, lastName, dob, gender, email, profile,
				contactNumber, null, null, null, HardCodedConstants.emptyString, canImpersonate, extraInfo, updateList);
	}

	public OrgMember updateMember(String orgId, String userId, String orgMemberId, String memberId, String firstName,
			String lastName, String dob, Gender gender, String email, OrgMemberProfile profile, String contactNumber,
			MemberParentInfo father, MemberParentInfo mother, MemberParentInfo guardian, String parentEmail,
			List<OrgMemberExtraInfo> extraInfo, Set<String> updateList) throws VedantuException {

		return updateMember(orgId, userId, orgMemberId, memberId, firstName, lastName, dob, gender, email, profile,
				contactNumber, father, mother, guardian, parentEmail, false, extraInfo, updateList);
	}

	
	private void updateModel(OrgMember orgMember, ArrayList<String> strings) {

	}

	private void setUpdateList(Set<String> updateList, UpdateOrgMemberReq request) {

		if (!CollectionUtils.isEmpty(request.updateList)) {
			for (String key : request.updateList) {
				if (key.equals(ConstantsGlobal.ORG_ID)) {
					updateList.add(ConstantsGlobal.ORG_ID);
				} else if (key.equals(UpdateOrgMemberReq.CONTACT_NUMBER)) {
					updateList.add(OrgMember.FIELD_CONTACT_NUMBER);
				} else if (key.equals(UpdateOrgMemberReq.CAN_IMPERSONATE)) {
					updateList.add(OrgMember.FIELD_CAN_IMPERSONATE);
				} else if (key.equals(UpdateOrgMemberReq.FIRST_NAME)) {
					updateList.add(OrgMember.FIELD_FIRST_NAME);
				} else if (key.equals(UpdateOrgMemberReq.LAST_NAME)) {
					updateList.add(OrgMember.FIELD_LAST_NAME);
				} else if (key.equals(UpdateOrgMemberReq.DOB)) {
					updateList.add(OrgMember.FIELD_DOB);
				} else if (key.equals(UpdateOrgMemberReq.GENDER)) {
					updateList.add(OrgMember.FIELD_GENDER);
				} else if (key.equals(UpdateOrgMemberReq.EMAIL)) {
					updateList.add(OrgMember.FIELD_EMAIL);
				} else if (key.equals(UpdateOrgMemberReq.PROFILE)) {
					updateList.add(OrgMember.FIELD_PROFILE);
				} else if (key.equals(UpdateOrgMemberReq.FATHER)) {
					updateList.add(OrgMember.FIELD_FATHER);
				} else if (key.equals(UpdateOrgMemberReq.MOTHER)) {
					updateList.add(OrgMember.FIELD_MOTHER);
				} else if (key.equals(UpdateOrgMemberReq.GUARDIAN)) {
					updateList.add(OrgMember.FIELD_GUARDIAN);
				} else if (key.equals(UpdateOrgMemberReq.EXTRA_INFO)) {
					updateList.add(OrgMember.FIELD_EXTRA_INFO);
				} else if (key.equals(UpdateOrgMemberReq.PARENT_EMAIL)) {
					updateList.add(OrgMember.FIELD_PARENT_EMAIL);
				} else if (key
						.equals(UpdateOrgMemberReq.MOTHER + FileUtils.SEPARATOR_DOT + MemberParentInfo.FIELD_NAME)) {
					updateList.add(OrgMember.FIELD_MOTHER + FileUtils.SEPARATOR_DOT + MemberParentInfo.FIELD_NAME);
				} else if (key
						.equals(UpdateOrgMemberReq.FATHER + FileUtils.SEPARATOR_DOT + MemberParentInfo.FIELD_NAME)) {
					updateList.add(OrgMember.FIELD_FATHER + FileUtils.SEPARATOR_DOT + MemberParentInfo.FIELD_NAME);
				} else if (key
						.equals(UpdateOrgMemberReq.GUARDIAN + FileUtils.SEPARATOR_DOT + MemberParentInfo.FIELD_NAME)) {
					updateList.add(OrgMember.FIELD_GUARDIAN + FileUtils.SEPARATOR_DOT + MemberParentInfo.FIELD_NAME);
				}

				else if (key
						.equals(UpdateOrgMemberReq.MOTHER + FileUtils.SEPARATOR_DOT + MemberParentInfo.FIELD_EMAIL)) {
					updateList.add(OrgMember.FIELD_MOTHER + FileUtils.SEPARATOR_DOT + MemberParentInfo.FIELD_EMAIL);
				} else if (key
						.equals(UpdateOrgMemberReq.FATHER + FileUtils.SEPARATOR_DOT + MemberParentInfo.FIELD_EMAIL)) {
					updateList.add(OrgMember.FIELD_FATHER + FileUtils.SEPARATOR_DOT + MemberParentInfo.FIELD_EMAIL);
				} else if (key
						.equals(UpdateOrgMemberReq.GUARDIAN + FileUtils.SEPARATOR_DOT + MemberParentInfo.FIELD_EMAIL)) {
					updateList.add(OrgMember.FIELD_GUARDIAN + FileUtils.SEPARATOR_DOT + MemberParentInfo.FIELD_EMAIL);
				} else if (key.equals(
						UpdateOrgMemberReq.MOTHER + FileUtils.SEPARATOR_DOT + MemberParentInfo.FIELD_CONTACTNUMBER)) {
					updateList.add(
							OrgMember.FIELD_MOTHER + FileUtils.SEPARATOR_DOT + MemberParentInfo.FIELD_CONTACTNUMBER);
				} else if (key.equals(
						UpdateOrgMemberReq.FATHER + FileUtils.SEPARATOR_DOT + MemberParentInfo.FIELD_CONTACTNUMBER)) {
					updateList.add(
							OrgMember.FIELD_FATHER + FileUtils.SEPARATOR_DOT + MemberParentInfo.FIELD_CONTACTNUMBER);
				} else if (key.equals(
						UpdateOrgMemberReq.GUARDIAN + FileUtils.SEPARATOR_DOT + MemberParentInfo.FIELD_CONTACTNUMBER)) {
					updateList.add(
							OrgMember.FIELD_GUARDIAN + FileUtils.SEPARATOR_DOT + MemberParentInfo.FIELD_CONTACTNUMBER);
				}

			}

			if (updateList.size() != request.updateList.size()) {
				throw new VedantuException(VedantuErrorCode.INCORRECT_UPDATE_DATA_PROVIDED);
			}
		}

	}

	private RemoveOrgMemberMappingRes removeOrgMemberMapping(RemoveOrgMemberMappingReq removeOrgMemberMappingReq) {

		AuthHandler authHandler = getAuthHandler(removeOrgMemberMappingReq.orgId);

		if (authHandler.authType == AuthType.EXT_AUTH_ORG) {
			throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED,
					"removing a member mapping is not allowed for external authentication");
		}

		AtomicBoolean isRemoved = new AtomicBoolean(false);
		Set<String> tSectionIds = null == removeOrgMemberMappingReq.sectionIds ? null
				: new HashSet<String>(removeOrgMemberMappingReq.sectionIds);
		List<String> orgSectinIds = new ArrayList<String>(tSectionIds);

		List<OrgSection> paidSections = orgSectionRepo.findAllByIdInAndRevenueModelAndAccessScope(orgSectinIds,
				RevenueModel.PAID, AccessScope.OPEN);
		/*
		 * OrgSectionDAO.INSTANCE.find(
		 * OrgSectionDAO.INSTANCE.createQuery().field("_id")
		 * .in(ObjectIdUtils.toObjectIds(new ArrayList<String>(tSectionIds), true))
		 * .filter(OrgSection.FIELD_REVENUE_MODEL, RevenueModel.PAID)
		 * .filter(OrgSection.FIELD_ACCESS_SCOPE, AccessScope.OPEN)
		 * .retrievedFields(true, "_id")).asList();
		 */

		if (!CollectionUtils.isEmpty(paidSections)) {
			throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED,
					"removing a member mapping not allowed from a paid program/section");
		}

		OrgMember orgMember = removeOrganizatMemberMapping(removeOrgMemberMappingReq.orgId,
				removeOrgMemberMappingReq.targetUserId, removeOrgMemberMappingReq.targetOrgMemberId,
				removeOrgMemberMappingReq.programId, removeOrgMemberMappingReq.centerId, tSectionIds, isRemoved);

		RemoveOrgMemberMappingRes removeOrgMemberMappingRes = new RemoveOrgMemberMappingRes(orgMember._getStringId(),
				orgMember.recordState, isRemoved.get());
		return removeOrgMemberMappingRes;
	}

	public OrgMember removeOrganizatMemberMapping(String orgId, String userId, String orgMemberId, String programId,
			String centerId, Set<String> sectionIds, AtomicBoolean isRemoved) throws VedantuException {

		OrgMember orgMember = organizationsImpl.getMemberByUserId(orgId, userId);
		if (null == orgMember) {
			logger.error("orgMember not found");
			throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
		}
		if (orgMember._getStringId().equals(orgMemberId)) {
			logger.error("orgMember._id: " + orgMember._getStringId() + " does not match orgMemberId: " + orgMemberId);
			throw new VedantuException(VedantuErrorCode.INVALID_ID);
		}



		for (String sectionId : sectionIds) {
        boolean noSeparator = true;
       
  			logger.debug("for sectionId: " + sectionId);

			OrgMemberMappingInfo orgMemberMappingInfo = new OrgMemberMappingInfo(programId, centerId, sectionId, null);
			logger.debug("looking for orgMemberMappingInfo: " + orgMemberMappingInfo);

			OrgMemberMappingInfo removedMapping = orgMember.remove(orgMemberMappingInfo);
			logger.debug("removedMapping: " + removedMapping);
        
			if (null == removedMapping) {
				logger.error("orgMemberMapping not found for orgId: " + orgId + ", userId: " + userId
						+ ", orgMemberId: " + orgMemberId + ", programId: " + programId + ", centerId: " + centerId
						+ ", sectionId: " + sectionId);
				throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_MAPPING_NOT_FOUND);
			}

		}
		orgMemberRepo.save(orgMember);
		isRemoved.set(true);

		logger.info("removeOrgMemberMapping updated orgMember: " + orgMember);

		return orgMember;
	}

	private UpdateOrgMemberMappingRes updateOrgMemberMapping(UpdateOrgMemberMappingReq updateOrgMemberMappingReq) {


		Set<String> tSectionIds = null == updateOrgMemberMappingReq.sectionIds ? null
				: new HashSet<String>(updateOrgMemberMappingReq.sectionIds);
		Set<String> tAddCourseIds = null == updateOrgMemberMappingReq.courseIds ? null
				: new HashSet<String>(updateOrgMemberMappingReq.courseIds);
		Set<String> tRemoveCourseIds = null == updateOrgMemberMappingReq.removeCourseIds ? null
				: new HashSet<String>(updateOrgMemberMappingReq.removeCourseIds);
		AtomicBoolean isUpdated = new AtomicBoolean(false);
		OrgMember orgMember = updateOrganizationMemberMapping(updateOrgMemberMappingReq.orgId,
				updateOrgMemberMappingReq.targetUserId, updateOrgMemberMappingReq.targetOrgMemberId,
				updateOrgMemberMappingReq.programId, updateOrgMemberMappingReq.centerId, tSectionIds, tAddCourseIds,
				tRemoveCourseIds, isUpdated);

		UpdateOrgMemberMappingRes updateOrgMemberMappingRes = new UpdateOrgMemberMappingRes(orgMember._getStringId(),
				orgMember.recordState, isUpdated.get());
		return updateOrgMemberMappingRes;

	}

	public OrgMember updateOrganizationMemberMapping(String orgId, String userId, String orgMemberId, String programId,
			String centerId, Set<String> sectionIds, Set<String> addCourseIds, Set<String> removeCourseIds,
			AtomicBoolean isUpdated) throws VedantuException {

		OrgMember orgMember = organizationsImpl.getMemberByUserId(orgId, userId);
		if (null == orgMember) {
			logger.error("orgMember not found");
			throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
		}
		if (!orgMember._getStringId().equals(orgMemberId)) {
			logger.error("orgMember._id: " + orgMember._getStringId() + " does not match orgMemberId: " + orgMemberId);
			throw new VedantuException(VedantuErrorCode.INVALID_ID);
		}
		if (CollectionUtils.isEmpty(orgMember.mappings)) {
			logger.debug("no mappings found");
			return orgMember;
		}

		boolean updated = false;
		logger.debug("iterating through sectionIds");

		for (String sectionId : sectionIds) {

			logger.debug("for sectionId: " + sectionId);

			OrgMemberMappingInfo orgMemberMappingInfo = new OrgMemberMappingInfo(programId, centerId, sectionId, null);
			logger.debug("looking for orgMemberMappingInfo: " + orgMemberMappingInfo);

			OrgMemberMappingInfo infoToModify = null;

			for (OrgMemberMappingInfo info : orgMember.mappings) {
				if (null == info) {
					continue;
				}
				if (orgMemberMappingInfo.equals(info)) {
					infoToModify = info;
					break;
				}
			}
			if (null == infoToModify) {
				logger.debug("corresponding mapping not found");
				continue;
			}

			logger.debug("corresponding mapping to be modified infoToModify: " + infoToModify);

			infoToModify.addCourses(addCourseIds);
			infoToModify.removeCourses(removeCourseIds);

			logger.debug("after adding and removing courses infoToModify: " + infoToModify);

			if (CollectionUtils.isEmpty(infoToModify.courseIds)) {
				logger.debug("for orgMemberId: " + orgMemberId + ", profile: " + orgMember.profile
						+ " will remove infoToModify: " + infoToModify + " as it has no courses");
				orgMember.remove(infoToModify);
			}

			updated = true;
		}
		if (updated) {
			orgMemberRepo.save(orgMember);
			isUpdated.set(true);

			logger.info("updateOrgMemberMapping updated orgMember: " + orgMember);
		} else {
			logger.info("updateOrgMemberMapping not updated");
		}
		return orgMember;
	}

	public OrgMember updateEndDateMapping(String orgId, String userId, String sectionId, long endTime)
			throws VedantuException {

		OrgMember orgMember = organizationsImpl.getMemberByUserId(orgId, userId);
		if (null == orgMember) {
			logger.error("orgMember not found");
			throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
		}
		if (CollectionUtils.isEmpty(orgMember.mappings)) {
			logger.debug("no mappings found");
			return orgMember;
		}

		OrgMemberMappingInfo mappingToUpdate = null;
		for (OrgMemberMappingInfo info : orgMember.mappings) {
			if (null == info) {
				continue;
			}
			if (sectionId.equals(info.sectionId)) {
				mappingToUpdate = info;
				break;
			}
		}
		if (null == mappingToUpdate) {
			logger.error("corresponding mapping not found updateEndDateMapping for section:" + sectionId);
			return orgMember;
		}

		mappingToUpdate.endTime = endTime;
		orgMemberRepo.save(orgMember);
		return orgMember;
	}

	private AddOrgMemberMappingRes addOrgMemMapping(AddOrgMemberMappingReq addOrgMemberMappingReq,
			boolean noExceptionOnExistingMapping) throws VedantuException {
		killFreezedRewards(addOrgMemberMappingReq.getUserId());
		AuthHandler authHandler = getAuthHandler(addOrgMemberMappingReq.orgId);
		AddOrgMemberMappingRes res = addMemberMapping(addOrgMemberMappingReq, noExceptionOnExistingMapping);
		if (addOrgMemberMappingReq.targetProfile == OrgMemberProfile.TEACHER) {
			for (String courseId : addOrgMemberMappingReq.courseIds) {
				TeacherAnalytics teacher = new TeacherAnalytics(courseId, addOrgMemberMappingReq.targetOrgMemberId);
				teacherAnalyticsRepo.save(teacher);
			}
		}
		if (addOrgMemberMappingReq.returnOrgProfileWithCourseInfo) {
			Optional<OrgMember> orgMember = orgMemberRepo.findById(addOrgMemberMappingReq.targetOrgMemberId);
			res.info = getOrgMemberProfileRes(orgMember.get(), true, false).info;
		}
		if (addOrgMemberMappingReq.returnNewlyAddedMapping) {
			res.newlyAddedMapping = getMemberMappingForSection(addOrgMemberMappingReq.orgId,
					addOrgMemberMappingReq.targetUserId, addOrgMemberMappingReq.sectionIds.get(0));
		}
		return res;
	}

	public OrgMemberMappingInfo getMemberMappingForSection(String orgId, String userId, String sectionId)
			throws VedantuException {
		OrgMember orgMember = organizationsImpl.getMemberByUserId(orgId, userId);

		if (null == orgMember) {
			throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
		}
		for (OrgMemberMappingInfo mInfo : orgMember.mappings) {
			if (mInfo.sectionId.equals(sectionId)) {
				return mInfo;
			}
		}
		return null;
	}

	private AddOrgMemberMappingRes addMemberMapping(AddOrgMemberMappingReq addOrgMemberMappingReq,
			boolean noExceptionOnExistingMapping) {

		checkIfAddMappingAllowed(addOrgMemberMappingReq);
		return addOrgMemberMapping(addOrgMemberMappingReq, noExceptionOnExistingMapping);
	}

	public SaleDetails addSaleDetails(SaleDetailsInfo info, String orderId, String pointOfSale, String salesPersonId,
			String targetOrgMemberId, String orgId, String sectionId) {
		SaleDetails saleDetails = new SaleDetails(orgId, targetOrgMemberId, orderId, pointOfSale, salesPersonId,
				sectionId, info.origSaleAmount, info.discountPercentage, info.roundOff, info.totalSaleAmount,
				info.paymentItems);
		saleDetails.calculateTotalSaleAmount();
		saleDetailsRepo.save(saleDetails);
		return saleDetails;
	}

	private AddOrgMemberMappingRes addOrgMemberMapping(AddOrgMemberMappingReq addOrgMemberMappingReq,
			boolean noExceptionOnExistingMapping) {

		Set<String> tSectionIds = null == addOrgMemberMappingReq.sectionIds ? null
				: new HashSet<String>(addOrgMemberMappingReq.sectionIds);
		Set<String> tCourseIds = null == addOrgMemberMappingReq.courseIds ? null
				: new HashSet<String>(addOrgMemberMappingReq.courseIds);
		AtomicBoolean isAdded = new AtomicBoolean(false);
		OrgMember orgMember = addOrganizationMemberMapping(addOrgMemberMappingReq.orgId,
				addOrgMemberMappingReq.targetUserId, addOrgMemberMappingReq.targetOrgMemberId,
				addOrgMemberMappingReq.programId, addOrgMemberMappingReq.centerId, tSectionIds, tCourseIds, isAdded,
				noExceptionOnExistingMapping, addOrgMemberMappingReq.transactionId, addOrgMemberMappingReq.packageDays);
		String orderId = getOrderIdForAddedSection(orgMember, addOrgMemberMappingReq.sectionIds.get(0));

		if (addOrgMemberMappingReq.saleDetailsInfo != null) {
			SaleDetails saleDetails = addSaleDetails(addOrgMemberMappingReq.saleDetailsInfo, orderId,
					addOrgMemberMappingReq.pointOfSale, addOrgMemberMappingReq.userId,
					addOrgMemberMappingReq.targetOrgMemberId, addOrgMemberMappingReq.orgId,
					addOrgMemberMappingReq.sectionIds.get(0));
			addSaleDetailsToMapping(addOrgMemberMappingReq.orgId, addOrgMemberMappingReq.targetUserId,
					addOrgMemberMappingReq.sectionIds.get(0), saleDetails._getStringId());
		}
		// TODO: add relationship between saleDetails and orgMember mapping
		AddOrgMemberMappingRes addOrgMemberMappingRes = new AddOrgMemberMappingRes(orgMember._getStringId(),
				orgMember.recordState, isAdded.get());
		return addOrgMemberMappingRes;

	}

	

	public OrgMember addSaleDetailsToMapping(String orgId, String userId, String sectionId, String saleDetailsId)
			throws VedantuException {
		OrgMember orgMember = organizationsImpl.getMemberByUserId(orgId, userId);
		if (null == orgMember) {
			logger.error("orgMember not found");
			throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
		}
		if (CollectionUtils.isEmpty(orgMember.mappings)) {
			logger.debug("no mappings found");
			throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_MAPPING_NOT_FOUND);
		}

		OrgMemberMappingInfo mappingToUpdate = null;
		for (OrgMemberMappingInfo info : orgMember.mappings) {
			if (null == info) {
				continue;
			}
			if (sectionId.equals(info.sectionId)) {
				mappingToUpdate = info;
				break;
			}
		}
		if (null == mappingToUpdate) {
			logger.error("corresponding mapping not found addSaleDetails for section:" + sectionId);
			throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_MAPPING_NOT_FOUND);
		}
		mappingToUpdate.saleDetailsId = saleDetailsId;
		orgMemberRepo.save(orgMember);
		return orgMember;
	}


	public OrgMember addOrganizationMemberMapping(String orgId, String userId, String orgMemberId, String programId,
			String centerId, Set<String> sectionIds, Set<String> courseIds, AtomicBoolean isAdded,
			boolean noExceptionOnExistingMapping, String transactionId, int packageDays) throws VedantuException {

		OrgMember orgMember = organizationsImpl.getMemberByUserId(orgId, userId);

		if (null == orgMember) {
			throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
		}

		if (!orgMember._getStringId().equals(orgMemberId)) {
			throw new VedantuException(VedantuErrorCode.INVALID_ID, "found orgMember[" + orgMember._getStringId()
					+ "] and provide orgMemberId[" + orgMemberId + "] are different");
		}

		logger.debug("iterating through sectionIds");

		boolean addedMapping = false;
		for (String sectionId : sectionIds) {

			logger.debug("for sectionId: " + sectionId);

			OrgMemberMappingInfo orgMemberMappingInfo = new OrgMemberMappingInfo(programId, centerId, sectionId,
					courseIds);

			if (!CollectionUtils.isEmpty(orgMember.mappings) && orgMember.mappings.contains(orgMemberMappingInfo)) {
				logger.error("orgMemberMapping already exists for orgId: " + orgId + ", userId: " + userId
						+ ", orgMemberId: " + orgMemberId + ", programId: " + programId + ", centerId: " + centerId
						+ ", sectionId: " + sectionId);
				if (!noExceptionOnExistingMapping) {
					throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_MAPPING_ALREADY_EXISTS);
				} else {
					// To indicate whether user already has this section
					addedMapping = true;
				}
			} else {
				SrcEntity item = new SrcEntity(EntityType.SECTION, sectionId);

				addedMapping = orgMember.add(orgMemberMappingInfo);
				if (addedMapping) {
					// when a user is added to paid section/programe then sectionIds.size() will be
					// ==
					// 1,
					// which we already have verified before this API call
					orgMemberMappingInfo.orderId = StringUtils.isEmpty(transactionId) ? HardCodedConstants.emptyString
							: markTransactionConmpleted(item, transactionId);
					orgMemberMappingInfo.timeJoined = System.currentTimeMillis();
					if (packageDays > 0) {
						orgMemberMappingInfo.endTime = orgMemberMappingInfo.timeJoined
								+ TimeUnit.DAYS.toMillis(packageDays);
					}
				}
				logger.info("added orgMemberMappingInfo: " + orgMemberMappingInfo);
			}
		}
		if (addedMapping) {
			orgMemberRepo.save(orgMember);
			logger.info("addOrgMemberMapping saved orgMember: " + orgMember);
		}

		isAdded.set(addedMapping);

		return orgMember;
	}

	public String markTransactionConmpleted(SrcEntity item, String transactionId) throws VedantuException {

		Optional<Transaction> transaction = transactionRepo.findById(transactionId);

		Order order = orderRepo.findByOrderId(transaction.get().orderId);

		if (transaction.get().isConsumed()) {
			throw new VedantuException(VedantuErrorCode.INVALID_TRANSACTION,
					"transaction[" + transactionId + "] is already completed");
		}

		if (transaction.get().status != TransactionStatus.SUCCESS) {
			throw new VedantuException(VedantuErrorCode.INCOMPLETE_TRANSACTION,
					"transaction[" + transactionId + "] was not completed, status: " + transaction.get().status);
		}

		if (item != null && !order.__isValidItem(item)) {
			throw new VedantuException(VedantuErrorCode.INVALID_TRANSACTION,
					"transaction[" + transactionId + "] is not associated to  : " + item);
		}
		transaction.get().setConsumed(true);
		order.consumed = true;
		List<String> updateFields = Arrays.asList("consumed");
		orderRepo.save(order);
		transactionRepo.save(transaction.get());
		if (order.couponCode != null && order.couponCode != "") {
			CouponCode couponCode = couponCodeRepo.findByCode(order.couponCode);
			couponCode.usageCount += 1;
			couponCodeRepo.save(couponCode);
		}
		return String.valueOf(transaction.get().getOrderId());
	}

	private List<OrgSection> checkIfAddMappingAllowed(AddOrgMemberMappingReq addOrgMemberMappingReq) {
		logger.error("checkIfAddMappingAllowed Inside checkIfAddMappingAllowed");
		logger.debug("checking for sectionids: " + addOrgMemberMappingReq.sectionIds);
		if (!StringUtils.isEmpty(addOrgMemberMappingReq.userId)
				&& !StringUtils.isEmpty(addOrgMemberMappingReq.targetUserId)) {
			if (addOrgMemberMappingReq.userId.equalsIgnoreCase(addOrgMemberMappingReq.targetUserId)) {
				OrgMember orgMember = organizationsImpl.getMemberByUserId(addOrgMemberMappingReq.orgId,
						addOrgMemberMappingReq.userId);
				OrgMemberState orgMemberState = orgMember._getMemberState(System.currentTimeMillis());
				logger.error("checkIfAddMappingAllowed OrgMemberState is " + orgMemberState.name());
				if (orgMemberState == OrgMemberState.BLOCKED) {
					throw new VedantuException(VedantuErrorCode.MEMBER_IS_BLOCKED, "Your Profile Is De-Activated");
				}
			} else {
				OrgMember salesPerson = organizationsImpl.getMemberByUserId(addOrgMemberMappingReq.orgId,
						addOrgMemberMappingReq.userId);
				OrgMember orgMember = organizationsImpl.getMemberByUserId(addOrgMemberMappingReq.orgId,
						addOrgMemberMappingReq.targetUserId);
                OrgMemberState salesPersonState = salesPerson._getMemberState(System.currentTimeMillis());
                OrgMemberState orgMemberState = orgMember._getMemberState(System.currentTimeMillis());
                if (orgMemberState == OrgMemberState.BLOCKED) {
                    throw new VedantuException(VedantuErrorCode.MEMBER_IS_BLOCKED, "Student Profile Is De-Activated");
                }
                if (salesPersonState == OrgMemberState.BLOCKED) {
                    throw new VedantuException(VedantuErrorCode.MEMBER_IS_BLOCKED, "Your Profile Is De-Activated");
                }
            }
        }
        List<OrgSection> orgSections = getSectionsByIds(addOrgMemberMappingReq.orgId, addOrgMemberMappingReq.programId.trim(),
                ObjectIdUtils.toObjectIds(addOrgMemberMappingReq.sectionIds, true));
        if (orgSections.isEmpty()) {
            String msg = "Can not find any section for given sectionIds : " + addOrgMemberMappingReq.sectionIds;
            logger.error(msg);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_PROGRAM_SECTION_NOT_FOUND, msg);
        }

        if (addOrgMemberMappingReq.targetProfile != OrgMemberProfile.STUDENT) {
            return orgSections;
        }
        // now verify the transactionId if the section is open and paid
        // if any section is paid in the list then only one action can be
		// performed (adding to one
		// section)
		int sectionsLength = orgSections.size();
		for (OrgSection orgSection : orgSections) {
			if (AccessScope.OPEN.equals(orgSection.accessScope) && RevenueModel.PAID.equals(orgSection.revenueModel)) {
				if (sectionsLength > 1) {
					throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED,
							"You can add a student to only one paid program at a time.");
				}
				// this section is paid so now verify and mark the transaction
				// consumed
				SrcEntity item = new SrcEntity(EntityType.SECTION, orgSection._getStringId());
				String transactionId = addOrgMemberMappingReq.transactionId;
				if (StringUtils.isEmpty(transactionId)) {
					Optional<Organization> org = organizationRepo.findById(addOrgMemberMappingReq.orgId);
					if (!org.isPresent()) {
						throw new VedantuException(VedantuErrorCode.INVALID_ACCESS_CODE,
								"with the given orgId,should not find Oranization");
					}
					if (StringUtils.isEmpty(addOrgMemberMappingReq.sellerReferenceNo)
							|| StringUtils.isEmpty(addOrgMemberMappingReq.pointOfSale)
							|| !(org.get().__isValidPointOfSale(addOrgMemberMappingReq.pointOfSale))) {
						OrgMember orgMember = organizationsImpl.getMemberByUserId(addOrgMemberMappingReq.orgId,
								addOrgMemberMappingReq.targetUserId);
						throw new VedantuException(VedantuErrorCode.INVALID_INPUT_DATA,
								"Data provided is not sufficient. Please check if correct value of TransactionId or Seller Reference No["
										+ addOrgMemberMappingReq.sellerReferenceNo + "] with its Point Of Sale["
										+ addOrgMemberMappingReq.pointOfSale + "] are provided for user : "
										+ orgMember.firstName + " " + orgMember.lastName
										+ (StringUtils.isEmpty(orgMember.email) ? "" : ", Email: " + orgMember.email));
					}
					// now we should create an order and transaction for this
					// record
					SrcEntity customer = new SrcEntity(EntityType.USER, addOrgMemberMappingReq.targetUserId);
					addOrgMemberMappingReq.transactionId = createThirdPartyOrderAndTransaction(customer,
							addOrgMemberMappingReq.userId, DeviceType.WEB, orgSection._getSellableItemDetails(),
							addOrgMemberMappingReq.pointOfSale, addOrgMemberMappingReq.sellerReferenceNo);
				} else {

					verifyTransactionInfo(item, transactionId);
				}
			}
		}
		return orgSections;
	}

	private Transaction verifyTransactionInfo(SrcEntity item, String transactionId) {
		Optional<Transaction> transaction = transactionRepo.findById(transactionId);
		if (!transaction.isPresent()) {
			throw new VedantuException(VedantuErrorCode.INVALID_TRANSACTION, "invalid transactionId");
		}

		Order order = orderRepo.findByOrderId(transaction.get().getOrderId());

		if (!order.__isValidItem(item)) {
			throw new VedantuException(VedantuErrorCode.INVALID_TRANSACTION,
					"transaction[" + transactionId + "] is not associated to  : " + item);
		}
		return transaction.get();
	}

	private String createThirdPartyOrderAndTransaction(SrcEntity customer, String userId, DeviceType deviceType,
			SellableItemDetails getSellableItemDetails, String pointOfSale, String sellerReferenceNo) {

		Order order = createOrder(userId, deviceType, customer);

		OrderItemDetails orderItemDetails = new OrderItemDetails();

		orderItemDetails.period = new Interval(System.currentTimeMillis(), Interval.NO_END);

		addItemToOrder(order, getSellableItemDetails,
				ItemCategory.valueOf(getSellableItemDetails.getItem().type.name()), 1, HardCodedConstants.emptyString,
				orderItemDetails, new Location());

		Object sellableItemInfo;
		String transactionId = createThirdPartyTransaction(userId, order, deviceType,
				getSellableItemDetails.getCostRate(), pointOfSale, sellerReferenceNo, customer, null, null);
		//
		// VedantuTransactionManager transactionManager =
		// VedantuTransactionManager.getInstance();
		//
		// String transactionId =
		// transactionManager.getVedantuTransactionId(userId, order.orderId,
		// StringUtils.EMPTY, deviceType, costRate.value,
		// costRate.currencyCode);
		//
		// Transaction transaction =
		// TransactionDAO.INSTANCE.getTransaction(transactionId);
		// transaction.amountPaid = costRate.value;
		// transaction.paymentChannel = Transaction.PAYMENT_CHANNEL_THIRD_PATY;
		// transaction.pointOfSale = pointOfSale;
		// transaction.sellerReferenceNo = sellerReferenceNo;
		// transaction.status = TransactionStatus.SUCCESS;
		// transaction.transactionTime =
		// String.valueOf(System.currentTimeMillis());
		// transaction.type = TransactionType.THIRD_PARTY_CREDIT;
		// transaction.ipAddress =
		// Http.Context.current().request().getHeader("X-Real-IP");
		// TransactionDAO.INSTANCE.save(transaction);
		//
		// User user = UserDAO.INSTANCE.getById(customer.id);
		// order.billingEmail = user._getCommunicationEmail();
		// order.ipAddress = transaction.ipAddress;
		// order.orderState = OrderState.CONFIRMED;
		// order.pointOfSale = transaction.pointOfSale;
		// order.sellerReferenceNo = transaction.sellerReferenceNo;
		// transactionManager.updateOrderAndGetPaymentReceivedRes(order,
		// transaction);
		return transactionId;
	}

	public String createThirdPartyTransaction(String userId, Order order, DeviceType deviceType, CostRate costRate,
			String pointOfSale, String sellerReferenceNo, SrcEntity customer, /*
																				 * billTo and shipTo will be null if the
																				 * order tempUser==true
																				 */
			AddressTo billTo, AddressTo shipTo) throws VedantuException {

		String transactionId = getVedantuTransactionId(userId, order.orderId, HardCodedConstants.emptyString,
				deviceType, costRate.getValue(), costRate.getCurrencyCode());

		Transaction transaction = getTransaction(transactionId);
		transaction.amountPaid = costRate.getValue();
		transaction.paymentChannel = Transaction.PAYMENT_CHANNEL_THIRD_PATY;
		transaction.pointOfSale = pointOfSale;
		transaction.sellerReferenceNo = sellerReferenceNo;
		transaction.status = TransactionStatus.SUCCESS;
		transaction.transactionTime = String.valueOf(System.currentTimeMillis());
		transaction.type = TransactionType.THIRD_PARTY_CREDIT;
		// transaction.ipAddress =
		// Http.Context.current().request().getHeader("X-Real-IP");
		transactionRepo.save(transaction);

		order.ipAddress = transaction.ipAddress;
		order.orderState = OrderState.CONFIRMED;
		order.pointOfSale = transaction.pointOfSale;
		order.sellerReferenceNo = transaction.sellerReferenceNo;
		if (!order.tempUser) {
			Optional<User> user = userRepo.findById(customer.getId());
			order.billingEmail = user.get()._getCommunicationEmail();
		} else {
			generateInvoiceForTempUser(order, billTo, shipTo);
		}
		updateOrderAndGetPaymentReceivedRes(order, transaction);
		return transactionId;
	}

	private Transaction getTransaction(String transactionId) {
		if (StringUtils.isEmpty(transactionId)) {
			throw new VedantuException(VedantuErrorCode.INVALID_TRANSACTION, "invalid transactionId");
		}
		Optional<Transaction> transaction = transactionRepo.findById(transactionId);
		if (!transaction.isPresent()) {
			throw new VedantuException(VedantuErrorCode.INVALID_TRANSACTION, "invalid transactionId");
		}
		return transaction.get();
	}

	public String getVedantuTransactionId(String userId, long orderId, String paymentChannel, DeviceType deviceType,
			int amount, String currencyCode) {

		Transaction transaction = new Transaction(userId, orderId, paymentChannel, deviceType, TransactionType.CREDIT,
				amount, currencyCode);
		transactionRepo.save(transaction);
		return transaction._getStringId();
	}

	public OnPaymentReceivedRes updateOrderAndGetPaymentReceivedRes(Order order, Transaction transaction)
			throws VedantuException {

		if (transaction.status == TransactionStatus.SUCCESS) {
			if (order.orderState == OrderState.CONFIRMED && !order.tempUser) {
				generateInvoice(order);
			}
			order.updatePaymentStatus(transaction.paymentMethod, transaction._getStringId(), transaction.amountPaid);
		}

		orderRepo.save(order);
		OnPaymentReceivedRes res = new OnPaymentReceivedRes(transaction.orderId, transaction._getStringId(),
				transaction.status, transaction.item_sku, transaction.callbackUrl);
		logger.debug("sending onPayment received res : " + res);
		return res;
	}

	public void generateInvoice(Order order) throws VedantuException {
		Optional<User> user = userRepo.findById(order.customer.getId());
		if (!user.isPresent()) {
			throw new VedantuException(VedantuErrorCode.INVALID_CODE, "orgId not found");
		}
		generateInvoice(order, user.get(), null, null);
	}

	public void generateInvoice(Order order, User user, String contactNo, String address) throws VedantuException {

		if (CollectionUtils.isEmpty(order.items) || order.orderState != OrderState.CONFIRMED) {
			throw new VedantuException(VedantuErrorCode.INVALID_ORDER,
					"no items or invalid orderState in order[orderId: " + +order.orderId + ", orderState:"
							+ order.orderState + "]");
		}

		InvoiceInfo invoiceInfo = new InvoiceInfo();
		/*
		 * invoiceInfo.invoiceNo = String.valueOf(CounterDAO.INSTANCE.getNextSequence(
		 * OrderDAO.INSTANCE.getCollection().getName(), "invoiceNo"));
		 */
		invoiceInfo.customer = order.customer;
		invoiceInfo.currencyCode = order.items.get(0).rate.getCurrencyCode();
		if (!order.tempUser) {
			populateBillAndShipAddress(order.billingEmail, invoiceInfo, user, contactNo, address);
		}
		order.invoiceInfo = invoiceInfo;
		order.calculateFinalBillAmount();
		orderRepo.save(order);
	}

	private void populateBillAndShipAddress(String billingEmail, InvoiceInfo invoiceInfo, User user, String contactNo,
			String address) {
		// for now process it only for org and user
		if (invoiceInfo.customer.type != EntityType.ORGANIZATION && invoiceInfo.customer.type != EntityType.USER) {
			return;
		}

		if (StringUtils.isEmpty(billingEmail)) {
			billingEmail = user.email;
		}
		AddressTo billAddress = new AddressTo(user._getFullName(), contactNo, billingEmail, address);

		AddressTo shipAddress = new AddressTo(user._getFullName(), contactNo, billingEmail, address);
		invoiceInfo.billTo = billAddress;
		invoiceInfo.shipTo = shipAddress;
	}

	private void generateInvoiceForTempUser(Order order, AddressTo billTo, AddressTo shipTo) {

		InvoiceInfo invoiceInfo = new InvoiceInfo();
		/*
		 * invoiceInfo.invoiceNo = String.valueOf(CounterDAO.INSTANCE.getNextSequence(
		 * OrderDAO.INSTANCE.getCollection().getName(), "invoiceNo"));
		 */
		invoiceInfo.customer = order.customer;
		invoiceInfo.currencyCode = order.items.get(0).rate.getCurrencyCode();
		invoiceInfo.billTo = billTo;
		invoiceInfo.shipTo = shipTo;
		order.invoiceInfo = invoiceInfo;
		order.calculateFinalBillAmount();
	}

	public void addItemToOrder(Order order, SellableItemDetails sellableItemDetails, ItemCategory category, int count,
			String desc, OrderItemDetails details, Location shippedToLocation) throws VedantuException {

		addItemToOrder(order, sellableItemDetails, category, count, desc, details, shippedToLocation, false);
	}

	public void addItemToOrder(Order order, SellableItemDetails sellableItemDetails, ItemCategory category, int count,
			String desc, OrderItemDetails details, Location shippedToLocation, boolean dontSave)
			throws VedantuException {

		if (sellableItemDetails.getSeller() != null
				&& EntityType.ORGANIZATION.equals(sellableItemDetails.getSeller().type) && StringUtils.isEmpty(desc)) {
			Optional<Organization> org = organizationRepo.findById(sellableItemDetails.getSeller().id);
			if (!org.isPresent()) {
				throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
			}
			desc = org.get().getFullName();
			if (StringUtils.isEmpty(desc)) {
				desc = org.get().getName();
			}
		}
		OrderedItem orderedItem = new OrderedItem(sellableItemDetails.getItemName(), sellableItemDetails.getItem(),
				sellableItemDetails.getSeller(), category, sellableItemDetails.getCostRate(), desc, count, details);
		List<Tax> taxes = getTaxes(category, shippedToLocation);
		orderedItem.calculateCost(taxes);
		if (order.items == null) {
			order.items = new ArrayList<OrderedItem>();
		}
		order.items.add(orderedItem);
		order.updateOrderTotal();
		if (!dontSave) {
			orderRepo.save(order);
		}
	}

	

	private Order createOrder(String userId, DeviceType deviceType, SrcEntity customer) {
		/*
		 * long orderId =
		 * CounterDAO.INSTANCE.getNextSequence(OrderDAO.INSTANCE.getCollection()
		 * .getName(), Order.ORDER_ID); Order order = new Order(userId, deviceType,
		 * customer, orderId); OrderDAO.INSTANCE.save(order);
		 * 
		 * return order;
		 */
		return null;
	}

	private List<OrgSection> getSectionsByIds(String orgId, String programId, List<ObjectId> sectionsIds) {

		return organizationsImpl.getSectionsByIds(orgId, programId, sectionsIds, null, null, null, NO_START, NO_LIMIT,
				new AtomicLong());
        
    }

   
  

	private void killFreezedRewards(String userId) {
        OrgMember orgMember = orgMemberRepo.findByUserId(userId);
        orgMember.freezedRewards = 0;
        orgMember.freezedRewardsOrderId = 0;
        orgMemberRepo.save(orgMember);
    }

	public AddOrgMemberRes addOrgMember(AddOrgMemberReq addOrgMemberReq) {
		AuthHandler authHandler = getAuthHandler(addOrgMemberReq.orgId);

		boolean isMemberIdSysGenerated = authHandler.authType != AuthType.EXT_AUTH_ORG
				&& StringUtils.isEmpty(addOrgMemberReq.getTargetMemberId())
				&& (addOrgMemberReq.getEmail() != null || addOrgMemberReq.contactNumber != null);
		if (isMemberIdSysGenerated) {
            addOrgMemberReq.setTargetMemberId(getNextOrgMemberId(addOrgMemberReq.getOrgId()));
            if (addOrgMemberReq.isOTPsignup) {
                addOrgMemberReq.usePhoneAsUsername = true;
                addOrgMemberReq.useEmailAsUsername = false;
            } else {
                addOrgMemberReq.usePhoneAsUsername = false;
                addOrgMemberReq.useEmailAsUsername = true;
            }
        }

        return addOrgMember(addOrgMemberReq, isMemberIdSysGenerated, authHandler);
    }

	
		private List<OrgMember> getOrganizationMembers(String orgId, OrgMemberProfile targetProfile,
			List<OrgMemberProfile> excludeProfiles, String programId, String centerId, String sectionId,
			String courseId, String query, int start, int size, List<String> excludes, Boolean canImpersonate,
			AtomicLong totalHits) {
		return getOrgMembers(orgId, targetProfile, excludeProfiles, programId, centerId, sectionId, courseId, query,
				start, size, excludes, null, canImpersonate, totalHits);
	}

	public List<OrgMember> getOrgMembers(String orgId, OrgMemberProfile targetProfile,
			List<OrgMemberProfile> excludeProfiles, String programId, String centerId, String sectionId,
			String courseId, String query, int start, int size, List<String> excludes, List<String> includeUserIds,
			Boolean canImpersonate, AtomicLong totalHits) {

		logger.debug("getOrgMembers orgId: " + orgId + ", profiles: " + targetProfile + ", programId: " + programId
				+ ", centerId: " + centerId + ", sectionId: " + sectionId + ", courseId: " + courseId
				+ " exclude profiles " + excludeProfiles + " excludeUserIds " + excludes + ", includeUserIds:"
				+ includeUserIds);
		Criteria criteria = new Criteria();
		Query query1 = new Query();

		// Query<OrgMember> query = getQuery().filter("orgId", orgId);
		criteria.and("orgId").is(orgId);
		// This is one is done because of this issue
		// http://code.google.com/p/morphia/issues/detail?id=225
		// so if profile exists we precisely use that one otherwise we exclude all
		// excludeprofiles
		if (targetProfile != null) {
			criteria.and("profile").is(targetProfile);
			// query.field("profile").hasAllOf(Arrays.asList(profile));

		} else if (excludeProfiles != null) {
			criteria.and("profile").nin(excludeProfiles);
		}

		if (query != null) {
			criteria.orOperator(criteria.and(ConstantsGlobal.FIRST_NAME).is(query.trim()),
					criteria.and(ConstantsGlobal.LAST_NAME).is(query.trim()),
					criteria.and(ConstantsGlobal.EMAIL).is(query.trim()),
					criteria.and(ConstantsGlobal.MEMBER_ID).is(query.trim()));
		}

		if (programId != null) {
			criteria.and("mappings.programId").is(programId);
			// query.filter("mappings.programId", programId);
		}
		if (centerId != null) {
			criteria.and("mappings.centerId").is(centerId);
			// query.filter("mappings.centerId", centerId);
		}
		if (sectionId != null) {
			criteria.and("mappings.sectionId").is(sectionId);
			// query.filter("mappings.sectionId", sectionId);
		}
		if (courseId != null) {
			criteria.and("mappings.courseIds").is(courseId);
			// query.filter("mappings.courseIds", courseId);
		}

		if (excludes != null) {
			criteria.and("userId").nin(excludes);
			// query.field("userId").hasNoneOf(excludeUserIds);
		}

		if (includeUserIds != null) {
			criteria.and("userId").nin(includeUserIds);
			// query.field("userId").hasAnyOf(includeUserIds);
		}

		if (canImpersonate != null) {
			criteria.and("canImpersonate").is(canImpersonate);
			// query.field("canImpersonate").equal(canImpersonate.booleanValue());
		}

		query1.addCriteria(criteria);
//            query1.getSortObject();
//            query1.offset(start).limit(size);
		logger.debug("query: " + query1);

		List<OrgMember> orgMembers = mongoTemplate.find(query1, OrgMember.class);
        List<OrgMember> orgMembers1=orgMemberRepo.findByOrgIdAndProfile(orgId,targetProfile.toString());
        if (totalHits != null) {
            totalHits.set(orgMembers.size());
        }
        return orgMembers;

	}

      public OrgMember updateMember(String orgId, String userId, String orgMemberId, String memberId,
                                  String firstName, String lastName, String dob, Gender gender, String email,
                                  OrgMemberProfile profile, String contactNumber, MemberParentInfo father,
                                  MemberParentInfo mother, MemberParentInfo guardian, String parentEmail,
                                  boolean canImpersonate, List<OrgMemberExtraInfo> extraInfo, Set<String> updateList)
            throws VedantuException {
        logger.debug("updateMember orgId: " + orgId + ", userId: " + userId + ", orgMemberId: "
                + orgMemberId + ", memberId" + memberId + ", firstName: " + firstName
                + ", lastName:" + lastName + ", dob: " + dob + ", gender" + gender + ", email"
                + email + ", profile" + profile + ", contactNumber: " + contactNumber);

        OrgMember orgMember = organizationsImpl.getMemberByUserId(orgId, userId);

        if (null == orgMember) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }
        if (!orgMember._getStringId().equals(orgMemberId)) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID, "orgMemberId is invalid");
        }
        if (!StringUtils.isEmpty(memberId))
            orgMember.memberId = memberId;
        if (!StringUtils.isEmpty(firstName) && firstName != null)
            orgMember.firstName = firstName;
        if (!StringUtils.isEmpty(lastName))
            orgMember.lastName = lastName;
        if (!StringUtils.isEmpty(dob))
            orgMember.dob = dob;
        if (!StringUtils.isEmpty(gender))
            orgMember.gender = gender;
        if (!StringUtils.isEmpty(email))
            orgMember.email = email;
        if (!StringUtils.isEmpty(profile))
            orgMember.profile = profile;
        if (!StringUtils.isEmpty(contactNumber))
            orgMember.contactNumber = contactNumber;
        if (father != null && !StringUtils.isEmpty(father))
            orgMember.father = father;
        if (mother != null && !StringUtils.isEmpty(mother))
            orgMember.mother = mother;
        if (guardian != null && !StringUtils.isEmpty(guardian))
            orgMember.guardian = guardian;
        if (parentEmail != null)
            orgMember.parentEmail = parentEmail;
        if (!StringUtils.isEmpty(canImpersonate))
            orgMember.canImpersonate = canImpersonate;
        if (!CollectionUtils.isEmpty(extraInfo) && extraInfo != null)
            orgMember.extraInfo = extraInfo;
        if (!CollectionUtils.isEmpty(updateList)) {
            updateModel(orgMember, new ArrayList<String>(updateList));
        } else {
            orgMemberRepo.save(orgMember);
        }
        logger.info("updatedMember updated orgMember: " + orgMember);

        return organizationsImpl.getMemberByUserId(orgId, userId);
    }


    private GetOrgMemberProfileRes getOrgMemberByMemberId(GetOrgMemberReq getOrgMemberReq) {
        OrgMember orgMember = getMemberByMemberId(getOrgMemberReq.getOrgId(), getOrgMemberReq.getMemberId());
        if (orgMember == null) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND,
                    "No member found with memberId : " + getOrgMemberReq.getMemberId());
        }

        GetOrgMemberProfileRes getOrgMemberProfileRes = getOrgMemberProfileRes(orgMember,
                getOrgMemberReq.isEnsureCourseInfo(), getOrgMemberReq.getKey);
        return getOrgMemberProfileRes;
    }

    private OrgMember getMemberByMemberId(String orgId, String memberId) {
        logger.info("getMemberByMemberId orgId: " + orgId + ", memberId: " + memberId);

        //OrgMember orgMember = orgMemberRepo.findByIdAndOrgId(memberId, orgId);
        OrgMember orgMember = orgMemberRepo.findByMemberIdAndOrgId(memberId, orgId);

        if (null == orgMember) {
            logger.error("cannot find orgMember for orgId: " + orgId + ", memberId: " + memberId);
        }

        logger.info("getMemberByMemberId found orgMember: " + orgMember);

        return orgMember;
    }

    private GetOrgMemberProfileRes getOrgMemberProfileRes(OrgMember orgMember, boolean ensureCourseInfo, boolean addKey)
            throws VedantuException {

        OrgMemberExtendedInfo orgMemberExtendedInfo = (OrgMemberExtendedInfo) orgMemberInfo.toExtendedInfo(orgMember);
        populateUserPublicProfileDetails(orgMember, orgMemberExtendedInfo);

        populateProgramHierarchy(orgMember, orgMemberExtendedInfo, ensureCourseInfo);

        GetOrgMemberProfileRes getOrgMemberProfileRes = new GetOrgMemberProfileRes();
        Optional<Organization> org = organizationRepo.findById(orgMember.getOrgId());
        getOrgMemberProfileRes.setInfo(orgMemberExtendedInfo);
        getOrgMemberProfileRes.setDoubtsForumMode(org.get().getDoubtsForumMode());
        getOrgMemberProfileRes.setShowClassroomConnect(org.get().isShowClassroomConnect());
        if (addKey) {
            getOrgMemberProfileRes.key = getPrivateKey(orgMember.getUserId());
        }
        return getOrgMemberProfileRes;
    }

    private AddOrgMemberRes addOrgMember(AddOrgMemberReq addOrgMemberReq,
                                         boolean isMemberIdSysGenerated, AuthHandler authHandler) throws VedantuException {

        AuthHandler handler = null != authHandler ? authHandler : getAuthHandler(addOrgMemberReq.orgId);

        return addMember(addOrgMemberReq, isMemberIdSysGenerated);
    }

    public AddOrgMemberRes addMember(AddOrgMemberReq req, boolean isMemberIdSysGenerated)
            throws VedantuException {
        if (!req.isOTPsignup)
            verifyExtraInputFields(req);
        return addOrgMember(req, isMemberIdSysGenerated);
    }

    protected void verifyExtraInputFields(AddOrgMemberReq addMemberReq) throws VedantuException {

        Map<String, OrgMemberExtraInfo> extraInputFieldsmap = convertToInputFieldMap(addMemberReq.extraInfo);
        Organization org = new Organization();
        List<InputFieldInfo> orgExtraInfos = org.extraMemberInfoFields == null ? null
                : org.extraMemberInfoFields.get(addMemberReq.profile);

        if (!CollectionUtils.isEmpty(orgExtraInfos)) {

            List<String> missingFields = new ArrayList<String>();

            List<String> invalidFields = new ArrayList<String>();
            for (InputFieldInfo inputFieldInfoFromOrg : orgExtraInfos) {
                OrgMemberExtraInfo iFieldInfo = extraInputFieldsmap.get(inputFieldInfoFromOrg.name);

                if (inputFieldInfoFromOrg.required
                        && (iFieldInfo == null || StringUtils.isEmpty(iFieldInfo.value))) {
                    missingFields.add(inputFieldInfoFromOrg.name);
                    continue;
                }
                if (iFieldInfo != null
                        && !StringUtils.isEmpty(iFieldInfo.value)
                        && !inputFieldInfoFromOrg.validationType.validate(inputFieldInfoFromOrg,
                        iFieldInfo.value)) {

                    invalidFields.add(inputFieldInfoFromOrg.name + " : " + iFieldInfo.value);
                }
            }

            if (!CollectionUtils.isEmpty(missingFields)) {
                throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                        "missing required fields : " + missingFields.stream().collect(Collectors.joining(",")));
            }

            if (!CollectionUtils.isEmpty(invalidFields)) {
                throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                        " invalid fields value " + invalidFields.stream().collect(Collectors.joining(",")));
            }
        }
        extraInputFieldsmap.clear();

    }

    protected Map<String, OrgMemberExtraInfo> convertToInputFieldMap(
            List<OrgMemberExtraInfo> inputFieldInfos) {

        Map<String, OrgMemberExtraInfo> fieldsMap = new HashMap<String, OrgMemberExtraInfo>();
        if (CollectionUtils.isEmpty(inputFieldInfos)) {
            return fieldsMap;
        }
        for (OrgMemberExtraInfo inputFieldInfo : inputFieldInfos) {
            fieldsMap.put(inputFieldInfo.name, inputFieldInfo);
        }
        return fieldsMap;
    }

    private String getNextOrgMemberId(String orgId) {

        return prefex + getNextSequence("orgmembers", orgId, 1);
    }


    public long getNextSequence(String collectionName, String field, int byValue) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("collection").is(collectionName);
        criteria.and("field").is(field);
        Counter counter = mongoTemplate.findOne(query.addCriteria(criteria), Counter.class);
        logger.info("counterValu++++++++++++++" + counter.getValue());
        counter.setValue(counter.getValue() + byValue);
        counterRepo.save(counter);
        logger.info("After counterValu++++++++++++++" + counter.getValue());
        return counter.value;

    }

    private GetOrgMembersRes getOrgantionMembers(GetOrgMembersReq getOrgMembersReq) {
        AtomicLong totalHits = new AtomicLong();
        List<OrgMember> orgMembers = getOrganizationMembers(getOrgMembersReq.getOrgId(),
                getOrgMembersReq.getTargetProfile(), getOrgMembersReq.getExcludeProfiles(),
                getOrgMembersReq.getProgramId(), getOrgMembersReq.getCenterId(), getOrgMembersReq.getSectionId(),
                getOrgMembersReq.getCourseId(), getOrgMembersReq.getQuery(), getOrgMembersReq.getStart(),
                getOrgMembersReq.getSize(), getOrgMembersReq.getExcludes(), getOrgMembersReq.getCanImpersonate(),
                totalHits);

        GetOrgMembersRes getOrgMembersRes = new GetOrgMembersRes();
        if (!orgMembers.isEmpty()) {
            List<OrgMemberExtendedInfo> orgMemberExtendedInfos = new ArrayList<OrgMemberExtendedInfo>();
            Optional<User> user = null;
            for (OrgMember orgMember : orgMembers) {
                if (null == orgMember) {
                    continue;
                }
                user = userRepo.findById(orgMember.getUserId());
                if (!user.isPresent()) {
                    continue;
                }
                OrgMemberExtendedInfo orgMemberExtendedInfo = (OrgMemberExtendedInfo) orgMember
                        .toExtendedInfo();
                orgMemberExtendedInfo.isEmailVerified = user.get().isEmailVerified;
                orgMemberExtendedInfo.isPhoneVerified = user.get().isPhoneVerified;
                orgMemberExtendedInfos.add(orgMemberExtendedInfo);

                populateProgramHierarchy(orgMember, orgMemberExtendedInfo, false);
            }

            getOrgMembersRes.list = orgMemberExtendedInfos;
            getOrgMembersRes.totalHits = totalHits.longValue();
        }

        return getOrgMembersRes;
    }

    private GetOrgMemberProfileRes getOrgMemberWithEmail(GetOrgMemberWithEmailReq getOrgMemberWithEmailReq) {

        OrgMember orgMember = orgMemberRepo.findByOrgIdAndEmail(getOrgMemberWithEmailReq.getOrgId(), getOrgMemberWithEmailReq.getEmail());

        if (orgMember == null) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND, "No member found with memberId : " + getOrgMemberWithEmailReq.getEmail());
        }

        GetOrgMemberProfileRes getOrgMemberProfileRes = getOrgMemberProfileRes(orgMember,
                getOrgMemberWithEmailReq.isEnsureCourseInfo(), getOrgMemberWithEmailReq.isGetKey());

        return getOrgMemberProfileRes;
    }

    private GetOrgMemberProfileRes getOrgMember(GetOrgMemberProfileReq getOrgMemberProfileReq) {

        updateOrgMemberExpiredMappings(getOrgMemberProfileReq.getOrgId(),
                getOrgMemberProfileReq.getTargetUserId());
        OrgMember orgMember = orgMemberRepo.findByOrgIdAndUserId(getOrgMemberProfileReq.getOrgId(), getOrgMemberProfileReq.getTargetUserId());


        if (null == orgMember) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }

        GetOrgMemberProfileRes getOrgMemberProfileRes = getOrgMemberProfileRes(orgMember, getOrgMemberProfileReq.isEnsureCourseInfo(), getOrgMemberProfileReq.getKey);

        return getOrgMemberProfileRes;

    }

    private void populateProgramHierarchy(OrgMember orgMember,
                                          OrgMemberExtendedInfo orgMemberExtendedInfo, boolean ensureCourseInfo) {

        if (orgMember.getMappings() != null) {

            for (OrgMemberMappingInfo mapping : orgMember.getMappings()) {
                if (mapping == null) {
                    continue;
                }

                OrgStructureBasicInfo program = getProgramBasicInfo(mapping.getProgramId());
                OrgProgramBasicInfo programInfo = null;
                if (program != null)
                    programInfo = orgMemberExtendedInfo.mappings
                            ._getOrAddProgram(program);

                if (ensureCourseInfo && program != null) {
                    mapping.courseIds = programInfo.courseIds;
                }

                OrgStructureBasicInfo progCenter = getCenterBasicInfo(mapping.centerId);
                OrgProgramCenterBasicInfo progCenterInfo = null;
                if (progCenter != null && programInfo != null)
                    progCenterInfo = programInfo._getOrAddProgramCenter(progCenter);

                Optional<OrgSection> orgSection = orgSectionRepo.findById(mapping.getSectionId());
                if (orgSection.isPresent()) {

                    OrgStructureBasicInfo progSection = (OrgStructureBasicInfo) orgSection.get().toBasicInfo();
                    if (progSection != null && progCenterInfo != null) {
                        OrgProgramSectionBasicInfo progSectionInfo = progCenterInfo
                                ._getOrAddProgramSection(progSection);

                        progSectionInfo.setOrderId(mapping.getOrderId());
                        progSectionInfo.setTimeJoined(mapping.timeJoined);
                        progSectionInfo.setEndTime(mapping.endTime);
                        if (ensureCourseInfo) {
                            // for now only add desc, if needed we can
                            // progSectionInfo.addSectionExtraInfo(orgSection);
                            progSectionInfo.desc = (orgSection.get().getDesc() != null) ? orgSection.get().getDesc() : HardCodedConstants.emptyString;

                            progSectionInfo.addSectionExtraInfo(orgSection.get());
                        }


                        if (!mapping.getCourseIds().isEmpty()) {
                            for (String courseId : mapping.getCourseIds()) {
                                if (StringUtils.isEmpty(courseId)) {
                                    continue;
                                }
                                BoardBasicInfo course = getBoardBasicInfo(courseId);
                                if (null != course) {
                                    progSectionInfo._getOrAddProgramCourse(course);
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    private BoardBasicInfo getBoardBasicInfo(String courseId) {
        Optional<Board> board = boardRepo.findById(courseId);
        if (!board.isPresent())
            return null;
        BoardBasicInfo orgStructureBasicInfo = new BoardBasicInfo(board.get());
        return orgStructureBasicInfo;
    }

    private OrgStructureBasicInfo getProgramBasicInfo(String programId) {
        Optional<OrgProgram> org = orgProgramRepo.findById(programId);
        if (!org.isPresent())
            return null;
        OrgStructureBasicInfo orgStructureBasicInfo = new OrgStructureBasicInfo(org.get());
        return orgStructureBasicInfo;
    }

    private OrgStructureBasicInfo getCenterBasicInfo(String centerId) {
        Optional<OrgCenter> org = orgCenterRepo.findById(centerId);
        if (!org.isPresent())
            return null;
        OrgStructureBasicInfo orgStructureBasicInfo = new OrgStructureBasicInfo(org.get());
        return orgStructureBasicInfo;
    }

    public String getPrivateKey(String userId) throws VedantuException {

        Optional<User> user = userRepo.findById(userId);
        if (!user.isPresent()) {
            return null;
        }
        SecurityCredentials credentials = user.get().getCredentials();
        if (credentials == null) {
            credentials = setCredentials(user.get());
        }
        // DatatypeConverter.printHexBinary(credentials.getPrivateKey())
        return Base64.encode(credentials.getPrivateKey());
    }

    private synchronized SecurityCredentials setCredentials(User user)
            throws VedantuException {

        if (user.getCredentials() != null) {
            return user.getCredentials();

        }
        user.setCredentials(EncryptionUtils.generateKeys());
        userRepo.save(user);
        return user.getCredentials();
    }

    private void populateUserPublicProfileDetails(OrgMember orgMember,
                                                  OrgMemberExtendedInfo orgMemberExtendedInfo) throws VedantuException {

        UserExtendedInfo userExtendedInfo = getExtendedInfo(orgMember.getUserId());
        if (null == userExtendedInfo) {
            logger.debug("populateUserPublicProfileDetails no userExtendedInfo found for userId: "
                    + orgMember.userId);
            return;
        }
        final String username = userExtendedInfo.getUsername();
        final String verifiedEmail = userExtendedInfo.isEmailVerified ? userExtendedInfo.getEmail()
                : HardCodedConstants.emptyString;
        String s = getAuthHandler(orgMember.getOrgId())
                .getMemberUsername(orgMember.getOrgId(), orgMember.getMemberId());
        final boolean isUsernameOrgSpecific = username.equals(s);

        orgMemberExtendedInfo.setUserPublicProfileDetails(username, verifiedEmail,
                isUsernameOrgSpecific);

        // if the no joinedTime is populated in the org member mapping so update
        // it as the time of
        // user creation
        boolean updatedMapping = false;

        if (orgMember.getMappings() != null) {
            for (OrgMemberMappingInfo mapping : orgMember.getMappings()) {
                if (mapping.getTimeJoined() < 1) {
                    mapping.setTimeJoined(orgMember.getTimeCreated());
                    updatedMapping = true;
                }
            }
        }

        if (updatedMapping) {
            orgMemberRepo.save(orgMember);
        }

    }

    public AuthHandler getAuthHandler(String orgId) throws VedantuException {

        Optional<Organization> organization = organizationRepo.findById(orgId.trim());
        if (!organization.isPresent()) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND,
                    "no org found with id: " + orgId);
        }

        AuthHandler authHandler = AuthHandlerFactory.getInstance().getAuthHandler(organization.get());
        return authHandler;

    }

    private UserExtendedInfo getExtendedInfo(String userId) {
        Optional<User> user = userRepo.findById(userId);
        if (!user.isPresent())
            return null;
        UserExtendedInfo extendedInfo = new UserExtendedInfo(user.get());
        return extendedInfo;
    }

    private void updateOrgMemberExpiredMappings(String orgId, String userId) {


        OrgMember member = orgMemberRepo.findByOrgIdAndUserId(orgId, userId);

        if (member == null) {
            logger.error("cannot find orgMember for orgId: " + orgId + ", userId: " + userId);
        }
        if (null == member) {
            logger.error("orgMember not found");
            return;
        }
        logger.debug("Updating expired mappings for member with userId: " + userId);
        boolean modified = false;
        if (member.getExpiredMappings() == null) {
            member.setExpiredMappings(new ArrayList<OrgMemberMappingInfo>());
            modified = true;
        }
        if (member.mappings != null && !member.mappings.isEmpty()) {
            Iterator<OrgMemberMappingInfo> mappingIterator = member.mappings.iterator();
            while (mappingIterator.hasNext()) {
                OrgMemberMappingInfo mapping = mappingIterator.next();
                if (mapping.endTime > 0 && mapping.endTime < System.currentTimeMillis()) {
                    // This mapping has expired. Add to expiredMappingsList
                    member.expiredMappings.add(mapping);
                    logger.debug("Mapping has expired for Section:" + mapping.sectionId);
                    mappingIterator.remove();
                    modified = true;
                }
            }
        }
        if (modified) {
            orgMemberRepo.save(member);
        }

    }

    private UserAuthRes authenticateOTPMember(UserAuthReq userAuthReq) {
        UserAuthRes userAuthRes = null;
        User user = getUsersIdByContact(userAuthReq.getContactNumber(), userAuthReq.getCountryCode());
        userAuthRes = getAuthResFromUser(user);
        return userAuthRes;
    }

    private User getUsersIdByContact(String contactNumber, String countryCode) {
        List<OrgMember> members = orgMemberRepo.findByContactNumberAndCountryCode(countryCode, contactNumber);

        List<OrgMember> finalMembers = new ArrayList<OrgMember>();
        for (OrgMember mem : members) {
            boolean isVerified = userRepo.findById(mem.getUserId()).get().isPhoneVerified;
            if (isVerified)
                finalMembers.add(mem);
        }

        if (finalMembers.size() == 0 || finalMembers.size() > 1) {
            throw new VedantuException(
                    VedantuErrorCode.DUPLICATE_CONTACTS_EXIST,
                    "Your Contact Number is Not Verified, "
                            + "Please Login Through Your Email/InstituteID and Verify Your Contact Number");
        }
        Optional<User> user = userRepo.findById(finalMembers.get(0).getUserId());
        return user.get();
    }

    private UserAuthRes authenticateOrgMember(MemberAuthReq memberAuthReq) {
        AuthHandler authHandler = getAuthHandler(
                memberAuthReq.orgId);
        OrgMember orgMember = orgMemberRepo.findByOrgIdAndMemberId(memberAuthReq.getOrgId(), memberAuthReq.getMemberId());
        UserAuthReq userAuthReq = authHandler.authenticate(memberAuthReq.getMemberId(), memberAuthReq.password, orgMember);

        UserAuthRes userAuthRes = authenticateUser(userAuthReq);
        return userAuthRes;
    }

    private UserAuthRes authenticateUser(UserAuthReq userAuthReq) {
        User user = null;
        if (!userAuthReq.isDl()) {
            user = authenticateUser(userAuthReq.getUsername(), userAuthReq.getPassword());

        } else {
            Optional<User> userForKeys = userRepo.findByUsername(userAuthReq.getUsername());
            if (!userForKeys.isPresent() || userForKeys.get().getCredentials() == null) {
                throw new VedantuException(VedantuErrorCode.ACCESS_DENIED);
            }

            UserAuthPojo userPojo = new UserAuthPojo();
            JSONObject json;
            try {
                logger.debug("userName: " + userAuthReq.getUsername());
                String userPojoDecrypted = EncryptionUtils.decryptWithPrivateKey(
                        userAuthReq.getPassword(), userForKeys.get().getCredentials().getPrivateKey());
                logger.debug("userPojodecrepted" + userPojoDecrypted);
                json = new JSONObject(userPojoDecrypted);

            } catch (JSONException e) {
                throw new VedantuException(VedantuErrorCode.ACCESS_DENIED);
            }
            userPojo.fromJSON(json);
            logger.debug("user auth pojo " + userPojo);

            user = authenticateUserWithSaltedPassword(userPojo.getUserName(),
                    userPojo.getPassword());
        }
        return getAuthResFromUser(user);

    }

    public User authenticateUser(String username, String password) throws VedantuException {

        logger.debug("authenticateUser username: " + username);
        final boolean isOnlyCheck = true;
        String saltedPassword = organizationsImpl.getUserPassHash(username, password, isOnlyCheck);

        return authenticateUserWithSaltedPassword(username, saltedPassword);
    }

    public User authenticateUserWithSaltedPassword(String username, String saltedPassword)
            throws VedantuException {

        logger.debug("authenticateUserWithSaltedPassword username: " + username);

        Optional<User> user = userRepo.findByUsernameAndPassword(username, saltedPassword);

        if (!user.isPresent()) {
            logger.error("authentication failed for username: " + username);
            throw new VedantuException(VedantuErrorCode.AUTHENTICATION_FAILED, "user not found with given username or password " + username + " " + saltedPassword);
        }
        logger.info("authenticateUserWithSaltedPassword user: " + user);
        return user.get();
    }

    private UserAuthRes getAuthResFromUser(User user) {
        UserAuthRes userAuthRes = new UserAuthRes();

        if (null != user) {
            userAuthRes.setId(user._getStringId());
            userAuthRes.setFirstName(user.getFirstName());
            userAuthRes.setLastName(user.getLastName());
            userAuthRes.setLatestTnCVersion(TNC_VERSION);
            boolean b = false;
            if (user.getTncAcceptance() == null) {
                b = true;
            } else if (user.getTncAcceptance().getVersion() == null || !user.getTncAcceptance().getVersion().equals(TNC_VERSION)) {
                b = true;

            }
            userAuthRes.setNeedsTnCAcceptance(b);

            userAuthRes.setAcceptedTNCVersion(user.getTncAcceptance() != null ? user.getTncAcceptance().getVersion() : null);

            userAuthRes.setThumbnail(user._getThumbnailUrl());
            userAuthRes.setAuthType(user.getAuthType());
            logger.debug("user username: " + user.getUsername() + " authenticated with id: "
                    + userAuthRes.getId());
        }
        return userAuthRes;
    }

    @Override
    public VedantuResponse sendOTP(SendOTPReq request) {
        if (request == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        SendOTPRes response = new SendOTPRes();
        response.hasEmail = checkIfUserEmailExists(request.contactNumber, request.countryCode);
        response.isNewPhone = isNewPhone(request.contactNumber, request.countryCode);
        response.OTP = request.existingOTP.isEmpty() ? generateOTP(OTPSize) : request.existingOTP;
        // This message should match the template of smCountry. You can't change
        // the template here.
        String message = "Hello! Welcome to Learnpedia Family. Your verification code is " + response.OTP
                + " We are delighted to have you as one of our valuable customers. Happy to help.";

        response.smsReference = sendOTP(request.countryCode + request.contactNumber, message, request.orgId);
        response.contactNumber = request.contactNumber;
        if (!request.fullName.isEmpty()) {
            response.fullname = request.fullName;
        }
        if (!request.progType.isEmpty()) {
            response.progType = request.progType;
        }
        response.countryCode = request.countryCode;
        return new VedantuResponse(response);
    }

    // Need Clarification on smsGateway
    public String sendOTP(String mobilenumber, String message, String orgId) throws VedantuException {
        String postData = "";
        String response = "";
        URL url;
        try {
            Organization organization = organizationRepo.findById(orgId).get();
            if (organization == null) {
                throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
            }
            //Make message dynamic by adding respective Organization Name.
            //Also make sure the specific template is available for respective organization for SMSCOUNTRY.
            message = message.replace("Learnpedia", organization.fullName);
            SmsGatewayInfo smsGateway = organization.smsGateway;
            //check for this condition later,This must be !=
            if (smsGateway != null) {
                if (smsGateway.host.equalsIgnoreCase("SMSCOUNTRY")) {
                    postData = smsGateway.postData + "&mobilenumber=" + mobilenumber + "&message=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
                } else {
                    postData = smsGateway.postData + "&mobile=" + mobilenumber + "&message=" + (message);
                }
                logger.error("SMSAPI postData " + postData);
                url = new URL(smsGateway.url);
                logger.error("SMSAPI url " + url);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                con.setDoOutput(true);
                OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
                out.write(postData);
                out.close();
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String decodedString;
                while ((decodedString = in.readLine()) != null) {
                    decodedString = decodedString.split(":")[1];
                    response += decodedString;
                }
                logger.error("SMSAPI " + response);
                in.close();
                return response;
            } else {
                throw new VedantuException(VedantuErrorCode.SMS_GATEWAY_DETAILS_NOT_FOUND);
            }
        } catch (MalformedURLException e) {
            logger.error("MalformedURLException in sendOTP function", e);
        } catch (IOException e) {
            logger.error("IOException in sendOTP function", e);
        }
        return response;
    }

    public boolean checkIfUserEmailExists(String contactNumber, String countryCode) {
        OrgMember member = orgMemberRepo.findByCountryCodeAndContactNumber(countryCode, contactNumber);
        if (member != null) {
            if (member.email != null && !member.email.isEmpty()) {
                return true;
            } else {
                logger.info("User email not found for contact number : " + contactNumber);
                return false;
            }
        }
        logger.info("User doesn't exist for contact number : " + contactNumber);
        return false;
    }

    public boolean isNewPhone(String contactNumber, String countryCode) {
        OrgMember member = orgMemberRepo.findByCountryCodeAndContactNumber(countryCode, contactNumber);
        if (member == null) {
            logger.info(contactNumber + " is new contact number");
            return true;
        }
        logger.info(contactNumber + " is already existing contact number");
        return false;
    }

    @Override
    public VedantuResponse validateOTP(ValidateOTPReq request) {
        if (request == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        ValidateOTPRes response = new ValidateOTPRes();
        if (request.sessionOTP.equals(request.userOTP)) {
            logger.info("OTP validated");
            if (checkIfUserEmailExists(request.countryCode, request.contactNumber) == false
                    && isNewPhone(request.contactNumber, request.countryCode) == true) {
                response.isNewUser = true;
            }
        } else {
            throw new VedantuException(VedantuErrorCode.INVALID_CODE, "OTP is NOT valid");
        }
        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse validateContactNumber(SendOTPReq request) {
        if (request == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        SendOTPRes response = new SendOTPRes();
        response.contactNumber = request.contactNumber;
        response.fullname = request.fullName;
        response.countryCode = request.countryCode;
        response.isNewPhone = isNewPhone(request.contactNumber, request.countryCode);
        validatePhoneNumber(request.userId);
        addOrUpdateContactNumber(request.userId, request.contactNumber, request.countryCode);
        return new VedantuResponse(response);
    }

    public void validatePhoneNumber(String userId)
            throws VedantuException {
        User user = userRepo.findById(userId).get();
        if (null == user) {
            logger.error("cannot validate contact number, as user does not exist for userId: " + userId);
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        user.isPhoneVerified = true;
        userRepo.save(user);
    }

    public void addOrUpdateContactNumber(String userId, String contactNumber, String countryCode) {
        OrgMember member = orgMemberRepo.findByUserId(userId);
        member.contactNumber = contactNumber;
        member.countryCode = countryCode;
        orgMemberRepo.save(member);
    }

    @Override
    public VedantuResponse verifyContactNumber(SendOTPReq request) {
        if (request == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        SendOTPRes response = new SendOTPRes();
        response.contactNumber = request.contactNumber;
        response.fullname = request.fullName;
        response.countryCode = request.countryCode;
        response.isNewPhone = isNewPhone(request.contactNumber, request.countryCode);
        if (response.isNewPhone == true)
            return new VedantuResponse(response);
        else {
            getUsersIdByContact(request.countryCode, request.contactNumber);
        }
        return new VedantuResponse(response);

    }

    public boolean isPhoneVerified(String userId) {
        User user = userRepo.findById(userId).get();
        return user.isPhoneVerified;
    }

    @Override
    public VedantuResponse getAllUserData(GetAllUserDataReq getAllUserDataReq) {
        GetAllUserDataRes response = null;
        try {
            response = getAllUserDataRes(getAllUserDataReq);
        } catch (VedantuException e) {
            throw e;
        }
        return new VedantuResponse(response);
    }

    public GetAllUserDataRes getAllUserDataRes(GetAllUserDataReq request) throws VedantuException {
        if (StringUtils.isEmpty(request.orgId)) {
            throw new VedantuException(VedantuErrorCode.ACCESS_DENIED);
        }
        List<OrgMember> members = getUsersByOrgId(request.orgId, OrgMemberProfile.STUDENT, request.targetUserId, request.lastUpdated);
        if (CollectionUtils.isEmpty(members)) {
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        Map<String, OrgMember> orgMemberMap = new LinkedHashMap<String, OrgMember>();
        Map<String, User> userMap = new HashMap<String, User>();
        Map<String, String> userSalts = new LinkedHashMap<String, String>();
        for (OrgMember member : members) {
            orgMemberMap.put(member.userId, member);
        }
        List<String> userIds = new ArrayList<String>(orgMemberMap.keySet());
        String[] fields = {};
        List<User> users = userRepo.findByIdIn(userIds);
        List<String> usernames = new ArrayList<String>();
        for (User user : users) {
            usernames.add(user.username);
            userMap.put(user._getStringId(), user);
        }
        List<UserSalt> usersSalts = userSaltrepo.findByUsernameIn(usernames);
        for (UserSalt userSalt : usersSalts) {
            userSalts.put(userSalt.username, userSalt.salt);
        }
        List<UserOrgAuth> userOrgAuthList = new ArrayList<UserOrgAuth>();
        for (String userId : userIds) {
            User user = userMap.get(userId);
            UserOrgAuth userOrgAuth = new UserOrgAuth();
            userOrgAuth.username = user.username;
            userOrgAuth.password = user.password;
            userOrgAuth.acceptedTNCVersion = user.tncAcceptance != null ? user.tncAcceptance.version
                    : null;
            userOrgAuth.thumbnail = user._getThumbnailUrl();
            userOrgAuth.authType = user.authType;
            userOrgAuth.id = user._getStringId();
            userOrgAuth.firstName = user.firstName;
            userOrgAuth.lastName = user.lastName;
            userOrgAuth.latestTnCVersion = TNC_VERSION;
            userOrgAuth.needsTnCAcceptance = null == user.tncAcceptance;
            userOrgAuth.memberId = orgMemberMap.get(user._getStringId()).memberId;
            userOrgAuth.salt = userSalts.get(user.username);
            OrgMemberExtendedInfo orgMemberExtendedInfo = (OrgMemberExtendedInfo) orgMemberMap.get(user._getStringId()).toExtendedInfo();
            //populateUserPublicProfileDetails(orgMemberMap.get(user._getStringId()), orgMemberExtendedInfo);

            // populateProgramHierarchy(orgMemberMap.get(user._getStringId()), orgMemberExtendedInfo, true);
            userOrgAuth.orgProfile.info = orgMemberExtendedInfo;
            // userOrgAuth.orgProfile.key = UserManager.getPrivateKey(user._getStringId());
            userOrgAuthList.add(userOrgAuth);
        }
        GetAllUserDataRes res = new GetAllUserDataRes();
        res.users = userOrgAuthList;
        return res;
    }

    public List<OrgMember> getUsersByOrgId(String orgId, OrgMemberProfile profile, String targetUserId, long lastUpdated) {
        List<OrgMember> orgMembersList = new ArrayList<OrgMember>();
        if (!StringUtils.isEmpty(targetUserId)) {
            orgMembersList = orgMemberRepo.findByOrgIdAndProfileAndUserId(orgId, profile.name(), targetUserId);
        } else {
            if (lastUpdated != Long.MIN_VALUE) {
                orgMembersList = orgMemberRepo.findByOrgIdAndProfileAndLastUpdated(orgId, lastUpdated);
            } else {
                orgMembersList = orgMemberRepo.findByOrgIdAndProfile(orgId, profile.name());
            }
        }
        return orgMembersList;
    }

    @Override
    public VedantuResponse unsetEmail(UnsetEmailReq unsetEmailReq) {
        if (ObjectIdUtils.hasInvalidId(unsetEmailReq.orgId, unsetEmailReq.targetUserId,
                unsetEmailReq.targetOrgMemberId)) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID);

        }

        UnsetEmailRes unsetEmailRes = null;
        try {
            unsetEmailRes = unsetEmailRes(unsetEmailReq);
        } catch (VedantuException e) {
            throw e;
        }
        return new VedantuResponse(unsetEmailRes);
    }

    public UnsetEmailRes unsetEmailRes(UnsetEmailReq unsetEmailReq) throws VedantuException {

        OrgMember orgMember = orgMemberRepo.findByOrgIdAndUserId(unsetEmailReq.orgId, unsetEmailReq.targetUserId);
        if (null == orgMember) {
            logger.error("orgMember not found for orgId: " + unsetEmailReq.orgId
                    + ", targetUserId: " + unsetEmailReq.targetUserId);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }
        if (!unsetEmailReq.targetOrgMemberId.equals(orgMember._getStringId())) {
            logger.error("orgMember._id: " + orgMember._getStringId()
                    + " does not match for orgId: " + unsetEmailReq.orgId + ", targetUserId: "
                    + unsetEmailReq.targetUserId + ", targetOrgMemberId: "
                    + unsetEmailReq.targetOrgMemberId);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }

        orgMember.email = "";
        orgMember.lastUpdated = System.currentTimeMillis();
        orgMemberRepo.save(orgMember);
        // OrgMemberDAO.INSTANCE.updateModel(orgMember, Arrays.asList(ConstantsGlobal.EMAIL));
        UnsetEmailRes unsetEmailRes = new UnsetEmailRes();
        unsetEmailRes.done = true;

        return unsetEmailRes;
    }

    @Override
    public VedantuResponse activateUser(UserActivationUpdateReq userActivationReq) {
        UserActivationRes userActivationRes = null;
        try {
            userActivationRes = recordChange(userActivationReq);
        } catch (VedantuException e) {
            throw e;
        }

        return new VedantuResponse(userActivationRes);
    }

    public UserActivationRes recordChange(UserActivationUpdateReq userActivationRequest)
            throws VedantuException {

        UserActivationRes userActivationRes = new UserActivationRes();

        try {
            long currentTime = new Date().getTime();
            OrgMember orgMember = orgMemberRepo.findByOrgIdAndUserId(userActivationRequest.orgId, userActivationRequest.targetUserId);
            ArrayList<String> changeList = new ArrayList<String>();
            Interval interval = new Interval();
            if (null == orgMember) {
                throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
            }

            if (orgMember.interval == null) {
                interval.setFrom(currentTime);
                interval.setTill(Long.MAX_VALUE);
                orgMember.interval = interval;
            }

            interval.setFrom(orgMember.interval.getFrom());
            interval.setTill(orgMember.interval.getTill());

            long userActiveFrom = interval.getFrom();// activeFrom;
            long userActiveTill = interval.getTill();
            if (userActiveTill == -1) {
                userActiveTill = Long.MAX_VALUE;
            }

            if (userActivationRequest.activateFrom == -2) {
                userActivationRequest.activateFrom = currentTime;
            }

            if (userActivationRequest.activateTill == -2) {
                userActivationRequest.activateTill = currentTime;
            }

            if ((int) (userActivationRequest.activateFrom / (24 * 60 * 60 * 1000)) < (int) (currentTime / (24 * 60 * 60 * 1000))
                    && userActivationRequest.activateFrom > 0) {
                throw new VedantuException(VedantuErrorCode.FROM_DATE_TIME_LESS_THAN_CURRENT,
                        "From Date time belongs to past");
            }

            if (userActivationRequest.activateTill < (currentTime - 60000)
                    && userActivationRequest.activateTill > 0) {
                throw new VedantuException(VedantuErrorCode.TILL_DATE_TIME_LESS_THAN_CURRENT,
                        "To Date time belongs to past");
            }

            if (userActivationRequest.activateFrom > 0 && userActivationRequest.activateTill < 0) {
                if (userActiveFrom < currentTime && userActiveTill > currentTime) {
                    throw new VedantuException(VedantuErrorCode.USER_ALREADY_ACTIVE);
                }

                if (userActiveFrom < currentTime && userActiveTill < currentTime) {
                    userActivationRes.done = recordChange(
                            userActivationRequest.orgId, userActivationRequest.targetUserId,

                            userActivationRequest.userId, interval);
                    interval.setFrom(userActivationRequest.activateFrom);
                    interval.setTill(-1);
                }

                if (userActiveFrom > currentTime && userActiveTill > currentTime) {
                    interval.setFrom(userActivationRequest.activateFrom);
                }
            }

            if (userActivationRequest.activateFrom < 0 && userActivationRequest.activateTill > 0) {
                if (userActiveFrom < currentTime && userActiveTill > currentTime) {
                    interval.setTill(userActivationRequest.activateTill);
                }

                if (userActiveFrom < currentTime && userActiveTill < currentTime) {
                    throw new VedantuException(
                            VedantuErrorCode.USER_ALREADY_DEACTIVATED_WITH_NO_FUTURE_ACTIVATION);
                }

                if (userActiveFrom > currentTime && userActiveTill > currentTime) {
                    interval.setFrom(userActivationRequest.activateFrom);
                    interval.setTill(userActivationRequest.activateFrom);
                }
            }

            if (userActivationRequest.activateFrom > 0 && userActivationRequest.activateTill > 0) {
                if (userActivationRequest.activateFrom > userActivationRequest.activateTill) {
                    throw new VedantuException(VedantuErrorCode.FROM_DATE_GREATER_THAN_TILL_DATE);
                }

                if (userActiveFrom < currentTime && userActiveTill > currentTime) {
                    interval.setTill(userActivationRequest.activateTill);
                }

                if (userActiveFrom < currentTime && userActiveTill < currentTime) {
                    logger.trace("Value inserted in UserStateLog");
                    userActivationRes.done = recordChange(
                            userActivationRequest.orgId, userActivationRequest.targetUserId,

                            userActivationRequest.userId, interval);

                    interval.setFrom(userActivationRequest.activateFrom);
                    interval.setTill(userActivationRequest.activateTill);
                }

                if (userActiveFrom > currentTime && userActiveTill > currentTime) {
                    interval.setFrom(userActivationRequest.activateFrom);
                    interval.setTill(userActivationRequest.activateTill);
                }
            }
            orgMember.interval = interval;
            // changeList.add("interval");
            // OrgMemberDAO.INSTANCE.updateModel(orgMember, changeList);
            orgMemberRepo.save(orgMember);
            userActivationRes.done = true;
        } catch (Exception exception) {
            logger.debug("Could not record state changes", exception);
            userActivationRes.done = false;
        }

        return userActivationRes;
    }

    public boolean recordChange(String orgId, String userId,
                                String setByUserId,
                                Interval interval) {

        UserStateLog userStateLog = new UserStateLog();
        userStateLog.orgId = orgId;
        userStateLog.userId = userId;
        userStateLog.setByUserId = setByUserId;
        userStateLog.interval = interval;

        UserStateLog result = userStateLogRepo.save(userStateLog);
        return result != null;
    }

    @Override
    public VedantuResponse activationPeriod(MemberActivationPeriodsReq memberActivationPeriodsReq) {
        MemberActivationPeriodsRes memberActivationPeriodsRes = null;
        try {
            memberActivationPeriodsRes = getMemberActivationPeriods(memberActivationPeriodsReq);
        } catch (VedantuException e) {
            throw e;
        }

        return new VedantuResponse(memberActivationPeriodsRes);
    }

    public MemberActivationPeriodsRes getMemberActivationPeriods(
            MemberActivationPeriodsReq memberActivationPeriodsReq) throws VedantuException {

        MemberActivationPeriodsRes memberActivationPeriodsRes = new MemberActivationPeriodsRes();
        try {
            OrgMember orgMember = orgMemberRepo.findByOrgIdAndUserId(memberActivationPeriodsReq.orgId, memberActivationPeriodsReq.userId);
            if (null == orgMember) {
                throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
            }
            Interval interval = new Interval();
            interval.setFrom(memberActivationPeriodsReq.from);
            interval.setTill(memberActivationPeriodsReq.till);
            memberActivationPeriodsRes.intervals = getMemberActivationPeriods(
                    memberActivationPeriodsReq.orgId, memberActivationPeriodsReq.userId, interval);
        } catch (Exception exception) {
            logger.debug("Could not record state changes", exception);
        }
        return memberActivationPeriodsRes;
    }

    public List<Interval> getMemberActivationPeriods(String orgId, String userId,
                                                     Interval interval) { // ((y1<x2)&&(x1<y2)), where (y1,y2) is period
        // to be passed

        List<Interval> intervals = new ArrayList<Interval>();
        //need to check intervals = UserStateLogDAO.INSTANCE.getMemberActivationPeriods(orgId, userId, interval);
        Interval currentInterval = getMemberCurrentActivationPeriod(orgId,
                userId);
        if (interval.getFrom() < currentInterval.getTill()
                && currentInterval.getFrom() < interval.getTill()) {
            intervals.add(currentInterval);
        }
        return intervals;
    }

    public Interval getMemberCurrentActivationPeriod(String orgId, String userId) {

        logger.debug("getMemberActivationPeriods orgId: " + orgId + ", userId: " + userId);

        OrgMember orgMember = orgMemberRepo.findByOrgIdAndUserId(orgId, userId);
        Interval interval = orgMember.interval;
        return interval;
    }

    @Override
    public VedantuResponse getSection(GetOrgSectionReq getOrgSectionReq) {

        GetOrgSectionRes response = null;

        try {

            response = organizationsImpl.getProgramSection(getOrgSectionReq);
        } catch (VedantuException e) {
            throw e;
        }

        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse getPaymentInfo(GetPaymentInfoReq getPaymentInfoReq) {
        GetPaymentInfoRes response = null;

        try {

            response = getProgramPaymentInfo(getPaymentInfoReq);
        } catch (VedantuException e) {
            throw e;
        }

        return new VedantuResponse(response);
    }

    public GetPaymentInfoRes getProgramPaymentInfo(GetPaymentInfoReq request)
            throws VedantuException {
        OrgMember member = orgMemberRepo.findByOrgIdAndUserId(request.orgId, request.userId);
        if (member == null) {
            throw new VedantuException(
                    VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }
        long orderId = 0;
        boolean found = false;
        for (OrgMemberMappingInfo info : member.mappings) {
            if (info.endTime <= 0 || info.endTime > System.currentTimeMillis()) {
                if (request.sectionId.equals(info.sectionId)) {
                    if (info.orderId.equals(null) || info.orderId.isEmpty()) {
                        orderId = 0;
                    } else {
                        long orderIdLong = Long.parseLong(info.orderId);
                        orderId = orderIdLong;
                    }
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            throw new VedantuException(
                    VedantuErrorCode.ORGANIZATION_MEMBER_MAPPING_NOT_FOUND);
        }
        InvoiceInfo invoiceInfo = new InvoiceInfo();
        GetPaymentInfoRes response = new GetPaymentInfoRes();
        if (orderId != 0) {
            Order order = getOrderById(orderId);
            logger.debug("siddhardha order: " + order);
            invoiceInfo = order.invoiceInfo;
            response.info = invoiceInfo;
        } else {
            invoiceInfo.total = 0;
            response.info = invoiceInfo;
        }
        return response;
    }

    public Order getOrderById(long orderId) throws VedantuException {

        Order order = orderRepo.findByOrderId(orderId);

        if (order == null) {
            throw new VedantuException(VedantuErrorCode.INVALID_ORDER,
                    "no order found with orderId: " + orderId);
        }
        return order;
    }

    @Override
    public VedantuResponse updateEndDateMapping(UpdateEndTimeMappingReq updateEndTimeMappingReq) {
        UpdateEndTimeMappingRes response = null;

        try {
            response = updateEndDateMappingRes(updateEndTimeMappingReq);
        } catch (VedantuException e) {
            throw e;
        }

        return new VedantuResponse(response);

    }

    public UpdateEndTimeMappingRes updateEndDateMappingRes(
            UpdateEndTimeMappingReq updateEndDateMappingReq) throws VedantuException {

        OrgMember orgMember = updateEndDateMapping(
                updateEndDateMappingReq.orgId, updateEndDateMappingReq.targetUserId,
                updateEndDateMappingReq.sectionId, updateEndDateMappingReq.endTime);

        UpdateEndTimeMappingRes updateEndDateMappingRes = new UpdateEndTimeMappingRes();
        updateEndDateMappingRes.done = true;
        return updateEndDateMappingRes;
    }

    @Override
    public VedantuResponse getSaleDetails(GetSaleDetailsReq getSaleDetailsReq) {

        GetSaleDetailsRes response = null;

        response = getSaleDetailsRes(getSaleDetailsReq);
        return new VedantuResponse(response);
    }

    public GetSaleDetailsRes getSaleDetailsRes(GetSaleDetailsReq request) throws VedantuException {
        GetSaleDetailsRes response = new GetSaleDetailsRes();
        OrgMemberMappingInfo mappingInfo = getMemMappingForSection(request.orgId, request.targetUserId,
                request.sectionId);
        String saleDetailsId = mappingInfo.saleDetailsId;
        if (saleDetailsId == null || saleDetailsId.isEmpty()) {
            response.saleDetailsInfo = null;
        } else {
            Optional<SaleDetails> saleDetailsOptional = salesDetailsRepo.findById(saleDetailsId);
            if (saleDetailsOptional.isPresent()) {

                response.saleDetailsInfo = new SaleDetailsInfo(saleDetailsOptional.get());
            }
        }
        return response;
    }

    public OrgMemberMappingInfo getMemMappingForSection(String orgId, String userId, String sectionId)
            throws VedantuException {
        OrgMember orgMember = orgMemberRepo.findByOrgIdAndUserId(orgId, userId);

        if (null == orgMember) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }
        for (OrgMemberMappingInfo mInfo : orgMember.mappings) {
            if (mInfo.sectionId.equals(sectionId)) {
                return mInfo;
            }
        }
        return null;
    }

    @Override
    public VedantuResponse updateSaleDetails(UpdateSaleDetailsReq updateSaleDetailsReq) {
        UpdateSaleDetailsRes response = null;

        response = updateSaleDetailsRes(updateSaleDetailsReq);
        return new VedantuResponse(response);
    }

    public UpdateSaleDetailsRes updateSaleDetailsRes(UpdateSaleDetailsReq request) throws VedantuException {
        UpdateSaleDetailsRes response = new UpdateSaleDetailsRes();
        SaleDetails saleDetails = updateSaleDetails(request.saleDetailsId, request.paymentItems,
                request.targetOrgMemberId);
        response.done = true;
        response.saleDetailsInfo = new SaleDetailsInfo(saleDetails);
        return response;
    }

    public SaleDetails updateSaleDetails(String saleDetailsId, List<PaymentItem> paymentItems, String targetOrgMemberId)
            throws VedantuException {
        Optional<SaleDetails> saleDetailsOptional = salesDetailsRepo.findById(saleDetailsId);
        if (!saleDetailsOptional.isPresent()) {
            logger.error("Sale details not found corresponding to id: " + saleDetailsId);
            throw new VedantuException(VedantuErrorCode.SALE_DETAILS_NOT_FOUND);
        }
        SaleDetails saleDetails = saleDetailsOptional.get();
        if (!saleDetails.orgMemberId.equals(targetOrgMemberId)) {
            logger.error("Sale details correspond to orgMemberId: " + saleDetails.orgMemberId
                    + " But expected orgMemberId is:" + targetOrgMemberId);
            throw new VedantuException(VedantuErrorCode.SALE_DETAILS_MISMATCH);
        }

        saleDetails.paymentItems = paymentItems;
        salesDetailsRepo.save(saleDetails);
        return saleDetails;
    }

    public UpdateUsernameRes updateUsername(UpdateUsernameReq updateUsernameReq) throws VedantuException {
        if (updateUsernameReq.getNewUsername() == null) {
            throw new VedantuException(VedantuErrorCode.SHOULD_NOT_BE_NULL, "NewUserName required");
        }
        if (updateUsernameReq.getTargetUserId() == null) {
            throw new VedantuException(VedantuErrorCode.SHOULD_NOT_BE_NULL, "UserId required");
        }
        Optional<User> user = userRepo.findById(updateUsernameReq.getTargetUserId());
        Optional<User> user1 = userRepo.findByUsername(updateUsernameReq.getNewUsername());
        UserDto userDto = null;
        if (user.isPresent()) {
            userDto = convertToUserDto(user.get());
            if (user.get().getUsername() == updateUsernameReq.getNewUsername()) {
                throw new VedantuException(VedantuErrorCode.USER_ALREADY_ACTIVE, "User Alreday Exist");

            } else {
                if (user1.isPresent()) {
                    throw new VedantuException(VedantuErrorCode.USER_ALREADY_EXISTS, "this new username already exist");
                } else {
                    user.get().setUsername(updateUsernameReq.getNewUsername());
                    final boolean isOnlyCheck = false;
                    String pwd = getUserPassHash(updateUsernameReq.getNewUsername(), updateUsernameReq.getNewPassword(),
                            isOnlyCheck);
                    user.get().setPassword(pwd);
                    userRepo.save(user.get());
                    userDto.setUsername(updateUsernameReq.getNewUsername());
                }
            }

        } else {
            throw new VedantuException(VedantuErrorCode.INVALID_CODE, "Given target id is not valid");
        }

        UpdateUsernameRes updateUsernameRes = new UpdateUsernameRes();
        updateUsernameRes.setDone(true);
        return updateUsernameRes;
    }

    public UserDto convertToUserDto(User user) {

        UserDto userDto = new UserDto();
        userDto.setDob(user.getDob());
        userDto.setUserId(user.getId().toString());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setUsername(user.getUsername());
        userDto.setPassword(user.getPassword());
        userDto.setSysGenPassword(user.isSysGenPassword);
        userDto.setLastName(user.getLastName());
        userDto.setEmailVerified(user.isEmailVerified);
        userDto.setOTPuser(user.isOTPuser);
        userDto.setThumbnail(user.getThumbnail());
        userDto.setPhoneVerified(user.isPhoneVerified);
        userDto.setEmailChangeReq(user.getEmailChangeReq());
        return userDto;
    }

    private String getUserPassHash(String username, String password, boolean isOnlyCheck) {

        logger.error("getUserPassHash username: " + username);
        String hashedPass = OrganizationsImpl.getHashed(getSaltedPassword(username, password, isOnlyCheck), "SHA-256");
        logger.error("Hashed password for username: " + username + " : " + hashedPass);
        return hashedPass;
    }

    private String getSaltedPassword(String username, String password, boolean isOnlyCheck) {

        UserSalt userSalt = userSaltrepo.findByUsername(username);
        if (null == userSalt) {
            logger.debug("user-salt not found for username: " + username);

            if (isOnlyCheck) {
                logger.debug("will not create new user-salt for username: " + username);
                return HardCodedConstants.emptyString;
            }

            userSalt = new UserSalt(username, UUID.randomUUID().toString());
            userSaltrepo.save(userSalt);
        }

        String saltedPassword = userSalt.salt + SYSTEM_SALT + password;
        return saltedPassword;
    }

    public UpdateUserPasswordRes updateUserPassword(String targetUserId, String newPassword) throws VedantuException {
        logger.debug("updateUserPassword userId: " + targetUserId);
        Optional<User> user = userRepo.findById(targetUserId);
        logger.debug("found user: " + user);

        if (!user.isPresent()) {
            logger.error("cannot update password as user does not exist for userId: " + targetUserId);
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND,
                    "cannot update password as user does not exist for userId");
        }

        if (user.get().getAuthType() == AuthType.EXT_AUTH_ORG) {
            throw new VedantuException(VedantuErrorCode.EXTERNAL_AUTH_SUPPORTED);
        }

        final boolean isOnlyCheck = false;

        String s = getUserPassHash(user.get().getUsername(), newPassword, isOnlyCheck);
        user.get().setPassword(s);
        user.get().setSysGenPassword(false);

        userRepo.save(user.get());
        logger.info("updateUserPassword password updated user: " + user);
        UpdateUserPasswordRes updateUserPasswordRes = new UpdateUserPasswordRes();
        updateUserPasswordRes.setDone(true);
        return updateUserPasswordRes;
    }

    @Override
    public VedantuResponse uploadProfilePic(MultipartFile file, UploadProfilePicReq uploadProfilePicReq) {
        VedantuResponse result = null;
        if (ObjectIdUtils.hasInvalidId(uploadProfilePicReq.orgId, uploadProfilePicReq.targetUserId,
                uploadProfilePicReq.targetOrgMemberId)) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID);

        }

        try {
            uploadProfilePicReq.inputFile = userProfilePicEntityFileStorage.convertMultiPartToFile(file);
            UploadOrgPicRes uploadProfilePicRes = uploadProfilePic(uploadProfilePicReq);
            result = new VedantuResponse(uploadProfilePicRes);
        } catch (VedantuException e) {
            throw e;

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        deleteFile(uploadProfilePicReq.fileName, uploadProfilePicReq.inputFile);
        return result;
    }

    public UploadOrgPicRes uploadProfilePic(UploadProfilePicReq uploadProfilePicReq) throws VedantuException {

        ImageFilter filter = new ImageFilter();
        boolean isImg = filter.accept(new File(uploadProfilePicReq.fileName));
        if (!isImg) {
            throw new VedantuException(VedantuErrorCode.UNSUPPORTED_FILE_TYPE, "not an image file");
        }

        OrgMember orgMember = orgMemberRepo.findByOrgIdAndUserId(uploadProfilePicReq.orgId,
                uploadProfilePicReq.targetUserId);
        if (null == orgMember) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }

        UploadOrgPicRes uploadProfilePicRes = storeUserProfilePic(orgMember, uploadProfilePicReq.inputFile,
                uploadProfilePicReq.fileName);
        return uploadProfilePicRes;
    }

    protected void deleteFile(String fileName, File file) {

        FileUtils.deleteFile(fileName, file);
    }

    private UploadOrgPicRes storeUserProfilePic(OrgMember orgMember, File inputFile, String fileName)
            throws VedantuException {

        final String imageName = orgMember.userId + "_" + orgMember._getStringId();

        try {
            userProfilePicEntityFileStorage.AbstractEntityFileStorageEntity(EntityType.USER);
            StorageResult picStorageResult = userProfilePicEntityFileStorage.storeImage(imageName, inputFile,
                    FileCategory.ORIGINAL, ImageSize.ORIGINAL, null);
            logger.debug(picStorageResult.toString());


            for (ImageSize imageSize : new ImageSize[]{ImageSize.MEDIUM,
                    ImageSize.SMALL, ImageSize.EXTRA_SMALL}) {
                File convertedFile = userProfilePicEntityFileStorage.createImage(inputFile, imageSize, fileName);
                picStorageResult = userProfilePicEntityFileStorage.storeImage(imageName, convertedFile, FileCategory.CONVERTED,
                        imageSize, null);
                logger.debug(picStorageResult.toString());


                FileUtils.deleteFile(convertedFile.getName(), convertedFile);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new VedantuException(VedantuErrorCode.FILE_UPLOAD_ERROR);
        }

        orgMember.thumbnail = imageName;
        orgMemberRepo.save(orgMember);

        //String thumbnailUrl = orgMember._getThumbnailUrl();
        String thumbnailUrl = userProfilePicEntityFileStorage.getSecuredURL(imageName, EntityType.USER,
                FileUtils.JPG_EXTENTION_WITHOUT_DOT, com.lms.common.vedantu.entity.storage.MediaType.IMAGE,
                FileCategory.CONVERTED, ImageSize.SMALL).getSecuredURL();
        UploadOrgPicRes uploadProfilePicRes = new UploadOrgPicRes(true, thumbnailUrl);
        return uploadProfilePicRes;
    }

    @Override
    public VedantuResponse bulkUploadProfilePics(MultipartFile file,
                                                 BulkUploadProfilePicsReq bulkUploadProfilePicsReq) {
        VedantuResponse result = null;
        try {
            bulkUploadProfilePicsReq.inputFile = userProfilePicEntityFileStorage.convertMultiPartToFile(file);
            BulkUploadOrgMembersProfilePicRes bulkUploadProfilePicRes = bulkUploadMembersProfilePic(
                    bulkUploadProfilePicsReq);
            result = new VedantuResponse(bulkUploadProfilePicRes);
        } catch (VedantuException e) {
            result = new VedantuResponse(e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        deleteFile(bulkUploadProfilePicsReq.fileName, bulkUploadProfilePicsReq.inputFile);
        return result;
    }

    public BulkUploadOrgMembersProfilePicRes bulkUploadMembersProfilePic(BulkUploadProfilePicsReq req)
            throws VedantuException {

        BulkUploadOrgMembersProfilePicRes uploadMembersProfilePicRes = new BulkUploadOrgMembersProfilePicRes();
        InputStream is = null;
        ZipInputStream zipStream = null;
        byte[] buffer = new byte[1024];
        // LocalFileSystemHandler tempFs = FileSystemFactory.INSTANCE.getTempFS();
        localFileSystemHandler.localFileSystemHandlerTempDirectory(false);

        String outDir = localFileSystemHandler.getDirectory();
        try {
            localFileSystemHandler.createParent(req.orgId);
            outDir = outDir + File.separator + req.orgId;
        } catch (FileStoreException e) {
            logger.error(e.getMessage(), e);
        }

        try {
            is = new FileInputStream(req.inputFile);
            zipStream = new ZipInputStream(is);
            ZipEntry entry = null;
            while ((entry = zipStream.getNextEntry()) != null) {
                String s = String.format("Entry: %s len %d added %TD", entry.getName(), entry.getSize(),
                        new Date(entry.getTime()));
                String memberId = "SCOREJEE"; //StringUtils.substringBefore(entry.getName(), ".").trim();
                logger.debug(s + ", memberId: " + memberId);

                String outPath = outDir + File.separator + entry.getName();
                File imageOutputFile = new File(outPath);
                FileOutputStream output = null;
                try {
                    output = new FileOutputStream(imageOutputFile);
                    int len = 0;
                    while ((len = zipStream.read(buffer)) > 0) {
                        output.write(buffer, 0, len);
                    }
                    OrgMember orgMember = orgMemberRepo.findByOrgIdAndMemberId(req.orgId, memberId);
                    UploadOrgPicRes uploadPicResult = storeUserProfilePic(orgMember, imageOutputFile, entry.getName());
                    uploadMembersProfilePicRes.status.put(memberId, uploadPicResult);
                    try {
                        logger.debug("sleeping for " + 200 + "ms");
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        logger.error(e.getMessage(), e);
                    }
                } finally {
                    IOUtils.closeQuietly(output);
                    FileUtils.deleteFile(entry.getName(), imageOutputFile);
                }
            }

        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(zipStream);
            IOUtils.closeQuietly(is);
        }
        return uploadMembersProfilePicRes;
    }

    @Override
    public VedantuResponse sendForgotPasswordMail(SendForgotPasswordEmailReq sendForgotPasswordEmailReq) {
        if (ObjectIdUtils.hasInvalidId(sendForgotPasswordEmailReq.orgId)) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID);

        }
        SendForgotPasswordEmailRes sendForgotPasswordEmailRes = null;
        try {
            sendForgotPasswordEmailRes = sendForgotPasswordEmail(sendForgotPasswordEmailReq);
        } catch (VedantuException e) {
            throw e;
        }
        return new VedantuResponse(sendForgotPasswordEmailRes);
    }

    @Override
    public VedantuResponse bulkUpdateStudentsInSection(BulkUpdateStudentInSectionReq bulkUpdateStudentInSectionReq) {
        BulkUpdateStudentsInSectionRes res = null;

        try {
            res = updateStudentsInSectionRes(bulkUpdateStudentInSectionReq);

        } catch (VedantuException e) {
            throw e;
        }

        return new VedantuResponse(res);
    }

    public BulkUpdateStudentsInSectionRes updateStudentsInSectionRes(BulkUpdateStudentInSectionReq req)
            throws VedantuException {
        String errorMsg = "";
        /*
         * String errorMsg = req.validate();
         *
         * if (errorMsg != null) { throw new
         * VedantuException(VedantuErrorCode.MISSING_PARAMETERS, errorMsg); }
         */

        Optional<Organization> orgOptional = organizationRepo.findById(req.orgId);
        Organization org = orgOptional.get();

        if (org.authType == AuthType.EXT_AUTH_ORG) {
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED,
                    "this operation is not allowed for organization using External authentication");
        }

        OrgMember callingMeber = orgMemberRepo.findByOrgIdAndUserId(req.orgId, req.callingUserId);
        if (callingMeber == null || callingMeber.profile != OrgMemberProfile.MANAGER) {
            errorMsg = "updateStudentsInSection is only allowed to MANAGERS ";
            logger.error(errorMsg);
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED, errorMsg);
        }

        BulkUpdateStudentsInSectionRes res = new BulkUpdateStudentsInSectionRes();

        OrgSection fromOrgSection = orgSectionRepo.findByIdAndOrgId(req.fromSectionId, req.fromSectionId);

        OrgSection toOrgSection = StringUtils.isEmpty(req.toSectionId) ? null
                : orgSectionRepo.findByIdAndOrgId(req.orgId, req.toSectionId);

        if ((req.operationType == OrgMappingBulkOperationType.COPY
                || req.operationType == OrgMappingBulkOperationType.MOVE)
                && toOrgSection.revenueModel == RevenueModel.PAID) {
            errorMsg = "bulk addition operation can not be done to PAID program/section";
            logger.error(errorMsg);
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED, errorMsg);
        }

        if (req.operationType == OrgMappingBulkOperationType.MOVE
                && (fromOrgSection == null || fromOrgSection.revenueModel == RevenueModel.PAID)) {
            errorMsg = "move operation can not be done from a PAID program/section";
            logger.error(errorMsg);
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED, errorMsg);
        }

        // fetch all the student of a sections
        AtomicLong hits = new AtomicLong();
        int start = 0;
        while (true) {
            List<OrgMember> members = getOrgMembers(req.orgId, OrgMemberProfile.STUDENT, null, fromOrgSection.programId,
                    fromOrgSection.centerId, fromOrgSection._getStringId(), null, null, start,
                    DEFAULT_SIZE_STUDENT_BULK_OPERATION, null, req.targetUserIds, null, hits);
            bulkUpdateOrgStudentsMapping(members, fromOrgSection, toOrgSection, req.operationType);
            start += DEFAULT_SIZE_STUDENT_BULK_OPERATION;
            if (hits.intValue() <= start) {
                break;
            }
        }
        res.updatedCount = hits.intValue();
        return null;
    }

    private void bulkUpdateOrgStudentsMapping(List<OrgMember> members, OrgSection fromSection, OrgSection toSection,
                                              OrgMappingBulkOperationType operationType) {

        OrgMemberMappingInfo fromOrgMemberMappingInfo = new OrgMemberMappingInfo(fromSection.programId,
                fromSection.centerId, fromSection._getStringId(), null);

        OrgMemberMappingInfo toOrgMemberMappingInfo = toSection == null ? null
                : new OrgMemberMappingInfo(toSection.programId, toSection.centerId, toSection._getStringId(),
                new HashSet<String>());
        logger.debug("===== updating section from: " + fromOrgMemberMappingInfo + ", to : " + toOrgMemberMappingInfo);

        switch (operationType) {
            case COPY:
                addBulkOrgStudentsMapping(members, toOrgMemberMappingInfo);
                break;
            case MOVE:
                removeBulkOrgStudentsMapping(members, fromOrgMemberMappingInfo);
                addBulkOrgStudentsMapping(members, toOrgMemberMappingInfo);
                break;
            case REMOVE:
                removeBulkOrgStudentsMapping(members, fromOrgMemberMappingInfo);
                break;
            default:
                break;
        }

    }





	private void addBulkOrgStudentsMapping(List<OrgMember> members, OrgMemberMappingInfo mappingInfo) {
		List<OrgMember> orgMembers = new ArrayList<>();
		for (OrgMember member : members) {

			if (!member.add(mappingInfo)) {
				continue;
			}
			orgMembers.add(member);
			logger.debug("==== new orgMember mapping: " + member.mappings);
		}
		orgMemberRepo.saveAll(orgMembers);
	}

	private void removeBulkOrgStudentsMapping(List<OrgMember> members, OrgMemberMappingInfo mappingInfo) {
		List<OrgMember> orgMembers = new ArrayList<>();
		for (OrgMember member : members) {
			member.remove(mappingInfo);
			orgMembers.add(member);
			logger.debug("==== new orgMember mapping: " + member.mappings);
		}
		orgMemberRepo.saveAll(orgMembers);
	}

	

    @Override
    public VedantuResponse uploadStudents(MultipartFile file, UploadOrgStudentsReq uploadOrgStudentsReq) {
        VedantuResponse result = null;
        if (ObjectIdUtils.hasInvalidId(uploadOrgStudentsReq.orgId,
                uploadOrgStudentsReq.orgMemberId, uploadOrgStudentsReq.programId)) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID);

        } else {
            try {
                uploadOrgStudentsReq.inputFile = userProfilePicEntityFileStorage.convertMultiPartToFile(file);
                UploadOrgStudentsRes uploadOrgStudentsRes = uploadOrgStudents(uploadOrgStudentsReq);
                result = new VedantuResponse(uploadOrgStudentsRes);
            } catch (VedantuException e) {
                throw e;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return result;
    }

    protected AddOrgMemberRes addOrgMember(AddOrgMemberReq addOrgMemberReq,
                                           boolean isMemberIdSysGenerated) throws VedantuException {

        if (!VedantuStringUtils.isValidDOB(addOrgMemberReq.dob)) {
            throw new VedantuException(VedantuErrorCode.INVALID_DATE_FORMAT);
        }

        if (!VedantuStringUtils.isValidContactNumber(addOrgMemberReq.contactNumber)) {
            throw new VedantuException(VedantuErrorCode.INVALID_CONTACT_NUMBER);
        }

        if (StringUtils.isEmpty(addOrgMemberReq.getTargetMemberId()) && StringUtils.isEmpty(addOrgMemberReq.getEmail()) && !addOrgMemberReq.isOTPsignup) {
            String msg = "memberId,email and contact number can not be empty";
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MISSING_MEMBER_ID_AND_EMAIL,
                    msg);
        }

        if (!isMemberIdSysGenerated
                && REGEX_SYSGEN_MEMBER_ID_PATTERN.equals(addOrgMemberReq.getTargetMemberId())) {
            logger.error("system generated memberId[" + addOrgMemberReq.getTargetMemberId()
                    + "] pattern not allowed as input");
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_ID_PATTERN_NOT_ALLOWED,
                    "system generated memberId[" + addOrgMemberReq.getTargetMemberId()
                            + "] pattern not allowed as input");
        }
        Optional<User> user = null;
        OrgMember orgMember = StringUtils.isEmpty(addOrgMemberReq.getTargetMemberId()) ? null
                : getMemberByMemberId(addOrgMemberReq.orgId,
                addOrgMemberReq.getTargetMemberId());
        if (null != orgMember) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_ALREADY_EXISTS);
        }
        if (!StringUtils.isEmpty(addOrgMemberReq.getEmail()) && !addOrgMemberReq.getTargetMemberId().equals(SUPER_ADMIN_MEMBER_ID)) {
            orgMember = getOrgMemberWithEmail(addOrgMemberReq.orgId, addOrgMemberReq.getEmail());
            user = getUserByEmail(addOrgMemberReq.getEmail());
        }
        if (null != orgMember || user.isPresent()) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_ALREADY_EXISTS);
        }
        if (!StringUtils.isEmpty(addOrgMemberReq.contactNumber) && !addOrgMemberReq.getTargetMemberId().equals(SUPER_ADMIN_MEMBER_ID)) {
            orgMember = orgMemberRepo.findByCountryCodeAndContactNumber(addOrgMemberReq.countryCode, addOrgMemberReq.contactNumber);
            user = userRepo.findByUsername(addOrgMemberReq.countryCode + addOrgMemberReq.contactNumber);
        }
        if (null != orgMember || user.isPresent()) {
            throw new VedantuException(VedantuErrorCode.USER_ALREADY_EXISTS);
        }
        if (!StringUtils.isEmpty(addOrgMemberReq.referrerCode)) {
            if (!isValidreferralCode(addOrgMemberReq.referrerCode)) {
                //  throw new VedantuException(VedantuErrorCode.INVALID_REFERER);
            }
        }


        Interval interval = new Interval();
        interval.setFrom(new Date().getTime());
        interval.setTill(-1);


        OrgMember member = null;
        String username = SUPER_ADMIN_MEMBER_ID.equals(addOrgMemberReq.getTargetMemberId())
                || addOrgMemberReq.useEmailAsUsername ? addOrgMemberReq.getEmail()
                : getMemberUsername(addOrgMemberReq.orgId, addOrgMemberReq.getTargetMemberId());
        if (null == addOrgMemberReq.countryCode || StringUtils.isEmpty(addOrgMemberReq.countryCode))
            addOrgMemberReq.countryCode = "91";
        if (!StringUtils.isEmpty(username) && !addOrgMemberReq.usePhoneAsUsername) {
            user = userRepo.findByUsername(username);
        } else if (addOrgMemberReq.isOTPsignup) {
            member = orgMemberRepo.findByCountryCodeAndContactNumber(addOrgMemberReq.countryCode, addOrgMemberReq.contactNumber);
            username = addOrgMemberReq.countryCode + addOrgMemberReq.contactNumber;
        }
        logger.info("userName..." + username);

        String userId = null;

        if (!user.isPresent() && null == member) {
            logger.debug("will add user for username: " + username);
            boolean isSignupWithAccessCode = StringUtils.isEmpty(addOrgMemberReq.password) && addOrgMemberReq.useEmailAsUsername && addOrgMemberReq.usePhoneAsUsername;

            boolean isSysGenPassword = StringUtils.isEmpty(addOrgMemberReq.password);

            String password = (addOrgMemberReq.password != null && !addOrgMemberReq.password.isEmpty()) ? addOrgMemberReq.password
                    : (addOrgMemberReq.useEmailAsUsername ? CampaignCodesServicesImpl.randomString(SYSTEM_GENERATED_PASS_LENGTH) : getMemberDefaultPassword(addOrgMemberReq.profile,
                    addOrgMemberReq.getTargetMemberId(), addOrgMemberReq.dob));
            AddUserReq addUserReq = new AddUserReq(username, password, addOrgMemberReq.firstName,
                    addOrgMemberReq.lastName, addOrgMemberReq.dob, addOrgMemberReq.gender,
                    addOrgMemberReq.getEmail(),
                    StringUtils.isEmpty(addOrgMemberReq.extUserId) ? AuthType.EXT_AUTH_ORG
                            : AuthType.VEDANTU);
            addUserReq.twitterHandle = addOrgMemberReq.twitterHandle;
            addUserReq.orgId = addOrgMemberReq.getOrgId();
            addUserReq.isSysGenPassword = isSysGenPassword;
            if (addOrgMemberReq.isOTPsignup || addOrgMemberReq.isValidPhone) {
                addUserReq.isPhoneVerified = true;
            }
            addUserReq.isOTPuser = addOrgMemberReq.isOTPsignup;
            addUserReq.callingAppId = addOrgMemberReq.callingAppId;
            AddUserRes addUserRes = organizationsImpl.addUser(addUserReq);
            userId = addUserRes.id;

            if (isSignupWithAccessCode) {

                user = userRepo.findById(userId);
               /* SignupWithAccessCodeEmailDetails emailDetails;
                try {
                    emailDetails = new SignupWithAccessCodeEmailDetails();
                    emailDetails.user = new UserEmailInfo();
                    emailDetails.password = password;
                    emailDetails.userName = username;
                    emailDetails.user
                            .fromUserExtendedInfo((UserExtendedInfo) user.toExtendedInfo());
                    emailDetails.addRecepient(emailDetails.user.get().getFullName(),
                            user.emailChangeReq.email);
                    emailDetails.orgId = organization._getStringId();
                    OrgMemberManager.generateEventAysc(user.get()._getStringId()), emailDetails,
                            EventType.SEND_INSTANT_EMAIL);
                } catch (ClassNotFoundException e) {
                    logger.error("Run time class not found", e);
                }*/

            }

        } else {
            if (addOrgMemberReq.useEmailAsUsername) {
                throw new VedantuException(VedantuErrorCode.USER_ALREADY_EXISTS,
                        "a user is already created with email[" + addOrgMemberReq.getEmail() + "]");
            } else if (addOrgMemberReq.usePhoneAsUsername) {
                throw new VedantuException(VedantuErrorCode.USER_ALREADY_EXISTS,
                        "a user is already created with phone[" + addOrgMemberReq.contactNumber
                                + "]");
            }
            // TODO: Send notification before linking
            userId = user.get()._getStringId();
        }

        if (OrgMemberProfile.STUDENT == addOrgMemberReq.profile) {
            orgMember = addMember(addOrgMemberReq.orgId, userId,
                    addOrgMemberReq.getTargetMemberId(), addOrgMemberReq.firstName,
                    addOrgMemberReq.lastName, addOrgMemberReq.dob, addOrgMemberReq.gender,
                    addOrgMemberReq.getEmail(), addOrgMemberReq.profile,
                    addOrgMemberReq.contactNumber, addOrgMemberReq.countryCode,
                    addOrgMemberReq.father, addOrgMemberReq.mother, addOrgMemberReq.guardian,
                    addOrgMemberReq.getParentEmail(), interval, addOrgMemberReq.extUserId,
                    addOrgMemberReq.extraInfo, addOrgMemberReq.referrerCode);

        } else {
            orgMember = addMember(addOrgMemberReq.orgId, userId,
                    addOrgMemberReq.getTargetMemberId(), addOrgMemberReq.firstName,
                    addOrgMemberReq.lastName, addOrgMemberReq.dob, addOrgMemberReq.gender,
                    addOrgMemberReq.getEmail(), addOrgMemberReq.profile,
                    addOrgMemberReq.contactNumber, addOrgMemberReq.countryCode, false, interval,
                    addOrgMemberReq.extUserId, addOrgMemberReq.extraInfo);
        }

        AddOrgMemberRes addOrgMemberRes = new AddOrgMemberRes(orgMember._getStringId(),
                orgMember.recordState, orgMember.orgId, orgMember.userId);
        addOrgMemberRes.orgMemberId = orgMember._getStringId();
        addOrgMemberRes.memberId = orgMember.memberId;
        addOrgMemberRes.firstName = orgMember.firstName;
        addOrgMemberRes.lastName = orgMember.lastName;
        // addOrgMemberRes.thumbnail = orgMember._getThumbnailUrl();
        addOrgMemberRes.authType = authType;
        addOrgMemberRes.contactNumber = orgMember.contactNumber;

        acceptTnC(orgMember.userId, true, TNC_VERSION);

        if (addOrgMemberReq.campaignCode != null && !addOrgMemberReq.campaignCode.equals("")) {
            logger.info(":::::::       Applying campaign code       ::::::::::");
            ApplyCampaignCodeReq applyCampaignCodeReq = new ApplyCampaignCodeReq();
            applyCampaignCodeReq.campaignCode = addOrgMemberReq.campaignCode;
            applyCampaignCodeReq.userId = userId;
            ApplyCampaignCodeRes applyCampaignCodeRes = applyCampaignCode(applyCampaignCodeReq);
            if (applyCampaignCodeRes.applied) {
                logger.info("::::::::    Coupon Code Applied     :::::::");
                addOrgMemberRes.showSpecialMessage = true;
                logger.info(":::: Initializing message    :::::");
                // This message should match the template of smCountry. You
                // can't change
                // the template here.
                String message = "Thank you for registering on Learnpedia! You can enjoy our JEE/NEET free demo. "
                        + "You will get Rs50 PayTM cash on your mobile number "
                        + orgMember.contactNumber + " within 7 days";
                String response = sendOTP(orgMember.countryCode + orgMember.contactNumber, message);
                logger.info("Response from smsCountry " + response);
            } else {
                logger.info("::::::::    Coupon Code Not Applied     :::::::");
            }
        }

        Integer packageDays = StringUtils.isEmpty(addOrgMemberReq.referrerCode) ? 0 : checkCampaignReferral(addOrgMemberReq);
        if (packageDays != null && packageDays > 0) {
            AddOrgMemberMappingReq addOrgMemberMappingReq = new AddOrgMemberMappingReq();
            addOrgMemberMappingReq.orgId = orgMember.orgId;
            addOrgMemberMappingReq.targetProfile = orgMember.profile;
            addOrgMemberMappingReq.targetUserId = orgMember.userId;
            addOrgMemberMappingReq.targetOrgMemberId = orgMember._getStringId();
            List<String> sectionIds = new ArrayList<String>();
            sectionIds.add(campaignSectionId);
            addOrgMemberMappingReq.programId = campaignProgramId;
            addOrgMemberMappingReq.centerId = campaignCenterId;
            addOrgMemberMappingReq.sectionIds = sectionIds;
            addOrgMemberMappingReq.packageDays = packageDays;
            addOrgMemberMapping(addOrgMemberMappingReq, true);
        }
        // Below code is to add demo programs for uprep students.
        String UPrepOrgId = "5df8a0d0e4b0897459b25d86";
        if (addOrgMemberReq.orgId.equals(UPrepOrgId)) {
            if (addOrgMemberReq.extraInfo != null && !addOrgMemberReq.extraInfo.isEmpty()) {
                for (OrgMemberExtraInfo extraInfo : addOrgMemberReq.extraInfo) {
                    if (extraInfo.name.equalsIgnoreCase("program")) {
                        if (extraInfo.value.equalsIgnoreCase("JEE")) {
                            addOrgMemberReq.autoAddDemoProgram = true;
                            addOrgMemberReq.progType = "JEE";
                        } else if (extraInfo.value.equalsIgnoreCase("NEET")) {
                            addOrgMemberReq.autoAddDemoProgram = true;
                            addOrgMemberReq.progType = "NEET";
                        }
                    }
                }
            }
        }

        if (addOrgMemberReq.autoAddDemoProgram) {
            AddOrgMemberMappingReq addOrgMemberMappingReq = new AddOrgMemberMappingReq();
            addOrgMemberMappingReq.orgId = orgMember.orgId;
            addOrgMemberMappingReq.targetProfile = orgMember.profile;
            addOrgMemberMappingReq.targetUserId = orgMember.userId;
            addOrgMemberMappingReq.targetOrgMemberId = orgMember._getStringId();
            boolean demoProgFound = false;
            List<String> sectionIds = new ArrayList<String>();
            if (addOrgMemberReq.progType != null && addOrgMemberReq.progType.equalsIgnoreCase("JEE")) {
                demoProgFound = true;
                sectionIds.add(jeedemoSectionId);
                addOrgMemberMappingReq.programId = jeedemoProgramId;
                addOrgMemberMappingReq.centerId = jeedemoCenterId;
            } else if (addOrgMemberReq.progType != null && addOrgMemberReq.progType.equalsIgnoreCase("NEET")) {
                demoProgFound = true;
                sectionIds.add(neetdemoSectionId);
                addOrgMemberMappingReq.programId = neetdemoProgramId;
                addOrgMemberMappingReq.centerId = neetdemoCenterId;
            }

            if (demoProgFound) {
                addOrgMemberMappingReq.sectionIds = sectionIds;
                checkIfAddMappingAllowed(addOrgMemberMappingReq);
                addOrgMemberMapping(addOrgMemberMappingReq, true);
                addOrgMemberRes.autoAddDemoProgram = true;
            } else {
                logger.error("::::::      Could not get correct program to auto add demo program        ::::::::");
            }
        }

        // Add Leadsquared Campaign program if required.
        if (addOrgMemberReq.autoAddCampaignProgram) {
            AddOrgMemberMappingReq addOrgMemberMappingReq = new AddOrgMemberMappingReq();
            addOrgMemberMappingReq.orgId = orgMember.orgId;
            addOrgMemberMappingReq.targetProfile = orgMember.profile;
            addOrgMemberMappingReq.targetUserId = orgMember.userId;
            addOrgMemberMappingReq.targetOrgMemberId = orgMember._getStringId();
            boolean campaignProgFound = false;
            List<String> sectionIds = new ArrayList<String>();
            if (addOrgMemberReq.campaignAddProgram.equalsIgnoreCase("emp")) {
                campaignProgFound = true;
                sectionIds.add(empSectionId);
                addOrgMemberMappingReq.programId = empProgramId;
                addOrgMemberMappingReq.centerId = empCenterId;
            } else if (addOrgMemberReq.campaignAddProgram.equalsIgnoreCase("jeesoln")) {
                campaignProgFound = true;
                sectionIds.add(jeesolnSectionId);
                addOrgMemberMappingReq.programId = jeesolnProgramId;
                addOrgMemberMappingReq.centerId = jeesolnCenterId;
            } else if (addOrgMemberReq.campaignAddProgram.equalsIgnoreCase("neetseries")) {
                campaignProgFound = true;
                sectionIds.add(neetseriesSectionId);
                addOrgMemberMappingReq.programId = neetseriesProgramId;
                addOrgMemberMappingReq.centerId = neetseriesCenterId;
            } else if (addOrgMemberReq.campaignAddProgram.equalsIgnoreCase("jeedemo")
                    || addOrgMemberReq.campaignAddProgram.equalsIgnoreCase("jee")) {
                campaignProgFound = true;
                sectionIds.add(jeedemoSectionId);
                addOrgMemberMappingReq.programId = jeedemoProgramId;
                addOrgMemberMappingReq.centerId = jeedemoCenterId;
            } else if (addOrgMemberReq.campaignAddProgram.equalsIgnoreCase("neetdemo")
                    || addOrgMemberReq.campaignAddProgram.equalsIgnoreCase("neet")) {
                campaignProgFound = true;
                sectionIds.add(neetdemoSectionId);
                addOrgMemberMappingReq.programId = neetdemoProgramId;
                addOrgMemberMappingReq.centerId = neetdemoCenterId;
            }

            if (campaignProgFound) {
                addOrgMemberMappingReq.sectionIds = sectionIds;
                addOrgMemberMappingReq.packageDays = DEFAULT_CAMPAIGN_PACKAGE_DAYS;
                checkIfAddMappingAllowed(addOrgMemberMappingReq);
                addOrgMemberMapping(addOrgMemberMappingReq, true);
                addOrgMemberRes.autoAddCampaignProgram = true;
            } else {
                logger.error("::::::      Could not find campaign program to auto add        ::::::::");
            }
        }


        return addOrgMemberRes;

    }

    public OrgMember getOrgMemberWithEmail(String orgId, String email) {

        OrgMember orgMember = orgMemberRepo.findByOrgIdAndEmailAndStatus(orgId, email, false);

        if (null == orgMember) {
            logger.error("cannot find orgMember for orgId: " + orgId + ", email: " + email);
        }

        return orgMember;
    }

    public Optional<User> getUserByEmail(String email) {
        logger.debug("entering the function doesUserExists " + email);
        Optional<User> user = userRepo.findByEmail(email);
        logger.debug("exiting the function doesUserExists " + email);
        return user;
    }

    public String getMemberUsername(String orgId, String memberId) {

        String memberUsername = (orgId + ":" + memberId).toLowerCase();
        return memberUsername;
    }

    public ApplyCampaignCodeRes applyCampaignCode(ApplyCampaignCodeReq request)
            throws VedantuException {

        ApplyCampaignCodeRes response = new ApplyCampaignCodeRes();
        CampaignCode campaignCode = campaignCodesServicesImpl.getCampaignCodeByCode(request.campaignCode);
        if (campaignCode == null) {
            logger.error("campaignCode does not exist, check the code : " + request.campaignCode);
            throw new VedantuException(VedantuErrorCode.DOES_NOT_EXIST);
        }
        if (campaignCode.maxUsageCount <= campaignCode.currentUsageCount) {
            logger.error("campaignCode used maximum times : " + request.campaignCode);
            response.applied = false;
            response.message = "Code is used maximum times";
            return response;
        }
        Optional<SalesCampaign> salesCampaign = salesCampaignRepo.findById(campaignCode.salesCampaignId);
        if (salesCampaign.isPresent()) {
            if (salesCampaign.get().getStartTime() > System.currentTimeMillis()
                    || salesCampaign.get().getExpiryTime() < System.currentTimeMillis()) {
                logger.error("salesCampaign is not started or expired " + campaignCode.salesCampaignId);
                response.applied = false;
                response.message = "Campaign not started or expired";
                return response;
            }
        }
        campaignCode.currentUsageCount += 1;
        campaignCode.consumerUserIds.add(request.userId);
        if (campaignCode.maxUsageCount == campaignCode.currentUsageCount) {
            campaignCode.expired = true;
        }
        campaignCodeRepo.save(campaignCode);
        response.applied = true;
        response.message = "Applied Successfully";
        return response;
    }

    private Integer checkCampaignReferral(AddOrgMemberReq addOrgMemberReq) {
        logger.debug("Referrer Code is: " + addOrgMemberReq.referrerCode);
        OrgMember orgMember = orgMemberRepo.findByReferralCode(addOrgMemberReq.referrerCode);
        Integer packageDays = Integer.valueOf(0);
        if (orgMember == null) {
            return packageDays;
        }
        if (CAMPAIGN_REFERRAL_CODE.equalsIgnoreCase(orgMember.referrerUserId)) {
            packageDays = campaignUserPackagedays;
        } else if (CAMPAIGN_REFERRAL_CODE.equalsIgnoreCase(orgMember.userId)) {
            packageDays = campaignUserPackagedays;
        }
        return packageDays;
    }

    public OrgMember addMember(String orgId, String userId, String memberId, String firstName,
                               String lastName, String dob, Gender gender, String email, OrgMemberProfile profile,
                               String contactNumber, String countryCode, boolean canImpersonate, Interval interval, String extUserId,
                               List<OrgMemberExtraInfo> extraInfo) throws VedantuException {

        return addMember(orgId, userId, memberId, firstName, lastName, dob, gender, email, profile,
                contactNumber, countryCode, null, null, null, HardCodedConstants.emptyString, canImpersonate, interval,
                extUserId, extraInfo, null);
    }

    // Should be used only for STUDENTs
    public OrgMember addMember(String orgId, String userId, String memberId, String firstName,
                               String lastName, String dob, Gender gender, String email, OrgMemberProfile profile,
                               String contactNumber, String countryCode, MemberParentInfo father, MemberParentInfo mother,
                               MemberParentInfo guardian, String parentEmail, Interval interval, String extUserId,
                               List<OrgMemberExtraInfo> extraInfo, String referrerCode)
            throws VedantuException {

        return addMember(orgId, userId, memberId, firstName, lastName, dob, gender, email, profile,
                contactNumber, countryCode, father, mother, guardian, parentEmail, false, interval, extUserId,
                extraInfo, referrerCode);

    }

    public OrgMember addMember(String orgId, String userId, String memberId, String firstName,
                               String lastName, String dob, Gender gender, String email, OrgMemberProfile profile,
                               String contactNumber, String countryCode, MemberParentInfo father, MemberParentInfo mother,
                               MemberParentInfo guardian, String parentEmail, boolean canImpersonate,
                               Interval interval, String extUserId, List<OrgMemberExtraInfo> extraInfo,
                               String referrerCode) throws VedantuException {

        logger.debug("addMember orgId: " + orgId + ", userId: " + userId + ", memberId" + memberId
                + ", firstName: " + firstName + ", lastName:" + lastName + ", dob: " + dob
                + ", gender" + gender + ", email" + email + ", profile" + profile
                + ", contactNumber: " + contactNumber + ", fathe: " + father + ", mother: "
                + mother + ", guardian: " + guardian + ", parentEmail: " + parentEmail
                + "Interval From: " + interval.getFrom() + "Interval Till: " + interval.getTill());
        OrgMember orgMember = orgMemberRepo.findByOrgIdAndMemberId(orgId, memberId);
        if (null != orgMember) {
            logger.error("cannot add orgMember as orgMember already exists for orgId: " + orgId
                    + ", memberId: " + memberId);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_ALREADY_EXISTS);
        }
        orgMember = new OrgMember();
        orgMember.userId = userId;
        orgMember.orgId = orgId;
        orgMember.memberId = memberId;
        orgMember.firstName = firstName;
        orgMember.lastName = lastName;
        orgMember.dob = dob;
        orgMember.gender = gender;
        orgMember.email = email;
        orgMember.profile = profile;
        orgMember.contactNumber = contactNumber;
        orgMember.countryCode = countryCode;
        orgMember.father = father;
        orgMember.mother = mother;
        orgMember.guardian = guardian;
        orgMember.parentEmail = parentEmail;
        orgMember.canImpersonate = canImpersonate;
        orgMember.interval = interval;
        orgMember.extUserId = extUserId;
        orgMember.extraInfo = extraInfo;
        String referralCode = generateReferralCode(firstName);

        if (OrgMemberProfile.STUDENT == orgMember.profile) {
            if (referrerCode != null) {
                String referrerUserId = getUserIdFromReferralCode(referrerCode.toLowerCase());
                if (referrerUserId != null && !referrerUserId.isEmpty()) {
                    orgMember.referrerUserId = referrerUserId;
                    addRewards(referrerUserId, orgMember, CampaignType.REFERRAL);
                } else {
                    orgMember.referrerCode = referrerCode;
                }
            }
            orgMember.referralCode = referralCode;
        }
        orgMemberRepo.save(orgMember);
        logger.info("addMember added orgMember: " + orgMember);
        return orgMember;
    }

    public boolean addRewards(String referrerUserId, OrgMember friendOrgMember,
                              CampaignType campaignType) {

        Campaign campaign = campaignRepo.findByCampaignTypeAndRecordState(campaignType, VedantuRecordState.ACTIVE);
        if (campaign != null) {
            int friendRewards = campaign.friendRewards;
            int referrerRewards = campaign.referrerRewards;
            OrgMember referrer = orgMemberRepo.findByUserId(referrerUserId);
            friendOrgMember.rewards += friendRewards;
            referrer.rewards += referrerRewards;
            orgMemberRepo.save(referrer);
        }
        return true;
    }

    public User acceptTnC(String userId, boolean agrees, String version) throws VedantuException {

        logger.debug("acceptTnC userId: " + userId + ", agrees: " + agrees + ", version: "
                + version);

        Optional<User> user = userRepo.findById(userId);
        logger.debug("found user: " + user);

        if (!user.isPresent()) {
            logger.error("cannot acceptTnC as user does not exist for userId: " + userId);
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }

        user.get().setTncAcceptance(new TnCAcceptance(agrees, version, System.currentTimeMillis()));
        userRepo.save(user.get());

        logger.info("acceptTnC updated user: " + user);

        return user.get();
    }

    public String sendOTP(String mobilenumber, String message) {
        String postData = "";
        String response = "";
        URL url;
        try {
            postData += "User=" + URLEncoder.encode(USER, StandardCharsets.UTF_8) + "&passwd=" + PSWD
                    + "&mobilenumber=" + mobilenumber + "&message="
                    + URLEncoder.encode(message, StandardCharsets.UTF_8) + "&sid=" + SID + "&mtype=" + MTYPE
                    + "&DR=" + DR;
            url = new URL(SMSCOUNTRYURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
            out.write(postData);
            out.close();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String decodedString;
            while ((decodedString = in.readLine()) != null) {
                decodedString = decodedString.split(":")[1];
                response += decodedString;
            }
            in.close();
            return response;
        } catch (MalformedURLException e) {
            logger.error("MalformedURLException in sendOTP function", e);
        } catch (IOException e) {
            logger.error("IOException in sendOTP function", e);
        }
        return response;
    }

    @Override
    public VedantuResponse sendEmailsToStudents(SendEmailsToStudentsReq sendEmailsToStudentsReq) {
        SendEmailsToStudentsRes response = null;

        response = sendEmailsToStudentRes(sendEmailsToStudentsReq);
        return new VedantuResponse(response);
    }

    public SendEmailsToStudentsRes sendEmailsToStudentRes(SendEmailsToStudentsReq request)
            throws VedantuException {

        SendEmailsToStudentsRes response = new SendEmailsToStudentsRes();
        long count = getCountOfMembers(request.orgId,
                OrgMemberProfile.STUDENT, request.programId, request.centerId, request.sectionId);
        for (int start = 0; start < count; start += EMAILS_BATCH_SIZE) {
            AtomicLong hits = null;
            List<OrgMember> members = getOrganizationMembers(request.orgId,
                    OrgMemberProfile.STUDENT, null, request.programId, request.centerId,
                    request.sectionId, null, null, start, EMAILS_BATCH_SIZE, null, null, hits);
            SendEmailToStudentsDetails details;
            try {
                details = new SendEmailToStudentsDetails();
            } catch (ClassNotFoundException e) {
                logger.error("SendEmailToStudentsDetails class not found", e);
                throw new VedantuException(VedantuErrorCode.EMAIL_CAN_NOT_BE_SEND);
            }
            details.setSubject(request.subject);
            details.message = request.message;
            for (OrgMember member : members) {
                details.addBccRecepient(member.firstName, member.email);
            }
            if (!details.getBCCRecepients().isEmpty()) {
                generateEventAysc(request.userId, details, EventType.SEND_EMAIL);
            }
        }
        response.success = true;

        return response;
    }

    public long getCountOfMembers(String orgId, OrgMemberProfile profile,
                                  String programId, String centerId, String sectionId) {
        Criteria criteria = new Criteria();
        Query query = new Query();
        criteria.and("orgId").is(orgId);

        if (profile != null) {
            criteria.and("profile").is(Arrays.asList(profile));
        }
        if (!StringUtils.isEmpty(programId)) {
            criteria.and("mappings.programId").is(programId);
        }
        if (!StringUtils.isEmpty(centerId)) {
            criteria.and("mappings.centerId").is(centerId);
        }
        if (!StringUtils.isEmpty(sectionId)) {
            criteria.and("mappings.sectionId").is(sectionId);
        }
        query.addCriteria(criteria);
        List<OrgMember> orgMembers = mongoTemplate.find(query, OrgMember.class);

        long count = orgMembers.size();
        return count;
    }

    private void generateEventAysc(String userId, SendEmailToStudentsDetails details, EventType eventType) {
        CompletableFuture.runAsync(() -> {
            eventUtil.generateEvent(eventType, null, userId, details,
                    details.__getSrcEntity(), UserActionType.EventActionType.ADD, 0);
        });

    }

    @Override
    public VedantuResponse addMemberWithAccessCode(AddOrgMemberWithRequestCodeReq addOrgMemberWithRequestCodeReq) {
        AddOrgMemberRes res = null;

        try {
            res = addOrgMemberWithAccessCode(addOrgMemberWithRequestCodeReq);

        } catch (VedantuException e) {
            throw e;
        }

        return new VedantuResponse(res);
    }

    public AddOrgMemberRes addOrgMemberWithAccessCode(AddOrgMemberWithRequestCodeReq req)
            throws VedantuException {

        logger.debug("Request " + req);
        OrgSection orgSection = getSectionByAccessCode(req.accessCode);
        AddOrgMemberReq addOrgMemberReq = new AddOrgMemberReq();
        addOrgMemberReq.dob = new SimpleDateFormat("yyyy-MM-dd").format(new Date(0));
        addOrgMemberReq.firstName = req.firstName;
        addOrgMemberReq.lastName = req.lastName;
        addOrgMemberReq.orgId = orgSection.orgId;
        addOrgMemberReq.profile = OrgMemberProfile.STUDENT;
        addOrgMemberReq.setEmail(req.email);
        addOrgMemberReq.useEmailAsUsername = true;
        addOrgMemberReq.twitterHandle = req.twitterHandle;
        AddOrgMemberRes addOrgMemberRes = null;

        String orgMemberId = getNextOrgMemberId(orgSection.orgId);
        addOrgMemberReq.setTargetMemberId(orgMemberId);
        addOrgMemberRes = addOrgMember(addOrgMemberReq, true, null);
        addOrgMemberMapping(orgSection.orgId, addOrgMemberRes.userId, addOrgMemberRes.id,
                orgSection.programId, orgSection.centerId,
                new HashSet<String>(Arrays.asList(orgSection._getStringId())), null,
                new AtomicBoolean(), true);

        return addOrgMemberRes;
    }

    public OrgSection getSectionByAccessCode(String accessCode) throws VedantuException {


        OrgSection orgSection = orgSectionRepo.findByAccessCodeAndRecordState(accessCode, VedantuRecordState.ACTIVE);
        if (null == orgSection) {
            logger.error("cannot find orgSection for accessCode: " + accessCode);
            throw new VedantuException(VedantuErrorCode.INVALID_ACCESS_CODE, "invalid access code");
        }

        return orgSection;
    }

    public OrgMember addOrgMemberMapping(String orgId, String userId, String orgMemberId,
                                         String programId, String centerId, Set<String> sectionIds, Set<String> courseIds,
                                         AtomicBoolean isAdded, boolean noExceptionOnExistingMapping) throws VedantuException {

        return addOrganizationMemberMapping(orgId, userId, orgMemberId, programId, centerId, sectionIds,
                courseIds, isAdded, noExceptionOnExistingMapping, null, 0);
    }

    public UploadOrgStudentsRes uploadOrgStudents(UploadOrgStudentsReq uploadOrgStudentsReq)
            throws VedantuException {

        Organization organization = organizationsImpl.getOrganizationById(uploadOrgStudentsReq.orgId);

        // check program
        OrgProgram program = programServiceImpl.getProgramById(uploadOrgStudentsReq.orgId,
                uploadOrgStudentsReq.programId);
        if (null == program) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_PROGRAM_NOT_FOUND);
        }

        // parse file and collect student, center, section information
        StudentsXLParser parser = new StudentsXLParser(uploadOrgStudentsReq.fileName,
                uploadOrgStudentsReq.inputFile, uploadOrgStudentsReq.programId,
                organization.extraMemberInfoFields == null ? null
                        : organization.extraMemberInfoFields.get(OrgMemberProfile.STUDENT));
        if (parser.hasErrors()) {
            throw new VedantuException(VedantuErrorCode.STUDENT_FILE_UNPARSEABLE, parser.getErrors().stream().collect(Collectors.joining(", ")));
        }

        // verify center existence for codes
        Set<String> centerCodes = parser.getCenters();
        if (CollectionUtils.isEmpty(centerCodes) || parser.getRecords() == null
                || parser.getRecords().isEmpty()) {
            throw new VedantuException(VedantuErrorCode.STUDENT_FILE_UNPARSEABLE,
                    "Students Records can not be empty");
        }

        Map<String, OrgStructureBasicInfo> codeToCenter =
                getBasicInfosByCode(uploadOrgStudentsReq.orgId, centerCodes);
        if (centerCodes.size() > codeToCenter.size()) {
            Set<String> missingCenters = new HashSet<String>(centerCodes);
            missingCenters.removeAll(codeToCenter.keySet());
            String errorMsg = "missing centers: {" + missingCenters.stream().collect(Collectors.joining(",")) + "}";
            throw new VedantuException(VedantuErrorCode.STUDENT_FILE_UNPARSEABLE, errorMsg);
        }

        // verify if centers exist in program
        Set<String> nonProgramCenters = new HashSet<String>();
        for (Map.Entry<String, OrgStructureBasicInfo> centerEntry : codeToCenter.entrySet()) {
            OrgProgramCenterSections centerSections = program
                    ._getOrgProgramCenterSections(centerEntry.getValue().id);
            if (null == centerSections) {
                nonProgramCenters.add(centerEntry.getKey());
            }
        }
        if (!CollectionUtils.isEmpty(nonProgramCenters)) {
            String errorMsg = "non-program centers: {" + nonProgramCenters.stream().collect(Collectors.joining(", "))
                    + "}";
            throw new VedantuException(VedantuErrorCode.STUDENT_FILE_UNPARSEABLE, errorMsg);
        }

        // verify section existence for codes
        Set<String> centerQualifiedSectionCodes = parser.getSections();
        Map<String, OrgStructureBasicInfo> centerQualifiedCodeToSection =
                getBasicInfosByCode(uploadOrgStudentsReq.orgId, uploadOrgStudentsReq.programId,
                        codeToCenter, centerQualifiedSectionCodes);
        if (centerQualifiedSectionCodes.size() >
                centerQualifiedCodeToSection.size()) {
            Set<String> missingSections = new HashSet<String>(centerQualifiedSectionCodes);
            missingSections.removeAll(centerQualifiedCodeToSection.keySet());
            String errorMsg = "missing sections: {" + missingSections.stream().collect(Collectors.joining(", ")) + "}";
            throw new VedantuException(VedantuErrorCode.STUDENT_FILE_UNPARSEABLE, errorMsg);
        }

        // verify if sections exist in program/center
        Set<String> nonProgramSections = new HashSet<String>();
        for (Map.Entry<String, OrgStructureBasicInfo> sectionEntry : centerQualifiedCodeToSection
                .entrySet()) {

            String centerQualifiedSectionCode = sectionEntry.getKey();

            String centerCode = getCenterPart(centerQualifiedSectionCode);
            OrgStructureBasicInfo centerBasicInfo = codeToCenter.get(centerCode);

            OrgProgramCenterSections centerSections = program
                    ._getOrgProgramCenterSections(centerBasicInfo.id);
            if (null == centerSections) {
                nonProgramSections.add(centerQualifiedSectionCode);
                continue;
            }

            OrgStructureBasicInfo sectionBasicInfo = centerQualifiedCodeToSection
                    .get(centerQualifiedSectionCode);
            if (null == sectionBasicInfo || !centerSections.hasSection(sectionBasicInfo.id)) {
                nonProgramSections.add(centerQualifiedSectionCode);
            }
        }

        if (!CollectionUtils.isEmpty(nonProgramSections)) {
            String errorMsg = "non-program sections: {"
                    + nonProgramSections.stream().collect(Collectors.joining(", ")) + "}";
            throw new VedantuException(VedantuErrorCode.STUDENT_FILE_UNPARSEABLE, errorMsg);
        }

        // Check Members existence
        Set<String> memberIdsOrEmails = parser.getRecords().keySet();

        Set<String> existingUsernames = getExistingUsernames(memberIdsOrEmails);

        Set<String> existingMemberIds = getExistingMemberIds(
                uploadOrgStudentsReq.orgId, memberIdsOrEmails);

        logger.debug("already presented usernames: " + existingUsernames + ", existingMemberIds:"
                + existingMemberIds + ", merge:" + uploadOrgStudentsReq.merge);

        if (!uploadOrgStudentsReq.merge && !CollectionUtils.isEmpty(existingMemberIds)) {
            String errorMsg = "already presente memberIds: {"
                    + existingMemberIds.stream().collect(Collectors.joining(", ")) + "}";
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_ALREADY_EXISTS,
                    errorMsg);
        }

        for (Map.Entry<String, List<StudentXLRecord>> recordEntry : parser.getRecords().entrySet()) {

            List<StudentXLRecord> records = recordEntry.getValue();
            StudentXLRecord studentRecord = records.get(0);
            logger.debug("uploadOrgStudents processing memberId: " + studentRecord.memberId
                    + ", email: " + studentRecord.email + ", record: " + studentRecord);

            OrgMember orgMember = null;
            if (!existingMemberIds.contains(recordEntry.getKey())
                    && !StringUtils.isEmpty(studentRecord.memberId)) {

                logger.debug("uploadOrgStudents will try to add orgMember memberId: "
                        + studentRecord.memberId);

                AddOrgMemberReq addOrgMemberReq = studentRecord
                        .toAddOrgMemberReq(uploadOrgStudentsReq.orgId);

                logger.debug("addOrgMemberReq : " + addOrgMemberReq);
                AddOrgMemberRes addOrgMemberRes = addOrgMember(organization, addOrgMemberReq, false);
                logger.debug("addOrgMemberRes: " + addOrgMemberRes);

                orgMember = getMemberByMemberId(uploadOrgStudentsReq.orgId,
                        studentRecord.memberId.toLowerCase());
            }

            if (!existingUsernames.contains(recordEntry.getKey())
                    && !StringUtils.isEmpty(studentRecord.email) && orgMember == null) {

                logger.debug("uploadOrgStudents will try to add orgMember with email as username");

                AddOrgMemberReq addOrgMemberReq = studentRecord
                        .toAddOrgMemberReq(uploadOrgStudentsReq.orgId);
                addOrgMemberReq.useEmailAsUsername = true;
                logger.debug("addOrgMemberReq : " + addOrgMemberReq);

                String memberId = getNextOrgMemberId(uploadOrgStudentsReq.orgId);
                addOrgMemberReq.setTargetMemberId(memberId);
                AddOrgMemberRes addOrgMemberRes = addOrgMember(organization, addOrgMemberReq, true);
                logger.debug("addOrgMemberRes: " + addOrgMemberRes);
                orgMember = getMemberByMemberId(uploadOrgStudentsReq.orgId,
                        memberId);
            }

            if (orgMember == null) {
                if (existingUsernames.contains(recordEntry.getKey())) {
                    Optional<User> userOptional = userRepo.findByUsername(recordEntry.getKey());
                    if (userOptional.isPresent()) {
                        User user = userOptional.get();
                        orgMember = organizationsImpl.getMemberByUserId(uploadOrgStudentsReq.orgId, user._getStringId());
                    }
                }

                if (orgMember == null && existingMemberIds.contains(recordEntry.getKey())) {
                    orgMember = getMemberByMemberId(
                            uploadOrgStudentsReq.orgId, recordEntry.getKey());
                }

            }

            if (orgMember == null) {
                continue;
            }
            int i = 0;
            for (StudentXLRecord record : records) {
                logger.debug("uploadOrgStudents found orgMember: " + orgMember);
                i++;
                OrgStructureBasicInfo centerBasicInfo = codeToCenter.get(record.center);
                String centerQualifiedSectionCode = getCenterQualifiedSectionCode(
                        record.center, record.section);
                OrgStructureBasicInfo sectionBasicInfo = centerQualifiedCodeToSection
                        .get(centerQualifiedSectionCode);
                logger.debug("uploadOrgStudents will add orgMemberMapping");

                AddOrgMemberMappingReq addOrgMemberMappingReq = new AddOrgMemberMappingReq(
                        uploadOrgStudentsReq.orgId, orgMember.userId, orgMember._getStringId(),
                        orgMember.profile, uploadOrgStudentsReq.programId, centerBasicInfo.id,
                        Arrays.asList(sectionBasicInfo.id), null, record.pointOfSale,
                        record.sellerReferenceNo, null);

                try {
                    OrgMemberMappingInfo orgMappingInfo = new OrgMemberMappingInfo(
                            uploadOrgStudentsReq.programId, centerBasicInfo.id,
                            sectionBasicInfo.id, null);
                    if (orgMember.mappings != null && orgMember.mappings.contains(orgMappingInfo)) {
                        logger.debug("member is already part of : " + orgMappingInfo);
                        continue;
                    }
                    final boolean noExceptionOnExistingMapping = true;
                    AddOrgMemberMappingRes addOrgMemberMappingRes = addOrgMemberMapping(
                            organization, addOrgMemberMappingReq, noExceptionOnExistingMapping);
                    logger.debug("addOrgMemberMappingRes: " + addOrgMemberMappingRes);
                } catch (VedantuException e) {
                    throw new VedantuException(e.errorCode, "At UserRecord Row No:" + i + " "
                            + e.getMessage());
                }
            }
        }

        UploadOrgStudentsRes uploadOrgStudentsRes = new UploadOrgStudentsRes();
        uploadOrgStudentsRes.done = true;

        return uploadOrgStudentsRes;

    }

    public List<Interval> getMemberActivationPeriodsRes(String orgId, String userId, Interval queryInterval) {
        logger.debug("getMemberActivationPeriods orgId: " + orgId + ", userId: " + userId);
        Criteria criteria = new Criteria();
        Query query = new Query();
        //((y1<x2)&&(x1<y2)), where (y1,y2) is period to be passed
        criteria.and("orgId").is(orgId);
        criteria.and("userId").is(userId);
        long startOfQueryInterval = queryInterval.getFrom();
        long endOfQueryInterval = queryInterval.getTill();
        criteria.and("interval.till").gt(startOfQueryInterval);
        criteria.and("interval.from").lt(endOfQueryInterval);
        query.addCriteria(criteria);

        List<UserStateLog> userStateLogs = mongoTemplate.find(query, UserStateLog.class);
        List<Interval> intervals = new ArrayList<Interval>();

        for (UserStateLog userStateLog : userStateLogs) {
            Interval interval = new Interval();
            interval.setFrom(userStateLog.interval.getFrom());
            interval.setTill(userStateLog.interval.getTill());

            if (userStateLog.interval.getFrom() < startOfQueryInterval) {
                interval.setFrom(startOfQueryInterval);
            }

            if (userStateLog.interval.getTill() > endOfQueryInterval) {
                interval.setTill(endOfQueryInterval);
            }
            intervals.add(interval);
        }
        return intervals;
    }

    public Map<String, OrgStructureBasicInfo> getBasicInfosByCode(String orgId,
                                                                  Set<String> codes) {
        List<OrgCenter> centers = orgCenterRepo.findByOrgIdAndCodeInOrderByCName(orgId, codes);

        List<ModelBasicInfo> centerBasicInfos = new ArrayList<>();//toBasicInfos(centers);
        centers.stream().forEach(orgCenter -> {
            OrgStructureBasicInfo orgStructureBasicInfo = new OrgStructureBasicInfo(orgCenter);
            centerBasicInfos.add(orgStructureBasicInfo);
        });


        Map<String, OrgStructureBasicInfo> codeToCenter = new HashMap<String, OrgStructureBasicInfo>();
        for (ModelBasicInfo centerBasicInfo : centerBasicInfos) {
            OrgStructureBasicInfo oCenterBasicInfo = (OrgStructureBasicInfo) centerBasicInfo;
            codeToCenter.put(oCenterBasicInfo.code, oCenterBasicInfo);
        }

        return codeToCenter;
    }

    public Map<String, OrgStructureBasicInfo>
    getBasicInfosByCode(String orgId, String programId,
                        Map<String, OrgStructureBasicInfo> codeToCenter,
                        Set<String> centerQualifiedSectionCodes) throws VedantuException {

        Map<String, OrgStructureBasicInfo> centerQualifiedCodeToSection = new HashMap<String, OrgStructureBasicInfo>();

        for (String centerQualifiedCode : centerQualifiedSectionCodes) {
            if (StringUtils.isEmpty(centerQualifiedCode)) {
                continue;
            }
            String[] tokens = StringUtils.split(centerQualifiedCode, CENTER_SECTION_SEPARATOR);

            if (null == tokens || tokens.length != 2) {
                logger.error("getBasicInfosByCode not properly qualified (center qualified) section: "
                        + centerQualifiedCode);
            }

            String centerCode = tokens[0];
            String sectionCode = tokens[1];

            OrgStructureBasicInfo centerBasicInfo = codeToCenter.get(centerCode);

    /* OrgSection section = getQuery().filter("orgId", orgId).filter("programId", programId)
             .filter("centerId", centerBasicInfo.id).filter("code", sectionCode)
             .order("cName").get();*/
            OrgSection section = orgSectionRepo.findByOrgIdAndProgramIdAndCenterIdAndCodeOrderByCName(orgId, programId, centerBasicInfo.id, sectionCode);
            if (null == section) {
                logger.error("getBasicInfosByCode no such (center qualified) section: "
                        + centerQualifiedCode);
                continue;
            }

            OrgStructureBasicInfo oSectionBasicInfo = (OrgStructureBasicInfo) section.toBasicInfo();

            centerQualifiedCodeToSection.put(centerQualifiedCode, oSectionBasicInfo);
        }

        return centerQualifiedCodeToSection;
    }

    public AddOrgMemberRes addOrgMember(Organization organization,
                                        AddOrgMemberReq addOrgMemberReq, boolean isMemberIdSysGenerated)
            throws VedantuException {

        // AuthHandler handler = AuthHandlerFactory.getInstance().getAuthHandler(organization);

        return addMember(addOrgMemberReq, isMemberIdSysGenerated);
    }

    public Set<String> getExistingUsernames(Set<String> usernames) {


        List<User> users = userRepo.findByUsernameIn(usernames);
        Set<String> existingUsernames = new HashSet<String>();

        for (User t : users) {
            existingUsernames.add(t.username);
        }
        return existingUsernames;
    }

    public Set<String> getExistingMemberIds(String orgId, Collection<String> memberIds) {

        Set<String> existingMemberIds = new HashSet<String>();
        List<OrgMember> members = orgMemberRepo.findByOrgIdAndMemberIdIn(orgId, memberIds);


        for (OrgMember t : members) {
            existingMemberIds.add(t.memberId);
        }
        return existingMemberIds;
    }

    public String getCenterQualifiedSectionCode(String centerCode, String sectionCode) {

        return Arrays.asList(centerCode, sectionCode).stream().collect(Collectors.joining(CENTER_SECTION_SEPARATOR));
    }

    public AddOrgMemberMappingRes addOrgMemberMapping(Organization organization,
                                                      AddOrgMemberMappingReq addOrgMemberMappingReq, boolean noExceptionOnExistingMapping)
            throws VedantuException {

        // AuthHandler authHandler = AuthHandlerFactory.getInstance().getAuthHandler(organization);
        AddOrgMemberMappingRes res = addMemberMapping(addOrgMemberMappingReq,
                noExceptionOnExistingMapping);
        if (addOrgMemberMappingReq.returnOrgProfileWithCourseInfo) {
            Optional<OrgMember> orgMemberOptional = orgMemberRepo.findById(addOrgMemberMappingReq.targetOrgMemberId);
            OrgMember orgMember = orgMemberOptional.get();
            res.info = getOrgMemberProfileRes(orgMember, true, false).info;
        }
        return res;
    }
    
    public SendForgotPasswordEmailRes sendForgotPasswordMailRes(com.lms.user.vedantu.user.requests.SendForgotPasswordEmailReq sendForgotPasswordEmailReq) throws VedantuException {

        User user = generateForgotPasswordReq(sendForgotPasswordEmailReq.getUsername());

        if (user.authType == AuthType.EXT_AUTH_ORG) {
            throw new VedantuException(VedantuErrorCode.EXTERNAL_AUTH_SUPPORTED);
        }
//TODO generate mail Event;
        boolean generatedEmail = generateSendForgotPasswordEmailEvent(user,
                sendForgotPasswordEmailReq.getOrgId(),sendForgotPasswordEmailReq.callingAppId);

        SendForgotPasswordEmailRes sendForgotPasswordEmailRes = new SendForgotPasswordEmailRes();
        sendForgotPasswordEmailRes.done = generatedEmail;

        return sendForgotPasswordEmailRes;
    }
    private boolean generateSendForgotPasswordEmailEvent(User user, String orgId, String callingAppId) throws VedantuException {
        //TODO need to generate ForgotPasswordEmailEvent
           ForgotPasswordDetails details;
           try {
               details = new ForgotPasswordDetails();
           } catch (ClassNotFoundException e) {
               logger.debug(" Not found email details", e);
               throw new VedantuException(VedantuErrorCode.EMAIL_CAN_NOT_BE_SEND);
           }

           details.user = new UserEmailInfo();
         //  details.user.fromUserExtendedInfo((UserExtendedInfo) user.toExtendedInfo());
           details.user.fromUserExtendedInfo(user);

           details.verificationLink = generatePasswordResetURL(user, orgId,callingAppId);
           details.addRecepient(details.user.getFullName(), user.email);

           generateEventAyscForForgotPassword(user._getStringId(), details, EventType.SEND_INSTANT_EMAIL);
           return true;
       }
    private User generateForgotPasswordReq(String username) throws VedantuException {


        logger.debug("generateForgotPasswordReq username: " + username);

        Optional<User> getuser = userRepo.findByUsername(username);
        if (!getuser.isPresent()) {
            logger.debug("generateForgotPasswordReq user not found for username: " + username);
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        User user=getuser.get();
        if (user.getAuthType() == AuthType.EXT_AUTH_ORG) {
            throw new VedantuException(VedantuErrorCode.EXTERNAL_AUTH_SUPPORTED);
        }

        if (null == user.getForgotPasswordReq()) {
            user.setForgotPasswordReq(new ForgotPasswordReqInfo(UUID.randomUUID().toString()));
            userRepo.save(user);
            logger.debug("generateForgotPasswordReq saved user: " + user);
        } else {
            logger.debug("generateForgotPasswordReq user already has a forgotPasswordReq for user: "
                    + user);
        }
        logger.info("generateForgotPasswordReq user: " + user);
        return user;


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
    private void generateEventAyscForForgotPassword(String userId, ForgotPasswordDetails details, EventType sendInstantEmail) {
        CompletableFuture.runAsync(() -> {
            eventUtil.generateEvent(sendInstantEmail, null, userId, details,
                    details.__getSrcEntity(), UserActionType.EventActionType.ADD, 0);
        });
    }

}
