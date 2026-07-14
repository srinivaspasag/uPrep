package com.vedantu.comm.daos;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.mongodb.BasicDBObject;
import com.vedantu.comm.enums.ShareType;
import com.vedantu.comm.models.mongo.EntityShare;
import com.vedantu.commons.ShareWithEntity;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuDBResult;
import com.vedantu.organization.daos.OrgMemberDAO;
import com.vedantu.organization.daos.OrgSectionDAO;
import com.vedantu.organization.models.OrgMember;
import com.vedantu.organization.models.OrgSection;

public class EntityShareDAO extends VedantuBasicDAO<EntityShare, ObjectId> {

    private static final ALogger LOGGER   = Logger.of(EntityShareDAO.class);
    public static EntityShareDAO INSTANCE = new EntityShareDAO();

    private EntityShareDAO() {

        super(EntityShare.class);
        // TODO Auto-generated constructor stub
    }

    public EntityShare addShare(String orgId, String userId, SrcEntity sharedEntity,
            List<ShareWithEntity> shareWith, String content, ShareType type)
            throws VedantuException {

        LOGGER.info("Getting eligible share ");
        Set<SrcEntity> entities = getEligibleSharee(userId, orgId, sharedEntity, shareWith);

        EntityShare entityShare = new EntityShare(userId, sharedEntity, entities, content, type);

        save(entityShare);
        return entityShare;
    }

    private Set<SrcEntity> getEligibleSharee(String userId, String orgId, SrcEntity content,
            List<ShareWithEntity> shareWith) throws VedantuException {

        LOGGER.debug("Getting eligible share" + shareWith);
        Set<String> shareeIds = new HashSet<String>();
        OrgMember orgMember = OrgMemberDAO.INSTANCE.getMemberByUserId(orgId, userId);
        LOGGER.debug("Getting org share" + orgMember.email);
        // Set<SrcEntity> userSections = new HashSet<SrcEntity>();
        // if (orgMember != null && CollectionUtils.isNotEmpty(
        // orgMember.mappings) ) {
        // LOGGER.debug(" OrgMember  found" + orgMember);
        //
        // for (OrgMemberMappingInfo mapping : orgMember.mappings) {
        // userSections.add(new SrcEntity(EntityType.SECTION,
        // mapping.sectionId));
        // }
        // } else {
        // LOGGER.debug(" OrgMember not found");
        // throw new VedantuException(VedantuErrorCode.SHARING_NOT_ALLOWED);
        // }

        Set<SrcEntity> sharedWithEntities = new HashSet<SrcEntity>();
        LOGGER.info("Getting eligible share" + shareWith);
        for (ShareWithEntity shareWitEntityInDetail : shareWith) {
            if (shareWitEntityInDetail.type == EntityType.USER) {
                if (shareWitEntityInDetail.id.equals(userId)) {
                    throw new VedantuException(VedantuErrorCode.CAN_NOT_SHARE_WITH_SELF);
                }
                sharedWithEntities.add(shareWitEntityInDetail);
                shareeIds.add(shareWitEntityInDetail.id);
            } else if (shareWitEntityInDetail.type == EntityType.PROGRAM) {
                Set<String> centerIds = new HashSet<String>();

                if (CollectionUtils.isNotEmpty(shareWitEntityInDetail.centers)) {

                    for (SrcEntity centerEntity : shareWitEntityInDetail.centers) {
                        Logger.info(" Sharing Requested centerIds" + centerEntity.id);
                        centerIds.add(centerEntity.id);
                    }
                }

                BasicDBObject getAllSectionsQuery = new BasicDBObject();
                getAllSectionsQuery.put(ConstantsGlobal.PROGRAM_ID, shareWitEntityInDetail.id);
                if (CollectionUtils.isNotEmpty(centerIds)) {

                    getAllSectionsQuery.put(ConstantsGlobal.CENTER_ID, new BasicDBObject(
                            MongoManager.IN_QUERY, centerIds));

                }

                VedantuDBResult<OrgSection> sections = OrgSectionDAO.INSTANCE.getInfos(
                        getAllSectionsQuery, null, MongoManager.NO_START, MongoManager.NO_LIMIT,
                        null);
                for (OrgSection orgSection : sections.results) {
                    sharedWithEntities.add(new SrcEntity(EntityType.SECTION, orgSection
                            ._getStringId()));
                    shareeIds.add(orgSection._getStringId());

                }
            } else if (shareWitEntityInDetail.type == EntityType.SECTION) {
                sharedWithEntities
                        .add(new SrcEntity(EntityType.SECTION, shareWitEntityInDetail.id));
                shareeIds.add(shareWitEntityInDetail.id);

            } else if (shareWitEntityInDetail.type == EntityType.ORGANIZATION) {
                LOGGER.debug("Posting news feed for organization " + shareWitEntityInDetail.id);
                BasicDBObject getAllSectionsQuery = new BasicDBObject();

                getAllSectionsQuery.put(ConstantsGlobal.ORG_ID, shareWitEntityInDetail.id);

                VedantuDBResult<OrgSection> sections = OrgSectionDAO.INSTANCE.getInfos(
                        getAllSectionsQuery, null, MongoManager.NO_START, MongoManager.NO_LIMIT,
                        null);

                for (OrgSection orgSection : sections.results) {
                    sharedWithEntities.add(new SrcEntity(EntityType.SECTION, orgSection
                            ._getStringId()));
                    shareeIds.add(orgSection._getStringId());

                }

            }
        }
        // LOGGER.debug(" UserSections" + userSections+
        // " shared with entities "+ sharedWithEntities);
        // if (!userSections.containsAll(sharedWithEntities)) {
        // throw new VedantuException(VedantuErrorCode.SHARING_NOT_ALLOWED);
        // }
        if (checkAlreadySharedWith(shareeIds, content)) {
            return sharedWithEntities;
        }

        return null;
    }

    private boolean checkAlreadySharedWith(Set<String> probableAlreadyShareeId, SrcEntity content)
            throws VedantuException {

        BasicDBObject alreadySharedWithCheckQuery = new BasicDBObject();
        alreadySharedWithCheckQuery.put("with.id", new BasicDBObject(MongoManager.IN_QUERY,
                probableAlreadyShareeId));
        alreadySharedWithCheckQuery.put("entity.type", content.type.name());
        alreadySharedWithCheckQuery.put("entity.id", content.id);

        VedantuDBResult<EntityShare> result = getInfos(alreadySharedWithCheckQuery, null,
                MongoManager.NO_START, MongoManager.NO_LIMIT, null);

        if (result.totalHits > 0) {
            throw new VedantuException(VedantuErrorCode.ALREADY_SHARED);

        }
        return true;
    }

    public Set<SrcEntity> getAllShares(SrcEntity content) {

        Query<EntityShare> entityShareQuery = getDS().createQuery(EntityShare.class);

        List<EntityShare> shareEventList = entityShareQuery.filter("entity.type", content.type)
                .filter("entity.id", content.id).asList();
        LOGGER.debug(" Entity Share query" + entityShareQuery.toString());
        Set<SrcEntity> sharedWithSet = new HashSet<SrcEntity>();
        if (CollectionUtils.isNotEmpty(shareEventList)) {
            for (EntityShare entityShare : shareEventList) {
                LOGGER.debug(" Entity Share with call" + entityShare.with);
                sharedWithSet.addAll(entityShare.with);
            }
        }
        return sharedWithSet;

    }

}