package com.vedantu.organization.pojos.responses.organizations;

import org.apache.commons.lang3.StringUtils;

import play.Play;

import com.vedantu.commons.enums.AuthType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.responses.ModelExtendedInfo;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.enums.OrganizationStatus;
import com.vedantu.organization.enums.OrganizationType;
import com.vedantu.organization.pojos.LicensingInfo;

public class OrgInfo extends ModelExtendedInfo {

    public String             fullName;
    public OrganizationType   type;
    public Scope              scope;
    public AuthType           authType;
    public OrganizationStatus status;
    public String             orgThumbnail;
    public LicensingInfo      planInfo;
    public String             referer;
    public String             orgURL;
    public String             slug;

    public OrgInfo() {

        super();
    }

    public OrgInfo(String id, String name, String fullName, OrganizationType type, Scope scope,
            OrganizationStatus status, String orgThumbnail, long timeCreated, long lastUpdated,
            String referer,String slug, VedantuRecordState recordState) {

        super(id, recordState, name, timeCreated, lastUpdated);
        this.fullName = fullName;
        this.type = type;
        this.scope = scope;
        this.status = status;
        this.orgThumbnail = orgThumbnail;
        this.referer = referer;
        this.slug = slug;

        String deploymentDomain = Play.application().configuration().getString("deployment.domain");
        this.orgURL = StringUtils.isNotEmpty(referer) ? referer
                : (StringUtils.isNotEmpty(slug) ? deploymentDomain + "/org/" + slug
                        : deploymentDomain);

    }

}
