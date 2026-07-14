package com.lms.pojo.request;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAppCheckReq;
import com.lms.common.vedantu.commons.pojos.requests.Location;
import com.lms.common.vedantu.enums.AppStore;
import com.lms.common.vedantu.enums.EncryptionLevel;
import com.lms.pojo.ExternalOrganizationEndpoints;
import com.lms.user.vedantu.user.pojo.SocialInfo;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
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

    public static final String ENC_LEVEL = "encLevel";
    public static final String SOCIAL_MEDIA = "socialMedia";
    public static final String REPRESENTATIVE = "representative";
    public static final String AUTH_TYPE = "authType";
    public static final String END_POINT = "endPoint";
    public static final String APP_INFO = "appInfos";
    public static final String POINTS_OF_SALE = "pointsOfSale";
    public static final String DOUBTS_FORUM_MODE = "doubtsForumMode";
    public static final String DISABLE_SIGNUP = "disableSignup";
    public static final String ENABLE_OTP = "enableOTP";
    public static final String SMS_GATEWAY = "smsGateway";
    public static final String DISABLE_SIGNUP_MESSAGE = "disableSignupMessage";
    public static final String SMTP_HOST = "smtpHost";
    public static final String SMTP_USER = "smtpUser";
    public static final String SMTP_PASSWORD = "smtpPassword";
    public static final String INSTAMOJO_CLIENT_ID = "instaMojoClientId";
    public static final String INSTAMOJO_CLIENT_SECRET = "instaMojoClientSecret";
    public static final String INSTAMOJO_API_KEY = "instaMojoApiKey";
    public static final String INSTAMOJO_AUTH_TOKEN = "instaMojoAuthToken";
    public static final String COMMUNICATION_MAIL = "communicationMail";
    public static final String VERSION_CODE = "versionCode";
    @NotBlank(message = "name should not be null")
    public String                        name;
    @NotBlank(message = "fullname should not be null")
    public String                        fullName;
    @NotBlank(message = "website should not be null")
    private String                       website;
    @NotBlank(message = "emailDomain should not be null")
    private String                       emailDomain;
    @NotBlank(message = "contactNumber should not be null")
    public String                        contactNumber;
    @NotBlank(message = "type should not be null")
    public String                        type;
    @NotBlank(message = "location should not be null")
    public List<Location> locations;
    @NotBlank(message = "address should not be null")
    public String                        address;
    public String                        description;
    @NotBlank(message = "scope should not be null")
    public String                        scope;

    @NotBlank(message = "slug should not be null")
    public String                        slug;
    public EncryptionLevel encLevel;
    public ExternalOrganizationEndpoints endPoint;

    public SocialInfo socialMedia;
    public Map<AppStore, String> appInfo;

    public List<String> updateList = new ArrayList<String>();

    public String getWebsite() {

        return website;
    }

    public void setWebsite(String website) {

        this.website = website.toLowerCase();
    }

    public String getEmailDomain() {

        return emailDomain;
    }

    public void setEmailDomain(String emailDomain) {

        this.emailDomain = emailDomain.toLowerCase();
    }

    public String validate() {


        if (slug == null) {
            return "missing slug";
        }


        return null;
    }

}
