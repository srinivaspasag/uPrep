package com.vedantu.cmds.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.HasChildQueryBuilder;
import org.elasticsearch.index.query.HasParentQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.facet.AbstractFacetBuilder;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.vedantu.cmds.daos.CmdsContentDAO;
import com.vedantu.cmds.daos.CmdsContentLinkDAO;
import com.vedantu.cmds.daos.SDCardDAO;
import com.vedantu.cmds.daos.SDCardGroupDAO;
import com.vedantu.cmds.enums.CmdsContentLinkType;
import com.vedantu.cmds.models.CMDSContentLink;
import com.vedantu.cmds.models.SDCard;
import com.vedantu.cmds.models.SDCardGroup;
import com.vedantu.cmds.models.event.search.details.CMDSResourceDetails;
import com.vedantu.cmds.pojos.export.SDCardInfo;
import com.vedantu.cmds.pojos.requests.exports.GetSDCardContentsReq;
import com.vedantu.cmds.pojos.requests.exports.GetSDCardReq;
import com.vedantu.cmds.pojos.responses.GetResourcesRes;
import com.vedantu.cmds.pojos.responses.GetSDCardInfoRes;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.daos.GranteeOrgProgramDAO;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.content.enums.search.SearchResultType;
import com.vedantu.content.managers.LibraryManager;
import com.vedantu.content.models.AbstractContentModel;
import com.vedantu.content.models.LibraryContentLink;
import com.vedantu.mongo.MongoManager.SortOrder;
import com.vedantu.mongo.SortOrderInfo;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.mongo.models.GranteeOrgProgram;
import com.vedantu.search.utils.ElasticSearchUtils;

public class SDCardManager extends AbstractCMDSContentManager {

    private static final ALogger LOGGER = Logger.of(SDCardManager.class);

    private SDCard               card;
    private double               allowdFraction;

    public SDCardManager() {

        allowdFraction = Play.application().configuration()
                .getDouble("sdcard.maximum.size.percentage", new Double(10)).doubleValue() / 100;

        LOGGER.debug("Allowed fraction " + allowdFraction);
    }

    public void initCard(SDCardGroup group) {

        SDCard card = new SDCard(group);
        card.groupId = group._getStringId();
        card.maxSize = group.cardSize;
        card.recordState = VedantuRecordState.TEMPORARY;
        card.contentSrc = group.contentSrc;
        card.target = group.target;
        
        card.contentSize = (long) (card.maxSize * allowdFraction); // buffer added initially
        group.size.addOriginal(card.contentSize);
        SDCardDAO.INSTANCE.save(card);
        this.card = card;
    }

    public SDCard getCard() {

        return card;
    }

    public static GetSDCardInfoRes getCard(GetSDCardReq request) throws VedantuException {

        SDCard sdCard = SDCardDAO.INSTANCE.getSDCard(request.id, request.orgId, request.groupId);
        if (sdCard == null) {
            throw new VedantuException(VedantuErrorCode.NOT_VALID_CMDS_CONTENT);
        }
        SDCardGroup group = SDCardGroupDAO.INSTANCE.getById(sdCard.groupId);

        GetSDCardInfoRes response = new GetSDCardInfoRes();

        SDCardInfo cardInfo = (SDCardInfo) sdCard.toBasicInfo();

        cardInfo.setName(group.__getCardName(sdCard._getStringId()));
        response.recordInfo = cardInfo;
        return response;
    }

    public static GetResourcesRes getCard(String groupId, String orgId, int start, int size)
            throws VedantuException {

        MutableLong totalHits = new MutableLong();
        List<SDCard> sdCards = SDCardDAO.INSTANCE.getSDCards(null, orgId, groupId, start, size,
                totalHits);
        if (sdCards == null) {
            throw new VedantuException(VedantuErrorCode.NOT_VALID_CMDS_CONTENT);
        }

        GetResourcesRes response = new GetResourcesRes();

        for (SDCard card : sdCards) {
            SDCardGroup group = SDCardGroupDAO.INSTANCE.getById(card.groupId);
            SDCardInfo cardInfo = (SDCardInfo) card.toBasicInfo();
            cardInfo.setName(group.__getCardName(card._getStringId()));

            response.list.add(cardInfo);
        }
        response.totalHits = totalHits.longValue();
        return response;
    }

