package com.lms.models;

import com.lms.common.utils.ImageDisplayURLUtil;
import com.lms.common.vedantu.commons.pojos.requests.AppSecurityCredentials;
import com.lms.common.vedantu.commons.pojos.requests.InputFieldInfo;
import com.lms.common.vedantu.commons.pojos.requests.Location;
import com.lms.common.vedantu.commons.pojos.requests.SecurityCredentials;
import com.lms.common.vedantu.constants.HardCodedConstants;
import com.lms.common.vedantu.enums.AuthType;
import com.lms.common.vedantu.enums.EncryptionLevel;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.Scope;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.enums.DoubtsForumMode;
import com.lms.enums.OrgMemberProfile;
import com.lms.enums.OrganizationStatus;
import com.lms.enums.OrganizationType;
import com.lms.pojo.ExternalOrganizationEndpoints;
import com.lms.pojo.request.AppInfo;
import com.lms.pojo.request.SmsGatewayInfo;
import com.lms.user.vedantu.user.pojo.SocialInfo;
import com.lms.user.vedantu.user.pojo.TnCAcceptance;
import com.lms.user.vedantu.user.pojo.UserBasicInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Document(collection = "organizations")
@Setter
@Getter
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
    public static final String FIELD_DISABLE_SIGNUP_MESSAGE = "disableSignupMessage";
    public static final String FIELD_SMS_GATEWAY = "smsGateway";
    public static final String COMMUNICATION_MAIL = "communicationMail";
    public static final String SMTP_HOST = "smtpHost";
    public static final String SMTP_USER = "smtpUser";
    public static final String SMTP_PASSWORD = "smtpPassword";
    public static final String INSTAMOJO_CLIENT_ID = "instaMojoClientId";
    public static final String INSTAMOJO_CLIENT_SECRET = "instaMojoClientSecret";
    public static final String INSTAMOJO_API_KEY = "instaMojoApiKey";
    public static final String INSTAMOJO_AUTH_TOKEN = "instaMojoAuthToken";
    public static final String VERSION_CODE = "versionCode";
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static final String FIELD_ENABLE_OTP = "enableOTP";


    @Transient
    public static transient String FIELD_APP_CREDENTIALS = "appCredentials";

    public String name;
    public String fullName;
    @Indexed(unique = true)
    public String website;
    public String emailDomain;
        public String                                      contactNumber;
        public OrganizationType type;
        public List<Location> locations;
        public String                                      address;
        public String                                      description;
        public Scope scope;
        public String                                      thumbnail;
        public UserBasicInfo representative;
        @Indexed
        public OrganizationStatus status;
        public OrganizationStatus                          studentPageStatus;

        public SecurityCredentials credentials;
        @Indexed(unique = true)
        public String                                      slug;
        @Indexed(unique = true, sparse = true)
        public String                                      referer;                                   // This


        public String                                      adminUserId;
        public String                                      adminOrgMemberId;

        public EncryptionLevel encLevel              = EncryptionLevel.NA;

        public Subscription subscription;
        public TnCAcceptance tncAcceptance;

        public AuthType authType;
        public ExternalOrganizationEndpoints endPoint;

        public SocialInfo socialMedia;
        public Map<OrgMemberProfile, List<InputFieldInfo>> extraMemberInfoFields;

        // app credentials will be used to verify external call in organization scope

    public List<AppSecurityCredentials> appCredentials;
    public List<AppInfo> appInfos;

    // this params will be used to define the organization points of sale (i.e
    // Snapdeal,Amazon,Flipkart etc)
    public List<String> pointsOfSale;
    public Set<String> digitalLibraryHiddenFields;
    public DoubtsForumMode doubtsForumMode;
    public boolean disableSignup;
    public String disableSignupMessage;
    public SmsGatewayInfo smsGateway;
    public boolean isNewUI;
    public boolean showSharedSubjects;
    public boolean showClassroomConnect;
    public boolean disableDownload;
    public boolean enableOTP;
    public String theme;
    public String communicationMail;
    public String smtpHost;
    public String smtpUser;
    public String smtpPassword;
    public String instaMojoClientId;
        public String                                      instaMojoClientSecret;
        public String                                      instaMojoApiKey;
        public String                                      instaMojoAuthToken;
        public int										   versionCode;
    public String getStringId() {

        return id!=null ? id.toString() : HardCodedConstants.emptyString;
    }
    public Organization() {

    }
    public static String _getThumbnailUrl(String thumbnail) {

        if(thumbnail!=null){
            return ImageDisplayURLUtil.getEntityThumbnail(
                    EntityType.ORGANIZATION, thumbnail);
        }
        else{

            return  ImageDisplayURLUtil.getEntityStaticThumbnail(EntityType.ORGANIZATION, Arrays.asList("default"));
        }


    }

     /*   @Override
        public ModelExtendedInfo toExtendedInfo() {


        OrgInfo orgInfo = new OrgInfo(_getStringId(), name, fullName, type, scope, status,
                _getThumbnailUrl(), timeCreated, lastUpdated, referer, slug, recordState);
        orgInfo.setAuthType(authType);
        if (subscription != null && !(subscription.getPlanId().isEmpty())) {
            orgInfo.planInfo = (LicensingInfo) LicensingPlanRepd(subscription.getPlanId());
        }
        orgInfo.slug = slug;

        return orgInfo;
    }

       public String _getThumbnailUrl() {

        return !(thumbnail).isEmpty() ? ImageDisplayURLUtil.getEntityThumbnail(
                EntityType.ORGANIZATION, thumbnail) : ImageDisplayURLUtil.getEntityStaticThumbnail(
                EntityType.ORGANIZATION, Arrays.asList("default"));
    }

     /*   @Override
        public ModelBasicInfo toBasicInfo() {

        OrgBasicInfo orgInfo = new OrgBasicInfo(_getStringId(), recordState, name);
        orgInfo.thumbnail = this._getThumbnailUrl();
        orgInfo.fullName = this.name;
        orgInfo.authType = this.authType;

        return orgInfo;
    }*/

        public String __getOrgType() {

        return type.name();
    }

        public AppSecurityCredentials __addOrGetAppCredentials(String appId, AtomicBoolean updated) {

        if (appCredentials == null) {
            appCredentials = new ArrayList<AppSecurityCredentials>();
        }

        for (AppSecurityCredentials appCred : appCredentials) {
            if (appCred.getAppId().equals(appId)) {
                return appCred;
            }
        }

        AppSecurityCredentials appCredential = new AppSecurityCredentials();
        appCredential.setAppId(appId);
       appCredential.setAuthToken(randomAlphaNumeric(20));
        appCredential.setSecretKey(UUID.randomUUID().toString().replace("-", ""));
        appCredentials.add(appCredential);
        updated.set(true);
            return appCredential;
    }

        public boolean __isValidPointOfSale(String pointOfSale) {

        if (!(pointsOfSale.isEmpty())) {
            return false;
        }
        for (String pSale : pointsOfSale) {
            if (pSale.equalsIgnoreCase(pointOfSale)) {
                return true;
            }
        }
        return false;
    }
    public static String randomAlphaNumeric(int count) {

        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }


}
