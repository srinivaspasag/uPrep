package com.vedantu.organization.managers;

import org.apache.commons.collections.CollectionUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.daos.MicrositeDAO;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.organization.models.OrgMicrositeConfig;
import com.vedantu.organization.models.Organization;
import com.vedantu.organization.pojos.requests.microsite.AddMicrositeConfigReq;
import com.vedantu.organization.pojos.requests.microsite.GetOrgMicrositeConfigReq;
import com.vedantu.organization.pojos.responses.microsite.GetOrgMicrositeRes;
import com.vedantu.organization.pojos.utils.GooglePlayUtil;

public class MicrositeManager {

    public static final ALogger LOGGER = Logger.of(MicrositeManager.class);

    public static GetOrgMicrositeRes addToConfig(AddMicrositeConfigReq request)
            throws VedantuException {

        LOGGER.debug("Saving microsite configuration");
        OrgMicrositeConfig config = MicrositeDAO.INSTANCE.addConfig(request.orgId,
                request.templateId, request.config, request.privacy);

        Organization organization = OrganizationDAO.INSTANCE.getById(request.orgId,
                VedantuRecordState.ACTIVE);
        if (organization == null) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
        }
        GetOrgMicrositeRes response = new GetOrgMicrositeRes(request.orgId);
        response.playURL = GooglePlayUtil.getUrlForOrganizationApp(organization.slug);

        if (config.configs != null && CollectionUtils.isNotEmpty(config.configs.keySet())) {
            response.config.putAll(config.configs);
        }
        response.privacyURL = config.privacy;
        return response;
    }

    public static GetOrgMicrositeRes getConfig(GetOrgMicrositeConfigReq request)
            throws VedantuException {

        OrgMicrositeConfig micrositeConfiguration = MicrositeDAO.INSTANCE.getConfig(request.orgId);
        Organization organization = OrganizationDAO.INSTANCE.getById(request.orgId,
                VedantuRecordState.ACTIVE);
        if (organization == null) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
        }
        if (!organization.slug.equalsIgnoreCase(request.slug)) {
            throw new VedantuException(VedantuErrorCode.INVALID_SLUG);
        }

        GetOrgMicrositeRes response = new GetOrgMicrositeRes(request.orgId);
        response.playURL = GooglePlayUtil.getUrlForOrganizationApp(request.slug);
        response.config.putAll(micrositeConfiguration.configs);

        if (micrositeConfiguration.configs != null
                && CollectionUtils.isNotEmpty(micrositeConfiguration.configs.keySet())) {
            response.config.putAll(micrositeConfiguration.configs);
        }

        response.privacyURL = micrositeConfiguration.privacy;

        return response;
    }
}
