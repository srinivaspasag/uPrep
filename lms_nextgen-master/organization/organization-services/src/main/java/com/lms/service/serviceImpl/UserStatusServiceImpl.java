package com.lms.service.serviceImpl;

import com.lms.board.model.Board;
import com.lms.board.pojos.test.BoardBasicInfo;
import com.lms.board.repo.BoardRepo;
import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.utils.EncryptionUtils;
import com.lms.common.vedantu.commons.pojos.requests.SecurityCredentials;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.enums.AuthType;
import com.lms.common.vedantu.enums.DeviceType;
import com.lms.enums.OrgMemberProfile;
import com.lms.models.*;
import com.lms.models.device.mgmt.ActivityRecord;
import com.lms.organization.auth.AuthHandler;
import com.lms.organization.auth.VedantuAuthHandler;
import com.lms.pojo.*;
import com.lms.pojo.request.GetOrgMemberProfileReq;
import com.lms.pojo.request.GetUserStatusReq;
import com.lms.pojo.request.RecordActivityReq;
import com.lms.pojo.request.device.mgmt.DeviceLoginReq;
import com.lms.pojo.request.device.mgmt.DeviceLogoutReq;
import com.lms.pojo.request.device.mgmt.GetUserDeviceStatusReq;
import com.lms.pojo.responce.*;
import com.lms.pojo.responce.device.mgmt.DeviceStatusRes;
import com.lms.pojo.responce.device.mgmt.GetUserDeviceStatusesRes;
import com.lms.repository.*;
import com.lms.service.MemberService;
import com.lms.service.UserStatusService;
import com.lms.user.vedantu.user.enums.UserStatus;
import com.lms.user.vedantu.user.model.LoginStatus;
import com.lms.user.vedantu.user.model.User;
import com.lms.user.vedantu.user.pojo.UserExtendedInfo;
import com.lms.user.vedantu.user.repository.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserStatusServiceImpl implements UserStatusService {
    private static final Logger logger = LoggerFactory.getLogger(UserStatusServiceImpl.class);
    @Autowired
    private OrgMemberRepo orgMemberRepo;
    @Autowired
    private ActivityRecordRepo activityRecordRepo;
    @Autowired
    private LoginStatusRepo loginStatusRepo;
    @Autowired
    private OrgProgramRepo orgProgramRepo;
    @Autowired
    private MemberService memberService;
    @Autowired
    private OrgCenterRepo orgCenterRepo;
    @Autowired
    private OrgSectionRepo orgSectionRepo;
    @Autowired
    private BoardRepo boardRepo;
    @Autowired
    private OrganizationRepo organizationRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private MongoTemplate mongoTemplate;
    private static final Map<AuthType, Class<? extends AuthHandler>> authHandlerMap = new HashMap<AuthType, Class<? extends AuthHandler>>();

    @Override
    public VedantuResponse recordActivity(RecordActivityReq request) {
        if (request.getOrgId().isEmpty()) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
        }
        boolean result = checkIfLoggedIn(request.userId, request.deviceId,
                request.deviceType.name());
        if (!result) {
            throw new VedantuException(VedantuErrorCode.ALREADY_LOGGED_OUT);
        }
        addActivity(request.callingAppId, request.callingApp,
                request.callingUserId, request.userId, request.orgId, request.deviceId,
                request.deviceType, request.page, request.userAction, request.entity,
                request.activityTime);

        return new VedantuResponse(new RecordActivityRes(true));
    }

    public boolean checkIfLoggedIn(String userId, String deviceId, String deviceType) {
        Boolean device;
        List<OrgMember> orgMemberList=orgMemberRepo.findByUserIdAndStatusDeviceIdAndStatusDeviceType(userId,deviceId,deviceType);

        if (orgMemberList!=null|| !orgMemberList.isEmpty()) {
            logger.debug("Results:" + orgMemberList);
            return true;
        }
        return false;

//        orgMemberRepo.findByUserId(userId);
//        OrgMember member = orgMemberRepo.findByUserId(userId);
//        if (member == null) {
//            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
//        }
//        if (!deviceId.isEmpty()||deviceId!=null && deviceType!=null||!deviceType.isEmpty()) {
//            List<LoginStatus> statusList = new ArrayList<>();
//            statusList = member.status;
//            if (statusList!=null||!statusList.isEmpty()) {
//                ListIterator<LoginStatus> statusiterator = statusList.listIterator();
//                while (statusiterator.hasNext()) {
//                    if (statusiterator.next().deviceId == deviceId && statusiterator.next().deviceType == deviceType) {
//                        device = true;
//                        break;
//                    }
//                }
//            } else {
//                device = true;
//            }
//
//        }
//        return device = true;
    }

    public ActivityRecord addActivity(String callingAppId, String callingApp, String callingUserId,
                                      String userId, String orgId, String deviceId, DeviceType deviceType, String page,
                                      String action, SrcEntity entity, long time) throws VedantuException {

        ActivityRecord record = new ActivityRecord(callingAppId, callingApp, callingUserId, userId,
                orgId, deviceId, deviceType, page, action, entity);
        if (time != 0) {
            record.timeCreated = time;
        }
        activityRecordRepo.save(record);
        return record;
    }

    @Override
    public VedantuResponse newLogin(DeviceLoginReq loginRequest) {
        if (!loginRequest.callingUserId.equals(loginRequest.userId)) {
            throw new VedantuException(VedantuErrorCode.INVALID_USER_ID);
        }
        LoginStatus status = new LoginStatus();
        status.deviceId = loginRequest.deviceId;
        status.loginTime = System.currentTimeMillis();
        status.expiryTime = loginRequest.expiryTimeOffset;
        status.deviceType = loginRequest.deviceType.name();
        status.status = UserStatus.LOGGED_IN;
        status.callingAppId = loginRequest.callingAppId;
        status.callingApp = loginRequest.callingApp;
        status.callingUserId = loginRequest.callingUserId;

        boolean result = updateLoginStatus(loginRequest.userId,
                status);
        return new VedantuResponse(new DeviceStatusRes(result));
    }


    public boolean updateLoginStatus(String userId, LoginStatus status) throws VedantuException {

        OrgMember orgMember = orgMemberRepo.findByUserId(userId);
        if (orgMember.status != null) {
            if (orgMember.status.size() > 0) {
                Iterator<LoginStatus> statusIterator = orgMember.status.iterator();
                while (statusIterator.hasNext()) {
                    LoginStatus previousStatus = statusIterator.next();
                    if (!previousStatus.deviceId.equals(status.deviceId)) {
                        if (previousStatus.status.equals(UserStatus.LOGGED_IN)) {
                            previousStatus.status = UserStatus.LOGGED_OUT;
                            previousStatus.logoutTime = System.currentTimeMillis();
                        }
                    }
                    orgMemberRepo.save(orgMember);
                }
            }
        }

        orgMember.status.add(status);
        orgMemberRepo.save(orgMember);
        return true;
    }

    @Override
    public VedantuResponse newLogout(DeviceLogoutReq request) {
        if (!request.callingUserId.equals(request.userId)) {
            throw new VedantuException(VedantuErrorCode.INVALID_USER_ID);
        }
        LoginStatus status = new LoginStatus();
        status.deviceId = request.deviceId;

        status.deviceType = request.deviceType.name();
        boolean result = updateLogoutStatus(request.userId, status);
        if (result) {
            loginStatusRepo.save(status);
        }
        return new VedantuResponse(new DeviceStatusRes(result));

    }


    public boolean updateLogoutStatus(String userId, LoginStatus status) throws VedantuException {

        if (!checkIfLoggedIn(userId, status.deviceId, status.deviceType)) {
            throw new VedantuException(VedantuErrorCode.ALREADY_LOGGED_OUT);
        }

        List<OrgMember> orgMemberStatus = getStatuses(userId, status.deviceId, status.deviceType);
        logger.info("Current statuses " + orgMemberStatus);

        OrgMember orgMember = orgMemberRepo.findByUserId(userId);

        if (!orgMemberStatus.isEmpty()) {
            logger.info("After logging out analysing and updating statuses" + status);
            List<LoginStatus> statuses = orgMemberStatus.get(0).status;
            for (LoginStatus currentStatus : statuses) {
                if (currentStatus.deviceId.equals(status.deviceId)) {
                    status.callingApp = currentStatus.callingApp;
                    status.callingAppId = currentStatus.callingAppId;
                    status.callingUserId = currentStatus.callingUserId;
                    status.expiryTime = currentStatus.expiryTime;
                    status.loginTime = currentStatus.loginTime;
                    status.logoutTime = System.currentTimeMillis();
                    status.status = UserStatus.LOGGED_OUT;
                    logger.info("Record will be removed and added to Logins" + status);
                    break;
                }
            }
        }
        orgMember.status.add(status);
        orgMemberRepo.save(orgMember);
        return true;
    }

    public List<OrgMember> getStatuses(String userId, String deviceId, String deviceType) {
        List<OrgMember> members = new ArrayList<>();
        Boolean device;
        OrgMember member = orgMemberRepo.findByUserId(userId);
        if (member == null) {
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        if (!deviceId.isEmpty() && deviceType.isEmpty()) {
            List<LoginStatus> statusList = new ArrayList<>();
            statusList = member.status;
            ListIterator<LoginStatus> statusiterator = statusList.listIterator();
            while (statusiterator.hasNext()) {
                LoginStatus status = statusiterator.next();
                if (status.deviceId == deviceId && statusiterator.next().deviceType == deviceType) {
                    device = true;
                    break;
                }
            }
        }
        if (device = true) {
            members.add(member);
            return members;
        } else {
            return null;
        }
    }

    @Override
    public VedantuResponse checkIfUserExists(DeviceLogoutReq request) {

        logger.debug("Inside check db user in UserStatus");
        if (!request.callingUserId.equals(request.userId)) {
            throw new VedantuException(VedantuErrorCode.INVALID_USER_ID);
        }

        boolean result = checkIfLoggedInDB(request.userId, request.deviceId);

        DeviceStatusRes response = new DeviceStatusRes(result);
        return new VedantuResponse(response);
    }

    public boolean checkIfLoggedInDB(String userId, String deviceId) {

        OrgMember orgMember = orgMemberRepo.findByUserId(userId);
        try {
            if (orgMember.status != null)
                if (orgMember.status.size() > 0) {
                    Iterator<LoginStatus> statusIterator = orgMember.status.iterator();
                    while (statusIterator.hasNext()) {
                        LoginStatus status = statusIterator.next();
                        if (status.deviceId.equals(deviceId)) {
                            if (status.status.equals(UserStatus.LOGGED_IN)) {
                                return true;
                            } else if (status.status.equals(UserStatus.LOGGED_OUT)) {
                                return false;
                            }
                        }
                    }
                }
        }
    catch(NullPointerException e)
    {
            throw  new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        return false;
    }

    // Need to confirm regarding hits
    @Override
    public VedantuResponse getUsers(GetUserStatusReq request) {
        GetUserStatusesRes response = new GetUserStatusesRes();
        AtomicLong hits = new AtomicLong();
        List<String> devices = new ArrayList<String>();
        if (request.deviceType != null && request.deviceType != DeviceType.UNKNOWN) {
            devices.add(request.deviceType.name());
        }
        GetOrgMembersRes orgMembers = getOnlineMembers(request.orgId,
                request.profile, request.programId, request.centerId, request.sectionId,
                request.query, request.start, request.size, hits, devices, request.status);
        if ((orgMembers.list)!=null) {
            for (OrgMemberExtendedInfo orgMember : orgMembers.list) {
                GetUserStatusRes userStatus = new GetUserStatusRes(orgMember);
                if ((orgMember.loginStatus)!=null) {
                    for (LoginStatus status : orgMember.loginStatus) {
                        logger.debug(" Login status " + orgMember.firstName + " status " + status);
                        if (!userStatus.statuses.containsKey(status.deviceType)) {
                            userStatus.statuses.put(status.deviceType, UserStatus.LOGGED_IN);

                        }
                    }

                }
                response.list.add(userStatus);
            }
        }
        response.totalHits = hits.longValue();
        return new VedantuResponse(response);
    }

    public GetOrgMembersRes getOnlineMembers(String orgId, OrgMemberProfile profile,
                                             String programId, String centerId, String sectionId, String queryText, int start,
                                             int size, AtomicLong hits, List<String> deviceTypes,
                                             UserStatus loggedIn) throws VedantuException {

        List<OrgMember> orgMembers = getOnlineUsers(orgId, profile, programId, centerId, sectionId, queryText, start,
                size, hits, deviceTypes, loggedIn);

        GetOrgMembersRes getOrgMembersRes = new GetOrgMembersRes();
        if ((orgMembers)!=null) {
            List<OrgMemberExtendedInfo> orgMemberExtendedInfos = new ArrayList<OrgMemberExtendedInfo>();

            for (OrgMember orgMember : orgMembers) {
                if (null == orgMember) {
                    continue;
                }

                OrgMemberExtendedInfo orgMemberExtendedInfo = (OrgMemberExtendedInfo) orgMember
                        .toExtendedInfo();
                orgMemberExtendedInfos.add(orgMemberExtendedInfo);

                populateProgramHierarchy(orgMember, orgMemberExtendedInfo);

            }

            getOrgMembersRes.list = orgMemberExtendedInfos;
            getOrgMembersRes.totalHits = hits.longValue();
        }
        hits.set(hits.longValue());
        return getOrgMembersRes;
    }

    private void populateProgramHierarchy(OrgMember orgMember,
                                          OrgMemberExtendedInfo orgMemberExtendedInfo) {

        populateProgramHierarchy(orgMember, orgMemberExtendedInfo, false);
    }

    private void populateProgramHierarchy(OrgMember orgMember,
                                          OrgMemberExtendedInfo orgMemberExtendedInfo, boolean ensureCourseInfo) {

        if ((orgMember.mappings)!=null) {

            for (OrgMemberMappingInfo mapping : orgMember.mappings) {
                if (null == mapping) {
                    continue;
                }

                OrgStructureBasicInfo program = getProgramBasicInfo(mapping.getProgramId());
                OrgProgramBasicInfo programInfo = orgMemberExtendedInfo.mappings
                        ._getOrAddProgram(program);

                if (ensureCourseInfo) {
                    mapping.courseIds = programInfo.courseIds;
                }

                OrgStructureBasicInfo progCenter = getCenterBasicInfo(mapping.centerId);
                OrgProgramCenterBasicInfo progCenterInfo = programInfo
                        ._getOrAddProgramCenter(progCenter);

                OrgSection orgSection = orgSectionRepo.findById(mapping.sectionId).get();
                OrgStructureBasicInfo progSection = (OrgStructureBasicInfo) orgSection
                        .toBasicInfo();
                OrgProgramSectionBasicInfo progSectionInfo = progCenterInfo
                        ._getOrAddProgramSection(progSection);
                progSectionInfo.orderId = mapping.orderId;
                progSectionInfo.timeJoined = mapping.timeJoined;
                progSectionInfo.endTime = mapping.endTime;
                if (ensureCourseInfo) {
                    // for now only add desc, if needed we can
                    // progSectionInfo.addSectionExtraInfo(orgSection);
//                    progSectionInfo.desc = StringUtils.defaultString(orgSection.desc,
//                            StringUtils.EMPTY);
                    progSectionInfo.addSectionExtraInfo(orgSection);
                }
                if ((mapping.courseIds)!=null) {
                    for (String courseId : mapping.courseIds) {
                        if ((courseId).isEmpty()) {
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

    private OrgStructureBasicInfo getSectionBasicInfo(String sectionid) {
        Optional<OrgSection> org = orgSectionRepo.findById(sectionid);
        if (!org.isPresent())
            return null;
        OrgStructureBasicInfo orgStructureBasicInfo = new OrgStructureBasicInfo(org.get());
        return orgStructureBasicInfo;
    }

    public List<OrgMember> getOnlineUsers(String orgId, OrgMemberProfile profile, String programId,
                                          String centerId, String sectionId, String queryText, int start, int size,
                                          AtomicLong hits, List<String> deviceTypes,
                                          UserStatus loggedIn) {
        Query dynamicQuery=new Query();
        Criteria criteria1=Criteria.where("orgId").is(orgId);
        dynamicQuery.addCriteria(criteria1);

        if (null != profile)
        {
            Criteria criteria2=Criteria.where("profile").is(profile);
        }
        if ((queryText)!=null) {
            Criteria criteria3 = new Criteria().orOperator(
                    Criteria.where("firstName").regex("^" + queryText.trim()),
                    Criteria.where("lastName").regex("^" + queryText.trim()),
                    Criteria.where("memberId").regex("^" + queryText.trim())
            );
            dynamicQuery.addCriteria(criteria3);
        }
        if ((programId)!=null) {
            Criteria criteria4=Criteria.where("mappings.programId").is(programId);
            dynamicQuery.addCriteria(criteria4);
        }
        if ((centerId)!=null) {
            Criteria criteria5=Criteria.where("mappings.centerId").is(centerId);
            dynamicQuery.addCriteria(criteria5);
        }
        if ((sectionId)!=null) {
            Criteria criteria6=Criteria.where("mappings.sectionId").is(sectionId);
            dynamicQuery.addCriteria(criteria6);
        }
        if (loggedIn == UserStatus.LOGGED_IN && (deviceTypes)!=null)
        {
            Criteria criteria7=Criteria.where("status.deviceType").in(deviceTypes);
            dynamicQuery.addCriteria(criteria7);
        } else if (loggedIn == UserStatus.LOGGED_OUT && (deviceTypes)!=null)
        {
            Criteria criteria7=Criteria.where("status.deviceType").not().in(deviceTypes);
            dynamicQuery.addCriteria(criteria7);
        }
        dynamicQuery.with(Sort.by(Sort.Direction.DESC,"firstName,lastName"));


        List<OrgMember> orgMember =mongoTemplate.find(dynamicQuery,OrgMember.class);
        logger.info("getOrgMembers orgMembers: {" + orgMember + ", " + "}");
//        if (hits != null) {
//            hits.set(dynamicQuery.countAll());
//        }
        return orgMember;
//        if (orgMember != null) {
//            orgMember = orgMemberRepo.findByOrgIdAndProfile(orgId, profile.name());
//        }
//        for (OrgMember orgMember1 : orgMember) {
//            if (orgMember1.getFirstName().startsWith(queryText.trim()) || orgMember1.getLastName().startsWith(queryText.trim()) || orgMember1.getMemberId().startsWith(queryText.trim())) {
//                orgMember.add(orgMember1);
//            }
//        }
//
//        if (!(programId).isEmpty()) {
//            for (OrgMember orgMember1 : orgMember) {
//                for (OrgMemberMappingInfo orgMemberMappingInfo : orgMember1.getMappings()) {
//                    if (orgMemberMappingInfo.programId.equalsIgnoreCase(programId)) {
//                        orgMember.add(orgMember1);
//                        break;
//                    }
//                }
//            }
//        }
//        if (!(centerId).isEmpty()) {
//            for (OrgMember orgMember1 : orgMember) {
//                for (OrgMemberMappingInfo orgMemberMappingInfo : orgMember1.getMappings()) {
//                    if (orgMemberMappingInfo.centerId.equalsIgnoreCase(centerId)) {
//                        orgMember.add(orgMember1);
//                        break;
//                    }
//                }
//            }
//        }
//        if (!(sectionId).isEmpty()) {
//            for (OrgMember orgMember1 : orgMember) {
//                for (OrgMemberMappingInfo orgMemberMappingInfo : orgMember1.getMappings()) {
//                    if (orgMemberMappingInfo.programId.equalsIgnoreCase(sectionId)) {
//                        orgMember.add(orgMember1);
//                        break;
//                    }
//                }
//            }
//        }
//        // findQuery.field("status.deviceId").hasAnyOf(
//        if (loggedIn == UserStatus.LOGGED_IN && !(deviceTypes).isEmpty()) {
//
//            for (OrgMember orgMember1 : orgMember) {
//                for (LoginStatus loginStatus : orgMember1.getStatus()) {
//                    for (String device : deviceTypes) {
//                        if (loginStatus.deviceType.equalsIgnoreCase(device)) {
//                            orgMember.add(orgMember1);
//                            break;
//                        }
//                    }
//                }
//            }
//
//        } else if (loggedIn == UserStatus.LOGGED_OUT && !(deviceTypes).isEmpty()) {
//            for (OrgMember orgMember1 : orgMember) {
//                for (LoginStatus loginStatus : orgMember1.getStatus()) {
//                    for (String device : deviceTypes) {
//                        if (loginStatus.deviceType != device) {
//                            orgMember.add(orgMember1);
//                        }
//                    }
//                }
//            }
//        }
//        logger.info("getOrgMembers orgMembers: {" + orgMember + ", " + "}");
//        if (hits != null) {
//            hits.set(query.countAll());
//        }
//        return orgMember;

    }

    @Override
    public VedantuResponse getUserStatus(GetUserDeviceStatusReq request) {
        if (request == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        GetUserDeviceStatusesRes response = new GetUserDeviceStatusesRes();
        AtomicLong totalHits = new AtomicLong();
        List<LoginStatus> loginStatuses = null;
        if (request.status == UserStatus.LOGGED_OUT) {
            loginStatuses = getAllStatus(request.targetUserId, null,
                    request.deviceType, request.status, request.start, request.size, totalHits);
        } else {
            GetOrgMemberProfileReq orgMemberRequest = new GetOrgMemberProfileReq();
            orgMemberRequest.targetUserId = request.targetUserId;
            orgMemberRequest.orgId = request.orgId;
            loginStatuses = new ArrayList<LoginStatus>();
            GetOrgMemberProfileRes orgMember = getOrgMember(orgMemberRequest);

            List<LoginStatus> status = orgMember.info.loginStatus;
            logger.debug("statuses " + status);
            // TODO solve using slice
            if ((status)!=null) {
                for (int i = request.start; i < (request.start + request.size) && i < status.size(); i++)
                {
                    LoginStatus loginStatus = new LoginStatus(request.targetUserId,
                            status.get(i).deviceId, DeviceType.valueOfKey(status.get(i).deviceType));
                    loginStatus.expiryTime = status.get(i).expiryTime;
                    loginStatus.status = UserStatus.LOGGED_IN;
                    loginStatus.expiryTime = status.get(i).expiryTime;
                    loginStatus.loginTime = status.get(i).loginTime;
                    loginStatus.logoutTime = status.get(i).logoutTime;
                    loginStatus.callingApp = status.get(i).callingApp;
                    loginStatus.callingAppId = status.get(i).callingAppId;
                    loginStatuses.add(loginStatus);
                }
                totalHits.set(status.size());
            }
        }

        response.totalHits = totalHits.longValue();
        if (!(loginStatuses).isEmpty()) {
            for (LoginStatus loginStatus : loginStatuses) {
                // TODO optimised by selecting last activity for devices
                ActivityRecord record = getLastActivity(
                        request.targetUserId, request.orgId, loginStatus.deviceId,
                        loginStatus.loginTime, loginStatus.deviceType);

                DeviceInfo info = new DeviceInfo(loginStatus);
                if (record != null) {
                    info.lastActivityTime = record.timeCreated;
                    info.page = record.page;
                    info.userAction = record.action;
                    info.entity = record.entity;
                    info.callingApp = record.callingApp;
                    info.callingAppId = record.callingAppId;
                    info.callingUserId = record.callingUserId;
                }
                response.list.add(info);
            }
        }

        return new  VedantuResponse(response);

    }

    public List<LoginStatus> getAllStatus(String userId, String deviceId, DeviceType deviceType,
                                          UserStatus loginStatus, int start, int size, AtomicLong totalHits)
            throws VedantuException {

        List<String> userIds = new ArrayList<String>();
        userIds.add(userId);

        return getStatus(userIds, deviceId, deviceType, loginStatus, start, size, totalHits);

    }

    public List<LoginStatus> getStatus(List<String> userIds, String deviceId,
                                       DeviceType deviceType, UserStatus loginStatus, int start, int size,
                                       AtomicLong totalHits) throws VedantuException {

        List<LoginStatus> loginStatuses = null;
        if (!(deviceId).isEmpty() && deviceType != null && deviceType != DeviceType.UNKNOWN && loginStatus != null && loginStatus != UserStatus.UNKNOWN) {
            loginStatuses = loginStatusRepo.findAllByUserIdAndDeviceIdAndDeviceTypeAndStatus(userIds, deviceId, deviceType.name(), loginStatus.name());
        }
//        totalHits.setValue(statusQuery.countAll());
        return loginStatuses;

    }

    public GetOrgMemberProfileRes getOrgMember(GetOrgMemberProfileReq getOrgMemberProfileReq)
            throws VedantuException {

        updateOrgMemberExpiredMappings(getOrgMemberProfileReq.orgId,
                getOrgMemberProfileReq.targetUserId);
        OrgMember orgMember = orgMemberRepo.findByOrgIdAndUserId(getOrgMemberProfileReq.orgId,
                getOrgMemberProfileReq.targetUserId);
        if (null == orgMember) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }

        GetOrgMemberProfileRes getOrgMemberProfileRes = getOrgMemberProfileRes(orgMember,
                getOrgMemberProfileReq.ensureCourseInfo, getOrgMemberProfileReq.getKey);

        return getOrgMemberProfileRes;
    }

    public void updateOrgMemberExpiredMappings(String orgId, String userId) {
        OrgMember member = orgMemberRepo.findByOrgIdAndUserId(orgId, userId);
        if (null == member) {
            logger.error("orgMember not found");
            return;
        }
        logger.debug("Updating expired mappings for member with userId: " + userId);
        boolean modified = false;
        if (member.expiredMappings == null) {
            member.expiredMappings = new ArrayList<OrgMemberMappingInfo>();
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

    private GetOrgMemberProfileRes getOrgMemberProfileRes(OrgMember orgMember,
                                                          boolean ensureCourseInfo, boolean addKey) throws VedantuException {

        OrgMemberExtendedInfo orgMemberExtendedInfo = (OrgMemberExtendedInfo) orgMember
                .toExtendedInfo();
        populateUserPublicProfileDetails(orgMember, orgMemberExtendedInfo);

        populateProgramHierarchy(orgMember, orgMemberExtendedInfo, ensureCourseInfo);

        GetOrgMemberProfileRes getOrgMemberProfileRes = new GetOrgMemberProfileRes();
        Organization org = organizationRepo.findById(orgMember.orgId).get();
        getOrgMemberProfileRes.info = orgMemberExtendedInfo;
        getOrgMemberProfileRes.doubtsForumMode = org.doubtsForumMode;
        getOrgMemberProfileRes.showClassroomConnect = org.showClassroomConnect;
        if (addKey) {
            getOrgMemberProfileRes.key = getPrivateKey(orgMember.userId);
        }
        return getOrgMemberProfileRes;
    }

    public String getPrivateKey(String userId) throws VedantuException {
        Base64.Encoder encoder = Base64.getEncoder();
        User user = userRepo.findById(userId).get();
        if (user == null) {
            return null;
        }
        SecurityCredentials credentials = user.credentials;
        if (credentials == null) {
            credentials = setCredentials(user);
        }
        // DatatypeConverter.printHexBinary(credentials.getPrivateKey())
        return encoder.encode(credentials.getPrivateKey()).toString();
    }

    private synchronized SecurityCredentials setCredentials(User user)
            throws VedantuException {

        if (user.credentials != null) {
            return user.credentials;

        }
        user.credentials = EncryptionUtils.generateKeys();
        userRepo.save(user);
        return user.credentials;
    }

    private UserExtendedInfo getExtendedInfo(String userId) {
        Optional<User> user = userRepo.findById(userId);
        if (!user.isPresent())
            return null;
        UserExtendedInfo extendedInfo = new UserExtendedInfo(user.get());
        return extendedInfo;
    }

    private void populateUserPublicProfileDetails(OrgMember orgMember,
                                                  OrgMemberExtendedInfo orgMemberExtendedInfo) throws VedantuException {

        UserExtendedInfo userExtendedInfo = getExtendedInfo(orgMember.userId);
        if (null == userExtendedInfo) {
            logger.debug("populateUserPublicProfileDetails no userExtendedInfo found for userId: "
                    + orgMember.userId);
            return;
        }
        final String username = userExtendedInfo.username;
        final String verifiedEmail = userExtendedInfo.isEmailVerified ? userExtendedInfo.email
                : "";
        final boolean isUsernameOrgSpecific =
                username.equalsIgnoreCase(
                        getAuthHandler(orgMember.orgId)
                                .getMemberUsername(orgMember.orgId, orgMember.memberId));
        orgMemberExtendedInfo.setUserPublicProfileDetails(username, verifiedEmail,
                isUsernameOrgSpecific);

        // if the no joinedTime is populated in the org member mapping so update
        // it as the time of
        // user creation
        boolean updatedMapping = false;

        if (orgMember.mappings != null) {
            for (OrgMemberMappingInfo mapping : orgMember.mappings) {
                if (mapping.timeJoined < 1) {
                    mapping.timeJoined = orgMember.timeCreated;
                    updatedMapping = true;
                }
            }
        }

        if (updatedMapping) {
            orgMemberRepo.save(orgMember);
        }

    }
    public ActivityRecord getLastActivity(String userId, String orgId, String deviceId,
                                          long loginTime, String deviceType) throws VedantuException {

        Query query = new Query();
        query.addCriteria(
                new Criteria().andOperator(
                        Criteria.where("callingUserId").is(userId),
                        Criteria.where("userId").is(userId),
                        Criteria.where("orgId").is(orgId),
                        Criteria.where("deviceId").is(deviceId),
                        Criteria.where("deviceType").is(deviceType),
                        Criteria.where("timeCreated").is(loginTime)));
        query.with(Sort.by(Sort.Direction.DESC,"timeCreated"));
        List<ActivityRecord> activityRecords= mongoTemplate.find(query,ActivityRecord.class);
//        activityRecordRepo.findAllByCallingUserIdAndUserIdAndOrgIdAndDeviceIdAndDeviceTypeAndTimeCreatedGreaterThan(userId,userId,orgId,deviceId,deviceType,loginTime)
        // this will only allow to see activits doesn by himself; not on the behalf of other users
        if (!(activityRecords).isEmpty()) {
            return activityRecords.get(0);
        }
        return null;
    }
    public AuthHandler getAuthHandler(String orgId) throws VedantuException {

        Optional<Organization> organization = organizationRepo.findById(orgId.trim());
        if (!organization.isPresent()) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND,
                    "no org found with id: " + orgId);
        }
        AuthType authType = organization.get().getAuthType() == null ? AuthType.VEDANTU : organization.get().getAuthType();

        try {
            return authHandlerMap.get(authType).getConstructor(Organization.class)
                    .newInstance(organization.get());
        } catch (Exception e) {
            return new VedantuAuthHandler(organization.get());
        }

    }
}