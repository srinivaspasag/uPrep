package com.vedantu.organization.managers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.mutable.MutableLong;
import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.DeviceType;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.organization.daos.OrgMemberDAO;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.organization.daos.device.mgmt.ActivityRecordDAO;
import com.vedantu.organization.models.OrgMember;
import com.vedantu.organization.models.device.mgmt.ActivityRecord;
import com.vedantu.organization.pojos.OrgMemberExtendedInfo;
import com.vedantu.organization.pojos.device.mgmt.DeviceInfo;
import com.vedantu.organization.pojos.device.mgmt.GetUserStatusRes;
import com.vedantu.organization.pojos.requests.device.mgmt.DeviceLoginReq;
import com.vedantu.organization.pojos.requests.device.mgmt.DeviceLogoutReq;
import com.vedantu.organization.pojos.requests.device.mgmt.DeviceStatusRes;
import com.vedantu.organization.pojos.requests.device.mgmt.GetUserDeviceStatusReq;
import com.vedantu.organization.pojos.requests.device.mgmt.GetUserStatusReq;
import com.vedantu.organization.pojos.requests.device.mgmt.RecordActivityReq;
import com.vedantu.organization.pojos.requests.members.GetOrgMemberProfileReq;
import com.vedantu.organization.pojos.requests.organizations.CheckIfSuperAdminReq;
import com.vedantu.organization.pojos.requests.organizations.GetLatestActivityReq;
import com.vedantu.organization.pojos.responses.device.mgmt.GetUserDeviceStatusesRes;
import com.vedantu.organization.pojos.responses.device.mgmt.GetUserStatusesRes;
import com.vedantu.organization.pojos.responses.device.mgmt.RecordActivityRes;
import com.vedantu.organization.pojos.responses.members.GetOrgMemberProfileRes;
import com.vedantu.organization.pojos.responses.members.GetOrgMembersRes;
import com.vedantu.organization.pojos.responses.organizations.CheckIfSuperAdminRes;
import com.vedantu.organization.pojos.responses.organizations.GetLatestActivityRes;
import com.vedantu.organization.pojos.responses.organizations.GetLatestActivityResponseList;
import com.vedantu.user.daos.LoginStatusDAO;
import com.vedantu.user.enums.UserStatus;
import com.vedantu.user.models.LoginStatus;
import com.vedantu.content.commons.daos.AssignmentDAO;
import com.vedantu.content.commons.daos.DocumentDAO;
import com.vedantu.content.commons.daos.ModuleDAO;
import com.vedantu.content.commons.daos.TestDAO;
import com.vedantu.content.commons.daos.VideoDAO;
import com.vedantu.content.models.Document;
import com.vedantu.content.models.Module;
import com.vedantu.content.models.Video;
import com.vedantu.content.models.tests.Assignment;
import com.vedantu.content.models.tests.Test;

public class UserStatusManager {

    private static final int        REQUEST_COUNT_MULTIPLIER = 5;

    public static UserStatusManager INSTANCE                 = new UserStatusManager();

    private final static ALogger    LOGGER                   = Logger.of(UserStatusManager.class);

    private UserStatusManager() {

    }

    public DeviceStatusRes newLogin(DeviceLoginReq loginRequest) throws VedantuException {

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

        boolean result = OrgMemberDAO.INSTANCE.updateLoginStatus(loginRequest.userId,
                status);
        return new DeviceStatusRes(result);

    }

    public DeviceStatusRes newLogout(DeviceLogoutReq request) throws VedantuException {

        if (!request.callingUserId.equals(request.userId)) {
            throw new VedantuException(VedantuErrorCode.INVALID_USER_ID);
        }
        LoginStatus status = new LoginStatus();
        status.deviceId = request.deviceId;

        status.deviceType = request.deviceType.name();
        boolean result = OrgMemberDAO.INSTANCE.updateLogoutStatus(request.userId, status);
        if (result) {
            LoginStatusDAO.INSTANCE.save(status);
        }
        return new DeviceStatusRes(result);

    }


	public DeviceStatusRes checkUserInDB(DeviceLogoutReq request) throws VedantuException {

        Logger.debug("Inside check db user in UserStatus");
        if (!request.callingUserId.equals(request.userId)) {
            throw new VedantuException(VedantuErrorCode.INVALID_USER_ID);
        }
        
        boolean result = OrgMemberDAO.INSTANCE.checkIfLoggedInDB(request.userId,request.deviceId);
        
        DeviceStatusRes response = new DeviceStatusRes(result);
        return response;
	

    }
	
