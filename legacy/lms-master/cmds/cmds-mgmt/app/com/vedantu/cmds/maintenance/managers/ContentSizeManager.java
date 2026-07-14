package com.vedantu.cmds.maintenance.managers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;
import com.vedantu.cmds.managers.CMDSLibraryManager;
import com.vedantu.cmds.models.event.details.CalculateSizeDetails;
import com.vedantu.cmds.pojos.responses.ReIndexRes;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.pojos.responses.ActionTakenRes;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.daos.OrgSectionDAO;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.organization.models.OrgSection;
import com.vedantu.organization.models.Organization;
import com.vedantu.user.managers.AbstractVedantuEventManager;

public class ContentSizeManager extends AbstractVedantuEventManager {

    public static final ContentSizeManager INSTANCE            = new ContentSizeManager();
    private static final ALogger           LOGGER              = Logger.of(ContentSizeManager.class);
    public static final int                INDEXING_BATCH_SIZE = 20;

    public static ReIndexRes calculate(EntityType type, List<String> ids, String userId) {

        ReIndexRes response = new ReIndexRes();
        response.accepted = true;

        CalculateSizeDetails details = new CalculateSizeDetails();
        details.type = type;
        LOGGER.debug("Looking for ids:" + ids);
        VedantuBasicDAO<?, ?> dao = EntityTypeDAOFactory.INSTANCE.get(type);

        if (CollectionUtils.isEmpty(ids)) {
            ids = new ArrayList<String>();

            Query<?> query = dao.createQuery();
            query.field("size.finalized").equal(false).field(ConstantsGlobal.RECORD_STATE).notEqual(VedantuRecordState.DELETED);
            long totalHits =query.countAll();
            
            List<?> keys = query.asKeyList();
            response.totalAffectedDocs = keys.size();
            for (Object key : keys) {

                String keyId = ((Key<?>) key).getId().toString();
                LOGGER.debug("Key" + keyId);
                ids.add(keyId);
            }
        }
        if( CollectionUtils.isEmpty(ids)){
            return response;
        }
        if (ids.size() > INDEXING_BATCH_SIZE) {
          
            int start = 0;
            while (start < ids.size()) {
                int offset = (ids.size() - start) > INDEXING_BATCH_SIZE ? INDEXING_BATCH_SIZE
                        : (ids.size() - start);
              
                details.ids =  ids.subList(start, start + offset);
               
                generateEventAysc(userId, details, EventType.CALCULATE_SIZE);
                start = start + offset;
            }

        } else {
     
            details.ids = new ArrayList<String>();
            details.ids.addAll(ids);
            generateEventAysc(userId, details, EventType.CALCULATE_SIZE);
        }
        response.totalAffectedDocs = ids.size();
        return response;

    }

    public static ActionTakenRes calculate(String orgId, String sectionId) throws VedantuException {

        ActionTakenRes response = new ActionTakenRes();
        response.done = true;

        List<String> orgIds = new ArrayList<String>();
        if (StringUtils.isNotEmpty(orgId)) {
            orgIds.add(orgId);
        } else {
            QueryResults<Organization> results = OrganizationDAO.INSTANCE.find();

            for (Organization organization : results.asList()) {
                orgIds.add(organization._getStringId());
            }
        }

        List<String> sectionIds = new ArrayList<String>();
        if (StringUtils.isNotEmpty(sectionId)) {
            sectionIds.add(sectionId);
        }
        for (String iOrgId : orgIds) {

            List<OrgSection> orgSections = OrgSectionDAO.INSTANCE.getSectionsByIds(iOrgId, null,
                    ObjectIdUtils.toObjectIds(sectionIds));

            for (OrgSection section : orgSections) {
                ActionTakenRes subResponse = CMDSLibraryManager.calculate(section._getStringId());
                response.done &= subResponse.done;
                if (!subResponse.done) {
                    LOGGER.debug("Failed to calculate size for section" + section.getName()
                            + "  id " + section._getStringId());
                }
            }
        }

        return response;
    }
}
