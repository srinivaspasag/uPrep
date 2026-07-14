package com.vedantu.organization.managers;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.commons.lang3.time.DateUtils;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.ning.http.util.Base64;
import com.vedantu.board.daos.BoardMappingsDAO;
import com.vedantu.board.models.BoardMapping;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.daos.GranteeOrgProgramDAO;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.entity.storage.ImageSize;
import com.vedantu.commons.entity.storage.OrganizationEntityFileStorage;
import com.vedantu.commons.entity.storage.StorageResult;
import com.vedantu.commons.enums.EncryptionLevel;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.AppSecurityCredentials;
import com.vedantu.commons.pojos.InputFieldInfo;
import com.vedantu.commons.pojos.Interval;
import com.vedantu.commons.pojos.SecurityCredentials;
import com.vedantu.commons.utils.DigExecutor;
import com.vedantu.commons.utils.EncryptionUtils;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.commons.utils.ImageFilter;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.commons.utils.image.ImageGenerator;
import com.vedantu.mongo.models.GranteeOrgProgram;
import com.vedantu.organization.daos.LicensingPlanDAO;
import com.vedantu.organization.daos.OrgMemberDAO;
import com.vedantu.organization.daos.OrgProgramDAO;
import com.vedantu.organization.daos.OrgSectionDAO;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.organization.enums.AccessScope;
import com.vedantu.organization.enums.OrgMemberProfile;
import com.vedantu.organization.enums.OrgMemberState;
import com.vedantu.organization.enums.OrganizationStatus;
import com.vedantu.organization.enums.OrganizationType;
import com.vedantu.organization.enums.PlanState;
import com.vedantu.organization.event.details.AddOrganizationEmailDetails;
import com.vedantu.organization.event.details.ApproveOrganizationEmailDetails;
import com.vedantu.organization.models.OrgMember;
import com.vedantu.organization.models.OrgProgram;
import com.vedantu.organization.models.OrgSection;
import com.vedantu.organization.models.Organization;
import com.vedantu.organization.models.Subscription;
import com.vedantu.organization.models.licensing.LicensingPlan;
import com.vedantu.organization.pojos.LicensingInfo;
import com.vedantu.organization.pojos.requests.members.AddOrgMemberReq;
import com.vedantu.organization.pojos.requests.organizations.AbstractAddOrgReq;
import com.vedantu.organization.pojos.requests.organizations.AcceptTncOrgReq;
import com.vedantu.organization.pojos.requests.organizations.AddOrgReq;
import com.vedantu.organization.pojos.requests.organizations.ApproveOrgReq;
import com.vedantu.organization.pojos.requests.organizations.CheckAppVersionReq;
import com.vedantu.organization.pojos.requests.organizations.CheckRefererReq;
import com.vedantu.organization.pojos.requests.organizations.CheckSlugReq;
import com.vedantu.organization.pojos.requests.organizations.CheckWebsiteReq;
import com.vedantu.organization.pojos.requests.organizations.GenerateAppCredentialsReq;
import com.vedantu.organization.pojos.requests.organizations.GetAssociatedOrgsReq;
import com.vedantu.organization.pojos.requests.organizations.GetDigitalLibraryFieldsReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgBySlugReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgMemberExtraInfoInputFieldsReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgPointsOfSaleReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgsReq;
import com.vedantu.organization.pojos.requests.organizations.GetSharedOrgsReq;
import com.vedantu.organization.pojos.requests.organizations.RemoveProgramSharingReq;
import com.vedantu.organization.pojos.requests.organizations.UpdateDigitalLibraryFieldsReq;
import com.vedantu.organization.pojos.requests.organizations.UpdateOrgMemberExtraInfoInputFieldsReq;
import com.vedantu.organization.pojos.requests.organizations.UpdateOrgRefererReq;
import com.vedantu.organization.pojos.requests.organizations.UpdateOrgReq;
import com.vedantu.organization.pojos.requests.organizations.UpdateOrgSlugReq;
import com.vedantu.organization.pojos.requests.organizations.UpdateOrgStatusReq;
import com.vedantu.organization.pojos.requests.organizations.UpdateOrganizationClassroomConnectStatus;
import com.vedantu.organization.pojos.requests.organizations.UpdateOrganizationDownloadStatus;
import com.vedantu.organization.pojos.requests.organizations.UpdateOrganizationSharedSubjects;
import com.vedantu.organization.pojos.requests.organizations.UploadOrgPicReq;
import com.vedantu.organization.pojos.requests.organizations.VerifyAppCredentialsReq;
import com.vedantu.organization.pojos.responses.licensing.AvailablePlansRes;
import com.vedantu.organization.pojos.responses.members.AddOrgMemberRes;
import com.vedantu.organization.pojos.responses.organizations.AddOrgRes;
import com.vedantu.organization.pojos.responses.organizations.ApproveOrgRes;
import com.vedantu.organization.pojos.responses.organizations.CheckAppVersionRes;
import com.vedantu.organization.pojos.responses.organizations.CheckSlugRes;
import com.vedantu.organization.pojos.responses.organizations.GenerateAppCredentialsRes;
import com.vedantu.organization.pojos.responses.organizations.GetAssociatedOrgsRes;
import com.vedantu.organization.pojos.responses.organizations.GetDigitalLibraryFieldsRes;
import com.vedantu.organization.pojos.responses.organizations.GetOrgMemberExtraInfoInputFieldsRes;
import com.vedantu.organization.pojos.responses.organizations.GetOrgPointsOfSaleRes;
import com.vedantu.organization.pojos.responses.organizations.GetOrgRes;
import com.vedantu.organization.pojos.responses.organizations.GetOrgResForInvoice;
import com.vedantu.organization.pojos.responses.organizations.GetOrgsRes;
import com.vedantu.organization.pojos.responses.organizations.GetSharedOrgsRes;
import com.vedantu.organization.pojos.responses.organizations.GetShowSharedSubjectsRes;
import com.vedantu.organization.pojos.responses.organizations.OrgAssociatedInfo;
import com.vedantu.organization.pojos.responses.organizations.OrgBasicInfo;
import com.vedantu.organization.pojos.responses.organizations.OrgInfo;
import com.vedantu.organization.pojos.responses.organizations.RemoveProgramSharingRes;
import com.vedantu.organization.pojos.responses.organizations.UpdateDigitalLibraryFieldsRes;
import com.vedantu.organization.pojos.responses.organizations.UpdateOrgMemberExtraInfoInputFieldsRes;
import com.vedantu.organization.pojos.responses.organizations.UpdateOrgRes;
import com.vedantu.organization.pojos.responses.organizations.UploadOrgPicRes;
import com.vedantu.organization.pojos.responses.organizations.VerifyAppCredentialsRes;
import com.vedantu.user.daos.UserDAO;
import com.vedantu.user.enums.Gender;
import com.vedantu.user.managers.AbstractVedantuEventManager;
import com.vedantu.user.managers.UserManager;
import com.vedantu.user.models.User;
import com.vedantu.user.pojos.TnCAcceptance;
import com.vedantu.user.pojos.UserEmailInfo;
import com.vedantu.user.pojos.UserExtendedInfo;
import com.vedantu.user.pojos.requests.AddUserReq;
import com.vedantu.user.pojos.responses.AcceptTnCRes;
import com.vedantu.user.pojos.responses.AddUserRes;
import com.vedantu.user.utils.PasswordUtils;