	public GetLatestActivityResponseList<GetLatestActivityRes> getStudentActivity(GetLatestActivityReq getLatestActivityReq)throws VedantuException 
	{
		
		List<ActivityRecord> activityRecords=ActivityRecordDAO.INSTANCE.getActivities
				(getLatestActivityReq.targetUserId,getLatestActivityReq.orgId,getLatestActivityReq.offset,getLatestActivityReq.limit);
		LOGGER.info("activityRecords size : "+activityRecords.size());
		GetLatestActivityResponseList<GetLatestActivityRes> getLatestActivityResList = new GetLatestActivityResponseList<GetLatestActivityRes>();
		getLatestActivityResList.totalHits=activityRecords.size();
		for(ActivityRecord activityRecord:activityRecords){
			if(activityRecord.entity!=null &&! activityRecord.entity.type.name().equalsIgnoreCase("USER") ){
				LOGGER.info("entity : "+activityRecord.entity.id);
				GetLatestActivityRes getLatestActivityRes=new GetLatestActivityRes(activityRecord.userId
						,activityRecord.orgId,activityRecord.deviceId,
						activityRecord.deviceType,activityRecord.page,activityRecord.action,activityRecord.timeCreated);
//				if(EntityTypeDAOFactory.INSTANCE.get(activityRecord.entity.type)!=null){
//					VedantuBasicDAO vedantuBasicDAO=EntityTypeDAOFactory.INSTANCE.get(activityRecord.entity.type);
//					String name =vedantuBasicDAO.getById(activityRecord.entity.id).toExtendedInfo().name;
//					LOGGER.info("name : "+name);
//				}
				if(activityRecord.entity.type.name().equals("TEST")){
					Test test=TestDAO.INSTANCE.getById(activityRecord.entity.id);
					if(test!=null){
						LOGGER.info("name : "+test.name);
						getLatestActivityRes.entityName=test.name;
					}
				}
				else if(activityRecord.entity.type.name().equals("VIDEO")){
					Video video=VideoDAO.INSTANCE.getById(activityRecord.entity.id);
					if(video!=null){
						LOGGER.info("name : "+video.name);
						getLatestActivityRes.entityName=video.name;
					}	
				}
				else if(activityRecord.entity.type.name().equals("DOCUMENT")){
					Document document=DocumentDAO.INSTANCE.getById(activityRecord.entity.id);
					if(document!=null){
						LOGGER.info("name : "+document.name);
						getLatestActivityRes.entityName=document.name;
					}
				}
				else if(activityRecord.entity.type.name().equals("ASSIGNMENT")){
					Assignment assignment=AssignmentDAO.INSTANCE.getById(activityRecord.entity.id);
					if(assignment!=null){
						LOGGER.info("name : "+assignment.name);
						getLatestActivityRes.entityName=assignment.name;
					}
				}
				else if(activityRecord.entity.type.name().equals("MODLE")){
					Module module=ModuleDAO.INSTANCE.getById(activityRecord.entity.id);
					if(module!=null){
						LOGGER.info("name : "+module.name);
						getLatestActivityRes.entityName=module.name;
					}
				}
				else{
					getLatestActivityRes.entityName="N.A";
				}
				getLatestActivityRes.entityid=activityRecord.entity.id;
				getLatestActivityResList.list.add(getLatestActivityRes);
				
			}else{
				
				GetLatestActivityRes getLatestActivityRes=new GetLatestActivityRes(activityRecord.userId
						,activityRecord.orgId,activityRecord.deviceId,activityRecord.deviceType,
						activityRecord.page,activityRecord.action,activityRecord.timeCreated);
				getLatestActivityRes.entityName="N.A";
				getLatestActivityRes.entityid="N.A";
				getLatestActivityResList.list.add(getLatestActivityRes);
				LOGGER.info("page : "+activityRecord.page);
			}
		}
		OrgMember orgMember=OrgMemberDAO.INSTANCE.getByUserId(getLatestActivityReq.targetUserId);
		if(orgMember!=null){
			getLatestActivityResList.studentName=orgMember.firstName+" "+orgMember.lastName;
			getLatestActivityResList.memberId=orgMember.memberId;
		}
		LOGGER.info("enddddd : "+getLatestActivityResList);
		return getLatestActivityResList;
		
	}


