package com.vedantu.organization.daos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.AuthType;
import com.vedantu.commons.enums.EncryptionLevel;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.InputFieldInfo;
import com.vedantu.commons.pojos.Location;
import com.vedantu.commons.pojos.SecurityCredentials;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.organization.enums.DoubtsForumMode;
import com.vedantu.organization.enums.OrgMemberProfile;
import com.vedantu.organization.enums.OrganizationStatus;
import com.vedantu.organization.enums.OrganizationType;
import com.vedantu.organization.models.Organization;
import com.vedantu.organization.models.Subscription;
import com.vedantu.organization.pojos.ExternalOrganizationEndpoints;
import com.vedantu.organization.pojos.requests.AppInfo;
import com.vedantu.organization.pojos.requests.organizations.SmsGatewayInfo;
import com.vedantu.user.pojos.SocialInfo;
import com.vedantu.user.pojos.TnCAcceptance;
import com.vedantu.user.pojos.UserBasicInfo;

public class OrganizationDAO extends VedantuBasicDAO<Organization, ObjectId> {

    private static final ALogger        LOGGER   = Logger.of(OrganizationDAO.class);

    public static final OrganizationDAO INSTANCE = new OrganizationDAO();

    private OrganizationDAO() {

        super(Organization.class);
    }

    public List<Organization> getAllOrganizations(OrganizationStatus status, MutableLong totalHits) {

        LOGGER.debug("getAllOrganizations status: " + status + ", totalHits: " + totalHits.getValue());
        List<Organization> organizations = null;

        Query<Organization> query = getQuery();
        if (null != status) {
            query.filter("status", status);
        }
        organizations = query.order("cName").asList();
        totalHits.setValue(query.countAll());

        LOGGER.info("getAllOrganizations status: " + status + ", totalHits: " + totalHits
                + ", organizations.size: " + CollectionUtils.size(organizations));
        return organizations;
    }

    public List<Organization> getAllOrganizationsExcept(List<ObjectId> orgIds) {

        List<Organization> organizations = null;

        Query<Organization> query = getQuery();
        query.field("_id").notIn(orgIds);
        organizations = query.order("cName").asList();
        return organizations;
    }

    public List<Organization> getOrganizations(OrganizationStatus status, MutableLong totalHits,
            String queryText) {

        LOGGER.debug("getOrganizations status: " + status + ", totalHits: " + totalHits.getValue());
        List<Organization> organizations = null;

        Query<Organization> query = getQuery();
        if (null != status) {
            query.filter("status", status);
        }
        if (StringUtils.isNotEmpty(queryText)) {
            queryText = queryText.trim();
            query.or(query.criteria(ConstantsGlobal.NAME).startsWithIgnoreCase(queryText), query
                    .criteria("fullName").startsWithIgnoreCase(queryText));
        }

        organizations = query.order("cName").asList();
        totalHits.setValue(query.countAll());

        LOGGER.info("getOrganizations status: " + status + ", totalHits: " + totalHits
                + ", organizations.size: " + CollectionUtils.size(organizations));
        return organizations;
    }

    public boolean checkIfSuperAdmin(String orgId, String userId) throws VedantuException {

        LOGGER.debug("checkIfSuperAdmin DAO Function orgId: " + orgId + ", userId: " + userId);

        if (ObjectIdUtils.hasInvalidId(orgId)) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
        }
        Organization organization = getQuery().filter(FIELD_ID, new ObjectId(orgId))
                .filter("adminUserId", userId).get();

        if (null == organization) {
            return false;
        }

