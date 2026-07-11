package com.lms.pojo.request;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.common.vedantu.commons.pojos.requests.Location;
import com.lms.common.vedantu.enums.AuthType;
import com.lms.common.vedantu.enums.EncryptionLevel;

import com.lms.enums.DoubtsForumMode;
import com.lms.pojo.ExternalOrganizationEndpoints;
import com.lms.pojo.request.AppInfo;
import com.lms.user.vedantu.user.pojo.SocialInfo;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Setter
@Getter
public class UpdateOrgReq extends AbstractAuthCheckReq {

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
    public static final Object           AUTH_TYPE      = "authType";
    public static final Object           END_POINT      = "endPoint";
    public static final Object           APP_INFO       = "appInfos";
    public static final String           COMMUNICATION_MAIL     = "communicationMail";
    public static final String           SMTP_HOST     = "smtpHost";
    public static final String           SMTP_USER     = "smtpUser";
    public static final String           SMTP_PASSWORD  = "smtpPassword";
    public static final String           INSTAMOJO_CLIENT_ID  = "instaMojoClientId";
    public static final String           INSTAMOJO_CLIENT_SECRET  = "instaMojoClientSecret";
    public static final String           INSTAMOJO_API_KEY        = "instaMojoApiKey";
    public static final String           INSTAMOJO_AUTH_TOKEN        = "instaMojoAuthToken";

    public String                        name;
    public String                        fullName;
    private String                       website;
    private String                       emailDomain;
    public String                        contactNumber;
    public String                        type;
    public List<Location> locations;
    public String                        address;
    public String                        description;
    public String                        scope;
    public String                        slug;
    public EncryptionLevel encLevel;
    public ExternalOrganizationEndpoints endPoint;
    public SocialInfo socialMedia;
    public List<AppInfo>                 appInfos;
    public String                        communicationMail;
    public String                        smtpHost;
    public String                        smtpUser;
    public String                        smtpPassword;

    public String                        instaMojoClientId;
    public String                        instaMojoClientSecret;
    public String                        instaMojoApiKey;
    public String                        instaMojoAuthToken;
    public int                           versionCode;
    // this params will be used to define the organization points of sale (i.e
    // Snapdeal,Amazon,Flipkart etc) (comma(,) separated values)
    public String                        pointsOfSale;

    @NotBlank(message = "orgId should not be null")
    public String                        orgId;
    public DoubtsForumMode doubtsForumMode;
    public boolean                       disableSignup;
    public String                        disableSignupMessage;

    public AuthType authType       = AuthType.VEDANTU;

    public List<String>                  updateList     = new ArrayList<String>();

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


}