    public boolean canAdd(long size) {

        LOGGER.debug("testing for size " + size + "  fraction size " + card.maxSize
                * allowdFraction + "  current card size " + card.getContentSize());

        if (card.getContentSize() + size >= card.maxSize) {
            Logger.debug("card size is exceeding");

            return false;

        }
        return true;
    }

    public boolean add(SrcEntity content, AbstractContentModel publishedContent)
            throws VedantuException {

        if (card == null) {
            return false;
        }
        SrcEntity publishedEntity = new SrcEntity(publishedContent.getContentType(),
                publishedContent._getStringId());
        LOGGER.debug("Evaluation contentsize" + publishedContent.getExportableSize()
                + " for entity " + publishedEntity);
        card.add(publishedContent.getExportableSize());

        SrcEntity target = new SrcEntity(EntityType.SDCARD, card._getStringId());

        CMDSContentLink link = CmdsContentLinkDAO.INSTANCE.addLink(content, target,
                CmdsContentLinkType.ADDED, null);

        LibraryContentLink libraryLink = LibraryManager.addToLibrary(publishedEntity, target,
                UserActionType.ADDED, null, Scope.ORG, null);

        link.globalLinkId = libraryLink._getStringId();
        CmdsContentLinkDAO.INSTANCE
                .updateModel(link, Arrays.asList(CMDSContentLink.GLOBAL_LINK_ID));

        CMDSContentLinkManager.INSTANCE.reIndex(link);
        card.count++;
        return true;

    }

    public SDCard confirm() throws VedantuException {

        Logger.debug("Confirming SDCard " + card._getStringId());
        card.recordState = VedantuRecordState.ACTIVE;
        SDCardDAO.INSTANCE.updateModel(card, Arrays.asList(SDCard.RECORD_STATE,
                SDCard.CONTENT_SIZE, SDCard.MAX_SIZE, SDCard.COUNT));
        SDCard testCard = card;
        card = null;
        return testCard;
    }

