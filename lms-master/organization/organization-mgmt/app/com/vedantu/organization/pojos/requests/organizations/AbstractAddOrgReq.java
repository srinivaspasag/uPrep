package com.vedantu.organization.pojos.requests.organizations;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.enums.AppStore;
import com.vedantu.commons.enums.EncryptionLevel;
import com.vedantu.commons.pojos.Location;
import com.vedantu.commons.pojos.requests.AbstractAppCheckReq;
import com.vedantu.organization.managers.OrganizationManager;
import com.vedantu.organization.pojos.ExternalOrganizationEndpoints;
import com.vedantu.user.pojos.SocialInfo;

public abstract class AbstractAddOrgReq extends AbstractAppCheckReq {

    public static final String           NAME           = "name";
    public static final String           FULL_NAME      = "fullName";
    public static final String           WEBSITE        = "website";
    public static final String           CONTACT_NUMBER = "contactNumber";
    public static final String           TYPE           = "type";
    public static final String           LOCATIONS      = "locations";
    public static final String           ADDRESS        = "address";
    public static final String           DESCRIPTION    = "description";
    public static final String           SCOPE          = "scope";
    public static final String           SLUG           = "slug";

    public static final String           ENC_LEVEL      = "encLevel";
    public static final String           SOCIAL_MEDIA   = "socialMedia";
    public static final String           REPRESENTATIVE = "representative";
    public static final String           AUTH_TYPE      = "authType";
    public static final String           END_POINT      = "endPoint";
    public static final String           APP_INFO       = "appInfos";
    public static final String           POINTS_OF_SALE = "pointsOfSale";
    public static final String           DOUBTS_FORUM_MODE = "doubtsForumMode";
    public static final String           DISABLE_SIGNUP    = "disableSignup";
    public static final String           ENABLE_OTP        = "enableOTP";
    public static final String           SMS_GATEWAY    =    "smsGateway";
    public static final String           DISABLE_SIGNUP_MESSAGE    = "disableSignupMessage";
    public static final String           SMTP_HOST = "smtpHost";
    public static final String           SMTP_USER = "smtpUser";
    public static final String           SMTP_PASSWORD = "smtpPassword";
    public static final String           INSTAMOJO_CLIENT_ID	   = "instaMojoClientId";
    public static final String           INSTAMOJO_CLIENT_SECRET   = "instaMojoClientSecret";
    public static final String           INSTAMOJO_API_KEY         = "instaMojoApiKey";
    public static final String           INSTAMOJO_AUTH_TOKEN         = "instaMojoAuthToken";
    public static final String           COMMUNICATION_MAIL   = "communicationMail";
    public static final String           VERSION_CODE   = "versionCode";
    @Required
    public String                        name;
    @Required
    public String                        fullName;
    @Required
    private String                       website;
    private String                       emailDomain;
    @Required
    public String                        contactNumber;
    @Required
    public String                        type;
    @Required
    public List<Location>                locations;
    @Required
    public String                        address;
    public String                        description;
    @Required
    public String                        scope;

    @Required
    public String                        slug;
    public EncryptionLevel               encLevel;
    public ExternalOrganizationEndpoints endPoint;

    public SocialInfo                    socialMedia;
    public Map<AppStore, String>         appInfo;

    // public List<String> updateList = new ArrayList<String>();

    public String getWebsite() {

        return website;
    }

    public void setWebsite(String website) {

        this.website = StringUtils.lowerCase(website);
    }

    public String getEmailDomain() {

        return emailDomain;
    }

    public void setEmailDomain(String emailDomain) {

        this.emailDomain = StringUtils.lowerCase(emailDomain);
    }

    public String validate() {

        String superValidate = super.validate();
        if (null != superValidate) {
            return superValidate;
        }
        if (slug == null) {
            return "missing slug";
        }
        if (!OrganizationManager.isValidSlug(slug)) {
            return "invalid slug, special characters other than .(dot) are not allowed";
        }

        if (null != locations) {
            for (Location location : locations) {
                if (null != location) {
                    String locationValidation = location.validate();
                    if (null != locationValidation) {
                        return locationValidation;
                    }
                }
            }
        }
        return null;
    }

}