        else {
            LOGGER.info(".... The user is SuperAdmin.....");
            return true;
        }
    }

    public boolean checkAppVersion(int reqVersionCode, String orgId) throws VedantuException {
        LOGGER.debug("checkAppVersion DAO Function orgId: " + orgId + "   version code : "
                + reqVersionCode);
        if (ObjectIdUtils.hasInvalidId(orgId)) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }
        Organization organization = getQuery().filter(FIELD_ID, new ObjectId(orgId)).get();

        if (null == organization) {
            return true;
        } else if (reqVersionCode < organization.versionCode) {
            return false;
        }
        return true;
    }

    public Organization addOrganization(String name, String fullName, String website,
            String emailDomain, String contactNumber, OrganizationType type,
            List<Location> locations, String address, String description, Scope scope, String slug,
            UserBasicInfo representative, OrganizationStatus status) throws VedantuException {

        return addOrganization(name, fullName, website, emailDomain, contactNumber, type,
                locations, address, description, scope, slug, representative, status);
    }

    public Organization addOrganization(String name, String fullName, String website,
            String emailDomain, String contactNumber, OrganizationType type,
            List<Location> locations, String address, String description, Scope scope, String slug,
            UserBasicInfo representative, OrganizationStatus status, OrganizationStatus studentPageStatus,
            SecurityCredentials credentials, EncryptionLevel level, Subscription subscription,
            TnCAcceptance acceptance, String theme, boolean isNewUI, boolean showSharedSubjects) throws VedantuException {

        LOGGER.debug("addOrganization name: " + name + ", fullName: " + fullName + ", website: "
                + website + ", emailDomain: " + emailDomain + ", contactNumber: " + contactNumber
                + ", type: " + type + ", locations: {" + StringUtils.join(locations, ", ") + "}"
                + ", address: " + address + ", description: " + description + ", scope: " + scope
                + ", representative: " + representative + ", status: " + status + ", slug:" + slug
                + ", level:" + level + " , planId:" + subscription.planId);

        Query<Organization> query = getQuery();
        query.or(query.criteria("website").containsIgnoreCase(website), query.and(
                query.criteria("slug").startsWithIgnoreCase(slug), query.criteria("slug")
                        .endsWithIgnoreCase(slug)));
        Organization organization = query.get();
        if (null != organization) {
            LOGGER.error("cannot add organization as organization already exists for website: "
                    + website + " or slug: " + slug);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_ALREADY_EXISTS);
        }

        organization = new Organization();
        organization.name = name;
        organization.fullName = fullName;
        organization.website = website;
        organization.emailDomain = emailDomain;
        organization.contactNumber = contactNumber;
        organization.type = type;
        organization.locations = locations;
        organization.address = address;
        organization.slug = slug;
        organization.description = description;
        organization.scope = scope;
        organization.representative = representative;
        organization.credentials = credentials;
        organization.status = status;
        organization.studentPageStatus = studentPageStatus;
        organization.subscription = subscription;
        organization.doubtsForumMode = DoubtsForumMode.PUBLIC;
        organization.encLevel = level;
        organization.tncAcceptance = acceptance;
		if (!StringUtils.isEmpty(theme)) {
			organization.theme = theme;
		}
        organization.isNewUI = isNewUI;
        organization.showSharedSubjects = showSharedSubjects;
        save(organization);

        LOGGER.info("addOrganization saved organization: " + organization._getStringId());

        return organization;
    }

	public Organization updateOrganization(String orgId, String name,
			String fullName, String website, String emailDomain,
			String contactNumber, OrganizationType type,
			List<Location> locations, String address, String description,
			Scope scope, EncryptionLevel encLevel, AuthType authType,
			ExternalOrganizationEndpoints endPoint, SocialInfo socialMedia,
			List<AppInfo> appInfos, List<String> pointsOfSale,
			DoubtsForumMode doubtsForumMode, Set<String> updateList,
			String smtpHost, String smtpUser, String smtpPassword,
			String instaMojoClientId ,String instaMojoClientSecret, String instaMojoApiKey, String instaMojoAuthToken, int versionCode, boolean disableSignup, String disableSignupMessage,
			SmsGatewayInfo smsGateway, boolean enableOTP) throws VedantuException {

		LOGGER.debug("updateOrganization orgId: " + orgId + ", name: " + name
				+ ", fullName: " + fullName + ", website: " + website
				+ ", emailDomain: " + emailDomain + ", contactNumber: "
				+ contactNumber + ", type: " + type + ", locations: {"
				+ StringUtils.join(locations, ", ") + "}" + ", address: "
				+ address + ", description: " + description + ", scope: "
				+ scope + " encLevel " + encLevel + " smtpHost " + smtpHost + " smtpUser " + smtpUser
				+ " smtpPassword " + smtpPassword + " instaMojoClientId "
				+ instaMojoClientId + " instaMojoClientSecret "
				+ instaMojoClientSecret + "instaMojoApiKey "+ instaMojoApiKey + "instaMojoAuthToken "+ instaMojoAuthToken);

        Organization organization = getOrganizationById(orgId);

        if (!StringUtils.equalsIgnoreCase(organization.website, website)) {
            Organization orgByWebsite = getQuery().filter("website", website).get();
            if (null != orgByWebsite) {
                LOGGER.error("cannot update organization as organization website: " + website
                        + " already exists for another organization: "
                        + orgByWebsite._getStringId());
                throw new VedantuException(VedantuErrorCode.ORGANIZATION_ALREADY_EXISTS,
                        "another organization already has a website: " + website);
            }
        }

        organization.name = name;
        organization.fullName = fullName;
        organization.website = website;
        organization.emailDomain = emailDomain;
        organization.contactNumber = contactNumber;
        organization.type = type;
        organization.locations = locations;
        organization.address = address;
        organization.description = description;
        organization.scope = scope;
        organization.encLevel = encLevel;
        organization.endPoint = endPoint;
        organization.authType = authType == null ? AuthType.VEDANTU : authType;
        organization.socialMedia = socialMedia;
        organization.appInfos = appInfos;
        organization.doubtsForumMode = doubtsForumMode;
        organization.disableSignup = disableSignup;
        organization.disableSignupMessage = disableSignupMessage;
        if(enableOTP){
            organization.smsGateway = smsGateway;
            organization.enableOTP = enableOTP;
        }
        else{
            organization.enableOTP = false;
            organization.smsGateway = null;
        }
		if (!StringUtils.isEmpty(smtpUser)
				&& !StringUtils.isEmpty(smtpPassword)
				&& !StringUtils.isEmpty(smtpHost)) {
			organization.smtpHost = smtpHost;
			organization.smtpUser = smtpUser;
			organization.smtpPassword = smtpPassword;
		}
		if (!StringUtils.isEmpty(instaMojoClientId)
				&& !StringUtils.isEmpty(instaMojoClientSecret) && !StringUtils.isEmpty(instaMojoApiKey) && !StringUtils.isEmpty(instaMojoAuthToken)) {
			organization.instaMojoClientId = instaMojoClientId;
			organization.instaMojoClientSecret = instaMojoClientSecret;
			organization.instaMojoApiKey = instaMojoApiKey;
			organization.instaMojoAuthToken = instaMojoAuthToken;
		}
		if (versionCode > 0){
			organization.versionCode = versionCode;
		}

        if (CollectionUtils.isNotEmpty(updateList)) {
            // update pointsOfSale only in partial update query(caller should specifically, specify
            // it in updateList)
            if (pointsOfSale == null) {
                pointsOfSale = new ArrayList<String>();
            }
            organization.pointsOfSale = pointsOfSale;
            updateModel(organization, new ArrayList<String>(updateList));
        } else {
            save(organization);
        }

        LOGGER.info("updateOrganization updated organization: " + organization._getStringId());

        return organization;
    }

    public Organization updateOrganizationSlug(String orgId, String slug) throws VedantuException {

        Organization organization = getOrganizationById(orgId);

        Organization slugOrg = getOrganizationBySlug(slug);
        if (slugOrg != null) {
            // given slug is already taken
            LOGGER.error("slug[" + slug + "] already taken");
            throw new VedantuException(VedantuErrorCode.SLUG_NOT_AVAILABLE);
        }
        organization.slug = slug;
        save(organization);
        return organization;
    }

	public Organization updateOrganizationStatus(String orgId, String status, String type)
			throws VedantuException {

		Organization organization = getOrganizationById(orgId);
		if (organization != null) {
		    if(type.equals("STUDENT")){
		        organization.studentPageStatus = OrganizationStatus.valueOfKey(status);
		    }
		    else{
		        organization.status = OrganizationStatus.valueOfKey(status);
		    }
			save(organization);
		}
		return organization;
	}

    public Organization updateOrganizationReferrer(String orgId, String referer)
            throws VedantuException {

        Organization organization = getOrganizationById(orgId);
        organization.referer = referer;
        updateModel(organization, Arrays.asList(Organization.FIELD_REFERER));
        return organization;
    }

    public Organization updateOrgMemberExtraInfoInputFields(String userId, String orgId,
            OrgMemberProfile orgMemberProfile, List<InputFieldInfo> fields) throws VedantuException {

        Organization organization = getOrganizationById(orgId);

        // this operation will only be allowed to SUPER_ADMIN (ADMIN who created this organization)
        if (organization.adminUserId == null || !organization.adminUserId.equals(userId)) {
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED,
                    "this operation is only allowed to super admin");
        }

        // only update for provided OrgMemberProfile
        if (organization.extraMemberInfoFields == null) {
            organization.extraMemberInfoFields = new HashMap<OrgMemberProfile, List<InputFieldInfo>>();
        }

        if (CollectionUtils.isNotEmpty(fields)) {
            for (InputFieldInfo info : fields) {
                if (!info.validate()) {
                    throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
                }
            }
        }
        organization.extraMemberInfoFields.put(orgMemberProfile, fields);
        OrganizationDAO.INSTANCE.save(organization);

        return organization;
    }

    public Organization updateDigitalLibraryFields(String userId, String orgId,
            List<String> fields) throws VedantuException {

        Organization organization = getOrganizationById(orgId);

        // this operation will only be allowed to SUPER_ADMIN (ADMIN who created this organization)
        if (organization.adminUserId == null || !organization.adminUserId.equals(userId)) {
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED,
                    "this operation is only allowed to super admin");
        }

        // only update for provided OrgMemberProfile
        if (organization.digitalLibraryHiddenFields == null) {
            organization.digitalLibraryHiddenFields = new HashSet<String>();
        }
        if (fields == null){
            organization.digitalLibraryHiddenFields.clear();
        }else{
            organization.digitalLibraryHiddenFields.clear();
            for (String field : fields) {
                organization.digitalLibraryHiddenFields.add(field);
            }
        }
        OrganizationDAO.INSTANCE.save(organization);

        return organization;
    }

    public Organization getOrganizationById(String orgId) throws VedantuException {

        Organization organization = getById(orgId);

        if (null == organization) {
            LOGGER.error("cannot find organization for id: " + orgId);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND,
                    "no org found with id: " + orgId);
        }

        return organization;
    }

    public Organization getOrganizationBySlug(String slug) {

        Query<Organization> query = getQuery();
        query.and(query.criteria("slug").startsWithIgnoreCase(slug), query.criteria("slug")
                .endsWithIgnoreCase(slug));

        Organization organization = query.get();

        if (null == organization) {
            LOGGER.error("cannot find organization for slug: " + slug);
        }

        return organization;
    }

    public Organization getOrganizationByReferer(String referer) {

        Query<Organization> query = getQuery();
        query.and(query.criteria(Organization.FIELD_REFERER).startsWithIgnoreCase(referer), query
                .criteria(Organization.FIELD_REFERER).endsWithIgnoreCase(referer));

        Organization organization = query.get();
        LOGGER.debug(query.toString());

        if (null == organization) {
            LOGGER.error("cannot find organization for referer: " + referer);
        }

        return organization;
    }

    public Organization getOrganizationByWebsite(String website) {

        Query<Organization> query = getQuery();
        query.and(query.criteria("website").startsWithIgnoreCase(website), query
                .criteria("website").endsWithIgnoreCase(website));

        Organization organization = query.get();

        if (null == organization) {
            LOGGER.error("cannot find organization for website: " + website);
        }

        return organization;
    }

    public List<Organization> getOrganizationsByIds(List<ObjectId> orgIds) {

        List<Organization> organizations = getByIds(orgIds);

        return organizations;
    }

    public Organization approveOrganization(String orgId, String adminUserId,
            String adminOrgMemberId) throws VedantuException {

        LOGGER.debug("approveOrganization orgId: " + orgId + ", adminUserId: " + adminUserId
                + ", adminOrgMemberId: " + adminOrgMemberId);

        Organization organization = getById(orgId);
        if (null == organization) {
            LOGGER.error("cannot approve organization as organization not found for _id: " + orgId);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
        }
        if (OrganizationStatus.APPROVED == organization.status) {
            LOGGER.error("cannot approve organization as organization already approved for _id: "
                    + orgId);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_ALREADY_APPROVED);
        }
        if (organization.tncAcceptance != null) {
            organization.tncAcceptance.acceptedBy = adminUserId;
        }
        organization.adminUserId = adminUserId;
        organization.adminOrgMemberId = adminOrgMemberId;
        organization.status = OrganizationStatus.APPROVED;
        organization.studentPageStatus = OrganizationStatus.APPROVED;
        save(organization);

        LOGGER.info("approveOrganization approved orgId: " + orgId);

        return organization;

    }

    public List<Organization>
            getByPlanId(String planId, int start, int size, MutableLong totalHits)
                    throws VedantuException {

        Query<Organization> findQuery = getQuery();

        findQuery.filter("subscription.planId", planId);

        LOGGER.debug("Organization planId: " + planId);

        List<Organization> organizations = getQuery().offset(start).limit(size).asList();

        totalHits.setValue(findQuery.countAll());
        return organizations;

    }

    public Organization updateOrganizationSharedSubjects(String orgId, boolean showSharedSubjects) throws VedantuException {
        Organization organization = getOrganizationById(orgId);
        if (organization != null) {
            organization.showSharedSubjects = showSharedSubjects;
            save(organization);
        }
        return organization;
    }

    public Organization updateOrganizationClassroomConnectStatus(String orgId, boolean showClassroomConnect) throws VedantuException {
        Organization organization = getOrganizationById(orgId);
        if (organization != null) {
            organization.showClassroomConnect = showClassroomConnect;
            save(organization);
        }
        return organization;
    }

    public Organization updateOrganizationDownloadStatus(String orgId, boolean disableDownload) throws VedantuException {
        Organization organization = getOrganizationById(orgId);
        if (organization != null) {
            organization.disableDownload = disableDownload;
            save(organization);
        }
        return organization;
    }

}
