package com.vedantu.organization.pojos.requests.organizations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.enums.AppStore;
import com.vedantu.commons.enums.AuthType;
import com.vedantu.commons.enums.EncryptionLevel;
import com.vedantu.commons.pojos.Location;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.vedantu.organization.enums.DoubtsForumMode;
import com.vedantu.organization.managers.OrganizationManager;
import com.vedantu.organization.pojos.ExternalOrganizationEndpoints;
import com.vedantu.organization.pojos.requests.AppInfo;
import com.vedantu.user.pojos.SocialInfo;

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
    public List<Location>                locations;
    public String                        address;
    public String                        description;
    public String                        scope;
    public String                        slug;
    public EncryptionLevel               encLevel;
    public ExternalOrganizationEndpoints endPoint;
    public SocialInfo                    socialMedia;
    public SmsGatewayInfo                smsGateway;
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

    @Required
    public String                        orgId;
    public DoubtsForumMode               doubtsForumMode;
    public boolean                       disableSignup;
    public boolean                       enableOTP;
    public String                        disableSignupMessage;

    public AuthType                      authType       = AuthType.VEDANTU;

    public List<String>                  updateList     = new ArrayList<String>();

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

    @Override
    public String validate() {

        String superValidate = super.validate();
        if (null != superValidate) {
            return superValidate;
        }
        if (CollectionUtils.isEmpty(updateList)) {
            return null;
        }

        if (updateList.contains(NAME)) {
            if (StringUtils.isEmpty(name)) {
                return "missing name";
            }

        }
        if (updateList.contains(FULL_NAME)) {
            if (StringUtils.isEmpty(fullName)) {
                return "missing full name";
            }
        }

        if (updateList.contains(WEBSITE)) {
            if (StringUtils.isEmpty(getWebsite())) {
                return "missing full website";
            }

        }

        if (updateList.contains(CONTACT_NUMBER)) {
            if (StringUtils.isEmpty(contactNumber)) {
                return "missing  contactNumber";
            }
        }

        if (updateList.contains(TYPE)) {
            if (StringUtils.isEmpty(type)) {
                return "missing  type";
            }
        }
        if (updateList.contains(LOCATIONS)) {
            if (CollectionUtils.isEmpty(locations)) {

                return "missing  locations";
            }
            for (Location location : locations) {
                if (null != location) {
                    String locationValidation = location.validate();
                    if (null != locationValidation) {
                        return locationValidation;
                    }
                }
            }
        }

        if (updateList.contains(ADDRESS)) {
            if (StringUtils.isEmpty(address)) {
                return "missing  address";
            }
        }

        if (updateList.contains(SCOPE) && StringUtils.isEmpty(scope)) {
            return "missing  scope";

        }

        if (updateList.contains(SLUG)) {
            if (StringUtils.isEmpty(slug)) {

                return "missing  slug";
            }
            if (!OrganizationManager.isValidSlug(slug)) {
                return "invalid slug, special characters other than .(dot) are not allowed";
            }
        }

        if (updateList.contains(APP_INFO)) {
            Set<String> storeSet = new HashSet<String>();
            if (CollectionUtils.isNotEmpty(appInfos)) {
                List<AppInfo> newAppInfos = new ArrayList<AppInfo>();
                for (AppInfo appInfo : appInfos) {
                    AppStore current = AppStore.valueOfKey(appInfo.type);
                    if (current == AppStore.UNKNOWN) {
                        return " unknown app store provided";
                    }
                    if (storeSet.contains(appInfo.type)) {
                        return "duplicate app store provided";

                    }
                    if (StringUtils.isEmpty(appInfo.url)) {
                        continue;
                    }

                    storeSet.add(appInfo.type);
                    newAppInfos.add(appInfo);
                }

                appInfos = newAppInfos;

            }
        }
        return null;
    }
}