public class OrganizationManager extends AbstractVedantuEventManager {

    private static final ALogger LOGGER      = Logger.of(OrganizationManager.class);

    private static final String  TNC_VERSION = Play.application().configuration()
                                                     .getString("org.tnc.version");

    public static String getLatestTnC() {

        return TNC_VERSION;
    }

    public static CheckSlugRes checkSlug(CheckSlugReq checkSlugReq) throws VedantuException {

        if (!isValidSlug(checkSlugReq.slug)) {
            LOGGER.error("invalid slug: " + checkSlugReq.slug);
            throw new VedantuException(VedantuErrorCode.INVALID_SLUG);
        }
        Organization organization = OrganizationDAO.INSTANCE
                .getOrganizationBySlug(checkSlugReq.slug);
        CheckSlugRes res = new CheckSlugRes();
        res.available = organization == null;
        return res;
    }

	public static CheckAppVersionRes checkAppVersion(CheckAppVersionReq req)
			throws VedantuException {
		CheckAppVersionRes resp = new CheckAppVersionRes();
		LOGGER.debug("..... Inside checkAppVersion function..... orgId : "
				+ req.orgId + "  reqVersionCode  " + req.appVersion);
		boolean result = OrganizationDAO.INSTANCE.checkAppVersion(
				req.appVersion, req.orgId);
		resp.appVersion = result;
		return resp;
	}

    public static CheckSlugRes checkReferer(CheckRefererReq request) throws VedantuException {

        if (!isValidReferer(request.referer)) {
            LOGGER.error("invalid referer: " + request.referer);
            throw new VedantuException(VedantuErrorCode.INVALID_REFERER);
        }
        Organization organization = OrganizationDAO.INSTANCE
                .getOrganizationByReferer(request.referer);
        CheckSlugRes res = new CheckSlugRes();
        res.available = organization == null;
        return res;
    }

    public static CheckSlugRes checkWebsite(CheckWebsiteReq checkWebsiteReq)
            throws VedantuException {

        if (!isValidWebsite(checkWebsiteReq.website)) {
            LOGGER.error("invalid website: " + checkWebsiteReq.website);
            throw new VedantuException(VedantuErrorCode.INVALID_WEBSITE);
        }
        Organization organization = OrganizationDAO.INSTANCE
                .getOrganizationByWebsite(checkWebsiteReq.website);
        CheckSlugRes res = new CheckSlugRes();
        res.available = organization == null;
        return res;
    }

    public static GetOrgRes updateOrganizationSlug(UpdateOrgSlugReq updateOrgSlugReq)
            throws VedantuException {

        if (!isValidSlug(updateOrgSlugReq.slug)) {
            LOGGER.error("invalid slug: " + updateOrgSlugReq.slug);
            throw new VedantuException(VedantuErrorCode.INVALID_SLUG);
        }
        // TODO: make check for permission of the user to update the organization slug
        Organization organization = OrganizationDAO.INSTANCE.updateOrganizationSlug(
                updateOrgSlugReq.orgId, updateOrgSlugReq.slug);
        GetOrgRes getOrgRes = getOrgRes(organization, false);
        return getOrgRes;
    }

	public static GetOrgRes updateOrganizationStatus(
			UpdateOrgStatusReq updateOrgStatusReq) throws VedantuException {

		if (OrganizationStatus.valueOfKey(updateOrgStatusReq.status) == OrganizationStatus.UNKNOWN) {
			LOGGER.error("invalid status: " + updateOrgStatusReq.status);
			throw new VedantuException(VedantuErrorCode.INVALID_STATUS);
		}
		Organization organization = OrganizationDAO.INSTANCE
				.updateOrganizationStatus(updateOrgStatusReq.orgId,
						updateOrgStatusReq.status,updateOrgStatusReq.type);
		GetOrgRes getOrgRes = getOrgRes(organization, false);
		return getOrgRes;
	}

    public static GetOrgRes getOrganizationBySlug(GetOrgBySlugReq getOrgReq)
            throws VedantuException {

        Organization organization = OrganizationDAO.INSTANCE.getOrganizationBySlug(getOrgReq.slug);
        if (null == organization) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
        }
        GetOrgRes getOrgRes = getOrgRes(organization, getOrgReq.getKey);
        getOrgRes.authType = organization.authType;

