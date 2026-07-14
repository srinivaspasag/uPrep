package com.vedantu.organization.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.annotations.Transient;
import com.vedantu.commons.enums.AuthType;
import com.vedantu.commons.enums.EncryptionLevel;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.AppSecurityCredentials;
import com.vedantu.commons.pojos.InputFieldInfo;
import com.vedantu.commons.pojos.Location;
import com.vedantu.commons.pojos.SecurityCredentials;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.pojos.responses.ModelExtendedInfo;
import com.vedantu.commons.utils.ImageDisplayURLUtil;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.organization.daos.LicensingPlanDAO;
import com.vedantu.organization.enums.DoubtsForumMode;
import com.vedantu.organization.enums.OrgMemberProfile;
import com.vedantu.organization.enums.OrganizationStatus;
import com.vedantu.organization.enums.OrganizationType;
import com.vedantu.organization.pojos.ExternalOrganizationEndpoints;
import com.vedantu.organization.pojos.LicensingInfo;
import com.vedantu.organization.pojos.requests.AppInfo;
import com.vedantu.organization.pojos.requests.organizations.SmsGatewayInfo;
import com.vedantu.organization.pojos.responses.organizations.OrgBasicInfo;
import com.vedantu.organization.pojos.responses.organizations.OrgInfo;
import com.vedantu.user.pojos.SocialInfo;
import com.vedantu.user.pojos.TnCAcceptance;
import com.vedantu.user.pojos.UserBasicInfo;

@Entity(value = "organizations", noClassnameStored = true)
public class Organization extends VedantuBaseMongoModel {

    public static final String                         FIELD_NAME              = "name";
    public static final String                         FIELD_FULL_NAME         = "fullName";
    public static final String                         FIELD_WEBSITE           = "website";
    public static final String                         FIELD_CONTACT_NUMBER    = "contactNumber";
    public static final String                         FIELD_TYPE              = "type";
    public static final String                         FIELD_LOCATIONS         = "locations";
    public static final String                         FIELD_ADDRESS           = "address";
    public static final String                         FIELD_DESCRIPTION       = "description";
    public static final String                         FIELD_SCOPE             = "scope";
    public static final String                         FIELD_REPRESENTATIVE    = "representative";
    public static final String                         FIELD_SLUG              = "slug";
    public static final String                         FIELD_SOCIAL_MEDIA      = "socialMedia";
    public static final String                         FIELD_ENC_LEVEL         = "encLevel";
    public static final String                         FIELD_AUTH_TYPE         = "authType";
    public static final String                         FIELD_END_POINT         = "endPoint";
    public static final String                         FIELD_REFERER           = "referer";
    public static final String                         FIELD_APP_INFOS         = "appInfos";
    public static final String                         FIELD_POINTS_OF_SALE    = "pointsOfSale";
    public static final String                         FIELD_DOUBTS_FORUM_MODE = "doubtsForumMode";
    public static final String                         FIELD_DISABLE_SIGNUP    = "disableSignup";
    public static final String                         FIELD_ENABLE_OTP        = "enableOTP";
    public static final String                         FIELD_SMS_GATEWAY       = "smsGateway";
    public static final String                         FIELD_DISABLE_SIGNUP_MESSAGE    = "disableSignupMessage";
    public static final String                         COMMUNICATION_MAIL      = "communicationMail";
    public static final String                         SMTP_HOST 			   = "smtpHost";
    public static final String                         SMTP_USER 			   = "smtpUser";
    public static final String                         SMTP_PASSWORD 		   = "smtpPassword";
    public static final String           			   INSTAMOJO_CLIENT_ID	   = "instaMojoClientId";
    public static final String           			   INSTAMOJO_CLIENT_SECRET = "instaMojoClientSecret";
    public static final String                         INSTAMOJO_API_KEY       = "instaMojoApiKey";
    public static final String                         INSTAMOJO_AUTH_TOKEN    = "instaMojoAuthToken";
    public static final String           			   VERSION_CODE			   = "versionCode";

    @Transient
    public static transient String                     FIELD_APP_CREDENTIALS = "appCredentials";

    public String                                      name;
    public String                                      fullName;
    @Indexed(unique = true)
    public String                                      website;
    public String                                      emailDomain;
    public String                                      contactNumber;
    public OrganizationType                            type;
    public List<Location>                              locations;
    public String                                      address;
    public String                                      description;
    public Scope                                       scope;
    public String                                      thumbnail;
    public UserBasicInfo                               representative;
    @Indexed
    public OrganizationStatus                          status;
    public OrganizationStatus                          studentPageStatus;

