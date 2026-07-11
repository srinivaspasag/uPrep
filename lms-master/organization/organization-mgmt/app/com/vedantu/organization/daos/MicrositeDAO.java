package com.vedantu.organization.daos;

import java.util.Map;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.mongodb.MongoException.DuplicateKey;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.organization.models.OrgMicrositeConfig;

public class MicrositeDAO extends VedantuBasicDAO<OrgMicrositeConfig, ObjectId> {

    private static final ALogger     LOGGER   = Logger.of(OrganizationDAO.class);

    public static final MicrositeDAO INSTANCE = new MicrositeDAO();

    public MicrositeDAO() {

        super(OrgMicrositeConfig.class);

    }

    public OrgMicrositeConfig getConfig(String orgId) {

        Query<OrgMicrositeConfig> configQuery = createQuery();
        configQuery.filter("orgId", orgId);
        return configQuery.get();
    }

    public OrgMicrositeConfig addConfig(String orgId, String templateId,
            Map<String, String> config, String privacy) throws VedantuException {

        Query<OrgMicrositeConfig> configQuery = createQuery();
        configQuery.filter("orgId", orgId);
        OrgMicrositeConfig createdConfig = configQuery.get();
        if (config == null) {
            createdConfig = new OrgMicrositeConfig();
        }
        createdConfig.orgId = orgId;
        if (config != null) {
            createdConfig.configs.putAll(config);
        }
        createdConfig.templateId = templateId;
        createdConfig.privacy = privacy;
        try {
            save(createdConfig);
            return createdConfig;

        } catch (DuplicateKey exception) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_ALREADY_EXISTS);
        }
    }

}