    public static GetResourcesRes getResources(GetSDCardContentsReq request)
            throws VedantuException {

        // check in EC

        LOGGER.debug("Getting resources in folder Id " + request.id + System.currentTimeMillis());
        GetResourcesRes response = new GetResourcesRes();
        List<ModelBasicInfo> basicInfos = new ArrayList<ModelBasicInfo>();
        try {

            SrcEntity contentSrc = new SrcEntity(EntityType.ORGANIZATION, request.orgId);

            //Get all the orgIds that gave access to the current organization
            MutableLong totalProgramHits = new MutableLong(0L);
       	 	List<GranteeOrgProgram> granteeOrgPrograms = GranteeOrgProgramDAO.INSTANCE.getGranteeOrgPrograms(request.orgId, null, totalProgramHits);
            List<String> grantedOrgs = new ArrayList<String>();
            for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
    			grantedOrgs.add(granteeOrgProgram.providerOrgId);
    		}            
            
            BoolQueryBuilder resourceQuery = buildSearchQuery(SearchResultType.ALL, request.userId,
                    contentSrc, null, null, null, request.query, null,grantedOrgs);

            BoolFilterBuilder boardFilterBuilder = FilterBuilders.boolFilter();
            // oring all boards assuming boards are of only one level
            ElasticSearchUtils.addBoardAndTargetFilter(request.brdIds, false, boardFilterBuilder);

            BoolQueryBuilder folderQuery = QueryBuilders.boolQuery();

            TermQueryBuilder folderIdTermQuery = QueryBuilders.termQuery("target.id", request.id);

            folderQuery.must(folderIdTermQuery);

            Set<String> entityTypeSet = new HashSet<String>();
            if (CollectionUtils.isNotEmpty(request.includes)) {
                for (EntityType include : request.includes) {
                    entityTypeSet.add(include.name().toLowerCase());
                }

                TermsQueryBuilder inContentTypeQuery = QueryBuilders.inQuery("source.type",
                        entityTypeSet.toArray());
                folderQuery.must(inContentTypeQuery);

                TermsQueryBuilder parentInContentTypeQuery = QueryBuilders.inQuery("content.type",
                        entityTypeSet.toArray());

                resourceQuery.must(parentInContentTypeQuery);
            }
            entityTypeSet.clear();
            if (CollectionUtils.isNotEmpty(request.excludes)) {
                entityTypeSet = new HashSet<String>();
                for (EntityType exclude : request.excludes) {
                    entityTypeSet.add(exclude.name().toLowerCase());
                }
                TermsQueryBuilder ninContentTypeQuery = QueryBuilders.inQuery("source.type",
                        entityTypeSet.toArray());
                folderQuery.mustNot(ninContentTypeQuery);

                TermsQueryBuilder parentNinContentTypeQuery = QueryBuilders.inQuery("content.type",
                        entityTypeSet.toArray());

                resourceQuery.mustNot(parentNinContentTypeQuery);
            }

            HasChildQueryBuilder folderQueryBuilder = QueryBuilders.hasChildQuery(
                    CmdsContentLinkType.ADDED.name().toLowerCase(), folderQuery);

            resourceQuery.must(folderQueryBuilder);

            QueryBuilder query = QueryBuilders
                    .filteredQuery(
                            resourceQuery,
                            CollectionUtils.isEmpty(request.brdIds) ? FilterBuilders
                                    .matchAllFilter() : boardFilterBuilder);

            SearchResponse searchResults = ElasticSearchUtils.getSearchResponse(query,
                    request.orderBy, request.sortOrder, request.start, request.size,
                    EntityType.CMDSRESOURCE.getIndexName(), EntityType.CMDSRESOURCE.getIndexType()
                            .toLowerCase(), null, false, (AbstractFacetBuilder[]) null);

            List<CMDSResourceDetails> details = new ArrayList<CMDSResourceDetails>();

            response.totalHits = AbstractCMDSContentManager.getBasicInfoFromESSearch(searchResults,
                    details);

            List<CMDSContentLink> links = new ArrayList<CMDSContentLink>();

            MutableLong totalHits = new MutableLong(0L);
            CMDSContentLink link = null;
            for (CMDSResourceDetails detail : details) {
                SrcEntity folderEntity = new SrcEntity(EntityType.SDCARD, request.id);
                List<CMDSContentLink> testLinks = CmdsContentLinkDAO.INSTANCE.getCmdsContentLinks(
                        detail.content, folderEntity, CmdsContentLinkType.ADDED, null, 0, 1,
                        totalHits);
                if (CollectionUtils.isNotEmpty(testLinks)) {
                    link = testLinks.get(0);
                    links.add(link);
                } else {
                    LOGGER.error(" Mismatch in ES and MONGODB results ");
                }

            }

            basicInfos = AbstractCMDSContentManager.getBasicInfoFromLinks(links, basicInfos);
            response.list.addAll(basicInfos);

        } catch (Exception exception) {
            LOGGER.debug(" Error", exception);
            Logger.debug(" Error", exception);
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, exception);
        }
        LOGGER.debug("Received resources " + request.id + System.currentTimeMillis());

        // check in
        return response;
    }

    
    /**
     * 
     * 
     * 
     */
    
    public static GetResourcesRes getResources2(GetSDCardContentsReq request)
            throws VedantuException {

        // check in EC
        GetResourcesRes response = new GetResourcesRes();

        LOGGER.debug("Getting resources in sdcard Id " + request.id + System.currentTimeMillis());
        try {

            // collect all sections


            List<ModelBasicInfo> basicInfos = new ArrayList<ModelBasicInfo>();

            SrcEntity contentSrc = new SrcEntity(EntityType.ORGANIZATION, request.orgId);

          //Get all the orgIds that gave access to the current organization
            MutableLong totalProgramHits = new MutableLong(0L);
       	 	List<GranteeOrgProgram> granteeOrgPrograms = GranteeOrgProgramDAO.INSTANCE.getGranteeOrgPrograms(request.orgId, null, totalProgramHits);
            List<String> grantedOrgs = new ArrayList<String>();
            for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
    			grantedOrgs.add(granteeOrgProgram.providerOrgId);
    		}   
            
            BoolQueryBuilder resourceQuery = buildSearchQuery(SearchResultType.ALL, request.userId,
                    contentSrc, null, null, null, request.query, null,grantedOrgs);

            BoolFilterBuilder boardFilterBuilder = FilterBuilders.boolFilter();
            ElasticSearchUtils.addBoardAndTargetFilter(request.brdIds, false, boardFilterBuilder);

            BoolQueryBuilder targetQuery = QueryBuilders.boolQuery();

            TermsQueryBuilder targetIdTypeQuery = QueryBuilders.inQuery("target.id",
                    request.id);

            TermQueryBuilder targetTypeQueryBuilder = QueryBuilders.termQuery("target.type",
                    EntityType.SDCARD.name().toLowerCase());

            targetQuery.must(targetIdTypeQuery);
            targetQuery.must(targetTypeQueryBuilder);

            Set<String> entityTypeSet = new HashSet<String>();
            if (CollectionUtils.isNotEmpty(request.includes)) {
                for (EntityType include : request.includes) {
                    entityTypeSet.add(include.name().toLowerCase());
                }

                TermsQueryBuilder inContentTypeQuery = QueryBuilders.inQuery("source.type",
                        entityTypeSet.toArray());
                targetQuery.must(inContentTypeQuery);

                TermsQueryBuilder parentInContentTypeQuery = QueryBuilders.inQuery("content.type",
                        entityTypeSet.toArray());

                resourceQuery.must(parentInContentTypeQuery);
            }

            entityTypeSet.clear();
            if (CollectionUtils.isNotEmpty(request.excludes)) {
                entityTypeSet = new HashSet<String>();
                for (EntityType exclude : request.excludes) {
                    entityTypeSet.add(exclude.name().toLowerCase());
                }
                TermsQueryBuilder ninContentTypeQuery = QueryBuilders.inQuery("source.type",
                        entityTypeSet.toArray());
                targetQuery.mustNot(ninContentTypeQuery);

                TermsQueryBuilder parentNinContentTypeQuery = QueryBuilders.inQuery("content.type",
                        entityTypeSet.toArray());

                resourceQuery.mustNot(parentNinContentTypeQuery);
            }

            QueryBuilder resourceBoardSpecificQuery = QueryBuilders.filteredQuery(resourceQuery,
                    CollectionUtils.isNotEmpty(request.brdIds) ? boardFilterBuilder
                            : FilterBuilders.matchAllFilter());

            HasParentQueryBuilder resourceQueryBuilder = QueryBuilders.hasParentQuery(
                    EntityType.CMDSRESOURCE.getIndexType().toLowerCase(),
                    resourceBoardSpecificQuery);

            targetQuery.must(resourceQueryBuilder);
            SearchResponse searchResults = ElasticSearchUtils.getSearchResponse(targetQuery,
                    request.orderBy, request.sortOrder, request.start, request.size,
                    EntityType.CMDSRESOURCE.getIndexName(),
                    CmdsContentLinkType.ADDED.getSearchIndexType(), null, false,
                    (AbstractFacetBuilder[]) null);

            List<String> details = new ArrayList<String>();

            response.totalHits = getLinkIds(searchResults, details);

            List<CMDSContentLink> contentLinks = CmdsContentLinkDAO.INSTANCE.getByIds(ObjectIdUtils
                    .toObjectIds(details), VedantuRecordState.ACTIVE, Arrays
                    .asList(new SortOrderInfo(SortOrder.valueOfKey(request.sortOrder),
                            request.orderBy)));

            basicInfos = getBasicInfoFromLinks(contentLinks, basicInfos);
            response.list.addAll(basicInfos);

        } catch (Exception exception) {
            LOGGER.debug(" Error", exception);
            LOGGER.debug(" Error", exception);
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, exception);
        }
        // check in
        return response;
    }
    /**
     * Move content from one SD Card to other SD Card
     * 
     * @param userId
     * @param entity
     * @param moveToFolder
     * @param organizationId
     * @return
     * @throws VedantuException
     */
    public boolean move(String userId, SrcEntity entity, String moveFromSDCardId,
            String moveToSDCardId, String organizationId) throws VedantuException {

        MutableLong totalHits = new MutableLong(0L);

        SDCard card = SDCardDAO.INSTANCE.getById(moveToSDCardId);
        VedantuBasicDAO<?, ?> contentDao = EntityTypeDAOFactory.INSTANCE.get(entity.type);

        SrcEntity target = new SrcEntity(EntityType.SDCARD, moveToSDCardId);

        CmdsContentLinkDAO.INSTANCE.updateTargetEntity(null, new SrcEntity(entity.type, entity.id),
                new SrcEntity(EntityType.SDCARD, moveFromSDCardId), target,
                CmdsContentLinkType.ADDED, null);
        SrcEntity content = new SrcEntity(entity.type, entity.id);

        if (totalHits.longValue() > 1) {
            LOGGER.error("Incorrect number of links for data");
            throw new VedantuException(VedantuErrorCode.CONTENT_CAN_NOT_BE_MOVED);
        }
        VedantuBasicDAO<?, ?> basicDAO = EntityTypeDAOFactory.INSTANCE.get(entity.type);
        if (basicDAO == null) {
            LOGGER.error("No dao found for content :" + entity);
        }

        long size = -1;

        if (basicDAO instanceof CmdsContentDAO) {
            CmdsContentDAO<?, ?> cmdsContentDAO = (CmdsContentDAO<?, ?>) basicDAO;
            if (!cmdsContentDAO.isMovingAllowed(entity.id)) {
                LOGGER.error("No moving is allowed found for question inside questionsets :"
                        + entity);
                throw new VedantuException(VedantuErrorCode.CONTENT_CAN_NOT_BE_MOVED);
            }
            AbstractContentModel model = (AbstractContentModel) cmdsContentDAO.getById(entity.id);
            size = model.getExportableSize();

            if ((card.maxSize - card.contentSize) <= size) {
                throw new VedantuException(VedantuErrorCode.CONTENT_CAN_NOT_BE_MOVED,
                        "no enough space available on sd card");
            }

        } else {
            LOGGER.error("Content does not have database dao :" + entity);
            throw new VedantuException(VedantuErrorCode.CONTENT_CAN_NOT_BE_MOVED);
        }

        List<CMDSContentLink> links = CmdsContentLinkDAO.INSTANCE.getCmdsContentLinks(content,
                target, CmdsContentLinkType.ADDED, null, 0, 1, null);
        CMDSContentLink linkage = links.get(0);
        if (linkage == null) {
            return false;
        }

        // CmdsContentLinkDAO.INSTANCE.save(linkage);

        if (size > 0) {
            boolean successful = updateContentLink(linkage._getStringId(), linkage.userId, content,
                    target, linkage.timeCreated, linkage.getScope(), linkage.position);
            SDCardDAO.INSTANCE.addSize(Arrays.asList(moveFromSDCardId), true, size);
            SDCardDAO.INSTANCE.addSize(Arrays.asList(moveToSDCardId), false, size);
        }
        return true;
    }

}