    public SecurityCredentials                         credentials;
    @Indexed(unique = true)
    public String                                      slug;
    @Indexed(unique = true, sparse = true)
    public String                                      referer;                                   // This
                                                                                                   // will
                                                                                                   // store
                                                                                                   // base
                                                                                                   // referrer
                                                                                                   // (
                                                                                                   // eg.
                                                                                                   // http://www.example.org/referring_page
                                                                                                   // ,
                                                                                                   // base
                                                                                                   // is
                                                                                                   // http://www.example.org
                                                                                                   // )

    public String                                      adminUserId;
    public String                                      adminOrgMemberId;

    public EncryptionLevel                             encLevel              = EncryptionLevel.NA;

    public Subscription                                subscription;
    public TnCAcceptance                               tncAcceptance;

    public AuthType                                    authType;
    public ExternalOrganizationEndpoints               endPoint;

    public SocialInfo                                  socialMedia;
    public Map<OrgMemberProfile, List<InputFieldInfo>> extraMemberInfoFields;

    // app credentials will be used to verify external call in organization scope

    public List<AppSecurityCredentials>                appCredentials;
    public List<AppInfo>                               appInfos;

    // this params will be used to define the organization points of sale (i.e
    // Snapdeal,Amazon,Flipkart etc)
    public List<String>                                pointsOfSale;
    public Set<String>                                 digitalLibraryHiddenFields;
    public DoubtsForumMode                             doubtsForumMode;
    public boolean                                     disableSignup;
    public String                                      disableSignupMessage;
    public SmsGatewayInfo                              smsGateway;
    public boolean                                     isNewUI;
    public boolean                                     showSharedSubjects;
    public boolean                                     showClassroomConnect;
    public boolean                                     disableDownload;
    public boolean                                     enableOTP;
    public String                                      theme;
    public String                                      communicationMail;
    public String                                      smtpHost;
    public String                                      smtpUser;
    public String                                      smtpPassword;
    public String                                      instaMojoClientId;
    public String                                      instaMojoClientSecret;
    public String                                      instaMojoApiKey;
    public String                                      instaMojoAuthToken;
    public int										   versionCode;

    public Organization() {
        super();
    }

    @Override
    public ModelExtendedInfo toExtendedInfo() {

        OrgInfo orgInfo = new OrgInfo(_getStringId(), name, fullName, type, scope, status,
                _getThumbnailUrl(), timeCreated, lastUpdated, referer, slug, recordState);
        orgInfo.authType = authType;
        if (subscription != null && StringUtils.isNotEmpty(subscription.planId)) {
            orgInfo.planInfo = (LicensingInfo) LicensingPlanDAO.INSTANCE.getById(
                    subscription.planId).toBasicInfo();
        }
        orgInfo.slug = slug;

        return orgInfo;
    }

    public String _getThumbnailUrl() {

        return StringUtils.isNotEmpty(thumbnail) ? ImageDisplayURLUtil.getEntityThumbnail(
                EntityType.ORGANIZATION, thumbnail) : ImageDisplayURLUtil.getEntityStaticThumbnail(
                EntityType.ORGANIZATION, Arrays.asList("default"));
    }

    @Override
    public ModelBasicInfo toBasicInfo() {

        OrgBasicInfo orgInfo = new OrgBasicInfo(_getStringId(), recordState, name);
        orgInfo.thumbnail = this._getThumbnailUrl();
        orgInfo.fullName = this.name;
        orgInfo.authType = this.authType;

        return orgInfo;
    }

    public String __getOrgType() {

        return type.name();
    }

    public AppSecurityCredentials __addOrGetAppCredentials(String appId, MutableBoolean updated) {

        if (appCredentials == null) {
            appCredentials = new ArrayList<AppSecurityCredentials>();
        }

        for (AppSecurityCredentials appCred : appCredentials) {
            if (appCred.appId.equals(appId)) {
                return appCred;
            }
        }

        AppSecurityCredentials appCredential = new AppSecurityCredentials();
        appCredential.appId = appId;
        appCredential.authToken = RandomStringUtils.randomAlphanumeric(20);
        appCredential.secretKey = UUID.randomUUID().toString().replace("-", "");
        appCredentials.add(appCredential);
        updated.setValue(true);
        return appCredential;
    }

    public boolean __isValidPointOfSale(String pointOfSale) {

        if (CollectionUtils.isEmpty(pointsOfSale)) {
            return false;
        }
        for (String pSale : pointsOfSale) {
            if (pSale.equalsIgnoreCase(pointOfSale)) {
                return true;
            }
        }
        return false;
    }
}