        return getOrgRes;
    }

    public static GetOrgRes updateOrganizationReferrer(UpdateOrgRefererReq request)
            throws VedantuException {

        if (!request.remove) {

            if (StringUtils.isEmpty(request.referer) || !isValidReferer(request.referer)) {
                LOGGER.error("invalid slug: " + request.referer);
                throw new VedantuException(VedantuErrorCode.INVALID_REFERER);
            }
        }
        // TODO: make check for permission of the user to update the organization referer
        Organization organization = OrganizationDAO.INSTANCE.updateOrganizationReferrer(
                request.orgId, request.referer);
        GetOrgRes getOrgRes = getOrgRes(organization, false);
        getOrgRes.referer = organization.referer;
        return getOrgRes;
    }

    public static GetOrgRes getOrganizationByReferer(String referer, boolean getKey)
            throws VedantuException {

        LOGGER.debug("Referer asked for" + referer);
        String refererHost = null;
        try {
            URL refererURL = new URL(referer);
            if (isValidReferer(refererURL.getHost())) {
                refererHost = refererURL.getHost();
            } else {
                throw new VedantuException(VedantuErrorCode.INVALID_REFERER);
            }

        } catch (MalformedURLException e) {
            throw new VedantuException(VedantuErrorCode.INVALID_REFERER);
        }

        Organization organization = OrganizationDAO.INSTANCE.getOrganizationByReferer(refererHost);
        if (null == organization) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
        }

        GetOrgRes getOrgRes = getOrgRes(organization, getKey);
        getOrgRes.authType = organization.authType;
        getOrgRes.referer = organization.referer;
        return getOrgRes;
    }

    public static boolean isValidSlug(String slug) {

        return !slug.matches(".*([^a-zA-Z0-9.]).*");
    }

    public static boolean isValidReferer(String referer) {

        //
        // try {
        // InetAddress address = InetAddress.getByName(referer);
        // LOGGER.debug("Validated referer " + referer + "  with ip address "
        // + address.getHostAddress());
        // return true;
        // } catch (UnknownHostException e) {
        // LOGGER.error("Provided incorrect referer" + referer);
        //
        // }

        try {
            boolean checkReferrerEnabled = Play.application().configuration()
                    .getBoolean("check.referer.enabled");
            if (!checkReferrerEnabled) {
                return true;
            }
            DigExecutor executor = new DigExecutor();
            executor.setMatchCNAME(referer);
            String localSetup = Play.application().configuration().getString("deployment.domain");
            return executor.match(localSetup);
        } catch (VedantuException e) {
            LOGGER.error("Failed to resolve referer", e);
            return false;
        }

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

    public static GetOrgRes getOrganization(GetOrgReq getOrgReq) throws VedantuException {

        Organization organization = OrganizationDAO.INSTANCE.getOrganizationById(getOrgReq.orgId);
        BoardMapping boardMapping = BoardMappingsDAO.INSTANCE.getBySharedToOrgId("N.A", getOrgReq.orgId);
        GetOrgRes getOrgRes = getOrgRes(organization, getOrgReq.getKey);
        getOrgRes.showSharedSubjects = organization.showSharedSubjects;
        getOrgRes.showClassroomConnect = organization.showClassroomConnect;
        getOrgRes.disableDownload = organization.disableDownload;
        getOrgRes.enableOTP = organization.enableOTP;
        getOrgRes.authType = organization.authType;
        getOrgRes.pointsOfSale = organization.pointsOfSale;
        getOrgRes.doubtsForumMode = organization.doubtsForumMode;
        getOrgRes.disableSignup = organization.disableSignup;
        getOrgRes.disableSignupMessage = organization.disableSignupMessage;
        getOrgRes.smsGateway = organization.smsGateway;
        getOrgRes.isNewUI = organization.isNewUI;
        getOrgRes.theme = organization.theme;
        getOrgRes.status = organization.status;
        getOrgRes.studentPageStatus = organization.studentPageStatus;
        if(boardMapping != null){
            getOrgRes.sharedQuestionsState = boardMapping.publish ? "ENABLED":"DISABLED";
        }else{
            getOrgRes.sharedQuestionsState = "N.A";
        }

        if (StringUtils.isNotEmpty(getOrgReq.userId)
                && getOrgReq.userId.equalsIgnoreCase(organization.adminUserId)) {
            getOrgRes.needsTnCAcceptance = true;
            getOrgRes.endPoint = organization.endPoint;
            getOrgRes.socialMedia = organization.socialMedia;
            getOrgRes.referer = organization.referer;
            if (getOrgRes.endPoint != null) {
                getOrgRes.endPoint.convertNullToEmptyValues();
            }
            getOrgRes.latestTnCVersion = getLatestTnC();
            getOrgRes.appInfos = organization.appInfos;
            if (organization.tncAcceptance != null) {

                getOrgRes.acceptedTNCVersion = organization.tncAcceptance.version;
                if (organization.tncAcceptance.version.equalsIgnoreCase(getOrgRes.latestTnCVersion)) {
                    getOrgRes.needsTnCAcceptance = false;
                }
            }
        }

        return getOrgRes;
    }

    public static GetOrgResForInvoice getOrganizationInfoForInvoice(GetOrgReq getOrgReq) throws VedantuException{
        Organization organization = OrganizationDAO.INSTANCE.getOrganizationById(getOrgReq.orgId);
        GetOrgResForInvoice getOrgResForInvoice = new GetOrgResForInvoice();
        getOrgResForInvoice.address = organization.address;
        getOrgResForInvoice.name = organization.name;
        getOrgResForInvoice.representative = organization.representative;
        getOrgResForInvoice.orgThumbnail = organization._getThumbnailUrl();
        getOrgResForInvoice.contactNumber = organization.contactNumber;
        getOrgResForInvoice.locations = organization.locations;
        return getOrgResForInvoice;
    }

    public static GetOrgsRes getOrganizations(GetOrgsReq getOrgsReq) {

        GetOrgsRes getOrgsRes = new GetOrgsRes();
        MutableLong totalHits = new MutableLong(0L);
        List<Organization> organizations = OrganizationDAO.INSTANCE.getOrganizations(
                getOrgsReq.status, totalHits, getOrgsReq.query);
        if (CollectionUtils.isNotEmpty(organizations)) {
            getOrgsRes.totalHits = totalHits.getValue();
            for (Organization organization : organizations) {
                OrgInfo orgInfo = (OrgInfo) organization.toExtendedInfo();
                orgInfo.authType = organization.authType;
                getOrgsRes.list.add(orgInfo);
            }
        }
        return getOrgsRes;
    }

    //providerOrgId/ granteeId --- > Learnpedia --> ending with 926d1a
    //subscriberOrgId/ orgId --- > karthikgamenous ---> ending with 5095a9

    public static GetSharedOrgsRes getSharedOrgsByProg(GetSharedOrgsReq getSharedOrgsReq) {

    	MutableLong totalHits = new MutableLong(0L);
        List<Organization> organizations = OrganizationDAO.INSTANCE.getAllOrganizations(
                null, totalHits);
    	GetSharedOrgsRes getOrgsRes = new GetSharedOrgsRes();
    	MutableLong totalProgramHits = new MutableLong(0L);
        List<GranteeOrgProgram> granteeOrgPrograms = GranteeOrgProgramDAO.INSTANCE.getGranteeOrgByProgId(getSharedOrgsReq.providerOrgId, getSharedOrgsReq.programId, totalProgramHits);
        LOGGER.debug("OrganizationManager getSharedOrgsByProg "+granteeOrgPrograms+" size="+granteeOrgPrograms.size());
        List<String> alreadyExistingOrgIds = new ArrayList<String>();
        if (CollectionUtils.isNotEmpty(granteeOrgPrograms)) {
            getOrgsRes.totalHits = totalProgramHits.getValue();
            for (GranteeOrgProgram granteeOrg : granteeOrgPrograms) {
                getOrgsRes.list.add(granteeOrg);
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


    public static GetSharedOrgsRes sharedProgToOrg(GetSharedOrgsReq getSharedOrgsReq) throws VedantuException{

        MutableLong totalHits = new MutableLong(0L);
        GetSharedOrgsRes getOrgsRes = new GetSharedOrgsRes();
        MutableLong totalProgramHits = new MutableLong(0L);
        List<GranteeOrgProgram> granteeOrgPrograms = GranteeOrgProgramDAO.INSTANCE.getGranteeOrgByProgId(getSharedOrgsReq.providerOrgId, getSharedOrgsReq.programId, totalProgramHits);
        if(hasSharedProgramAccess(getSharedOrgsReq.programId)){
            // Check limit, Should not share this program to not more than one organisation.
            if(granteeOrgPrograms.size() >= 1){
                throw new VedantuException(VedantuErrorCode.CAN_NOT_SHARE_THIS_PROGRAM);
            }
        }
        GranteeOrgProgram addedGranteeOrgProg = null;
        try {
            addedGranteeOrgProg = GranteeOrgProgramDAO.INSTANCE.addGranteeOrgProgram(
                    getSharedOrgsReq.providerOrgId, getSharedOrgsReq.subscriberOrgId,
                    getSharedOrgsReq.programId);
            LOGGER.debug("Shared granteeOrgProg " + addedGranteeOrgProg);
        } catch (VedantuException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        granteeOrgPrograms = GranteeOrgProgramDAO.INSTANCE.getGranteeOrgByProgId(getSharedOrgsReq.providerOrgId, getSharedOrgsReq.programId, totalProgramHits);
        LOGGER.debug("OrganizationManager sharedProgToOrg "+granteeOrgPrograms+" size="+granteeOrgPrograms.size());
        List<String> alreadyExistingOrgIds = new ArrayList<String>();
        if (CollectionUtils.isNotEmpty(granteeOrgPrograms)) {
            getOrgsRes.totalHits = totalProgramHits.getValue();
            for (GranteeOrgProgram granteeOrg : granteeOrgPrograms) {
                getOrgsRes.list.add(granteeOrg);
                alreadyExistingOrgIds.add(granteeOrg.subscriberOrgId);
            }
        }
        List<Organization> organizations = OrganizationDAO.INSTANCE.getAllOrganizations(
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

    private static boolean hasSharedProgramAccess(String programId) {
        // TODO Auto-generated method stub
        OrgProgram prog = OrgProgramDAO.INSTANCE.getById(programId);
        return prog.sharedProgramAccess;
    }

    public static RemoveProgramSharingRes removeSharedProgramFromOrg(
            RemoveProgramSharingReq removeSharingReq) {

        RemoveProgramSharingRes removeProgramSharingRes = new RemoveProgramSharingRes();
        GranteeOrgProgramDAO.INSTANCE.removeProgramSharing(
                removeSharingReq.providerOrgId,removeSharingReq.programId,
                removeSharingReq.subscriberOrgId);
        OrgMemberDAO.INSTANCE.removeSharedProgramMapping(removeSharingReq.subscriberOrgId,
                removeSharingReq.programId);
        removeProgramSharingRes.done = true;
        return removeProgramSharingRes;
    }

    public static AddOrgRes addOrganization(AddOrgReq addOrgReq) throws VedantuException {

        OrganizationType type = OrganizationType.valueOfKey(addOrgReq.type);
        Scope scope = Scope.valueOfKey(addOrgReq.scope);
        OrganizationStatus status = OrganizationStatus.REQUESTED;
        OrganizationStatus studentPageStatus = OrganizationStatus.REQUESTED;
        // check if encryption keys are need to be generated
        if (addOrgReq.encLevel == null) {
            addOrgReq.encLevel = EncryptionLevel.NA;
        }
        SecurityCredentials credentials = EncryptionUtils.generateKeys();
        long requestTime = System.currentTimeMillis();

        TnCAcceptance acceptance = new TnCAcceptance(true, addOrgReq.tncVersion, requestTime,
                addOrgReq.representative.getEmail());

        LicensingPlan plan = LicensingPlanDAO.INSTANCE.getById(addOrgReq.planId);

        Subscription subscription = new Subscription();
        subscription.planId = addOrgReq.planId;
        Date date = new Date(requestTime);

        subscription.validity = new Interval(requestTime, plan.peruser ? -1 : DateUtils.addYears(
                date, 1).getTime());
        Organization organization = OrganizationDAO.INSTANCE.addOrganization(addOrgReq.name,
                addOrgReq.fullName, addOrgReq.getWebsite(), addOrgReq.getEmailDomain(),
                addOrgReq.contactNumber, type, addOrgReq.locations, addOrgReq.address,
                addOrgReq.description, scope, addOrgReq.slug, addOrgReq.representative, status, studentPageStatus,
                credentials, addOrgReq.encLevel, subscription, acceptance, addOrgReq.theme, addOrgReq.isNewUI, addOrgReq.showSharedSubjects);

        AddOrgRes addOrgRes = new AddOrgRes();
        if (null != organization) {
            addOrgRes.id = organization._getStringId();
            LOGGER.debug("organization created with id: " + addOrgRes.id);
        }

        UserEmailInfo userEmailInfo = new UserEmailInfo();
        userEmailInfo.email = addOrgReq.representative.getEmail();
        userEmailInfo.firstName = addOrgReq.representative.firstName;
        userEmailInfo.lastName = addOrgReq.representative.lastName;
        userEmailInfo.id = organization._getStringId();
        generateAddOrganizationEmailEvent(organization, userEmailInfo);
        return addOrgRes;
    }

    public static GetOrgPointsOfSaleRes getOrganizationPoinsOfSale(GetOrgPointsOfSaleReq req)
            throws VedantuException {

        GetOrgPointsOfSaleRes res = new GetOrgPointsOfSaleRes();
        Organization org = OrganizationDAO.INSTANCE.getOrganizationById(req.orgId);
        res.pointsOfSale = org.pointsOfSale == null ? new ArrayList<String>() : org.pointsOfSale;
        return res;
    }

    private static boolean generateAddOrganizationEmailEvent(Organization organization,
            UserEmailInfo userEmailInfo) throws VedantuException {

        AddOrganizationEmailDetails details = null;
        try {
            details = new AddOrganizationEmailDetails();
        } catch (ClassNotFoundException e) {
            Logger.debug(" Not found email details", e);
            throw new VedantuException(VedantuErrorCode.EMAIL_CAN_NOT_BE_SEND);
        }

        details.user = userEmailInfo;

        details.addRecepient(details.user.getFullName(), details.user.email);
        details.orgInfo = (OrgInfo) organization.toExtendedInfo();
        details.organization = organization;
        details.orgId = organization._getStringId();

        // informing vedantu contact about this activity
        String contactPerson = Play.application().configuration()
                .getString("email.vedantu.contact");
        details.addCCRecepient("Vedantu", contactPerson);
        //
        generateEventAysc(userEmailInfo.id, details, EventType.SEND_INSTANT_EMAIL);

        return true;

    }

    public static ApproveOrgRes approveOrganization(ApproveOrgReq approveOrgReq)
            throws VedantuException {

        Organization organization = OrganizationDAO.INSTANCE
                .getOrganizationById(approveOrgReq.orgId);
        if (null == organization) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
        }
        if (OrganizationStatus.APPROVED == organization.status) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_ALREADY_APPROVED);
        }
        if (OrganizationStatus.APPROVED == organization.studentPageStatus) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_ALREADY_APPROVED);
        }
        if (organization != null
                && (organization.subscription == null || StringUtils
                        .isEmpty(organization.subscription.planId))) {
            throw new VedantuException(VedantuErrorCode.INVALID_LICENSING_PLAN);
        }

        AvailablePlansRes plans = LicensingManager.getPlans(
                Arrays.asList(organization.subscription.planId), PlanState.ACTIVE);
        if (CollectionUtils.isEmpty(plans.list)) {
            throw new VedantuException(VedantuErrorCode.INVALID_LICENSING_PLAN);
        }

        User superAdminUser = UserDAO.INSTANCE.findUser(organization.representative.getEmail());
        String password = StringUtils.EMPTY;

        String userId = null;
        String adminPasswordResetURL = null;
        boolean isNewUserAdded = false;
        if (null == superAdminUser) {
            password = PasswordUtils.generateRandomPassword();
            AddUserReq addUserReq = new AddUserReq(organization.representative, password,
                    UserDAO.UNKNOWN_DOB, Gender.UNKNOWN);
            AddUserRes addUserRes = UserManager.addUser(addUserReq);

            userId = addUserRes.id;
            isNewUserAdded = true;
            superAdminUser = UserDAO.INSTANCE.getById(userId);
            adminPasswordResetURL = UserManager.generatePasswordUpdate(addUserRes.id,
                    organization._getStringId(),approveOrgReq.callingAppId);
        } else {
            userId = superAdminUser._getStringId();
        }

        OrgMember superAdminOrgMember = OrgMemberDAO.INSTANCE.getMemberByMemberId(
                approveOrgReq.orgId, OrgMemberDAO.SUPER_ADMIN_MEMBER_ID);
        String orgMemberId = null;

        boolean isNewOrgMemberAdded = false;
        if (null == superAdminOrgMember) {

            AddOrgMemberReq addOrgMemberReq = new AddOrgMemberReq(approveOrgReq.orgId,
                    OrgMemberDAO.SUPER_ADMIN_MEMBER_ID, organization.representative.firstName,
                    organization.representative.lastName, UserDAO.UNKNOWN_DOB, Gender.UNKNOWN,
                    organization.representative.getEmail(), OrgMemberProfile.MANAGER,
                    organization.contactNumber);
            AddOrgMemberRes addOrgMemberRes = OrgMemberManager.addOrgMember(addOrgMemberReq);

            orgMemberId = addOrgMemberRes.id;
            isNewOrgMemberAdded = true;
        } else {
            orgMemberId = superAdminOrgMember._getStringId();
        }

        organization = OrganizationDAO.INSTANCE.approveOrganization(approveOrgReq.orgId, userId,
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
        info.fromUserExtendedInfo((UserExtendedInfo) superAdminUser.toExtendedInfo());

        generateApproveOrganizationEmailEvent(organization, info, adminPasswordResetURL);

        return approveOrgRes;
    }

    private static boolean generateApproveOrganizationEmailEvent(Organization organization,
            UserEmailInfo userEmailInfo, String adminPasswordResetURL) throws VedantuException {

        ApproveOrganizationEmailDetails details = null;
        try {
            details = new ApproveOrganizationEmailDetails();
        } catch (ClassNotFoundException e) {
            Logger.debug(" Not found email details", e);
            throw new VedantuException(VedantuErrorCode.EMAIL_CAN_NOT_BE_SEND);
        }

        details.user = userEmailInfo;
        details.orgId = organization._getStringId();
        details.passwordResetURL = adminPasswordResetURL;
        details.addRecepient(details.user.getFullName(), userEmailInfo.email);
        generateEventAysc(userEmailInfo.id, details, EventType.SEND_INSTANT_EMAIL);

        return true;

    }

    private static void setUpdateList(Set<String> updateList, UpdateOrgReq request)
            throws VedantuException {

        boolean matched = false;

        if (CollectionUtils.isNotEmpty(request.updateList)) {
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
                } else if(key.equals(AbstractAddOrgReq.DOUBTS_FORUM_MODE)) {
                    matched |= updateList.add(Organization.FIELD_DOUBTS_FORUM_MODE);
                } else if(key.equals(AbstractAddOrgReq.DISABLE_SIGNUP)) {
                    matched |= updateList.add(Organization.FIELD_DISABLE_SIGNUP);
                } else if(key.equals(AbstractAddOrgReq.DISABLE_SIGNUP_MESSAGE)) {
                    matched |= updateList.add(Organization.FIELD_DISABLE_SIGNUP_MESSAGE);
                } else if(key.equals(AbstractAddOrgReq.ENABLE_OTP)) {
                    matched |= updateList.add(Organization.FIELD_ENABLE_OTP);
                } else if(key.equals(AbstractAddOrgReq.SMS_GATEWAY)) {
                    matched |= updateList.add(Organization.FIELD_SMS_GATEWAY);
                }
                else if(key.equals(AbstractAddOrgReq.COMMUNICATION_MAIL)) {
                    matched |= updateList.add(Organization.COMMUNICATION_MAIL);
                }
                else if(key.equals(AbstractAddOrgReq.SMTP_HOST)) {
                    matched |= updateList.add(Organization.SMTP_HOST);
                }
                else if(key.equals(AbstractAddOrgReq.SMTP_USER)) {
                    matched |= updateList.add(Organization.SMTP_USER);
                }
                else if(key.equals(AbstractAddOrgReq.SMTP_PASSWORD)) {
                    matched |= updateList.add(Organization.SMTP_PASSWORD);
                }
                else if(key.equals(AbstractAddOrgReq.INSTAMOJO_CLIENT_ID)) {
                    matched |= updateList.add(Organization.INSTAMOJO_CLIENT_ID);
                }
                else if(key.equals(AbstractAddOrgReq.INSTAMOJO_CLIENT_SECRET)) {
                    matched |= updateList.add(Organization.INSTAMOJO_CLIENT_SECRET);
                }
                else if(key.equals(AbstractAddOrgReq.INSTAMOJO_API_KEY)) {
                    matched |= updateList.add(Organization.INSTAMOJO_API_KEY);
                }
                else if(key.equals(AbstractAddOrgReq.INSTAMOJO_AUTH_TOKEN)) {
                    matched |= updateList.add(Organization.INSTAMOJO_AUTH_TOKEN);
                }
                else if(key.equals(AbstractAddOrgReq.VERSION_CODE)) {
                    matched |= updateList.add(Organization.VERSION_CODE);
                }
                if (!matched) {
                    LOGGER.debug("Key didnt match for update " + key);
                }
            }
        }

        if (updateList.size() != request.updateList.size()) {
            throw new VedantuException(VedantuErrorCode.INCORRECT_UPDATE_DATA_PROVIDED);
        }

    }

    public static UpdateOrgRes updateOrganization(UpdateOrgReq updateOrgReq)
            throws VedantuException {

        Set<String> updateList = new HashSet<String>();

        setUpdateList(updateList, updateOrgReq);

        OrganizationType type = OrganizationType.valueOfKey(updateOrgReq.type);
        Scope scope = Scope.valueOfKey(updateOrgReq.scope);

		Organization organization = OrganizationDAO.INSTANCE
				.updateOrganization(
						updateOrgReq.orgId,
						updateOrgReq.name,
						updateOrgReq.fullName,
						updateOrgReq.getWebsite(),
						updateOrgReq.getEmailDomain(),
						updateOrgReq.contactNumber,
						type,
						updateOrgReq.locations,
						updateOrgReq.address,
						updateOrgReq.description,
						scope,
						updateOrgReq.encLevel,
						updateOrgReq.authType,
						updateOrgReq.endPoint,
						updateOrgReq.socialMedia,
						updateOrgReq.appInfos,
						updateOrgReq.pointsOfSale == null ? null : Arrays
								.asList(StringUtils.split(
										updateOrgReq.pointsOfSale, ",")),
						updateOrgReq.doubtsForumMode, updateList,
						updateOrgReq.smtpHost, updateOrgReq.smtpUser,
						updateOrgReq.smtpPassword,
						updateOrgReq.instaMojoClientId,
						updateOrgReq.instaMojoClientSecret,
						updateOrgReq.instaMojoApiKey,
						updateOrgReq.instaMojoAuthToken,
						updateOrgReq.versionCode,
						updateOrgReq.disableSignup,
						updateOrgReq.disableSignupMessage,
						updateOrgReq.smsGateway,
						updateOrgReq.enableOTP
						);
		organization.communicationMail = updateOrgReq.communicationMail;
		OrganizationDAO.INSTANCE.save(organization);
        UpdateOrgRes updateOrgRes = new UpdateOrgRes();
        updateOrgRes.id = organization._getStringId();
        updateOrgRes.recordState = organization.recordState;

        return updateOrgRes;
    }

    public static UpdateOrgMemberExtraInfoInputFieldsRes updateOrganizationMemberExtraInputFields(
            UpdateOrgMemberExtraInfoInputFieldsReq req) throws VedantuException {

        Organization organization = OrganizationDAO.INSTANCE.updateOrgMemberExtraInfoInputFields(
                req.userId, req.orgId, req.targetOrgMemberProfile, req.fields);
        UpdateOrgMemberExtraInfoInputFieldsRes res = new UpdateOrgMemberExtraInfoInputFieldsRes();
        res.targetOrgMemberProfile = req.targetOrgMemberProfile;
        res.fields = organization.extraMemberInfoFields.get(req.targetOrgMemberProfile);
        return res;
    }

    public static GetOrgMemberExtraInfoInputFieldsRes getOrganizationMemberExtraInputFields(
            GetOrgMemberExtraInfoInputFieldsReq req) throws VedantuException {

        if (req.checkIfSignupAllowed) {
            DBObject query = new BasicDBObject(ConstantsGlobal.ORG_ID, req.orgId);
            query.put(OrgSection.FIELD_ACCESS_SCOPE, AccessScope.OPEN.name());
            long openedSectionCount = OrgSectionDAO.INSTANCE.count(query);
            MutableLong totalHits = GranteeOrgProgramDAO.INSTANCE.getProgramsCountGrantedToMe(req.orgId);
            long totalSections = openedSectionCount + totalHits.longValue();
            if (totalSections < 1) {
                String errorMsg = "no program section is opened for signup for orgId: " + req.orgId;
                throw new VedantuException(VedantuErrorCode.ORG_SIGNUP_NOT_SUPPORTED, errorMsg);
            }
        }
        Organization organization = OrganizationDAO.INSTANCE.getById(req.orgId);
        if(organization.disableSignup){
            String errorMsg = StringUtils.isEmpty(organization.disableSignupMessage) ? "SignUp is Temporarily Disabled" : organization.disableSignupMessage;
            throw new VedantuException(VedantuErrorCode.ORG_SIGNUP_NOT_SUPPORTED, errorMsg);
        }
        GetOrgMemberExtraInfoInputFieldsRes res = new GetOrgMemberExtraInfoInputFieldsRes();
        res.targetOrgMemberProfile = req.targetOrgMemberProfile;

        if (organization.extraMemberInfoFields == null) {
            organization.extraMemberInfoFields = new HashMap<OrgMemberProfile, List<InputFieldInfo>>();
        }
        res.enableOTP = organization.enableOTP;
        res.fields = organization.extraMemberInfoFields.get(req.targetOrgMemberProfile);
        if (res.fields == null) {
            res.fields = new ArrayList<InputFieldInfo>();
        }
        return res;
    }

    public static UpdateDigitalLibraryFieldsRes updateDigitalLibraryFields(
            UpdateDigitalLibraryFieldsReq req) throws VedantuException {

        Organization organization = OrganizationDAO.INSTANCE.updateDigitalLibraryFields(
                req.userId, req.orgId, req.fields);
        UpdateDigitalLibraryFieldsRes res = new UpdateDigitalLibraryFieldsRes();
        res.fields = organization.digitalLibraryHiddenFields;
        return res;
    }

    public static GetDigitalLibraryFieldsRes getDigitalLibraryFields(
            GetDigitalLibraryFieldsReq req) throws VedantuException {

        Organization organization = OrganizationDAO.INSTANCE.getById(req.orgId);
        GetDigitalLibraryFieldsRes res = new GetDigitalLibraryFieldsRes();
        if (organization.digitalLibraryHiddenFields == null) {
            organization.digitalLibraryHiddenFields = new HashSet<String>();
        }
        res.fields = organization.digitalLibraryHiddenFields;
        if (res.fields == null) {
            res.fields = new HashSet<String>();
        }
        return res;
    }
    /**
     * TODO check with UI if this call has been made in context of slug based context
     *
     *
     * @param getAssociatedOrgsReq
     * @return
     * @throws VedantuException
     */

    public static GetAssociatedOrgsRes getAssociatedOrgsOfUser(
            GetAssociatedOrgsReq getAssociatedOrgsReq) throws VedantuException {

        List<OrgMember> memberships = OrgMemberDAO.INSTANCE
                .getAssociatedOrgs(getAssociatedOrgsReq.userId);
        GetAssociatedOrgsRes getAssociatedOrgsRes = new GetAssociatedOrgsRes();
        if (CollectionUtils.isEmpty(memberships)) {
            return getAssociatedOrgsRes;
        }
        Map<String, Organization> orgs = new HashMap<String, Organization>();
        for (OrgMember membership : memberships) {
            orgs.put(membership.orgId, null);
        }
        List<ObjectId> orgIds = ObjectIdUtils.toObjectIds(new ArrayList<String>(orgs.keySet()));
        if (CollectionUtils.isEmpty(orgIds)) {
            return getAssociatedOrgsRes;
        }
        List<Organization> organizations = OrganizationDAO.INSTANCE.getOrganizationsByIds(orgIds);
        if (CollectionUtils.isEmpty(organizations)) {
            return getAssociatedOrgsRes;
        }
        for (Organization organization : organizations) {
            if (null == organization) {
                continue;
            }
            orgs.put(organization._getStringId(), organization);
        }
        for (OrgMember membership : memberships) {
            Organization org = orgs.get(membership.orgId);
            if (null == org) {
                continue;
            }

            long currentTime = new Date().getTime();
            // OrgMemberState userState = OrgMemberState.ACTIVE;
            // if(membership._getMemberState(currentTime) == OrgMemberState.BLOCKED)
            // {
            // userState = OrgMemberState.BLOCKED;
            // }

            OrgAssociatedInfo info = new OrgAssociatedInfo(membership.orgId, org.name,
                    org.fullName, org.type, org.scope, org.status, org._getThumbnailUrl(),
                    membership.timeCreated, membership.lastUpdated, membership.recordState,
                    membership._getStringId(), membership.memberId, membership.firstName,
                    membership.lastName, membership.profile, membership._getThumbnailUrl(),
                    membership._getMemberState(currentTime), org.authType, org.referer, org.slug, org.showClassroomConnect);
            if(membership.profile == OrgMemberProfile.STUDENT){
                if(org.studentPageStatus != null){
                    if(org.studentPageStatus == OrganizationStatus.APPROVED){
                        info.orgStudentPageStatus = OrgMemberState.ACTIVE;
                    }else{
                        info.orgStudentPageStatus = OrgMemberState.BLOCKED;
                    }
                }
                else{
                    info.orgStudentPageStatus = OrgMemberState.ACTIVE;
                }
            }else{
                if(org.status == OrganizationStatus.APPROVED){
                    info.orgStudentPageStatus = OrgMemberState.ACTIVE;
                }else{
                    info.orgStudentPageStatus = OrgMemberState.BLOCKED;
                }
            }
//            info.orgStudentPageStatus = membership.profile == OrgMemberProfile.STUDENT ? (org.studentPageStatus == OrganizationStatus.APPROVED ? OrgMemberState.ACTIVE: OrgMemberState.BLOCKED):();
            getAssociatedOrgsRes.list.add(info);
            getAssociatedOrgsRes.totalHits++;
        }

        return getAssociatedOrgsRes;
    }

    public static UploadOrgPicRes uploadOrgPic(UploadOrgPicReq uploadOrgPicReq)
            throws VedantuException {

        ImageFilter filter = new ImageFilter();
        boolean isImg = filter.accept(new File(uploadOrgPicReq.fileName));
        if (!isImg) {
            throw new VedantuException(VedantuErrorCode.UNSUPPORTED_FILE_TYPE, "not an image file");
        }

        Organization organization = OrganizationDAO.INSTANCE
                .getOrganizationById(uploadOrgPicReq.orgId);

        OrgMember member = OrgMemberDAO.INSTANCE.getMemberByUserId(uploadOrgPicReq.orgId,
                uploadOrgPicReq.userId);
        if (null == member
                || !StringUtils.equals(member._getStringId(), uploadOrgPicReq.orgMemberId)) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }
        if (OrgMemberProfile.MANAGER != member.profile) {
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED);
        }

        final String imageName = organization._getStringId();

        OrganizationEntityFileStorage picStorage = new OrganizationEntityFileStorage();
        try {
            StorageResult picStorageResult = picStorage.storeImage(imageName,
                    uploadOrgPicReq.inputFile, FileCategory.ORIGINAL, ImageSize.ORIGINAL, null);
            LOGGER.debug(picStorageResult.toString());

            for (ImageSize imageSize : new ImageSize[] { ImageSize.MEDIUM, ImageSize.SMALL,
                    ImageSize.EXTRA_SMALL }) {
                File convertedFile = ImageGenerator.createImage(uploadOrgPicReq.inputFile,
                        imageSize, uploadOrgPicReq.fileName);
                picStorageResult = picStorage.storeImage(imageName, convertedFile,
                        FileCategory.CONVERTED, imageSize, null);
                LOGGER.debug(picStorageResult.toString());

                FileUtils.deleteFile(convertedFile.getName(), convertedFile);
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new VedantuException(VedantuErrorCode.FILE_UPLOAD_ERROR);
        }

        organization.thumbnail = imageName;
        OrganizationDAO.INSTANCE.save(organization);

        String thumbnailUrl = organization._getThumbnailUrl();
        UploadOrgPicRes uploadOrgPicRes = new UploadOrgPicRes(true, thumbnailUrl);
        return uploadOrgPicRes;
    }

    private static GetOrgRes getOrgRes(Organization organization, boolean addKey) {

        GetOrgRes getOrgRes = new GetOrgRes(
                organization._getStringId(),
                organization.name,
                organization.fullName,
                organization.website,
                organization.emailDomain,
                organization.contactNumber,
                organization.type,
                organization.locations,
                organization.address,
                organization.description,
                organization.scope,
                organization.representative,
                organization._getThumbnailUrl(),
                organization.slug,
                organization.encLevel,
                organization.subscription != null
                        && StringUtils.isNotEmpty(organization.subscription.planId) ? (LicensingInfo) LicensingPlanDAO.INSTANCE
                        .getBasicInfo(organization.subscription.planId) : null,

                organization.tncAcceptance, organization.socialMedia, organization.appInfos);
        //
        // getOrgRes.extURLs.add(new ExternalURLInfo(GooglePlayUtil.APP_STORE, GooglePlayUtil
        // .getUrlForOrganizationApp(organization.slug)));

        getOrgRes.adminUserId = organization.adminUserId;
        getOrgRes.referer = organization.referer;
        getOrgRes.doubtsForumMode = organization.doubtsForumMode;
        getOrgRes.showClassroomConnect = organization.showClassroomConnect;
        getOrgRes.disableDownload = organization.disableDownload;
        getOrgRes.disableSignup = organization.disableSignup;
        getOrgRes.disableSignupMessage = StringUtils.isEmpty(organization.disableSignupMessage) ? "SignUp is Temporarily Disabled" : organization.disableSignupMessage;
        getOrgRes.communicationMail = organization.communicationMail;
        getOrgRes.smtpHost = organization.smtpHost;
        getOrgRes.smtpUser = organization.smtpUser;
        getOrgRes.smtpPassword = organization.smtpPassword;
        getOrgRes.instaMojoClientId = organization.instaMojoClientId;
        getOrgRes.instaMojoClientSecret = organization.instaMojoClientSecret;
        getOrgRes.instaMojoApiKey = organization.instaMojoApiKey;
        getOrgRes.instaMojoAuthToken = organization.instaMojoAuthToken;
        getOrgRes.versionCode = organization.versionCode;
        getOrgRes.status = organization.status;
        getOrgRes.studentPageStatus = organization.studentPageStatus;
        if (addKey) {
            try {
                getOrgRes.key = getPublicKey(organization);
            } catch (VedantuException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return getOrgRes;
    }

    public static String getPublicKey(String orgId) throws VedantuException {

        Organization organization = OrganizationDAO.INSTANCE.getOrganizationById(orgId);
        return getPublicKey(organization);
    }

    public static String getPublicKey(Organization organization) throws VedantuException {

        if (organization == null) {
            return null;
        }
        SecurityCredentials credentials = organization.credentials;
        if (credentials == null) {
            credentials = setCredentials(organization);
        }
        // DatatypeConverter.printHexBinary(credentials.getPublicKey())
        return Base64.encode(credentials.getPublicKey());
    }

    private static synchronized SecurityCredentials setCredentials(Organization organization)
            throws VedantuException {

        if (organization.credentials != null) {
            return organization.credentials;

        }
        organization.credentials = EncryptionUtils.generateKeys();
        OrganizationDAO.INSTANCE.save(organization);
        return organization.credentials;
    }

    public static AcceptTnCRes acceptTnC(AcceptTncOrgReq acceptTnCReq) throws VedantuException {

        if (!acceptTnCReq.agrees) {
            throw new VedantuException(VedantuErrorCode.TNC_NOT_ACCEPTED);
        }

        TnCAcceptance acceptance = new TnCAcceptance(acceptTnCReq.agrees, acceptTnCReq.version,
                System.currentTimeMillis());

        acceptance.acceptedBy = acceptTnCReq.userId;
        Organization organization = OrganizationDAO.INSTANCE.getById(acceptTnCReq.orgId);
        if (StringUtils.isEmpty(acceptTnCReq.userId)
                || !StringUtils.equalsIgnoreCase(organization.adminUserId, acceptTnCReq.userId)) {
            throw new VedantuException(VedantuErrorCode.ACCESS_DENIED);
        }

        organization.tncAcceptance = acceptance;
        OrganizationDAO.INSTANCE.save(organization);

        AcceptTnCRes acceptTnCRes = new AcceptTnCRes(acceptance.agrees);
        return acceptTnCRes;
    }

    public static GenerateAppCredentialsRes generateAppCredentials(GenerateAppCredentialsReq req)
            throws VedantuException {

        Organization org = OrganizationDAO.INSTANCE.getOrganizationById(req.orgId);
        MutableBoolean update = new MutableBoolean(false);
        AppSecurityCredentials appSecurityCredentials = org.__addOrGetAppCredentials(req.appId,
                update);
        if (update.booleanValue()) {
            OrganizationDAO.INSTANCE.updateModel(org,
                    Arrays.asList(new String[] { Organization.FIELD_APP_CREDENTIALS }));
        }

        return new GenerateAppCredentialsRes(appSecurityCredentials.appId,
                appSecurityCredentials.authToken, appSecurityCredentials.secretKey);
    }

    public static VerifyAppCredentialsRes generateAppCredentials(VerifyAppCredentialsReq req)
            throws VedantuException {

        Organization org = OrganizationDAO.INSTANCE.getOrganizationById(req.orgId);
        AppSecurityCredentials appSecurityCredentials = org.__addOrGetAppCredentials(req.appId,
                new MutableBoolean(false));

        VerifyAppCredentialsRes res = new VerifyAppCredentialsRes();
        res.valid = appSecurityCredentials != null
                && appSecurityCredentials.authToken.equals(req.authToken)
                && appSecurityCredentials.secretKey.equals(req.secretKey);
        return res;
    }

    public static GetOrgRes updateOrganizationSharedSubjects(
            UpdateOrganizationSharedSubjects updateOrgStatusReq) throws VedantuException {
        Organization organization = OrganizationDAO.INSTANCE
                .updateOrganizationSharedSubjects(updateOrgStatusReq.orgId,
                        updateOrgStatusReq.showSharedSubjects);
        GetOrgRes getOrgRes = getOrgRes(organization, false);
        return getOrgRes;
    }

    public static GetShowSharedSubjectsRes getShowSharedSubjects(GetOrgReq getOrgReq) {
        // TODO Auto-generated method stub
        boolean getShowSharedSubjects = OrganizationDAO.INSTANCE.getById(getOrgReq.orgId).showSharedSubjects;
        GetShowSharedSubjectsRes res = new GetShowSharedSubjectsRes();
        res.showSharedSubjects = getShowSharedSubjects;
        return res;
    }

    public static GetOrgRes updateOrganizationClassroomConnectStatus(
            UpdateOrganizationClassroomConnectStatus updateOrgStatusReq) throws VedantuException {
        Organization organization = OrganizationDAO.INSTANCE
                .updateOrganizationClassroomConnectStatus(updateOrgStatusReq.orgId,
                        updateOrgStatusReq.showClassroomConnect);
        GetOrgRes getOrgRes = getOrgRes(organization, false);
        return getOrgRes;
    }

    public static GetOrgRes updateOrganizationDownloadStatus(
            UpdateOrganizationDownloadStatus updateOrgStatusReq) throws VedantuException {
        Organization organization = OrganizationDAO.INSTANCE
                .updateOrganizationDownloadStatus(updateOrgStatusReq.orgId,
                        updateOrgStatusReq.disableDownload);
        GetOrgRes getOrgRes = getOrgRes(organization, false);
        return getOrgRes;
    }

}
