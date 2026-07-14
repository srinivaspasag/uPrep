package com.vedantu.organization.pojos.responses.organizations;

import java.util.List;

import com.vedantu.commons.enums.AuthType;
import com.vedantu.commons.enums.EncryptionLevel;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.Location;
import com.vedantu.organization.enums.DoubtsForumMode;
import com.vedantu.organization.enums.OrganizationStatus;
import com.vedantu.organization.enums.OrganizationType;
import com.vedantu.organization.pojos.ExternalOrganizationEndpoints;
import com.vedantu.organization.pojos.LicensingInfo;
import com.vedantu.organization.pojos.requests.AppInfo;
import com.vedantu.organization.pojos.requests.organizations.SmsGatewayInfo;
import com.vedantu.user.pojos.SocialInfo;
import com.vedantu.user.pojos.TnCAcceptance;
import com.vedantu.user.pojos.UserBasicInfo;

public class GetOrgRes {

    public String                        id;
    public String                        name;
    public String                        fullName;
    public String                        website;
    public String                        emailDomain;
    public String                        contactNumber;
    public OrganizationType              type;
    public List<Location>                locations;
    public String                        address;
    public String                        description;
    public Scope                         scope;
    public String                        orgThumbnail;
    public String                        slug;
    public UserBasicInfo                 representative;
    public String                        key;               // public key of the organization
    public EncryptionLevel               encLevel;
    public LicensingInfo                 planInfo;
    public boolean                       needsTnCAcceptance;
    public String                        acceptedTNCVersion;
    public String                        latestTnCVersion;
    public AuthType                      authType;
    public ExternalOrganizationEndpoints endPoint;
    public SocialInfo                    socialMedia;
    public List<AppInfo>                 appInfos;
    public List<String>                  pointsOfSale;
    // public List<ExternalURLInfo> extURLs;
    public String                        adminUserId;
    public String                        referer;
    public DoubtsForumMode               doubtsForumMode;
    public boolean                       disableSignup;
    public SmsGatewayInfo                smsGateway;
    public String                        disableSignupMessage;
    public boolean                       isNewUI;
    public boolean                       showSharedSubjects;
    public boolean                       showClassroomConnect;
    public boolean                       disableDownload;
    public boolean                       enableOTP;
    public String                        theme;
    public String                        communicationMail;
    public String                        smtpHost;
    public String                        smtpUser;
    public String                        smtpPassword;
    public String                        instaMojoClientId;
    public String                        instaMojoClientSecret;
    public String                        instaMojoApiKey;
    public String                        instaMojoAuthToken;
    public int                        	 versionCode;
    public OrganizationStatus            status;
    public String                        sharedQuestionsState;
    public OrganizationStatus            studentPageStatus;

    public GetOrgRes() {

    }

    public GetOrgRes(String id, String name, String fullName, String website, String emailDomain,
            String contactNumber, OrganizationType type, List<Location> locations, String address,
            String description, Scope scope, UserBasicInfo representative, String orgThumbnail,
            String slug, EncryptionLevel encLevel, LicensingInfo licensingInfo,
            TnCAcceptance tncAcceptance, SocialInfo socialMedia, List<AppInfo> appInfos) {

        this.id = id;
        this.name = name;
        this.fullName = fullName;
        this.website = website;
        this.emailDomain = emailDomain;
        this.contactNumber = contactNumber;
        this.type = type;
        this.locations = locations;
        this.address = address;
        this.description = description;
        this.scope = scope;
        this.representative = representative;
        this.orgThumbnail = orgThumbnail;
        this.slug = slug;
        this.encLevel = encLevel;
        this.planInfo = licensingInfo;
        this.acceptedTNCVersion = tncAcceptance != null ? tncAcceptance.version : null;
        this.socialMedia = socialMedia;
        // this.extURLs = new ArrayList<ExternalURLInfo>();
        this.appInfos = appInfos;

    }

}
