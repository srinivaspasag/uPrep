package com.vedantu.organization.managers;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.billing.dao.OrderDAO;
import com.vedantu.billing.dao.SaleDetailsDAO;
import com.vedantu.billing.enums.OrderState;
import com.vedantu.billing.models.Order;
import com.vedantu.billing.models.SaleDetails;
import com.vedantu.billing.pojos.SaleDetailsInfo;
import com.vedantu.board.daos.BoardDAO;
import com.vedantu.board.pojos.BoardBasicInfo;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.daos.CounterDAO;
import com.vedantu.commons.daos.GranteeOrgProgramDAO;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.entity.storage.ImageSize;
import com.vedantu.commons.entity.storage.StorageResult;
import com.vedantu.commons.entity.storage.UserProfilePicEntityFileStorage;
import com.vedantu.commons.enums.AuthType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.fs.exception.FileStoreException;
import com.vedantu.commons.fs.handlers.FileSystemFactory;
import com.vedantu.commons.fs.handlers.LocalFileSystemHandler;
import com.vedantu.commons.pojos.Interval;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.commons.utils.ImageFilter;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.commons.utils.VedantuStringUtils;
import com.vedantu.commons.utils.image.ImageGenerator;
import com.vedantu.content.commons.daos.TeacherAnalyticsDAO;
import com.vedantu.content.models.TeacherAnalytics;
import com.vedantu.content.search.details.AbstractFileModelIndexSearchDetails;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuDBResult;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.mongo.models.GranteeOrgProgram;
import com.vedantu.organization.daos.*;
import com.vedantu.organization.enums.AccessScope;
import com.vedantu.organization.enums.OrgMappingBulkOperationType;
import com.vedantu.organization.enums.OrgMemberProfile;
import com.vedantu.organization.enums.RevenueModel;
import com.vedantu.organization.models.*;
import com.vedantu.organization.parsers.StudentsXLParser;
import com.vedantu.organization.pojos.*;
import com.vedantu.organization.pojos.requests.GetAllUserDataReq;
import com.vedantu.organization.pojos.requests.GetCountOfStudentsReq;
import com.vedantu.organization.pojos.requests.members.*;
import com.vedantu.organization.pojos.requests.members.SendForgotPasswordEmailReq;
import com.vedantu.organization.pojos.requests.members.UnsetEmailReq;
import com.vedantu.organization.pojos.requests.members.UploadProfilePicReq;
import com.vedantu.organization.pojos.requests.organizations.SmsGatewayInfo;
import com.vedantu.organization.pojos.requests.organizations.UserActivationUpdateReq;
import com.vedantu.organization.pojos.responses.GetAllUserDataRes;
import com.vedantu.organization.pojos.responses.GetCountOfStudentsRes;
import com.vedantu.organization.pojos.responses.members.*;
import com.vedantu.organization.pojos.responses.members.UnsetEmailRes;
import com.vedantu.organization.pojos.responses.organizations.UserActivationRes;
import com.vedantu.organizations.auth.AuthHandler;
import com.vedantu.organizations.auth.AuthHandlerFactory;
import com.vedantu.user.daos.TestUserDAO;
import com.vedantu.user.daos.UserDAO;
import com.vedantu.user.daos.UserSaltDAO;
import com.vedantu.user.enums.UserStatus;
import com.vedantu.user.event.details.SendEmailToStudentsDetails;
import com.vedantu.user.managers.AbstractVedantuEventManager;
import com.vedantu.user.managers.UserManager;
import com.vedantu.user.models.TestUser;
import com.vedantu.user.models.User;
import com.vedantu.user.models.UserSalt;
import com.vedantu.user.pojos.UserExtendedInfo;
import com.vedantu.user.pojos.requests.*;
import com.vedantu.user.pojos.responses.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableLong;
import org.joda.time.DateTime;
import org.joda.time.Days;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class OrgMemberManager extends AbstractVedantuEventManager {

    private static final ALogger LOGGER                              = Logger.of(OrgMemberManager.class);

    private static final int     DEFAULT_SIZE_STUDENT_BULK_OPERATION = 50;
    private static final int     EMAILS_BATCH_SIZE                   = 100;
    private static final long    FREEZE_TIME                         = 600000;
    // OTP Configurations
    private static final String  SMSCOUNTRYURL                       = Play.application()
                                                                             .configuration()
                                                                             .getString("smscountry.url");
    private static final Integer OTPSize                             = Play.application()
                                                                             .configuration()
                                                                             .getInt("otp.size");
    private static final String  USER                                = Play.application()
                                                                             .configuration()
                                                                             .getString("smscountry.user");
    private static final String  PASSWD                              = Play.application()
                                                                             .configuration()
                                                                             .getString("smscountry.passwd");
    private static final String  SID                                 = Play.application()
                                                                             .configuration()
                                                                             .getString("smscountry.sid");
    private static final String  MTYPE                               = Play.application()
                                                                             .configuration()
                                                                             .getString("smscountry.mtype");
    private static final String  DR                                  = Play.application()
                                                                             .configuration()
                                                                             .getString("smscountry.dr");

    public static UserAuthRes authenticateOrgMember(MemberAuthReq memberAuthReq)
            throws VedantuException {

        AuthHandler authHandler = AuthHandlerFactory.getInstance().getAuthHandler(
                memberAuthReq.orgId);

        UserAuthRes userAuthRes = authHandler.authenticate(memberAuthReq.getMemberId(),
                memberAuthReq.password);
        return userAuthRes;
    }

    public static UserAuthRes authenticateOTPMember(UserAuthReq userAuthReq)
            throws VedantuException {

        UserAuthRes userAuthRes  = null;
        User user = OrgMemberDAO.INSTANCE.getUsersIdByContact(userAuthReq.contactNumber,userAuthReq.countryCode);
        userAuthRes = UserManager.getAuthResFromUser(user);
        return userAuthRes;
    }

    public static List<Interval> getMemberActivationPeriods(String orgId, String userId,
            Interval interval) { // ((y1<x2)&&(x1<y2)), where (y1,y2) is period
                                 // to be passed

        List<Interval> intervals = new ArrayList<Interval>();
        intervals = UserStateLogDAO.INSTANCE.getMemberActivationPeriods(orgId, userId, interval);
        Interval currentInterval = OrgMemberDAO.INSTANCE.getMemberCurrentActivationPeriod(orgId,
                userId);
        if (interval.getFrom() < currentInterval.getTill()
                && currentInterval.getFrom() < interval.getTill()) {
            intervals.add(currentInterval);
        }
        return intervals;
    }

    public static MemberActivationPeriodsRes getMemberActivationPeriods(
            MemberActivationPeriodsReq memberActivationPeriodsReq) throws VedantuException {

        MemberActivationPeriodsRes memberActivationPeriodsRes = new MemberActivationPeriodsRes();
        try {
            OrgMember orgMember = OrgMemberDAO.INSTANCE.getMemberByUserId(
                    memberActivationPeriodsReq.orgId, memberActivationPeriodsReq.userId);
            if (null == orgMember) {
                throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
            }
            Interval interval = new Interval();
            interval.setFrom(memberActivationPeriodsReq.from);
            interval.setTill(memberActivationPeriodsReq.till);
            memberActivationPeriodsRes.intervals = getMemberActivationPeriods(
                    memberActivationPeriodsReq.orgId, memberActivationPeriodsReq.userId, interval);
        } catch (Exception exception) {
            LOGGER.debug("Could not record state changes", exception);
        }
        return memberActivationPeriodsRes;
    }

    // // If request for activateFrom and activateTill comes as -2, this
    // corresponds to
    // // activate or deactivate now. If request for activateFrom and
    // activateTill comes as -1
    // // this means that time need not to be considered.
    // // For example if the user wants to activate now request from the UI will
    // be (-2, -1)

    public static UserActivationRes recordChange(UserActivationUpdateReq userActivationRequest)
            throws VedantuException {

        UserActivationRes userActivationRes = new UserActivationRes();

        try {
            long currentTime = new Date().getTime();
            OrgMember orgMember = OrgMemberDAO.INSTANCE.getMemberByUserId(
                    userActivationRequest.orgId, userActivationRequest.targetUserId);

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
                    userActivationRes.done = UserStateLogDAO.INSTANCE.recordChange(
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
                    LOGGER.trace("Value inserted in UserStateLog");
                    userActivationRes.done = UserStateLogDAO.INSTANCE.recordChange(
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
            changeList.add("interval");
            OrgMemberDAO.INSTANCE.updateModel(orgMember, changeList);
            userActivationRes.done = true;
        } catch (Exception exception) {
            LOGGER.debug("Could not record state changes", exception);
            userActivationRes.done = false;
        }

        return userActivationRes;
    }

    public static AddOrgMemberRes addOrgMember(AddOrgMemberReq addOrgMemberReq)
            throws VedantuException {

        AuthHandler authHandler = AuthHandlerFactory.getInstance().getAuthHandler(
                addOrgMemberReq.orgId);

        boolean isMemberIdSysGenerated = authHandler.authType != AuthType.EXT_AUTH_ORG
                && StringUtils.isEmpty(addOrgMemberReq.getTargetMemberId())
                && (StringUtils.isNotEmpty(addOrgMemberReq.getEmail()) || StringUtils.isNotEmpty(addOrgMemberReq.contactNumber));
        if (isMemberIdSysGenerated) {
            addOrgMemberReq.setTargetMemberId(getNextOrgMemberId(addOrgMemberReq.orgId));
            if(addOrgMemberReq.isOTPsignup){
                addOrgMemberReq.usePhoneAsUsername = true;
                addOrgMemberReq.useEmailAsUsername = false;
            }else{
                addOrgMemberReq.usePhoneAsUsername = false;
                addOrgMemberReq.useEmailAsUsername = true;
            }
        }

        return addOrgMember(addOrgMemberReq, isMemberIdSysGenerated, authHandler);
    }

    /**
     *
     * @param addOrgMemberReq
     * @param isMemberIdSysGenerated
     * @return
     * @throws VedantuException
     */
    private static AddOrgMemberRes addOrgMember(AddOrgMemberReq addOrgMemberReq,
            boolean isMemberIdSysGenerated, AuthHandler authHandler) throws VedantuException {

        AuthHandler handler = null != authHandler ? authHandler : AuthHandlerFactory.getInstance()
                .getAuthHandler(addOrgMemberReq.orgId);

        return handler.addMember(addOrgMemberReq, isMemberIdSysGenerated);
    }

    /**
     *
     * @param organization
     * @param addOrgMemberReq
     * @param isMemberIdSysGenerated
     * @return
     * @throws VedantuException
     *             this method will be used for bulk uploading
     */
    public static AddOrgMemberRes addOrgMember(Organization organization,
            AddOrgMemberReq addOrgMemberReq, boolean isMemberIdSysGenerated)
            throws VedantuException {

        AuthHandler handler = AuthHandlerFactory.getInstance().getAuthHandler(organization);

        return handler.addMember(addOrgMemberReq, isMemberIdSysGenerated);
    }

    private static void setUpdateList(Set<String> updateList, UpdateOrgMemberReq request)
            throws VedantuException {

        if (CollectionUtils.isNotEmpty(request.updateList)) {
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
                } else if (key.equals(UpdateOrgMemberReq.MOTHER + FileUtils.SEPARATOR_DOT
                        + MemberParentInfo.FIELD_NAME)) {
                    updateList.add(OrgMember.FIELD_MOTHER + FileUtils.SEPARATOR_DOT
                            + MemberParentInfo.FIELD_NAME);
                } else if (key.equals(UpdateOrgMemberReq.FATHER + FileUtils.SEPARATOR_DOT
                        + MemberParentInfo.FIELD_NAME)) {
                    updateList.add(OrgMember.FIELD_FATHER + FileUtils.SEPARATOR_DOT
                            + MemberParentInfo.FIELD_NAME);
                } else if (key.equals(UpdateOrgMemberReq.GUARDIAN + FileUtils.SEPARATOR_DOT
                        + MemberParentInfo.FIELD_NAME)) {
                    updateList.add(OrgMember.FIELD_GUARDIAN + FileUtils.SEPARATOR_DOT
                            + MemberParentInfo.FIELD_NAME);
                }

                else if (key.equals(UpdateOrgMemberReq.MOTHER + FileUtils.SEPARATOR_DOT
                        + MemberParentInfo.FIELD_EMAIL)) {
                    updateList.add(OrgMember.FIELD_MOTHER + FileUtils.SEPARATOR_DOT
                            + MemberParentInfo.FIELD_EMAIL);
                } else if (key.equals(UpdateOrgMemberReq.FATHER + FileUtils.SEPARATOR_DOT
                        + MemberParentInfo.FIELD_EMAIL)) {
                    updateList.add(OrgMember.FIELD_FATHER + FileUtils.SEPARATOR_DOT
                            + MemberParentInfo.FIELD_EMAIL);
                } else if (key.equals(UpdateOrgMemberReq.GUARDIAN + FileUtils.SEPARATOR_DOT
                        + MemberParentInfo.FIELD_EMAIL)) {
                    updateList.add(OrgMember.FIELD_GUARDIAN + FileUtils.SEPARATOR_DOT
                            + MemberParentInfo.FIELD_EMAIL);
                } else if (key.equals(UpdateOrgMemberReq.MOTHER + FileUtils.SEPARATOR_DOT
                        + MemberParentInfo.FIELD_CONTACTNUMBER)) {
                    updateList.add(OrgMember.FIELD_MOTHER + FileUtils.SEPARATOR_DOT
                            + MemberParentInfo.FIELD_CONTACTNUMBER);
                } else if (key.equals(UpdateOrgMemberReq.FATHER + FileUtils.SEPARATOR_DOT
                        + MemberParentInfo.FIELD_CONTACTNUMBER)) {
                    updateList.add(OrgMember.FIELD_FATHER + FileUtils.SEPARATOR_DOT
                            + MemberParentInfo.FIELD_CONTACTNUMBER);
                } else if (key.equals(UpdateOrgMemberReq.GUARDIAN + FileUtils.SEPARATOR_DOT
                        + MemberParentInfo.FIELD_CONTACTNUMBER)) {
                    updateList.add(OrgMember.FIELD_GUARDIAN + FileUtils.SEPARATOR_DOT
                            + MemberParentInfo.FIELD_CONTACTNUMBER);
                }

            }

            if (updateList.size() != request.updateList.size()) {
                throw new VedantuException(VedantuErrorCode.INCORRECT_UPDATE_DATA_PROVIDED);
            }
        }

    }

    public static UpdateOrgMemberRes updateOrgMember(UpdateOrgMemberReq updateOrgMemberReq)
            throws VedantuException {

        Set<String> updateList = new HashSet<String>();

        setUpdateList(updateList, updateOrgMemberReq);

        AuthHandler authHandler = AuthHandlerFactory.getInstance().getAuthHandler(
                updateOrgMemberReq.orgId);

        boolean isValidUpdate = authHandler.isUpdateValid(updateList);
        LOGGER.debug("isValidUpdate: " + isValidUpdate);
        if ((CollectionUtils.isEmpty(updateList) || updateList.contains(OrgMember.FIELD_DOB))
                && !VedantuStringUtils.isValidDOB(updateOrgMemberReq.dob)) {
            throw new VedantuException(VedantuErrorCode.INVALID_DATE_FORMAT);
        }

        User user = UserDAO.INSTANCE.findUserById(updateOrgMemberReq.targetUserId);
        if (null == user) {
            LOGGER.error("user not found for targetUserId: " + updateOrgMemberReq.targetUserId);
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }

        OrgMember orgMember = OrgMemberDAO.INSTANCE.getMemberByUserId(updateOrgMemberReq.orgId,
                updateOrgMemberReq.targetUserId);
        if (null == orgMember) {
            LOGGER.error("orgMember not found for orgId: " + updateOrgMemberReq.orgId
                    + ", targetUserId: " + updateOrgMemberReq.targetUserId);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }
        if (!StringUtils.equals(updateOrgMemberReq.targetOrgMemberId, orgMember._getStringId())) {
            LOGGER.error("orgMember._id: " + orgMember._getStringId()
                    + " does not match for orgId: " + updateOrgMemberReq.orgId + ", targetUserId: "
                    + updateOrgMemberReq.targetUserId + ", targetOrgMemberId: "
                    + updateOrgMemberReq.targetOrgMemberId);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }

        boolean isMemberIdChanging = false;
        if ((CollectionUtils.isEmpty(updateList) || updateList.contains(OrgMember.FIELD_MEMBER_ID))
                && StringUtils.isNotEmpty(orgMember.memberId)) {
            isMemberIdChanging = !StringUtils.equals(updateOrgMemberReq.getTargetMemberId(),
                    orgMember.memberId);
        }

        boolean doesUserUseOrgCredentials = StringUtils.equals(user.username,
                authHandler.getMemberUsername(orgMember.orgId, orgMember.memberId));

        if (isMemberIdChanging) {
            OrgMember otherOrgMember = OrgMemberDAO.INSTANCE.getMemberByMemberId(
                    updateOrgMemberReq.orgId, updateOrgMemberReq.getTargetMemberId());
            if (null != otherOrgMember) {
                LOGGER.error("cannot create another orgMember with same targetMemberId: "
                        + updateOrgMemberReq.getTargetMemberId() + " for orgId: "
                        + updateOrgMemberReq.orgId + ", targetUserId: "
                        + updateOrgMemberReq.targetUserId + ", targetOrgMemberId: "
                        + updateOrgMemberReq.targetOrgMemberId);
                throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_ALREADY_EXISTS);
            }
        }
        boolean isDOBChanging = false;
        if ((CollectionUtils.isEmpty(updateList) || updateList.contains(OrgMember.FIELD_DOB))
                && StringUtils.isNotEmpty(updateOrgMemberReq.dob)) {
            isDOBChanging = !StringUtils.equals(updateOrgMemberReq.dob, orgMember.dob);
        }

        if (OrgMemberProfile.STUDENT == updateOrgMemberReq.profile) {
            orgMember = OrgMemberDAO.INSTANCE.updateMember(updateOrgMemberReq.orgId,
                    updateOrgMemberReq.targetUserId, updateOrgMemberReq.targetOrgMemberId,
                    updateOrgMemberReq.getTargetMemberId(), updateOrgMemberReq.firstName,
                    updateOrgMemberReq.lastName, updateOrgMemberReq.dob, updateOrgMemberReq.gender,
                    updateOrgMemberReq.getEmail(), updateOrgMemberReq.profile,
                    updateOrgMemberReq.contactNumber, updateOrgMemberReq.father,
                    updateOrgMemberReq.mother, updateOrgMemberReq.guardian,
                    updateOrgMemberReq.getParentEmail(), updateOrgMemberReq.extraInfo, updateList);
        } else {
            orgMember = OrgMemberDAO.INSTANCE.updateMember(updateOrgMemberReq.orgId,
                    updateOrgMemberReq.targetUserId, updateOrgMemberReq.targetOrgMemberId,
                    updateOrgMemberReq.getTargetMemberId(), updateOrgMemberReq.firstName,
                    updateOrgMemberReq.lastName, updateOrgMemberReq.dob, updateOrgMemberReq.gender,
                    updateOrgMemberReq.getEmail(), updateOrgMemberReq.profile,
                    updateOrgMemberReq.contactNumber, updateOrgMemberReq.isCanImpersonate(),
                    updateOrgMemberReq.extraInfo, updateList);
        }

        if (doesUserUseOrgCredentials) {
            if (isMemberIdChanging) {
                UpdateUsernameReq updateUsernameReq = new UpdateUsernameReq();
                updateUsernameReq.targetUserId = updateOrgMemberReq.targetUserId;
                updateUsernameReq.setNewUsername(authHandler.getMemberUsername(orgMember.orgId,
                        orgMember.memberId));
                updateUsernameReq.newPassword = getMemberDefaultPassword(
                        updateOrgMemberReq.profile, orgMember.memberId, orgMember.dob);
                UpdateUsernameRes updateUsernameRes = UserManager.updateUsername(updateUsernameReq);
                LOGGER.debug("update username response: " + updateUsernameRes.done);
            } else if (isDOBChanging) {
                LOGGER.debug("News dob" + orgMember.dob);
                UpdateUserPasswordReq updateUserPasswordReq = new UpdateUserPasswordReq();
                updateUserPasswordReq.targetUserId = updateOrgMemberReq.targetUserId;
                updateUserPasswordReq.newPassword = getMemberDefaultPassword(
                        updateOrgMemberReq.profile, orgMember.memberId, orgMember.dob);
                LOGGER.debug("New password" + updateUserPasswordReq.newPassword);
                UpdateUserPasswordRes updateUserPasswordRes = UserManager
                        .updateUserPassword(updateUserPasswordReq);
                user.dob = orgMember.dob;
                UserDAO.INSTANCE.updateModel(user, Arrays.asList(User.FIELD_DOB));
                LOGGER.debug("update user password response: " + updateUserPasswordRes.done);
            }
        }

        UpdateOrgMemberRes updateOrgMemberRes = new UpdateOrgMemberRes(orgMember._getStringId(),
                orgMember.recordState, orgMember.orgId, orgMember.userId);

        return updateOrgMemberRes;
    }

    public static UpdateOrgMemberRes updateOrgMemberEmail(UpdateOrgMemberReq updateOrgMemberReq)
            throws VedantuException {

        OrgMember orgMember = OrgMemberDAO.INSTANCE.getMemberByUserId(updateOrgMemberReq.orgId,
                updateOrgMemberReq.targetUserId);
        if (null == orgMember) {
            LOGGER.error("orgMember not found for orgId: " + updateOrgMemberReq.orgId
                    + ", targetUserId: " + updateOrgMemberReq.targetUserId);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }
        orgMember = OrgMemberDAO.INSTANCE.updateOrgMemberEmail(updateOrgMemberReq.orgId, updateOrgMemberReq.userId, updateOrgMemberReq.getEmail());
        UpdateOrgMemberRes updateOrgMemberRes = new UpdateOrgMemberRes(orgMember._getStringId(),
                orgMember.recordState, orgMember.orgId, orgMember.userId);

        return updateOrgMemberRes;

    }

    public static AddOrgMemberMappingRes addOrgMemberMapping(
            AddOrgMemberMappingReq addOrgMemberMappingReq, boolean noExceptionOnExistingMapping)
            throws VedantuException {
        OrgMemberDAO.INSTANCE.killFreezedRewards(addOrgMemberMappingReq.userId);
        AuthHandler authHandler = AuthHandlerFactory.getInstance().getAuthHandler(
                addOrgMemberMappingReq.orgId);
        AddOrgMemberMappingRes res = authHandler.addMemberMapping(addOrgMemberMappingReq,
                noExceptionOnExistingMapping);
        if(addOrgMemberMappingReq.targetProfile == OrgMemberProfile.TEACHER){
            for(String courseId : addOrgMemberMappingReq.courseIds){
                TeacherAnalytics teacher = new TeacherAnalytics(courseId, addOrgMemberMappingReq.targetOrgMemberId);
                TeacherAnalyticsDAO.INSTANCE.save(teacher);
            }
        }
        if (addOrgMemberMappingReq.returnOrgProfileWithCourseInfo) {
            OrgMember orgMember = OrgMemberDAO.INSTANCE
                    .getById(addOrgMemberMappingReq.targetOrgMemberId);
            res.info = getOrgMemberProfileRes(orgMember, true, false).info;
        }
        if (addOrgMemberMappingReq.returnNewlyAddedMapping) {
            res.newlyAddedMapping = OrgMemberDAO.INSTANCE.getMemberMappingForSection(
                    addOrgMemberMappingReq.orgId, addOrgMemberMappingReq.targetUserId,
                    addOrgMemberMappingReq.sectionIds.get(0));
        }
        return res;
    }

    public static AddOrgMemberMappingRes addOrgMemberMapping(Organization organization,
            AddOrgMemberMappingReq addOrgMemberMappingReq, boolean noExceptionOnExistingMapping)
            throws VedantuException {

        AuthHandler authHandler = AuthHandlerFactory.getInstance().getAuthHandler(organization);
        AddOrgMemberMappingRes res = authHandler.addMemberMapping(addOrgMemberMappingReq,
                noExceptionOnExistingMapping);
        if (addOrgMemberMappingReq.returnOrgProfileWithCourseInfo) {
            OrgMember orgMember = OrgMemberDAO.INSTANCE
                    .getById(addOrgMemberMappingReq.targetOrgMemberId);
            res.info = getOrgMemberProfileRes(orgMember, true, false).info;
        }
        return res;
    }

    public static UpdateOrgMemberMappingRes updateOrgMemberMapping(
            UpdateOrgMemberMappingReq updateOrgMemberMappingReq) throws VedantuException {

        Set<String> tSectionIds = null == updateOrgMemberMappingReq.sectionIds ? null
                : new HashSet<String>(updateOrgMemberMappingReq.sectionIds);
        Set<String> tAddCourseIds = null == updateOrgMemberMappingReq.courseIds ? null
                : new HashSet<String>(updateOrgMemberMappingReq.courseIds);
        Set<String> tRemoveCourseIds = null == updateOrgMemberMappingReq.removeCourseIds ? null
                : new HashSet<String>(updateOrgMemberMappingReq.removeCourseIds);
        MutableBoolean isUpdated = new MutableBoolean(false);
        OrgMember orgMember = OrgMemberDAO.INSTANCE.updateOrgMemberMapping(
                updateOrgMemberMappingReq.orgId, updateOrgMemberMappingReq.targetUserId,
                updateOrgMemberMappingReq.targetOrgMemberId, updateOrgMemberMappingReq.programId,
                updateOrgMemberMappingReq.centerId, tSectionIds, tAddCourseIds, tRemoveCourseIds,
                isUpdated);

        UpdateOrgMemberMappingRes updateOrgMemberMappingRes = new UpdateOrgMemberMappingRes(
                orgMember._getStringId(), orgMember.recordState, isUpdated.booleanValue());
        return updateOrgMemberMappingRes;
    }

    public static UpdateEndTimeMappingRes updateEndDateMapping(
            UpdateEndTimeMappingReq updateEndDateMappingReq) throws VedantuException {

        OrgMember orgMember = OrgMemberDAO.INSTANCE.updateEndDateMapping(
                updateEndDateMappingReq.orgId, updateEndDateMappingReq.targetUserId,
                updateEndDateMappingReq.sectionId, updateEndDateMappingReq.endTime);

        UpdateEndTimeMappingRes updateEndDateMappingRes = new UpdateEndTimeMappingRes();
        updateEndDateMappingRes.done = true;
        return updateEndDateMappingRes;
    }

    public static RemoveOrgMemberMappingRes removeOrgMemberMapping(
            RemoveOrgMemberMappingReq removeOrgMemberMappingReq) throws VedantuException {

        AuthHandler authHandler = AuthHandlerFactory.getInstance().getAuthHandler(
                removeOrgMemberMappingReq.orgId);

        if (authHandler.authType == AuthType.EXT_AUTH_ORG) {
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED,
                    "removing a member mapping is not allowed for external authentication");
        }

        MutableBoolean isRemoved = new MutableBoolean(false);
        Set<String> tSectionIds = null == removeOrgMemberMappingReq.sectionIds ? null
                : new HashSet<String>(removeOrgMemberMappingReq.sectionIds);

        List<OrgSection> paidSections = OrgSectionDAO.INSTANCE.find(
                OrgSectionDAO.INSTANCE.createQuery().field("_id")
                        .in(ObjectIdUtils.toObjectIds(new ArrayList<String>(tSectionIds), true))
                        .filter(OrgSection.FIELD_REVENUE_MODEL, RevenueModel.PAID)
                        .filter(OrgSection.FIELD_ACCESS_SCOPE, AccessScope.OPEN)
                        .retrievedFields(true, "_id")).asList();

        if (CollectionUtils.isNotEmpty(paidSections)) {
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED,
                    "removing a member mapping not allowed from a paid program/section");
        }

        OrgMember orgMember = OrgMemberDAO.INSTANCE.removeOrgMemberMapping(
                removeOrgMemberMappingReq.orgId, removeOrgMemberMappingReq.targetUserId,
                removeOrgMemberMappingReq.targetOrgMemberId, removeOrgMemberMappingReq.programId,
                removeOrgMemberMappingReq.centerId, tSectionIds, isRemoved);

        RemoveOrgMemberMappingRes removeOrgMemberMappingRes = new RemoveOrgMemberMappingRes(
                orgMember._getStringId(), orgMember.recordState, isRemoved.getValue());
        return removeOrgMemberMappingRes;
    }

    public static GetOrgMemberProfileRes getOrgMember(GetOrgMemberProfileReq getOrgMemberProfileReq)
            throws VedantuException {

        OrgMemberDAO.INSTANCE.updateOrgMemberExpiredMappings(getOrgMemberProfileReq.orgId,
                getOrgMemberProfileReq.targetUserId);
        OrgMember orgMember = OrgMemberDAO.INSTANCE.getMemberByUserId(getOrgMemberProfileReq.orgId,
                getOrgMemberProfileReq.targetUserId);
        if (null == orgMember) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }

        GetOrgMemberProfileRes getOrgMemberProfileRes = getOrgMemberProfileRes(orgMember,
                getOrgMemberProfileReq.ensureCourseInfo, getOrgMemberProfileReq.getKey);

        return getOrgMemberProfileRes;
    }

    public static GetOrgMemberProfileRes getOrgMemberByMemberId(GetOrgMemberReq req)
            throws VedantuException {

        OrgMember orgMember = OrgMemberDAO.INSTANCE.getMemberByMemberId(req.orgId, req.memberId);
        if (null == orgMember) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND,
                    "No member found with memberId : " + req.memberId);
        }

        GetOrgMemberProfileRes getOrgMemberProfileRes = getOrgMemberProfileRes(orgMember,
                req.ensureCourseInfo, req.getKey);

        return getOrgMemberProfileRes;

    }

    public static GetOrgMemberProfileRes getOrgMemberWithEmail(GetOrgMemberWithEmailReq req)
            throws VedantuException {

        OrgMember orgMember = OrgMemberDAO.INSTANCE.getOrgMemberWithEmail(req.orgId, req.email);
        if (null == orgMember) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND,
                    "No member found with memberId : " + req.email);
        }

        GetOrgMemberProfileRes getOrgMemberProfileRes = getOrgMemberProfileRes(orgMember,
                req.ensureCourseInfo, req.getKey);

        return getOrgMemberProfileRes;

    }

    private static GetOrgMemberProfileRes getOrgMemberProfileRes(OrgMember orgMember,
            boolean ensureCourseInfo, boolean addKey) throws VedantuException {

        OrgMemberExtendedInfo orgMemberExtendedInfo = (OrgMemberExtendedInfo) orgMember
                .toExtendedInfo();
        populateUserPublicProfileDetails(orgMember, orgMemberExtendedInfo);

        populateProgramHierarchy(orgMember, orgMemberExtendedInfo, ensureCourseInfo);

        GetOrgMemberProfileRes getOrgMemberProfileRes = new GetOrgMemberProfileRes();
        Organization org = OrganizationDAO.INSTANCE.getById(orgMember.orgId);
        getOrgMemberProfileRes.info = orgMemberExtendedInfo;
        getOrgMemberProfileRes.doubtsForumMode = org.doubtsForumMode;
        getOrgMemberProfileRes.showClassroomConnect = org.showClassroomConnect;
        if (addKey) {
            getOrgMemberProfileRes.key = UserManager.getPrivateKey(orgMember.userId);
        }
        return getOrgMemberProfileRes;
    }

    private static void populateUserPublicProfileDetails(OrgMember orgMember,
            OrgMemberExtendedInfo orgMemberExtendedInfo) throws VedantuException {

        UserExtendedInfo userExtendedInfo = UserDAO.INSTANCE.getExtendedInfo(orgMember.userId);
        if (null == userExtendedInfo) {
            LOGGER.debug("populateUserPublicProfileDetails no userExtendedInfo found for userId: "
                    + orgMember.userId);
            return;
        }
        final String username = userExtendedInfo.username;
        final String verifiedEmail = userExtendedInfo.isEmailVerified ? userExtendedInfo.email
                : StringUtils.EMPTY;
        final boolean isUsernameOrgSpecific = StringUtils.equals(
                username,
                AuthHandlerFactory.getInstance().getAuthHandler(orgMember.orgId)
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
            OrgMemberDAO.INSTANCE.save(orgMember);
        }

    }

    private static void populateProgramHierarchy(OrgMember orgMember,
            OrgMemberExtendedInfo orgMemberExtendedInfo) {

        populateProgramHierarchy(orgMember, orgMemberExtendedInfo, false);
    }

    private static void populateProgramHierarchy(OrgMember orgMember,
            OrgMemberExtendedInfo orgMemberExtendedInfo, boolean ensureCourseInfo) {

        if (CollectionUtils.isNotEmpty(orgMember.mappings)) {

            for (OrgMemberMappingInfo mapping : orgMember.mappings) {
                if (null == mapping) {
                    continue;
                }

                OrgStructureBasicInfo program = OrgProgramDAO.INSTANCE
                        .getBasicInfo(mapping.programId);
                OrgProgramBasicInfo programInfo = orgMemberExtendedInfo.mappings
                        ._getOrAddProgram(program);

                if (ensureCourseInfo) {
                    mapping.courseIds = programInfo.courseIds;
                }

                OrgStructureBasicInfo progCenter = OrgCenterDAO.INSTANCE
                        .getBasicInfo(mapping.centerId);
                OrgProgramCenterBasicInfo progCenterInfo = programInfo
                        ._getOrAddProgramCenter(progCenter);

                OrgSection orgSection = OrgSectionDAO.INSTANCE.getById(mapping.sectionId);
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
                    progSectionInfo.desc = StringUtils.defaultString(orgSection.desc,
                            StringUtils.EMPTY);
                    progSectionInfo.addSectionExtraInfo(orgSection);
                }
                if (CollectionUtils.isNotEmpty(mapping.courseIds)) {
                    for (String courseId : mapping.courseIds) {
                        if (StringUtils.isEmpty(courseId)) {
                            continue;
                        }
                        BoardBasicInfo course = BoardDAO.INSTANCE.getBasicInfo(courseId);
                        if (null != course) {
                            progSectionInfo._getOrAddProgramCourse(course);
                        }
                    }
                }
            }
        }
    }

    public static GetOrgMembersRes getOrgMembers(GetOrgMembersReq getOrgMembersReq)
            throws VedantuException {

        MutableLong totalHits = new MutableLong();
        List<OrgMember> orgMembers = OrgMemberDAO.INSTANCE.getOrgMembers(getOrgMembersReq.orgId,
                getOrgMembersReq.targetProfile, getOrgMembersReq.excludeProfiles,
                getOrgMembersReq.programId, getOrgMembersReq.centerId, getOrgMembersReq.sectionId,
                getOrgMembersReq.courseId, getOrgMembersReq.query, getOrgMembersReq.start,
                getOrgMembersReq.size, getOrgMembersReq.excludes, getOrgMembersReq.canImpersonate,
                totalHits);

        GetOrgMembersRes getOrgMembersRes = new GetOrgMembersRes();
        if (CollectionUtils.isNotEmpty(orgMembers)) {
            List<OrgMemberExtendedInfo> orgMemberExtendedInfos = new ArrayList<OrgMemberExtendedInfo>();
            User user = null;
            for (OrgMember orgMember : orgMembers) {
                if (null == orgMember) {
                    continue;
                }
                user = UserDAO.INSTANCE.getById(orgMember.userId);
                if(null == user){
                    continue;
                }
                OrgMemberExtendedInfo orgMemberExtendedInfo = (OrgMemberExtendedInfo) orgMember
                        .toExtendedInfo();
                orgMemberExtendedInfo.isEmailVerified = user.isEmailVerified;
                orgMemberExtendedInfo.isPhoneVerified = user.isPhoneVerified;
                orgMemberExtendedInfos.add(orgMemberExtendedInfo);

                populateProgramHierarchy(orgMember, orgMemberExtendedInfo);
            }

            getOrgMembersRes.list = orgMemberExtendedInfos;
            getOrgMembersRes.totalHits = totalHits.longValue();
        }

        return getOrgMembersRes;
    }

    public static UploadOrgStudentsRes uploadOrgStudents(UploadOrgStudentsReq uploadOrgStudentsReq)
            throws VedantuException {

        Organization organization = OrganizationDAO.INSTANCE
                .getOrganizationById(uploadOrgStudentsReq.orgId);

        // check program
        OrgProgram program = OrgProgramDAO.INSTANCE.getProgramById(uploadOrgStudentsReq.orgId,
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
            throw new VedantuException(VedantuErrorCode.STUDENT_FILE_UNPARSEABLE, StringUtils.join(
                    parser.getErrors(), ", "));
        }

        // verify center existence for codes
        Set<String> centerCodes = parser.getCenters();
        if (CollectionUtils.isEmpty(centerCodes) || parser.getRecords() == null
                || parser.getRecords().isEmpty()) {
            throw new VedantuException(VedantuErrorCode.STUDENT_FILE_UNPARSEABLE,
                    "Students Records can not be empty");
        }

        Map<String, OrgStructureBasicInfo> codeToCenter = OrgCenterDAO.INSTANCE
                .getBasicInfosByCode(uploadOrgStudentsReq.orgId, centerCodes);
        if (CollectionUtils.size(centerCodes) > CollectionUtils.size(codeToCenter)) {
            Set<String> missingCenters = new HashSet<String>(centerCodes);
            missingCenters.removeAll(codeToCenter.keySet());
            String errorMsg = "missing centers: {" + StringUtils.join(missingCenters, ", ") + "}";
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
        if (CollectionUtils.isNotEmpty(nonProgramCenters)) {
            String errorMsg = "non-program centers: {" + StringUtils.join(nonProgramCenters, ", ")
                    + "}";
            throw new VedantuException(VedantuErrorCode.STUDENT_FILE_UNPARSEABLE, errorMsg);
        }

        // verify section existence for codes
        Set<String> centerQualifiedSectionCodes = parser.getSections();
        Map<String, OrgStructureBasicInfo> centerQualifiedCodeToSection = OrgSectionDAO.INSTANCE
                .getBasicInfosByCode(uploadOrgStudentsReq.orgId, uploadOrgStudentsReq.programId,
                        codeToCenter, centerQualifiedSectionCodes);
        if (CollectionUtils.size(centerQualifiedSectionCodes) > CollectionUtils
                .size(centerQualifiedCodeToSection)) {
            Set<String> missingSections = new HashSet<String>(centerQualifiedSectionCodes);
            missingSections.removeAll(centerQualifiedCodeToSection.keySet());
            String errorMsg = "missing sections: {" + StringUtils.join(missingSections, ", ") + "}";
            throw new VedantuException(VedantuErrorCode.STUDENT_FILE_UNPARSEABLE, errorMsg);
        }

        // verify if sections exist in program/center
        Set<String> nonProgramSections = new HashSet<String>();
        for (Map.Entry<String, OrgStructureBasicInfo> sectionEntry : centerQualifiedCodeToSection
                .entrySet()) {

            String centerQualifiedSectionCode = sectionEntry.getKey();

            String centerCode = OrgSectionDAO.getCenterPart(centerQualifiedSectionCode);
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

        if (CollectionUtils.isNotEmpty(nonProgramSections)) {
            String errorMsg = "non-program sections: {"
                    + StringUtils.join(nonProgramSections, ", ") + "}";
            throw new VedantuException(VedantuErrorCode.STUDENT_FILE_UNPARSEABLE, errorMsg);
        }

        // Check Members existence
        Set<String> memberIdsOrEmails = parser.getRecords().keySet();

        Set<String> existingUsernames = UserManager.getExistingUsernames(memberIdsOrEmails);

        Set<String> existingMemberIds = OrgMemberDAO.INSTANCE.getExistingMemberIds(
                uploadOrgStudentsReq.orgId, memberIdsOrEmails);

        LOGGER.debug("already presented usernames: " + existingUsernames + ", existingMemberIds:"
                + existingMemberIds + ", merge:" + uploadOrgStudentsReq.merge);

        if (!uploadOrgStudentsReq.merge && CollectionUtils.isNotEmpty(existingMemberIds)) {
            String errorMsg = "already presente memberIds: {"
                    + StringUtils.join(existingMemberIds, ", ") + "}";
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_ALREADY_EXISTS,
                    errorMsg);
        }

        for (Map.Entry<String, List<StudentXLRecord>> recordEntry : parser.getRecords().entrySet()) {

            List<StudentXLRecord> records = recordEntry.getValue();
            StudentXLRecord studentRecord = records.get(0);
            LOGGER.debug("uploadOrgStudents processing memberId: " + studentRecord.memberId
                    + ", email: " + studentRecord.email + ", record: " + studentRecord);

            OrgMember orgMember = null;
            if (!existingMemberIds.contains(recordEntry.getKey())
                    && StringUtils.isNotEmpty(studentRecord.memberId)) {

                LOGGER.debug("uploadOrgStudents will try to add orgMember memberId: "
                        + studentRecord.memberId);

                AddOrgMemberReq addOrgMemberReq = studentRecord
                        .toAddOrgMemberReq(uploadOrgStudentsReq.orgId);

                LOGGER.debug("addOrgMemberReq : " + addOrgMemberReq);
                AddOrgMemberRes addOrgMemberRes = addOrgMember(organization, addOrgMemberReq, false);
                LOGGER.debug("addOrgMemberRes: " + addOrgMemberRes);

                orgMember = OrgMemberDAO.INSTANCE.getMemberByMemberId(uploadOrgStudentsReq.orgId,
                        studentRecord.memberId);
            }

            if (!existingUsernames.contains(recordEntry.getKey())
                    && StringUtils.isNotEmpty(studentRecord.email) && orgMember == null) {

                LOGGER.debug("uploadOrgStudents will try to add orgMember with email as username");

                AddOrgMemberReq addOrgMemberReq = studentRecord
                        .toAddOrgMemberReq(uploadOrgStudentsReq.orgId);
                addOrgMemberReq.useEmailAsUsername = true;
                LOGGER.debug("addOrgMemberReq : " + addOrgMemberReq);

                String memberId = getNextOrgMemberId(uploadOrgStudentsReq.orgId);
                addOrgMemberReq.setTargetMemberId(memberId);
                AddOrgMemberRes addOrgMemberRes = addOrgMember(organization, addOrgMemberReq, true);
                LOGGER.debug("addOrgMemberRes: " + addOrgMemberRes);
                orgMember = OrgMemberDAO.INSTANCE.getMemberByMemberId(uploadOrgStudentsReq.orgId,
                        memberId);
            }

            if (orgMember == null) {
                if (existingUsernames.contains(recordEntry.getKey())) {
                    User user = UserDAO.INSTANCE.find(
                            UserDAO.INSTANCE.createQuery()
                                    .filter(ConstantsGlobal.USERNAME, recordEntry.getKey())
                                    .retrievedFields(true, ConstantsGlobal._ID)).get();
                    if (user != null) {
                        orgMember = OrgMemberDAO.INSTANCE.getMemberByUserId(
                                uploadOrgStudentsReq.orgId, user._getStringId());
                    }
                }

                if (orgMember == null && existingMemberIds.contains(recordEntry.getKey())) {
                    orgMember = OrgMemberDAO.INSTANCE.getMemberByMemberId(
                            uploadOrgStudentsReq.orgId, recordEntry.getKey());
                }

            }

            if (orgMember == null) {
                continue;
            }
            int i = 0;
            for (StudentXLRecord record : records) {
                LOGGER.debug("uploadOrgStudents found orgMember: " + orgMember);
                i++;
                OrgStructureBasicInfo centerBasicInfo = codeToCenter.get(record.center);
                String centerQualifiedSectionCode = OrgSectionDAO.getCenterQualifiedSectionCode(
                        record.center, record.section);
                OrgStructureBasicInfo sectionBasicInfo = centerQualifiedCodeToSection
                        .get(centerQualifiedSectionCode);
                LOGGER.debug("uploadOrgStudents will add orgMemberMapping");

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
                        LOGGER.debug("member is already part of : " + orgMappingInfo);
                        continue;
                    }
                    final boolean noExceptionOnExistingMapping = true;
                    AddOrgMemberMappingRes addOrgMemberMappingRes = addOrgMemberMapping(
                            organization, addOrgMemberMappingReq, noExceptionOnExistingMapping);
                    LOGGER.debug("addOrgMemberMappingRes: " + addOrgMemberMappingRes);
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

    public static ResetUsernameRes resetUsername(ResetUsernameReq resetUsernameReq)
            throws VedantuException {

        User user = UserDAO.INSTANCE.findUserById(resetUsernameReq.targetUserId);
        if (null == user) {
            LOGGER.error("user not found for targetUserId: " + resetUsernameReq.targetUserId);
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }

        OrgMember orgMember = OrgMemberDAO.INSTANCE.getMemberByUserId(resetUsernameReq.orgId,
                resetUsernameReq.targetUserId);
        if (null == orgMember) {
            LOGGER.error("orgMember not found for orgId: " + resetUsernameReq.orgId
                    + ", targetUserId: " + resetUsernameReq.targetUserId);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }

        AuthHandler authHandler = AuthHandlerFactory.getInstance().getAuthHandler(
                resetUsernameReq.orgId);

        if (!StringUtils.equals(resetUsernameReq.targetOrgMemberId, orgMember._getStringId())) {
            LOGGER.error("orgMember._id: " + orgMember._getStringId()
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

        UpdateUsernameRes updateUsernameRes = UserManager.updateUsername(updateUsernameReq);

        ResetUsernameRes resetUsernameRes = new ResetUsernameRes();
        resetUsernameRes.done = updateUsernameRes.done;

        return resetUsernameRes;
    }

    public static UnsetEmailRes unsetEmail(UnsetEmailReq unsetEmailReq) throws VedantuException {

        OrgMember orgMember = OrgMemberDAO.INSTANCE.getMemberByUserId(unsetEmailReq.orgId,
                unsetEmailReq.targetUserId);
        if (null == orgMember) {
            LOGGER.error("orgMember not found for orgId: " + unsetEmailReq.orgId
                    + ", targetUserId: " + unsetEmailReq.targetUserId);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }
        if (!StringUtils.equals(unsetEmailReq.targetOrgMemberId, orgMember._getStringId())) {
            LOGGER.error("orgMember._id: " + orgMember._getStringId()
                    + " does not match for orgId: " + unsetEmailReq.orgId + ", targetUserId: "
                    + unsetEmailReq.targetUserId + ", targetOrgMemberId: "
                    + unsetEmailReq.targetOrgMemberId);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }

        orgMember.email = StringUtils.EMPTY;

        OrgMemberDAO.INSTANCE.updateModel(orgMember, Arrays.asList(ConstantsGlobal.EMAIL));

        UnsetEmailRes unsetEmailRes = new UnsetEmailRes();
        unsetEmailRes.done = true;

        return unsetEmailRes;
    }

    public static SendForgotPasswordEmailRes sendForgotPasswordEmail(
            SendForgotPasswordEmailReq sendForgotPasswordEmailReq) throws VedantuException {

        //
        AuthHandler authHandler = AuthHandlerFactory.getInstance().getAuthHandler(
                sendForgotPasswordEmailReq.orgId);

        com.vedantu.user.pojos.requests.SendForgotPasswordEmailReq tSendForgotPasswordEmailReq = new com.vedantu.user.pojos.requests.SendForgotPasswordEmailReq();
        tSendForgotPasswordEmailReq.setUsername(authHandler.getMemberUsername(
                sendForgotPasswordEmailReq.orgId, sendForgotPasswordEmailReq.getMemberId()));
        tSendForgotPasswordEmailReq.setOrgId(sendForgotPasswordEmailReq.orgId);
        SendForgotPasswordEmailRes sendForgotPasswordEmailRes = UserManager
                .sendForgotPasswordEmail(tSendForgotPasswordEmailReq);

        return sendForgotPasswordEmailRes;
    }

    public static UploadProfilePicRes uploadProfilePic(UploadProfilePicReq uploadProfilePicReq)
            throws VedantuException {

        ImageFilter filter = new ImageFilter();
        boolean isImg = filter.accept(new File(uploadProfilePicReq.fileName));
        if (!isImg) {
            throw new VedantuException(VedantuErrorCode.UNSUPPORTED_FILE_TYPE, "not an image file");
        }

        OrgMember orgMember = OrgMemberDAO.INSTANCE.getMemberByUserId(uploadProfilePicReq.orgId,
                uploadProfilePicReq.targetUserId);
        if (null == orgMember) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }
        if (!StringUtils.equals(orgMember._getStringId(), uploadProfilePicReq.targetOrgMemberId)) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }

        UploadProfilePicRes uploadProfilePicRes = storeUserProfilePic(orgMember,
                uploadProfilePicReq.inputFile, uploadProfilePicReq.fileName);
        return uploadProfilePicRes;
    }

    public static BulkUploadOrgMembersProfilePicRes bulkUploadMembersProfilePic(
            BulkUploadProfilePicsReq req) throws VedantuException {

        BulkUploadOrgMembersProfilePicRes uploadMembersProfilePicRes = new BulkUploadOrgMembersProfilePicRes();
        InputStream is = null;
        ZipInputStream zipStream = null;
        byte[] buffer = new byte[1024];
        LocalFileSystemHandler tempFs = FileSystemFactory.INSTANCE.getTempFS();
        String outDir = tempFs.getDirectory();
        try {
            tempFs.createParent(req.orgId);
            outDir = outDir + File.separator + req.orgId;
        } catch (FileStoreException e) {
            LOGGER.error(e.getMessage(), e);
        }

        try {
            is = new FileInputStream(req.inputFile);
            zipStream = new ZipInputStream(is);
            ZipEntry entry = null;
            while ((entry = zipStream.getNextEntry()) != null) {
                String s = String.format("Entry: %s len %d added %TD", entry.getName(),
                        entry.getSize(), new Date(entry.getTime()));
                String memberId = StringUtils.substringBefore(entry.getName(), ".").trim();
                LOGGER.debug(s + ", memberId: " + memberId);

                String outPath = outDir + File.separator + entry.getName();
                File imageOutputFile = new File(outPath);
                FileOutputStream output = null;
                try {
                    output = new FileOutputStream(imageOutputFile);
                    int len = 0;
                    while ((len = zipStream.read(buffer)) > 0) {
                        output.write(buffer, 0, len);
                    }
                    OrgMember orgMember = OrgMemberDAO.INSTANCE.getMemberByMemberId(req.orgId,
                            memberId);
                    if (null == orgMember) {
                        LOGGER.error("no member found with memberId:" + memberId
                                + ", for organization[" + req.orgId + "]");
                    } else {
                        UploadProfilePicRes uploadPicResult = storeUserProfilePic(orgMember,
                                imageOutputFile, entry.getName());
                        uploadMembersProfilePicRes.status.put(memberId, uploadPicResult);
                        try {
                            LOGGER.debug("sleeping for " + 200 + "ms");
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }
                } finally {
                    IOUtils.closeQuietly(output);
                    FileUtils.deleteFile(entry.getName(), imageOutputFile);
                }
            }

        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(zipStream);
            IOUtils.closeQuietly(is);
        }
        return uploadMembersProfilePicRes;
    }

    public static BulkUpdateStudentsInSectionRes updateStudentsInSection(
            BulkUpdateStudentInSectionReq req) throws VedantuException {

        String errorMsg = req.validate();

        if (errorMsg != null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, errorMsg);
        }

        Organization org = OrganizationDAO.INSTANCE.getOrganizationById(req.orgId);

        if (org.authType == AuthType.EXT_AUTH_ORG) {
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED,
                    "this operation is not allowed for organization using External authentication");
        }

        OrgMember callingMeber = OrgMemberDAO.INSTANCE.getMemberByUserId(req.orgId,
                req.callingUserId);
        if (callingMeber == null || callingMeber.profile != OrgMemberProfile.MANAGER) {
            errorMsg = "updateStudentsInSection is only allowed to MANAGERS ";
            LOGGER.error(errorMsg);
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED, errorMsg);
        }

        BulkUpdateStudentsInSectionRes res = new BulkUpdateStudentsInSectionRes();

        OrgSection fromOrgSection = OrgSectionDAO.INSTANCE.getSectionById(req.orgId,
                req.fromSectionId);

        OrgSection toOrgSection = StringUtils.isEmpty(req.toSectionId) ? null
                : OrgSectionDAO.INSTANCE.getSectionById(req.orgId, req.toSectionId);

        if ((req.operationType == OrgMappingBulkOperationType.COPY || req.operationType == OrgMappingBulkOperationType.MOVE)
                && toOrgSection.revenueModel == RevenueModel.PAID) {
            errorMsg = "bulk addition operation can not be done to PAID program/section";
            LOGGER.error(errorMsg);
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED, errorMsg);
        }

        if (req.operationType == OrgMappingBulkOperationType.MOVE
                && (fromOrgSection == null || fromOrgSection.revenueModel == RevenueModel.PAID)) {
            errorMsg = "move operation can not be done from a PAID program/section";
            LOGGER.error(errorMsg);
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED, errorMsg);
        }

        // fetch all the student of a sections
        MutableLong hits = new MutableLong();
        int start = 0;
        while (true) {
            List<OrgMember> members = OrgMemberDAO.INSTANCE.getOrgMembers(req.orgId,
                    OrgMemberProfile.STUDENT, null, fromOrgSection.programId,
                    fromOrgSection.centerId, fromOrgSection._getStringId(), null, null, start,
                    DEFAULT_SIZE_STUDENT_BULK_OPERATION, null, req.targetUserIds, null, hits);
            bulkUpdateOrgStudentsMapping(members, fromOrgSection, toOrgSection, req.operationType);
            start += DEFAULT_SIZE_STUDENT_BULK_OPERATION;
            if (hits.intValue() <= start) {
                break;
            }
        }
        res.updatedCount = hits.intValue();
        return res;
    }

    private static void bulkUpdateOrgStudentsMapping(List<OrgMember> members,
            OrgSection fromSection, OrgSection toSection, OrgMappingBulkOperationType operationType) {

        OrgMemberMappingInfo fromOrgMemberMappingInfo = new OrgMemberMappingInfo(
                fromSection.programId, fromSection.centerId, fromSection._getStringId(), null);

        OrgMemberMappingInfo toOrgMemberMappingInfo = toSection == null ? null
                : new OrgMemberMappingInfo(toSection.programId, toSection.centerId,
                        toSection._getStringId(), new HashSet<String>());
        LOGGER.debug("===== updating section from: " + fromOrgMemberMappingInfo + ", to : "
                + toOrgMemberMappingInfo);

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

    private static void addBulkOrgStudentsMapping(List<OrgMember> members,
            OrgMemberMappingInfo mappingInfo) {

        for (OrgMember member : members) {

            if (!member.add(mappingInfo)) {
                continue;
            }
            OrgMemberDAO.INSTANCE.save(member);
            LOGGER.debug("==== new orgMember mapping: " + member.mappings);
        }
    }

    private static void removeBulkOrgStudentsMapping(List<OrgMember> members,
            OrgMemberMappingInfo mappingInfo) {

        for (OrgMember member : members) {
            member.remove(mappingInfo);
            OrgMemberDAO.INSTANCE.save(member);
            LOGGER.debug("==== new orgMember mapping: " + member.mappings);
        }
    }

    private static UploadProfilePicRes storeUserProfilePic(OrgMember orgMember, File inputFile,
            String fileName) throws VedantuException {

        final String imageName = orgMember.userId + "_" + orgMember._getStringId();

        UserProfilePicEntityFileStorage picStorage = new UserProfilePicEntityFileStorage();
        try {
            StorageResult picStorageResult = picStorage.storeImage(imageName, inputFile,
                    FileCategory.ORIGINAL, ImageSize.ORIGINAL, null);
            LOGGER.debug(picStorageResult.toString());

            for (ImageSize imageSize : new ImageSize[] { ImageSize.MEDIUM, ImageSize.SMALL,
                    ImageSize.EXTRA_SMALL }) {
                File convertedFile = ImageGenerator.createImage(inputFile, imageSize, fileName);
                picStorageResult = picStorage.storeImage(imageName, convertedFile,
                        FileCategory.CONVERTED, imageSize, null);
                LOGGER.debug(picStorageResult.toString());

                FileUtils.deleteFile(convertedFile.getName(), convertedFile);
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new VedantuException(VedantuErrorCode.FILE_UPLOAD_ERROR);
        }

        orgMember.thumbnail = imageName;
        OrgMemberDAO.INSTANCE.save(orgMember);

        String thumbnailUrl = orgMember._getThumbnailUrl();
        UploadProfilePicRes uploadProfilePicRes = new UploadProfilePicRes(true, thumbnailUrl);
        return uploadProfilePicRes;
    }

    public static String getMemberDefaultPassword(OrgMemberProfile orgMemberProfile,
            String memberId, String dobYYYYMMDD) {

        boolean noSeparator = true;
        if (orgMemberProfile == OrgMemberProfile.OFFLINE_USER) {
            return StringUtils.EMPTY;
        }
        String defaultPassword = memberId;
        return defaultPassword;
    }

    public static GetOrgMembersRes getOnlineMembers(String orgId, OrgMemberProfile profile,
            String programId, String centerId, String sectionId, String queryText, int start,
            int size, org.apache.commons.lang.mutable.MutableLong hits, List<String> deviceTypes,
            UserStatus loggedIn) throws VedantuException {

        List<OrgMember> orgMembers = OrgMemberDAO.INSTANCE
                .getOnlineUsers(orgId, profile, programId, centerId, sectionId, queryText, start,
                        size, hits, deviceTypes, loggedIn);

        GetOrgMembersRes getOrgMembersRes = new GetOrgMembersRes();
        if (CollectionUtils.isNotEmpty(orgMembers)) {
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
        hits.setValue(hits.longValue());
        return getOrgMembersRes;
    }

    public static AddOrgMemberRes addOrgMemberWithAccessCode(AddOrgMemberWithRequestCodeReq req)
            throws VedantuException {

        LOGGER.debug("Request " + req);
        OrgSection orgSection = OrgSectionDAO.INSTANCE.getSectionByAccessCode(req.accessCode);
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
        OrgMemberDAO.INSTANCE
                .addOrgMemberMapping(orgSection.orgId, addOrgMemberRes.userId, addOrgMemberRes.id,
                        orgSection.programId, orgSection.centerId,
                        new HashSet<String>(Arrays.asList(orgSection._getStringId())), null,
                        new MutableBoolean(), true);

        return addOrgMemberRes;
    }

    private static String getNextOrgMemberId(String orgId) {

        return AuthHandler.getAutogeneratedMemeberIdKeyPrefix()
                + CounterDAO.INSTANCE.getNextSequence(OrgMemberDAO.INSTANCE.getCollection()
                        .getName(), orgId);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, ModelBasicInfo> getUserInfoMap(String orgId,
            Collection<String> userIds, boolean excludeOrgMappingInfo) {

        LOGGER.info("getUserInfoMap orgId:" + orgId + ", userIds: " + userIds);
        if (CollectionUtils.isEmpty(userIds)) {
            return new HashMap<String, ModelBasicInfo>();
        }

        DBObject memberQuery = new BasicDBObject();

        boolean isOrgReq = StringUtils.isNotEmpty(orgId);

        if (isOrgReq) {
            memberQuery.put(ConstantsGlobal.ORG_ID, orgId);
            memberQuery.put(ConstantsGlobal.USER_ID, new BasicDBObject(MongoManager.IN_QUERY,
                    userIds.toArray()));
        } else {
            memberQuery.put(ConstantsGlobal._ID, new BasicDBObject(MongoManager.IN_QUERY,
                    ObjectIdUtils.toObjectIds(new ArrayList<String>(userIds), true).toArray()));
        }
        VedantuDBResult<? extends VedantuBaseMongoModel> users = isOrgReq ? OrgMemberDAO.INSTANCE
                .getInfos(memberQuery, null, MongoManager.NO_START, userIds.size(), null)
                : UserDAO.INSTANCE.getInfos(memberQuery, null, MongoManager.NO_START,
                        userIds.size(), null);
        Map<String, ModelBasicInfo> userIdToBasicInfoMap = isOrgReq ? populateOrgMemberInfo(
                (List<OrgMember>) users.results, excludeOrgMappingInfo) : UserDAO.INSTANCE
                .toBasicInfosMap((Collection<User>) users.results);

        LOGGER.debug("userIds map : " + userIdToBasicInfoMap);
        return userIdToBasicInfoMap;
    }

    /**
     * added by Shankar
     *
     * @param orgMembers
     * @return
     */
    // as of now this function does not populate orgMember courses info {can be
    // added if needed}
    private static Map<String, ModelBasicInfo> populateOrgMemberInfo(List<OrgMember> orgMembers,
            boolean excludeOrgMappingInfo) {

        Set<String> centerIds = new HashSet<String>();
        Set<String> sectionIds = new HashSet<String>();
        Set<String> programIds = new HashSet<String>();

        if (!excludeOrgMappingInfo) {
            for (OrgMember orgMember : orgMembers) {
                if (orgMember.mappings == null) {
                    continue;
                }
                for (OrgMemberMappingInfo mapping : orgMember.mappings) {
                    if (null == mapping) {
                        continue;
                    }
                    programIds.add(mapping.programId);
                    centerIds.add(mapping.centerId);
                    sectionIds.add(mapping.sectionId);
                    // if (mapping.courseIds != null) {
                    // courseIds.addAll(mapping.courseIds);
                    // }
                }
            }
        }

        LOGGER.debug("programIds : " + programIds + " excludeMappingInfo : "
                + excludeOrgMappingInfo);
        LOGGER.debug("centerIds : " + centerIds);
        LOGGER.debug("sectionIds : " + sectionIds);
        Map<String, ModelBasicInfo> orgComponentBasicInfoMap = new HashMap<String, ModelBasicInfo>();
        // collect program info
        if (!excludeOrgMappingInfo) {
            DBObject query = new BasicDBObject(ConstantsGlobal._ID, new BasicDBObject(
                    MongoManager.IN_QUERY, ObjectIdUtils.toObjectIds(
                            new ArrayList<String>(programIds), true).toArray()));
            orgComponentBasicInfoMap.putAll(OrgProgramDAO.INSTANCE
                    .toBasicInfosMap(OrgProgramDAO.INSTANCE.getInfos(query, null,
                            MongoManager.NO_START, MongoManager.NO_LIMIT, null).results));

            // collect center info
            query = new BasicDBObject(ConstantsGlobal._ID, new BasicDBObject(MongoManager.IN_QUERY,
                    ObjectIdUtils.toObjectIds(new ArrayList<String>(centerIds), true).toArray()));
            orgComponentBasicInfoMap.putAll(OrgCenterDAO.INSTANCE
                    .toBasicInfosMap(OrgCenterDAO.INSTANCE.getInfos(query, null,
                            MongoManager.NO_START, MongoManager.NO_LIMIT, null).results));

            // collect section info
            query = new BasicDBObject(ConstantsGlobal._ID, new BasicDBObject(MongoManager.IN_QUERY,
                    ObjectIdUtils.toObjectIds(new ArrayList<String>(sectionIds), true).toArray()));

            orgComponentBasicInfoMap.putAll(OrgSectionDAO.INSTANCE
                    .toBasicInfosMap(OrgSectionDAO.INSTANCE.getInfos(query, null,
                            MongoManager.NO_START, MongoManager.NO_LIMIT, null).results));
            Logger.debug("orgComponentBasicInfoMap : " + orgComponentBasicInfoMap);
        }
        Map<String, ModelBasicInfo> userInfoMap = new HashMap<String, ModelBasicInfo>();

        for (OrgMember orgMember : orgMembers) {
            OrgMemberBasicInfo orgMemberBasicInfo = (OrgMemberBasicInfo) orgMember.toBasicInfo();
            if (!excludeOrgMappingInfo && orgMember.mappings != null) {
                for (OrgMemberMappingInfo mapping : orgMember.mappings) {
                    if (null == mapping) {
                        continue;
                    }
                    OrgStructureBasicInfo program = (OrgStructureBasicInfo) orgComponentBasicInfoMap
                            .get(mapping.programId);
                    LOGGER.debug("programId : " + mapping.programId);
                    if (program == null) {
                        continue;
                    }

                    OrgProgramBasicInfo programInfo = orgMemberBasicInfo.mappings
                            ._getOrAddProgram(program);

                    OrgStructureBasicInfo progCenter = (OrgStructureBasicInfo) orgComponentBasicInfoMap
                            .get(mapping.centerId);
                    OrgProgramCenterBasicInfo progCenterInfo = programInfo
                            ._getOrAddProgramCenter(progCenter);

                    OrgStructureBasicInfo progSection = (OrgStructureBasicInfo) orgComponentBasicInfoMap
                            .get(mapping.sectionId);
                    OrgProgramSectionBasicInfo progSectionInfo = progCenterInfo
                            ._getOrAddProgramSection(progSection);
                    LOGGER.debug("OrgProgramSectionBasicInfo :" + progSectionInfo);
                }
            }
            userInfoMap.put(orgMember.userId, orgMemberBasicInfo);
        }

        return userInfoMap;
    }

    protected void annotateLinkInfo(AbstractFileModelIndexSearchDetails model) {

        if (model.linkInfo != null) {
            model.linkInfo.populate();
        }
    }

    public static SendEmailsToStudentsRes sendEmailsToStudents(SendEmailsToStudentsReq request)
            throws VedantuException {

        SendEmailsToStudentsRes response = new SendEmailsToStudentsRes();
        long count = OrgMemberDAO.INSTANCE.getCountOfMembers(request.orgId,
                OrgMemberProfile.STUDENT, request.programId, request.centerId, request.sectionId);
        for (int start = 0; start < count; start += EMAILS_BATCH_SIZE) {
            MutableLong hits = null;
            List<OrgMember> members = OrgMemberDAO.INSTANCE.getOrgMembers(request.orgId,
                    OrgMemberProfile.STUDENT, request.programId, request.centerId,
                    request.sectionId, null, null, start, EMAILS_BATCH_SIZE, hits);
            SendEmailToStudentsDetails details;
            try {
                details = new SendEmailToStudentsDetails();
            } catch (ClassNotFoundException e) {
                LOGGER.error("SendEmailToStudentsDetails class not found", e);
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

    public static GetReferralDataRes getReferralData(GetReferralDataReq request) {

        GetReferralDataRes response = new GetReferralDataRes();
        Campaign campaign = CampaignDAO.INSTANCE.getCampaignWithCampaignType(request.campaignType);
        if (campaign != null) {
            response.message = campaign.message;
            response.friendRewards = campaign.friendRewards;
            response.referrerRewards = campaign.referrerRewards;
        }
        OrgMember referrer = OrgMemberDAO.INSTANCE.getByUserId(request.userId);
        if (referrer.freezedRewardsOrderId != 0) {
            try {
                if (!isRewardsFreezed(referrer)) {
                    referrer = OrgMemberDAO.INSTANCE.addBackRewards(request.userId);
                }
            } catch (VedantuException e) {
                LOGGER.error("Exception at getReferralData function", e);
            }
        }
        response.existingRewardPoints = referrer.rewards;
        if (referrer.referralCode == null) {
            referrer.referralCode = OrgMemberDAO.INSTANCE.generateReferralCode(referrer.firstName);
            OrgMemberDAO.INSTANCE.save(referrer);
            response.referralCode = referrer.referralCode;
        } else {
            response.referralCode = referrer.referralCode;
        }

        return response;

    }

    public static GetWalletBalanceRes getWalletBalance(GetWalletBalanceReq request) {
        GetWalletBalanceRes response = new GetWalletBalanceRes();
        OrgMember member = OrgMemberDAO.INSTANCE.getByUserId(request.userId);
        // If Freezed
        if (member.freezedRewardsOrderId != 0) {
            try {
                // If not Freezed, add back rewards
                if (!isRewardsFreezed(member)) {
                    member = OrgMemberDAO.INSTANCE.addBackRewards(request.userId);
                }
            } catch (VedantuException e) {
                LOGGER.error("Exception at getWalletBalance function", e);
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
        try {
            Order order = OrderDAO.INSTANCE.getOrderById(request.orderId);
            if ((order.totalAmount / 100) < response.existingRewardPoints
                    && response.maxRewardPointsToRedeem != 0) {
                response.maxRewardPointsToRedeem = order.totalAmount / 100;
            }
        } catch (VedantuException e1) {
            LOGGER.error("Exception at getWalletBalance function while checking orderId", e1);
        }
        return response;
    }

    public static UserExistenceRes isValidReferralCode(UserExistenceReq userExistenceReq) {
        boolean doesReferralCodeExists = OrgMemberDAO.INSTANCE
                .isValidReferralCode(userExistenceReq.referralCode);
        UserExistenceRes userExistenceRes = new UserExistenceRes();
        userExistenceRes.doesReferralCodeExists = doesReferralCodeExists;
        return userExistenceRes;
    }

    public static boolean isRewardsFreezed(OrgMember referrer) throws VedantuException {
        if (referrer.freezedRewardsOrderId > 0) {
            Order order = OrderDAO.INSTANCE.getOrderById(referrer.freezedRewardsOrderId);
            if (order == null) {
                throw new VedantuException(VedantuErrorCode.INVALID_ORDER_ID, "Order is NOT valid");
            }
            if (order.orderState.equals(OrderState.AWAITING_PAYMENT)
                    || order.orderState.equals(OrderState.DRAFT)) {
                // Freezing time of 10 mins
                if (System.currentTimeMillis() < (order.lastUpdated + FREEZE_TIME)) {
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        }
        return false;
    }

    public static SendOTPRes verifyContactNumber(SendOTPReq request) throws VedantuException {
        SendOTPRes response = new SendOTPRes();
        response.contactNumber = request.contactNumber;
        response.fullname = request.fullName;
        response.countryCode = request.countryCode;
        response.isNewPhone = OrgMemberDAO.INSTANCE.isNewPhone(request.contactNumber, request.countryCode);
        if(response.isNewPhone == true)
            return response;
        else{
            OrgMemberDAO.INSTANCE.getUsersIdByContact(request.contactNumber, request.countryCode);
        }
        return response;
    }

    public static SendOTPRes sendOTP(SendOTPReq request) throws VedantuException {
        SendOTPRes response = new SendOTPRes();
        response.hasEmail = OrgMemberDAO.INSTANCE.checkIfUserEmailExists(request.contactNumber,request.countryCode);
        response.isNewPhone = OrgMemberDAO.INSTANCE.isNewPhone(request.contactNumber, request.countryCode);
        response.OTP = request.existingOTP.isEmpty() ? generateOTP(OTPSize) : request.existingOTP;
        // This message should match the template of smCountry. You can't change
        // the template here.
        String message = "Hello! Welcome to Learnpedia Family. Your verification code is " + response.OTP
                + " We are delighted to have you as one of our valuable customers. Happy to help.";

        response.smsReference = sendOTP(request.countryCode+request.contactNumber, message , request.orgId);
        response.contactNumber = request.contactNumber;
        if(StringUtils.isNotEmpty(request.fullName)){
            response.fullname = request.fullName;
        }
        if(StringUtils.isNotEmpty(request.progType)){
            response.progType = request.progType;
        }
        response.countryCode = request.countryCode;
        return response;
    }

    public static SendOTPRes sendOTPApp(SendOTPReq request) throws VedantuException {
        SendOTPRes response = new SendOTPRes();
        response.hasEmail = OrgMemberDAO.INSTANCE.checkIfUserEmailExists(request.contactNumber,request.countryCode);
        response.isNewPhone = OrgMemberDAO.INSTANCE.isNewPhone(request.contactNumber, request.countryCode);
        if(!response.isNewPhone){
            return response;
        }
        response.OTP = request.existingOTP.isEmpty() ? generateOTP(OTPSize) : request.existingOTP;
        // This message should match the template of smCountry. You can't change
        // the template here.
        String message = "Hello! Welcome to Learnpedia Family. Your verification code is " + response.OTP
                + " We are delighted to have you as one of our valuable customers. Happy to help.";
        response.smsReference = sendOTP(request.countryCode+request.contactNumber, message , request.orgId);
        response.contactNumber = request.contactNumber;
        if(StringUtils.isNotEmpty(request.fullName)){
            response.fullname = request.fullName;
        }
        if(StringUtils.isNotEmpty(request.progType)){
            response.progType = request.progType;
        }
        response.countryCode = request.countryCode;
        return response;
    }

    public static ValidateOTPRes validateOTP(ValidateOTPReq request) throws VedantuException {
        ValidateOTPRes response = new ValidateOTPRes();
        if (request.sessionOTP.equals(request.userOTP)) {
            LOGGER.info("OTP validated");
            if (OrgMemberDAO.INSTANCE.checkIfUserEmailExists(request.contactNumber,request.countryCode) == false
                    && OrgMemberDAO.INSTANCE.isNewPhone(request.contactNumber,request.countryCode) == true) {
                response.isNewUser = true;
            }
        } else {
            throw new VedantuException(VedantuErrorCode.INVALID_CODE, "OTP is NOT valid");
        }
        return response;
    }

    public static SendOTPRes validateContactNumber(SendOTPReq request) throws VedantuException {
        SendOTPRes response = new SendOTPRes();
        UserDAO.INSTANCE.validatePhoneNumber(request.userId);
        OrgMemberDAO.INSTANCE.addOrUpdateContactNumber(request.userId, request.contactNumber,request.countryCode);
        return response;
    }

    public static String sendOTP(String mobilenumber, String message , String orgId) throws VedantuException {
        String postData = "";
        String response = "";
        URL url;
        try {
            Organization organization = OrganizationDAO.INSTANCE.getOrganizationById(orgId);
            if (organization == null) {
                throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
            }
            //Make message dynamic by adding respective Organization Name.
            //Also make sure the specific template is available for respective organization for SMSCOUNTRY.
            message = message.replace("Learnpedia", organization.fullName);
            SmsGatewayInfo smsGateway = organization.smsGateway;
            if(smsGateway != null){
                if(smsGateway.host.equalsIgnoreCase("SMSCOUNTRY")){
                    postData = smsGateway.postData+"&mobilenumber=" + mobilenumber + "&message=" + URLEncoder.encode(message, "UTF-8");
                }
                else{
                    postData = smsGateway.postData+"&mobile=" + mobilenumber + "&message=" +(message);
                }
                LOGGER.error("SMSAPI postData "+postData);
                url = new URL(smsGateway.url);
                LOGGER.error("SMSAPI url "+url);
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
                LOGGER.error("SMSAPI "+response);
                in.close();
                return response;
            }
            else{
                throw new VedantuException(VedantuErrorCode.SMS_GATEWAY_DETAILS_NOT_FOUND);
            }
        } catch (MalformedURLException e) {
            LOGGER.error("MalformedURLException in sendOTP function", e);
        } catch (IOException e) {
            LOGGER.error("IOException in sendOTP function", e);
        }
        return response;
    }

    public static String generateOTP(int otpsize) {
        int startValue = (int) Math.pow(10, otpsize - 1);
        int endValue = (int) Math.pow(10, otpsize);
        return showRandomInteger(startValue, endValue);
    }

    private static String showRandomInteger(int aStart, int aEnd){
        Random random = new Random();
        //get the range, casting to long to avoid overflow problems
        long range = (long)aEnd - (long)aStart + 1;
        // compute a fraction of the range, 0 <= frac < range
        long fraction = (long)(range * random.nextDouble());
        int randomNumber =  (int)(fraction + aStart);
        return randomNumber+"";
      }

    public static GetSaleDetailsRes getSaleDetails(GetSaleDetailsReq request)
            throws VedantuException {
        GetSaleDetailsRes response = new GetSaleDetailsRes();
        OrgMemberMappingInfo mappingInfo = OrgMemberDAO.INSTANCE.getMemberMappingForSection(
                request.orgId, request.targetUserId, request.sectionId);
        String saleDetailsId = mappingInfo.saleDetailsId;
        if (saleDetailsId == null || saleDetailsId.isEmpty()) {
            response.saleDetailsInfo = null;
        } else {
            SaleDetails saleDetails = SaleDetailsDAO.INSTANCE.getById(saleDetailsId);
            response.saleDetailsInfo = new SaleDetailsInfo(saleDetails);
        }
        return response;
    }

    public static UpdateSaleDetailsRes updateSaleDetails(UpdateSaleDetailsReq request)
            throws VedantuException {
        UpdateSaleDetailsRes response = new UpdateSaleDetailsRes();
        SaleDetails saleDetails = SaleDetailsDAO.INSTANCE.updateSaleDetails(request.saleDetailsId,
                request.paymentItems, request.targetOrgMemberId);
        response.done = true;
        response.saleDetailsInfo = new SaleDetailsInfo(saleDetails);
        return response;
    }

    public static UserExistenceRes doesContactNumberExists(UserExistenceReq userExistenceReq)
            throws VedantuException {
        boolean doesContactNumberExists = OrgMemberDAO.INSTANCE
                .isNewPhone(userExistenceReq.contactNumber,userExistenceReq.countryCode);
        UserExistenceRes userExistenceRes = new UserExistenceRes();
        userExistenceRes.doesContactNumberExists = doesContactNumberExists;
        return userExistenceRes;
    }

    public static GetCountOfStudentsRes getStudentsCount(GetCountOfStudentsReq request) {

        GetCountOfStudentsRes res = new GetCountOfStudentsRes();
        MutableLong totalHits = new MutableLong();
        List<GranteeOrgProgram> programs = GranteeOrgProgramDAO.INSTANCE.getProgramsGrantedToMe(
                request.orgId, null, totalHits);
        Set<String> programIds = new HashSet<String>();
        String learnpediaOrgId = request.orgId;
        for (GranteeOrgProgram program : programs) {
            learnpediaOrgId = program.providerOrgId;
            programIds.add(program.programId);
        }
        Map<String, OrgProgram> programIdsInfo = new HashMap<String, OrgProgram>();
        if (programIds != null && !programIds.isEmpty()) {
            programIdsInfo = OrgProgramDAO.INSTANCE.getProgramsMapByIds(programIds);
        }

        Map<String, RevenueModel> revenueModel = new HashMap<String, RevenueModel>();
        for (String programId : programIds) {
            MutableLong totalHitsSections = new MutableLong();
            List<OrgSection> sectionsOfaProgram = OrgSectionDAO.INSTANCE.getSectionsById(
                    learnpediaOrgId, programId, null, null, null, VedantuRecordState.ACTIVE,
                    MongoManager.NO_START, MongoManager.NO_LIMIT, totalHitsSections);
            for (OrgSection section : sectionsOfaProgram) {
                revenueModel.put(section._getStringId(), section.revenueModel);
            }
        }

        List<OrgProgram> programsCreatedByOrganization = OrgProgramDAO.INSTANCE.getPrograms(
                request.orgId, null, totalHits);

        Set<String> programIdsCreatedByOrg = new HashSet<String>();
        for (OrgProgram program : programsCreatedByOrganization) {
            programIdsCreatedByOrg.add(program._getStringId());
        }
        Map<String, OrgProgram> programIdsCreatedByOrgInfo = new HashMap<String, OrgProgram>();
        if (programIdsCreatedByOrg != null && !programIdsCreatedByOrg.isEmpty()) {
            programIdsCreatedByOrgInfo = OrgProgramDAO.INSTANCE
                    .getProgramsMapByIds(programIdsCreatedByOrg);
        }

        MutableLong countOfStudents = new MutableLong();

        List<OrgMember> orgMembers = OrgMemberDAO.INSTANCE.getOrgMembers(request.orgId,
                OrgMemberProfile.STUDENT, null, null, null, null, null, null,
                MongoManager.NO_START, MongoManager.NO_LIMIT, null, false, countOfStudents);
        Map<String, StudentsProgramDuration> responseMap = new HashMap<String, StudentsProgramDuration>();
        for (String programId : programIds) {
            String programName = programIdsInfo.get(programId).getName();
            responseMap.put(programId, new StudentsProgramDuration(programName));
        }
        for (String programId : programIdsCreatedByOrg) {
            String programName = programIdsCreatedByOrgInfo.get(programId).getName();
            responseMap.put(programId, new StudentsProgramDuration(programName));
        }
        member: for (OrgMember member : orgMembers) {
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
            mapping: for (OrgMemberMappingInfo mapping : allUserMappings) {
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
        while(iterator.hasNext()) {
            Map.Entry<String, StudentsProgramDuration> entry = iterator.next();
            StudentsProgramDuration studDuration = entry.getValue();
            if (studDuration.numberOfOneYear + studDuration.numberOfSixMonths
                    + studDuration.numberOfTwoYears == 0) {
                iterator.remove();
            }
        }
        res.countResponse = responseMap;
        return res;
    }

    public static boolean getIfUserIsFree(OrgMember member, Map<String, RevenueModel> revenueModel,
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

    public static boolean checkIfUserIsaAppUser(OrgMember member, Set<String> programIds,
            Map<String, RevenueModel> revenueModel, long startDate, long endDate) {
        boolean isAppUser = false;
        List<OrgMemberMappingInfo> allUserMappings = new ArrayList<OrgMemberMappingInfo>();
        allUserMappings.addAll(member.mappings);
        if (member.expiredMappings != null) {
            allUserMappings.addAll(member.expiredMappings);
        }
        for (OrgMemberMappingInfo mapping : allUserMappings) {
            if(startDate > mapping.timeJoined || mapping.timeJoined > endDate) {
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

    public static int getDurationOfUserMapping(OrgMemberMappingInfo mapping) {
        if (mapping.endTime == 0) {
            return 24;
        }
        if(mapping.timeJoined > mapping.endTime){
            return 0;
        }
        Date startDate = new Date(mapping.timeJoined);
        Date endDate = new Date(mapping.endTime);
        DateTime startDateTime = new DateTime(startDate);
        DateTime endDateTime = new DateTime(endDate);
        Days days = Days.daysBetween(startDateTime, endDateTime);
        int daysDifference = days.getDays();
        int monthsDifference = daysDifference/30;
        if(daysDifference%30>0){
            monthsDifference++;
        }
        return monthsDifference;
    }

    public static TestUserDataRes saveTestUserData(TestUserDataReq request) throws VedantuException {

        OrgMember admin = OrgMemberDAO.INSTANCE.getMemberByUserId(request.orgId, request.adminUserId);
        if(admin == null || admin.profile != OrgMemberProfile.MANAGER) {
            throw new VedantuException(VedantuErrorCode.ACCESS_DENIED, "Access to this service is denied for you");
        }
        TestUser testUser = TestUserDAO.INSTANCE.getTestUserByInstitueId(request.memberId);
        TestUserDataRes response = new TestUserDataRes();
        if(testUser == null) {
            try {
                testUser = new TestUser();
                testUser.createFromReq(request);
                TestUserDAO.INSTANCE.save(testUser);
                AddOrgMemberRes orgmemberRes = null;
                LOGGER.debug("saveTestUserData : Checking wheather student exist with memberId");
                OrgMember member = OrgMemberDAO.INSTANCE.getMemberByMemberId(request.orgId, request.memberId);
                if(member == null){
                    LOGGER.debug("saveTestUserData : Checking wheather student exist with email");
                    member = OrgMemberDAO.INSTANCE.getOrgMemberWithEmail(request.orgId, request.email);
                }
                if(member == null) {
                    LOGGER.debug("saveTestUserData : Checking wheather student exist with Mobile");
                    member = OrgMemberDAO.INSTANCE.getByUserPhone(request.studentsMobile, "+91");
                }
                if (member == null) {
                    LOGGER.debug("saveTestUserData : No user found. So creating one");
                    AddOrgMemberReq orgMemberReq = new AddOrgMemberReq();
                    orgMemberReq.fromTestDataReq(request);
                    orgmemberRes = addOrgMember(orgMemberReq);
                    if(StringUtils.isNotEmpty(orgmemberRes.userId)) {
                        testUser.userId = orgmemberRes.userId;
                    } else {
                        throw new VedantuException(VedantuErrorCode.USER_LOGIN_NOT_FOUND);
                    }
                }
                else {
                    LOGGER.debug("saveTestUserData : User found");
                    testUser.userId = member.userId;
                }

                TestUserDAO.INSTANCE.save(testUser);
                response.userAdded = true;
                response.userId = testUser.userId;
                addMappingForTestUser(request, response);

            }catch(VedantuException ve) {
                LOGGER.error("Error while saving testUserData", ve);
                throw ve;
            } catch (Exception e) {
                LOGGER.error("Error while saving testUserData", e);
                throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
            }
        }
        else {
            response.userId = testUser.userId;
            response.userAlreadyExists = true;
            addMappingForTestUser(request, response);
        }
        return response;
    }

    private static void addMappingForTestUser(TestUserDataReq request, TestUserDataRes response) throws VedantuException{
        OrgSection section = OrgSectionDAO.INSTANCE.getSectionById(request.orgId, request.sectionId);
        if(section == null) {
            throw new VedantuException(VedantuErrorCode.INVALID_SECTION_ID);
        }
        OrgMember member = OrgMemberDAO.INSTANCE.getMemberByUserId(request.orgId, response.userId);
        if(member == null) {
            throw new VedantuException(VedantuErrorCode.USER_LOGIN_NOT_FOUND);
        }
        AddOrgMemberMappingReq addReq = new AddOrgMemberMappingReq();
        addReq.createFromTestUserReq(request, section.programId, section.centerId, member);
        AddOrgMemberMappingRes addRes = addOrgMemberMapping(addReq, true);
        if (addRes.done) {
            response.mappingAdded = true;
        }
        else {
            throw new VedantuException(VedantuErrorCode.INVALID_CODE);
        }
    }

    public static File getReferralUsersData(String referralCode) throws VedantuException{
        if(StringUtils.isEmpty(referralCode)) {
            throw new VedantuException(VedantuErrorCode.INVALID_REFERER);
        }

        LocalFileSystemHandler tempFileSystemHandler = FileSystemFactory.INSTANCE
                .getTempFS();
        File generatedFile = tempFileSystemHandler.getFileWithSpecifiedName("ReferralData",
                "studentsData" + UUID.randomUUID().toString(), FileUtils.CSV_EXTENTION_WITHOUT_DOT);
        BufferedWriter writer = null;
        final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            String referrerUserId = OrgMemberDAO.INSTANCE.getUserIdFromReferralCode(referralCode);
            if(referrerUserId == null) {
                throw new VedantuException(VedantuErrorCode.INVALID_REFERER);
            }
            List<OrgMember> referrerUsers = OrgMemberDAO.INSTANCE.getReferredUsers(referrerUserId);
            if(referrerUsers == null || referrerUsers.isEmpty()) {
                return null;
            }
            writer = new BufferedWriter(new PrintWriter(generatedFile));
            writer.write("Date,Student Name,Student Email,Student Mobile," +
                    "Referrer Code,Referrer Name,Referrer Mobile,ReferrerEmail");
            for (OrgMember referrerUser : referrerUsers) {
                List<OrgMember> referredUsers = OrgMemberDAO.INSTANCE.getReferredUsers(referrerUser.userId);
                if(referredUsers == null || referredUsers.isEmpty()) {
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
            LOGGER.error(e.getMessage(), e);
            return null;
        }
        finally {
            IOUtils.closeQuietly(writer);
        }
        generatedFile.deleteOnExit();
        return generatedFile;
    }

    public static File getTestUsersData(String orgId, Long startDate, Long endDate) throws VedantuException{
        if(StringUtils.isEmpty(orgId)) {
            throw new VedantuException(VedantuErrorCode.ACCESS_DENIED);
        }
        LocalFileSystemHandler tempFileSystemHandler = FileSystemFactory.INSTANCE
                .getTempFS();
        File generatedFile = tempFileSystemHandler.getFileWithSpecifiedName("testUsersData",
                "studentsData" + UUID.randomUUID().toString(), FileUtils.CSV_EXTENTION_WITHOUT_DOT);
        BufferedWriter writer = null;
        final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            List<TestUser> testUsers = TestUserDAO.INSTANCE.getTestUsers(orgId,startDate,endDate);
            if(testUsers == null || testUsers.isEmpty()) {
                return null;
            }
            writer = new BufferedWriter(new PrintWriter(generatedFile));
            writer.write("Date,Student Name,Student Email,Member Id,Father Mobile," +
                    "DOB,Address,Caste,School Name,School Place,Medium,Percentage,Course,"+
            "Maths Marks, Science Marks,English Marks,Qualified For,Ambition,"+
                    "Father Name,Father Qualification,Subcaste,Mother Occupation");
            for (TestUser testUser: testUsers) {
                    writer.newLine();
                    writer.write(sdf.format(new Date(testUser.timeCreated)) + ",");
                    writer.write((testUser.name == null ? "N.A":escapeCommaForCsv(testUser.name)) + ",");
                    writer.write((testUser.email == null ? "N.A":escapeCommaForCsv(testUser.email)) + ",");
                    writer.write((testUser.memberId == null ? "N.A":escapeCommaForCsv(testUser.memberId)) + ",");
                    writer.write((testUser.studentsMobile == null ? "N.A":escapeCommaForCsv(testUser.studentsMobile)) + ",");
                    writer.write((testUser.dob == null ? "N.A":escapeCommaForCsv(testUser.dob)) + ",");
                    writer.write((testUser.address == null ? "N.A":escapeCommaForCsv(testUser.address)) + ",");
                    writer.write((testUser.caste == null ? "N.A":escapeCommaForCsv(testUser.caste)) + ",");
                    writer.write((testUser.schoolName == null ? "N.A":escapeCommaForCsv(testUser.schoolName)) + ",");
                    writer.write((testUser.schoolPlace == null ? "N.A":escapeCommaForCsv(testUser.schoolPlace)) + ",");
                    writer.write((testUser.medium == null ? "N.A":escapeCommaForCsv(testUser.medium)) + ",");
                    writer.write(testUser.percentage + ",");
                    writer.write((testUser.course == null ? "N.A":escapeCommaForCsv(testUser.course)) + ",");
                    writer.write(testUser.mathMarks + ",");
                    writer.write(testUser.scienceMarks + ",");
                    writer.write(testUser.englishMarks + ",");
                    writer.write((testUser.qualifiedFor == null ? "N.A":escapeCommaForCsv(testUser.qualifiedFor)) + ",");
                    writer.write((testUser.ambition == null ? "N.A":escapeCommaForCsv(testUser.ambition)) + ",");
                    writer.write((testUser.fatherName == null ? "N.A":escapeCommaForCsv(testUser.fatherName)) + ",");
                    writer.write((testUser.fatherQualification == null ? "N.A":escapeCommaForCsv(testUser.fatherQualification)) + ",");
                    writer.write((testUser.subcaste == null ? "N.A":escapeCommaForCsv(testUser.subcaste)) + ",");
                    writer.write(testUser.motherQualification == null ? "N.A":escapeCommaForCsv(testUser.motherQualification));
                }
            writer.flush();

        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new VedantuException(VedantuErrorCode.CANNOT_WRITE_FILE);
        }
        finally {
            IOUtils.closeQuietly(writer);
        }
        generatedFile.deleteOnExit();
        return generatedFile;
    }

    private static String escapeCommaForCsv(String text) {
        return "\""+text+"\"";
    }

    public static File getStudentsData(String orgId, Long startDate, Long endDate) throws VedantuException {
        if(StringUtils.isEmpty(orgId)) {
            throw new VedantuException(VedantuErrorCode.ACCESS_DENIED);
        }
        LocalFileSystemHandler tempFileSystemHandler = FileSystemFactory.INSTANCE
                .getTempFS();
        File generatedFile = tempFileSystemHandler.getFileWithSpecifiedName("studentUsersData",
                "studentsData" + UUID.randomUUID().toString(), FileUtils.CSV_EXTENTION_WITHOUT_DOT);
        BufferedWriter writer = null;
        final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            List<OrgMember> students = OrgMemberDAO.INSTANCE.getStudents(orgId,startDate,endDate);
            if(CollectionUtils.isEmpty(students)) {
                return null;
            }
            // Get Shared Programs of this organisation
            List<GranteeOrgProgram> sharedPrograms = GranteeOrgProgramDAO.INSTANCE.getGranteeOrgPrograms(orgId);
            // Get all sections of this organisation
            List<OrgSection> sections = OrgSectionDAO.INSTANCE.getOrganizationSections(orgId);
            // Get all sections of shared program
            for(GranteeOrgProgram sharedProgram: sharedPrograms){
                sections.addAll(OrgSectionDAO.INSTANCE.getSectionsByProgramId(sharedProgram.programId));
            }
            Map<String,String> programMap = new HashMap<String, String>();
            Map<String,String> centerMap = new HashMap<String, String>();

            Map<String, String> programsDataMap = new HashMap<String, String>();
            OrgProgram program = null;
            OrgCenter center = null;
            for(OrgSection section:sections){
                if(!programMap.containsKey(section.programId)){
                    program = OrgProgramDAO.INSTANCE.getProgramById(orgId, section.programId);
                    programMap.put(section.programId, program.getName());
                }
                if(!centerMap.containsKey(section.centerId)){
                    center = OrgCenterDAO.INSTANCE.getCenterById(section.centerId);
                    centerMap.put(section.centerId, center.getName());
                }
                programsDataMap.put(section._getStringId(),programMap.get(section.programId)+"-"+centerMap.get(section.centerId)+"-"+section.getName());
            }
            writer = new BufferedWriter(new PrintWriter(generatedFile));
            writer.write("Date,Name,MemberId,Email,Student Number,Program,Extra Info");
            for (OrgMember student: students) {
                writer.newLine();
                writer.write(sdf.format(new Date(student.timeCreated)) + ",");
                writer.write(escapeCommaForCsv(student.firstName+" "+ student.lastName) +",");
                writer.write((student.memberId == null || student.memberId.isEmpty() ? "N/A":escapeCommaForCsv(student.memberId))+",");
                writer.write((student.email == null || student.email.isEmpty() ? "N/A": escapeCommaForCsv(student.email))+",");
                writer.write((student.contactNumber == null || student.contactNumber.isEmpty() ? "N/A": escapeCommaForCsv(student.contactNumber))+",");
                if(CollectionUtils.isNotEmpty(student.mappings)){
                    String sb = StringUtils.EMPTY;
                    for(OrgMemberMappingInfo mapping:student.mappings){
                        if(programsDataMap.containsKey(mapping.sectionId)){
                            sb = sb + programsDataMap.get(mapping.sectionId)+", ";
                        }
                    }
                    sb.trim();
                    if(sb.length() > 2){
                        sb = sb.substring(0, sb.length()-2);
                    }
                    writer.write(escapeCommaForCsv(sb)+",");
                }else{
                    writer.write("No Programs Found"+",");
                }
                if(CollectionUtils.isNotEmpty(student.extraInfo)){
                    String value = StringUtils.EMPTY;
                    for(OrgMemberExtraInfo extra:student.extraInfo ){
                        value += extra.name +": "+extra.value+"  & ";
                    }
                    value.trim();
                    if(value.length() > 2){
                        value = value.substring(0, value.length()-2);
                    }
                    writer.write(escapeCommaForCsv(value));
                }
                else{
                    writer.write("N/A");
                }
            }
            writer.flush();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new VedantuException(VedantuErrorCode.CANNOT_WRITE_FILE);
        }
        finally {
            IOUtils.closeQuietly(writer);
        }
        generatedFile.deleteOnExit();
        return generatedFile;
    }

    public static GetAllUserDataRes getAllUserData(GetAllUserDataReq request) throws VedantuException {
        if(StringUtils.isEmpty(request.orgId)) {
            throw new VedantuException(VedantuErrorCode.ACCESS_DENIED);
        }
        List<OrgMember> members = OrgMemberDAO.INSTANCE.getUsersByOrgId(request.orgId,OrgMemberProfile.STUDENT,request.targetUserId,request.lastUpdated);
        if(CollectionUtils.isEmpty(members)) {
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        Map<String,OrgMember> orgMemberMap = new LinkedHashMap<String, OrgMember>();
        Map<String, User> userMap = new HashMap<String, User>();
        Map<String,String> userSalts = new LinkedHashMap<String, String>();
        for(OrgMember member:members){
            orgMemberMap.put(member.userId, member);
        }
        List<String> userIds = new ArrayList<String>(orgMemberMap.keySet());
        String[] fields = {};
        List<User> users = UserDAO.INSTANCE.getUsers(userIds,fields);
        List<String> usernames = new ArrayList<String>();
        for(User user: users){
            usernames.add(user.username);
            userMap.put(user._getStringId(), user);
        }
        List<UserSalt> usersSalts = UserSaltDAO.INSTANCE.getUserSalts(usernames, fields);
        for(UserSalt userSalt : usersSalts){
            userSalts.put(userSalt.username, userSalt.salt);
        }
        List<UserOrgAuth> userOrgAuthList = new ArrayList<UserOrgAuth>();
        for(String userId:userIds){
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
            userOrgAuth.latestTnCVersion = Play.application().configuration()
                    .getString("tnc.version");
            userOrgAuth.needsTnCAcceptance = null == user.tncAcceptance;
            userOrgAuth.memberId = orgMemberMap.get(user._getStringId()).memberId;
            userOrgAuth.salt = userSalts.get(user.username);
            OrgMemberExtendedInfo orgMemberExtendedInfo = (OrgMemberExtendedInfo) orgMemberMap.get(user._getStringId()).toExtendedInfo();
            populateUserPublicProfileDetails(orgMemberMap.get(user._getStringId()), orgMemberExtendedInfo);

            populateProgramHierarchy(orgMemberMap.get(user._getStringId()), orgMemberExtendedInfo, true);
            userOrgAuth.orgProfile.info = orgMemberExtendedInfo;
            userOrgAuth.orgProfile.key = UserManager.getPrivateKey(user._getStringId());
            userOrgAuthList.add(userOrgAuth);
        }
        GetAllUserDataRes res = new GetAllUserDataRes();
        res.users = userOrgAuthList;
        return res;
    }
}
