package com.lms.service.serviceImpl;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.utils.ObjectIdUtils;
import com.lms.common.utils.URLUtils;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.models.OrgMicrositeConfig;
import com.lms.models.Organization;
import com.lms.pojo.request.AddMicrositeConfigReq;
import com.lms.pojo.request.GetOrgMicrositeConfigReq;
import com.lms.pojo.request.ValidateExternalURLReq;
import com.lms.pojo.responce.CheckSlugRes;
import com.lms.pojo.responce.GetOrgMicrositeRes;
import com.lms.repository.OrgMicrositeConfigRepo;
import com.lms.repository.OrganizationRepo;
import com.lms.service.MicrositesService;
import com.lms.util.GooglePlayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Map;

@Service
public class MicrositesServiceImpl implements MicrositesService {
    private static final Logger logger = LoggerFactory.getLogger(MicrositesServiceImpl.class);

    @Autowired
    private OrgMicrositeConfigRepo orgMicrositeConfigRepo;
    @Autowired
    private OrganizationRepo organizationRepo;
    @Override
    public VedantuResponse getConfiguRation(GetOrgMicrositeConfigReq request) {
        if (request==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        if (ObjectIdUtils.hasInvalidId(request.getOrgId().trim())) {
           throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }
        GetOrgMicrositeRes getOrgRes = getConfiguration(request);
        return new VedantuResponse(getOrgRes);
    }

    @Override
    public VedantuResponse addToConfig(AddMicrositeConfigReq addMicrositeConfigReq) {
        if (addMicrositeConfigReq==null)
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);


        if (ObjectIdUtils.hasInvalidId(addMicrositeConfigReq.getOrgId())) {
           throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }
        GetOrgMicrositeRes getOrgRes = addToConfigure(addMicrositeConfigReq);

        return new VedantuResponse(getOrgRes);
    }

    @Override
    public VedantuResponse checkURL(ValidateExternalURLReq validateExternalURLReq) {

        if (validateExternalURLReq==null)
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        CheckSlugRes checkOrgSlugRes = new CheckSlugRes();
        checkOrgSlugRes.setAvailable(URLUtils.isURLExist(validateExternalURLReq.getUrl()));

        return new VedantuResponse(checkOrgSlugRes);
    }

    private GetOrgMicrositeRes addToConfigure(AddMicrositeConfigReq addMicrositeConfigReq) {
        logger.debug("Saving microsite configuration");
        OrgMicrositeConfig config = addOrgConfig(addMicrositeConfigReq.getOrgId(),
                addMicrositeConfigReq.getTemplateId(), addMicrositeConfigReq.getConfig(), addMicrositeConfigReq.getPrivacy());

        Organization organization = organizationRepo.findByIdAndRecordState(addMicrositeConfigReq.getOrgId(),VedantuRecordState.ACTIVE);
        if (organization == null) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
        }
        GetOrgMicrositeRes response = new GetOrgMicrositeRes(addMicrositeConfigReq.getOrgId());
        GooglePlayUtil googlePlayUtil=new GooglePlayUtil();
        response.playURL = googlePlayUtil.getUrlForOrganizationApp(organization.slug);

        if (config.getConfigs() != null && !config.getConfigs().keySet().isEmpty()) {
            response.config.putAll(config.getConfigs());
        }
        response.setPrivacyURL(config.privacy);
        return response;

    }
    public OrgMicrositeConfig addOrgConfig(String orgId, String templateId,
                                           Map<String, String> config, String privacy) throws VedantuException {
        OrgMicrositeConfig createdConfig=orgMicrositeConfigRepo.findByOrgId(orgId);
        if (config == null) {
            createdConfig = new OrgMicrositeConfig();
        }
        createdConfig.orgId = orgId;
        if (config != null) {
            createdConfig.configs.putAll(config);
        }
        createdConfig.setTemplateId(templateId);
        createdConfig.setPrivacy(privacy);
        orgMicrositeConfigRepo.save(createdConfig);
            return createdConfig;

    }

    private GetOrgMicrositeRes getConfiguration(GetOrgMicrositeConfigReq request) {
        OrgMicrositeConfig micrositeConfiguration = getConfig(request.getOrgId());
        Organization organization = organizationRepo.findByIdAndRecordState(request.getOrgId(), VedantuRecordState.ACTIVE);

        if (organization == null) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
        }
        if (!organization.slug.equalsIgnoreCase(request.getSlug())) {
            throw new VedantuException(VedantuErrorCode.INVALID_SLUG);
        }

        GetOrgMicrositeRes response = new GetOrgMicrositeRes(request.orgId);
        GooglePlayUtil googlePlayUtil=new GooglePlayUtil();
        response.setPlayURL(googlePlayUtil.getUrlForOrganizationApp(request.getSlug()));
        //response.config.putAll(micrositeConfiguration.getConfigs());

        if (micrositeConfiguration!=null&&micrositeConfiguration.getConfigs() != null
                && !micrositeConfiguration.getConfigs().keySet().isEmpty()) {
            response.getConfig().putAll(micrositeConfiguration.getConfigs());
        }
if(micrositeConfiguration!=null)
        response.setPrivacyURL(micrositeConfiguration.getPrivacy());

        return response;
    }

    private OrgMicrositeConfig getConfig(String orgId) {
        OrgMicrositeConfig orgMicrositeConfig= orgMicrositeConfigRepo.findByOrgId(orgId);
        return orgMicrositeConfig;
    }
}