    //
    public GetUserStatusesRes getUsers(GetUserStatusReq request) throws VedantuException {

        GetUserStatusesRes response = new GetUserStatusesRes();
        MutableLong hits = new MutableLong();

        List<String> devices = new ArrayList<String>();
        if (request.deviceType != null && request.deviceType != DeviceType.UNKNOWN) {
            devices.add(request.deviceType.name());
        }

        GetOrgMembersRes orgMembers = OrgMemberManager.getOnlineMembers(request.orgId,
                request.profile, request.programId, request.centerId, request.sectionId,
                request.query, request.start, request.size, hits, devices, request.status);

        if (CollectionUtils.isNotEmpty(orgMembers.list)) {
            for (OrgMemberExtendedInfo orgMember : orgMembers.list) {
                GetUserStatusRes userStatus = new GetUserStatusRes(orgMember);
                if (CollectionUtils.isNotEmpty(orgMember.loginStatus)) {
                    for (LoginStatus status : orgMember.loginStatus) {
                        LOGGER.debug(" Login status " + orgMember.firstName + " status " + status);
                        if (!userStatus.statuses.containsKey(status.deviceType)) {
                            userStatus.statuses.put(status.deviceType, UserStatus.LOGGED_IN);

                        }
                    }

                }
                response.list.add(userStatus);
            }
        }
        response.totalHits = hits.longValue();
        return response;

    }

    
    public static CheckIfSuperAdminRes checkIfSuperAdmin(CheckIfSuperAdminReq request)
            throws VedantuException
    {
        LOGGER.debug("..... Inside checkIfSuperAdmin function....." + request.orgId + " "+ request.userId);
        boolean result = OrganizationDAO.INSTANCE.checkIfSuperAdmin(request.orgId, request.userId);
        CheckIfSuperAdminRes response = new CheckIfSuperAdminRes();
        response.isSuperAdmin = result;
        return response;
    }
    
    public GetUserDeviceStatusesRes getUserStatus(GetUserDeviceStatusReq request)
            throws VedantuException {

        GetUserDeviceStatusesRes response = new GetUserDeviceStatusesRes();
        MutableLong totalHits = new MutableLong();
        List<LoginStatus> loginStatuses = null;

        if (request.status == UserStatus.LOGGED_OUT) {
            loginStatuses = LoginStatusDAO.INSTANCE.getAllStatus(request.targetUserId, null,
                    request.deviceType, request.status, request.start, request.size, totalHits);

        } else {
            GetOrgMemberProfileReq orgMemberRequest = new GetOrgMemberProfileReq();
            orgMemberRequest.targetUserId = request.targetUserId;
            orgMemberRequest.orgId = request.orgId;
            loginStatuses = new ArrayList<LoginStatus>();
            GetOrgMemberProfileRes orgMember = OrgMemberManager.getOrgMember(orgMemberRequest);

            List<LoginStatus> status = orgMember.info.loginStatus;
            LOGGER.debug("statuses " + status);
            // TODO solve using slice
            if (CollectionUtils.isNotEmpty(status)) {
                for (int i = request.start; i < ( request.start + request.size ) && i < status.size(); i++) {

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
                totalHits.setValue(status.size());
            }
        }

        response.totalHits = totalHits.longValue();
        if (CollectionUtils.isNotEmpty(loginStatuses)) {
            for (LoginStatus loginStatus : loginStatuses) {
                // TODO optimised by selecting last activity for devices
                ActivityRecord record = ActivityRecordDAO.INSTANCE.getLastActivity(
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

        return response;

    }

    public RecordActivityRes recordActivity(RecordActivityReq request) throws VedantuException {

        if (StringUtils.isEmpty(request.orgId)) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
        }
        boolean result = OrgMemberDAO.INSTANCE.checkIfLoggedIn(request.userId, request.deviceId,
                request.deviceType.name());
        if (!result) {
            throw new VedantuException(VedantuErrorCode.ALREADY_LOGGED_OUT);
        }

        ActivityRecordDAO.INSTANCE.addActivity(request.callingAppId, request.callingApp,
                request.callingUserId, request.userId, request.orgId, request.deviceId,
                request.deviceType, request.page, request.userAction, request.entity,
                request.activityTime);

        return new RecordActivityRes(true);

    }

}
