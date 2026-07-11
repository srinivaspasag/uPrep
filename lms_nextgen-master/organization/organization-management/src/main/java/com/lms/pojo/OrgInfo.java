package com.lms.pojo;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelExtendedInfo;
import com.lms.common.vedantu.enums.AuthType;
import com.lms.common.vedantu.enums.Scope;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.OrganizationType;
import com.lms.enums.OrganizationStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;


@Setter
@Getter
public class OrgInfo extends ModelExtendedInfo {

    public String fullName;
    public OrganizationType type;
    public Scope scope;
    public AuthType authType;
    public OrganizationStatus status;
    public String orgThumbnail;
    public LicensingInfo planInfo;
    public String referer;
    public String orgURL;
    public String slug;
    @Value("${deployment.domain}")
    private String deploymentDomain;

    public OrgInfo() {

        super();
    }

    public OrgInfo(String id, String name, String fullName, OrganizationType type, Scope scope,
                   OrganizationStatus status, String orgThumbnail, long timeCreated, long lastUpdated,
                   String referer, String slug, VedantuRecordState recordState) {

        super(id, recordState, name, timeCreated, lastUpdated);
        this.fullName = fullName;
        this.type = type;
        this.scope = scope;
        this.status = status;
        this.orgThumbnail = orgThumbnail;
        this.referer = referer;
        this.slug = slug;


        this.orgURL = referer!=null ? referer : slug!=null ? deploymentDomain + "/org/" + slug
                : deploymentDomain;

    }
}
