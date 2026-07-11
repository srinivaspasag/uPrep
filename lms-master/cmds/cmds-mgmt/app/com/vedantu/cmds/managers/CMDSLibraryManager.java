package com.vedantu.cmds.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;
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

import com.google.code.morphia.query.Query;
import com.vedantu.cmds.daos.CMDSAssignmentDAO;
import com.vedantu.cmds.daos.CMDSDocumentDAO;
import com.vedantu.cmds.daos.CMDSFileDAO;
import com.vedantu.cmds.daos.CMDSModuleDAO;
import com.vedantu.cmds.daos.CMDSQuestionDAO;
import com.vedantu.cmds.daos.CMDSTestDAO;
import com.vedantu.cmds.daos.CMDSVideoDAO;
import com.vedantu.cmds.daos.CmdsContentDAO;
import com.vedantu.cmds.daos.CmdsContentLinkDAO;
import com.vedantu.cmds.enums.CmdsContentLinkType;
import com.vedantu.cmds.mgmt.interfaces.ICMDSModel;
import com.vedantu.cmds.mgmt.publishers.EntityTypePublisherFactory;
import com.vedantu.cmds.mgmt.publishers.IPublisher;
import com.vedantu.cmds.models.CMDSAssignment;
import com.vedantu.cmds.models.CMDSContentLink;
import com.vedantu.cmds.models.CMDSDocument;
import com.vedantu.cmds.models.CMDSModule;
import com.vedantu.cmds.models.CMDSQuestion;
import com.vedantu.cmds.models.CMDSTest;
import com.vedantu.cmds.models.CMDSVideo;
import com.vedantu.cmds.models.event.details.EntityPublishingDetails;
import com.vedantu.cmds.models.event.search.details.CMDSContentLinkDetails;
import com.vedantu.cmds.models.event.search.details.CMDSResourceDetails;
import com.vedantu.cmds.pojos.content.solution.metadata.MCQsolutionInfo;
import com.vedantu.cmds.pojos.requests.EntityOperationStatusRes;
import com.vedantu.cmds.pojos.requests.GetLibraryResourcesReq;
import com.vedantu.cmds.pojos.requests.MakeVisibleReq;
import com.vedantu.cmds.pojos.requests.OrgContentVisibleOption;
import com.vedantu.cmds.pojos.requests.library.GetEntityPublishingStatusReq;
import com.vedantu.cmds.pojos.requests.library.GetVisibilityChartReq;
import com.vedantu.cmds.pojos.requests.library.PublishReq;
import com.vedantu.cmds.pojos.requests.library.UpdateRankReq;
import com.vedantu.cmds.pojos.responses.AddToLibraryRes;
import com.vedantu.cmds.pojos.responses.EntityResponse;
import com.vedantu.cmds.pojos.responses.GetLibraryResourceRes;
import com.vedantu.cmds.pojos.responses.MakeVisibleRes;
import com.vedantu.cmds.pojos.responses.library.ContentWiseVisibilityReport;
import com.vedantu.cmds.pojos.responses.library.GetStatus;
import com.vedantu.cmds.pojos.responses.library.GetVisibilityChartRes;
import com.vedantu.cmds.pojos.responses.library.PublishRes;
import com.vedantu.cmds.pojos.responses.library.VisibilityReport;
import com.vedantu.cmds.utils.VisibilityReportComparator;
import com.vedantu.commons.AsynExecutorService;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.content.interfaces.IPublishable;
import com.vedantu.commons.daos.EntityOperationStatusDAO;
import com.vedantu.commons.daos.GranteeOrgProgramDAO;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.models.mongo.EntityOperationStatus;
import com.vedantu.commons.pojos.OrganizationEntity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ActionTakenRes;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.content.daos.AnswerDAO;
import com.vedantu.content.daos.ContentGroupDAO;
import com.vedantu.content.daos.LibraryContentLinksDAO;
import com.vedantu.content.daos.ModuleSchedulesDAO;
import com.vedantu.content.daos.QuestionDAO;
import com.vedantu.content.daos.TestDAO;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.enums.search.SearchResultType;
import com.vedantu.content.event.details.MadeVisibleDetails;
import com.vedantu.content.managers.LibraryManager;
import com.vedantu.content.models.AbstractContentModel;
import com.vedantu.content.models.Answer;
import com.vedantu.content.models.ContentGroup;
import com.vedantu.content.models.LibraryContentLink;
import com.vedantu.content.models.ModuleEntry;
import com.vedantu.content.models.ModuleSchedules;
import com.vedantu.content.models.Question;
import com.vedantu.content.models.tests.Test;
import com.vedantu.content.pojos.tests.TestDetails;
import com.vedantu.content.pojos.tests.TestMetadata;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.MongoManager.SortOrder;
import com.vedantu.mongo.SortOrderInfo;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.mongo.models.GranteeOrgProgram;
import com.vedantu.organization.daos.OrgCenterDAO;
import com.vedantu.organization.daos.OrgMemberDAO;
import com.vedantu.organization.daos.OrgProgramDAO;
import com.vedantu.organization.daos.OrgSectionDAO;
import com.vedantu.organization.managers.OrgProgramManager;
import com.vedantu.organization.models.OrgMember;
import com.vedantu.organization.models.OrgProgram;
import com.vedantu.organization.models.OrgSection;
import com.vedantu.organization.pojos.OrgMemberMappingInfo;
import com.vedantu.organization.pojos.OrgProgramSectionBasicInfo;
import com.vedantu.organization.pojos.OrgStructureBasicInfo;
import com.vedantu.search.utils.ElasticSearchUtils;

public class CMDSLibraryManager extends AbstractCMDSContentManager {

    private static final ALogger     LOGGER       = Logger.of(CMDSLibraryManager.class);
    final static AsynExecutorService asynExecutor = AsynExecutorService.getInstance();

    public static AddToLibraryRes addToLibrary(String userId, String orgId,
            List<OrganizationEntity> orgEntities, List<SrcEntity> contents) throws VedantuException {

        for (OrganizationEntity srcEntity : orgEntities) {
            if (!ObjectIdUtils.hasInvalidId(srcEntity.id)) {
                // Check if this Program is Organisations or Not
                if(srcEntity.type == EntityType.PROGRAM){
                    OrgProgram program = OrgProgramDAO.INSTANCE.getById(srcEntity.id);
                    // Check whether the program orgId and request orgId are equal or not
                    if(!program.orgId.equals(orgId)){
                        // As program orgId and request orgId are not equal, check if it has sharedProgramAccess
                        if(!hasSharedProgramAccess(srcEntity.id)){
                            throw new VedantuException(VedantuErrorCode.PRIVATE_PROGRAM, "You can't add content to a shared program");
                        }
                    }
                }else if (srcEntity.type == EntityType.SECTION){
                    OrgSection section = OrgSectionDAO.INSTANCE.getById(srcEntity.id);
                    OrgProgram program = OrgProgramDAO.INSTANCE.getById(section.programId);
                    // Check whether the program orgId and request orgId are equal or not
                    if(!program.orgId.equals(orgId)){
                        // As program orgId and request orgId are not equal, check if it has sharedProgramAccess
                        if(!hasSharedProgramAccess(srcEntity.id)){
                            throw new VedantuException(VedantuErrorCode.PRIVATE_PROGRAM, "You can't add content to a shared program");
                        }
                    }
                }
            } else {
                throw new VedantuException(VedantuErrorCode.INCORRECT_ORGANIZATION_ENTITY);
            }
            //
        }

        Set<SrcEntity> collectedSections = getSections(orgId, orgEntities);
        AddToLibraryRes response = new AddToLibraryRes(userId);

        for (SrcEntity orgEntity : collectedSections) {
            @SuppressWarnings("unchecked")
            VedantuBasicDAO<? extends VedantuBaseMongoModel, ObjectId> basicDAO = EntityTypeDAOFactory.INSTANCE
                    .get(orgEntity.type);
            ModelBasicInfo orgEntityInfo = basicDAO.getBasicInfo(orgEntity.id);

            for (SrcEntity content : contents) {

                try {
                    // checking for valid entitiy types
                    if (!EntityType.isValidOrgEntity(orgEntity.type)) {
                        throw new VedantuException(VedantuErrorCode.UNSUPPORTED_ORG_TYPE);
                    }

                    if (!EntityType.isSupportedCMDSLibraryEntityType(content.type)) {
                        throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE);
                    }

                    CMDSContentLink createdLink = CmdsContentLinkDAO.INSTANCE.addLink(content,
                            orgEntity, CmdsContentLinkType.ADDED, userId);

                    CMDSContentLinkDetails libraryContentLinkDetails = new CMDSContentLinkDetails(
                            createdLink._getStringId(), userId, content, orgEntity,
                            createdLink.getScope(), createdLink.timeCreated, createdLink.position);
                    SrcEntity resource = new SrcEntity(EntityType.CMDSRESOURCE,
                            getResourceId(content));
                    updateUserActionMappintToEs(libraryContentLinkDetails, resource,
                            UserActionType.ADDED, EventActionType.ADD);

                } catch (VedantuException exception) {

                    EntityResponse entityResponse = new EntityResponse(content, exception.errorCode);
                    entityResponse.orgEntity = orgEntityInfo;

                    LOGGER.debug("Error code: " + exception.errorCode);

                    response.list.add(entityResponse);
                }
            }
            response.totalHits = response.list.size();
        }

        VedantuErrorCode cumulativeErrorCode = EntityResponse.getCumulativeErrorCode(response.list);
        if (cumulativeErrorCode != null) {
            response.cumulativeErrorCode = cumulativeErrorCode;
        }

        return response;
    }

    public static AddToLibraryRes removeFromLibrary(String userId, String orgId,
            List<OrganizationEntity> orgEntities, List<SrcEntity> contents) throws VedantuException {

        Set<SrcEntity> collectedSections = getSections(orgId, orgEntities);
        AddToLibraryRes response = new AddToLibraryRes(userId);

        for (SrcEntity orgEntity : collectedSections) {
            @SuppressWarnings("unchecked")
            VedantuBasicDAO<? extends VedantuBaseMongoModel, ObjectId> basicDAO = EntityTypeDAOFactory.INSTANCE
                    .get(orgEntity.type);
            ModelBasicInfo orgEntityInfo = basicDAO.getBasicInfo(orgEntity.id);
            OrgContentVisibleOption option = new OrgContentVisibleOption();
            option.downloadble = false;
            option.visible = false;
            option.encLevel = null;
            option.schedule = null;
            option.orgEntity = orgEntity;

            for (SrcEntity content : contents) {

                try {
                    // checking for valid entity types
                    if (!EntityType.isValidOrgEntity(orgEntity.type)) {
                        throw new VedantuException(VedantuErrorCode.UNSUPPORTED_ORG_TYPE);
                    }

                    if (!EntityType.isSupportedCMDSLibraryEntityType(content.type)) {
                        throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE);
                    }

//                    if(isPrivateContent(content, orgId)){
//                        throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_REMOVED);
//                    }

                    // make invisible from current orgEntity
                    MakeVisibleReq request = new MakeVisibleReq();
                    request.options.add(option);
                    request.contents.add(content);

                    makeVisible(request,null);

                    // drop link

                    CMDSContentLink removedLink = CmdsContentLinkDAO.INSTANCE.remove(content,
                            orgEntity, CmdsContentLinkType.ADDED);

                    if (removedLink != null) {
                        // reindexing link
                        CMDSContentLinkManager.INSTANCE.reIndex(removedLink);
                    }

                } catch (VedantuException exception) {

                    EntityResponse entityResponse = new EntityResponse(content, exception.errorCode);
                    entityResponse.orgEntity = orgEntityInfo;

                    LOGGER.debug("Error code: " + exception.errorCode);

                    response.list.add(entityResponse);
                }
            }
            response.totalHits = response.list.size();
        }
        VedantuErrorCode cumulativeErrorCode = EntityResponse.getCumulativeErrorCode(response.list);
        if (cumulativeErrorCode != null) {
            response.cumulativeErrorCode = cumulativeErrorCode;
        }
        return response;

    }

    private static boolean isPrivateContent(SrcEntity content, String orgId) {
        // TODO Auto-generated method stub
        if(content.type == EntityType.CMDSTEST){
            String ownerOrgId = CMDSTestDAO.INSTANCE.getById(content.id).contentSrc.id;
            if(orgId.equals(ownerOrgId)){
                return false;
            }else{
                return true;
            }
        }else if(content.type == EntityType.CMDSVIDEO){
            String ownerOrgId = CMDSVideoDAO.INSTANCE.getById(content.id).contentSrc.id;
            if(orgId.equals(ownerOrgId)){
                return false;
            }else{
                return true;
            }
        }else if(content.type == EntityType.CMDSMODULE){
            String ownerOrgId = CMDSModuleDAO.INSTANCE.getById(content.id).contentSrc.id;
            if(orgId.equals(ownerOrgId)){
                return false;
            }else{
                return true;
            }
        }else if(content.type == EntityType.CMDSDOCUMENT){
            String ownerOrgId = CMDSDocumentDAO.INSTANCE.getById(content.id).contentSrc.id;
            if(orgId.equals(ownerOrgId)){
                return false;
            }else{
                return true;
            }
        }else if(content.type == EntityType.CMDSASSIGNMENT){
            String ownerOrgId = CMDSAssignmentDAO.INSTANCE.getById(content.id).contentSrc.id;
            if(orgId.equals(ownerOrgId)){
                return false;
            }else{
                return true;
            }
        }else if(content.type == EntityType.CMDSFILE){
            String ownerOrgId = CMDSFileDAO.INSTANCE.getById(content.id).contentSrc.id;
            if(orgId.equals(ownerOrgId)){
                return false;
            }else{
                return true;
            }
        }else{
            return false;
        }
    }

    public static GetLibraryResourceRes getResources(GetLibraryResourcesReq request)
            throws VedantuException {

        // check in EC
        GetLibraryResourceRes response = new GetLibraryResourceRes();
        LOGGER.debug("Getting Libary resources in folder Id " + request.orgEntity);
        SrcEntity entityAtRequestedLevel = request.orgEntity;
        if (CollectionUtils.isNotEmpty(request.orgEntity.centers)) {
            // getting first center as this CALL meant only for one library;
            entityAtRequestedLevel = request.orgEntity.centers.get(0);
        }
        try {

            // collect all sections

            List<OrganizationEntity> orgEntities = new ArrayList<OrganizationEntity>();
            orgEntities.add(request.orgEntity);
            Set<SrcEntity> sectionEntities = null;

            if (StringUtils.isNotEmpty(request.orgEntity.id)) {

                if (ObjectIdUtils.hasInvalidId(request.orgEntity.id) /*
                                                                      * TODO || check for invalid
                                                                      * entity type
                                                                      */) {
                    throw new VedantuException(VedantuErrorCode.INCORRECT_ORGANIZATION_ENTITY);
                }

                sectionEntities = getSections(request.orgId, orgEntities);

            }

            Set<String> sections = new HashSet<String>();
            for (SrcEntity sectionEntity : sectionEntities) {
                sections.add(sectionEntity.id);
            }

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
                    sections.toArray());

            TermQueryBuilder targetTypeQueryBuilder = QueryBuilders.termQuery("target.type",
                    EntityType.SECTION.name().toLowerCase());

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

            HasChildQueryBuilder targetQueryBuilder = QueryBuilders.hasChildQuery(
                    CmdsContentLinkType.ADDED.name().toLowerCase(), targetQuery);

            resourceQuery.must(targetQueryBuilder);

            QueryBuilder resourceBoardSpecificQuery = QueryBuilders.filteredQuery(resourceQuery,
                    CollectionUtils.isNotEmpty(request.brdIds) ? boardFilterBuilder
                            : FilterBuilders.matchAllFilter());

            SearchResponse searchResults = ElasticSearchUtils.getSearchResponse(
                    resourceBoardSpecificQuery, request.orderBy, request.sortOrder, request.start,
                    request.size, EntityType.CMDSRESOURCE.getIndexName(), EntityType.CMDSRESOURCE
                            .getIndexType().toLowerCase(), null, false,
                    (AbstractFacetBuilder[]) null);

            List<CMDSResourceDetails> details = new ArrayList<CMDSResourceDetails>();

            response.totalHits = getBasicInfoFromESSearch(searchResults, details);

            List<CMDSContentLink> links = new ArrayList<CMDSContentLink>();

            MutableLong totalHits = new MutableLong(0L);
            CMDSContentLink link = null;

            for (CMDSResourceDetails detail : details) {

                if (entityAtRequestedLevel.type == EntityType.SECTION) {
                    SrcEntity sectionEntity = new SrcEntity(EntityType.SECTION,
                            entityAtRequestedLevel.id);
                    List<CMDSContentLink> testLinks = CmdsContentLinkDAO.INSTANCE
                            .getCmdsContentLinks(detail.content, sectionEntity,
                                    CmdsContentLinkType.ADDED, null, 0, 1,
                                    VedantuRecordState.ACTIVE, totalHits);
                    if (CollectionUtils.isNotEmpty(testLinks)) {
                        link = testLinks.get(0);
                        LOGGER.debug(" Found link for section" + link);

                    } else {
                        LOGGER.error(" Mismatch in ES and MONGODB results ");
                    }
                } else {
                    link = new CMDSContentLink();
                    link.source = detail.content;
                    link.target = entityAtRequestedLevel;
                    link.linkType = CmdsContentLinkType.ADDED;

                }

                links.add(link);
            }

            basicInfos = getBasicInfoFromLinks(links, basicInfos);
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
     * GetResource with rank query
     *
     * @param request
     * @return
     * @throws VedantuException
     */

    public static GetLibraryResourceRes getResources2(GetLibraryResourcesReq request)
            throws VedantuException {

        // check in EC
        GetLibraryResourceRes response = new GetLibraryResourceRes();
        LOGGER.debug("Getting Libary resources in section Id " + request.orgEntity);
        SrcEntity entityAtRequestedLevel = request.orgEntity;
        if (CollectionUtils.isNotEmpty(request.orgEntity.centers)) {
            // getting first center as this CALL meant only for one library;
            entityAtRequestedLevel = request.orgEntity.centers.get(0);
        }
        try {

            // collect all sections

            List<OrganizationEntity> orgEntities = new ArrayList<OrganizationEntity>();
            orgEntities.add(request.orgEntity);
            Set<SrcEntity> sectionEntities = null;

            if (StringUtils.isNotEmpty(request.orgEntity.id)) {

                if (ObjectIdUtils.hasInvalidId(request.orgEntity.id) /*
                                                                      * TODO || check for invalid
                                                                      * entity type
                                                                      */) {
                    throw new VedantuException(VedantuErrorCode.INCORRECT_ORGANIZATION_ENTITY);
                }

                sectionEntities = getSections(request.orgId, orgEntities);

            }

            Set<String> sections = new HashSet<String>();
            for (SrcEntity sectionEntity : sectionEntities) {
                sections.add(sectionEntity.id);
            }

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
                    sections.toArray());

            TermQueryBuilder targetTypeQueryBuilder = QueryBuilders.termQuery("target.type",
                    EntityType.SECTION.name().toLowerCase());

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

    public static MakeVisibleRes makeVisible(MakeVisibleReq request,Map<String, Object> sessionParams) throws VedantuException {

        // check if target Entity is section
        LOGGER.debug("............." + request.options.get(0).downloadableEntities + ".......");
        String orgId = "";
        if(sessionParams!=null){
            orgId = sessionParams.get("orgId").toString();
            String sectionId = request.options.get(0).orgEntity.id.toString();
            String programId = OrgSectionDAO.INSTANCE.getById(sectionId).programId;
            OrgProgram program = OrgProgramDAO.INSTANCE.getById(programId);
            if(!orgId.equals(program.orgId)){
                if(!program.sharedProgramAccess){
                    throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_PUBLISHED, "You Can't Publish Content In A Shared Program");
                }
            }
            // Letting Momentum to publish/unpublsh the content in the program we shared.
//            if(orgId.equals("5582b31ae4b0f968650c78d1") && programId.equals("5c7f975be4b0746da4df7340")){
//                // Do nothing. Let this organisation publish/unpublish the content
//                LOGGER.debug("Momentum orgId "+orgId+ " and programId "+programId);
//            }else{
//                // Others. Check whether they are publshing/unpublishing there own content.
//                OrgSectionDAO.INSTANCE.getSectionById(orgId, sectionId);
//            }
        }
        MakeVisibleRes response = new MakeVisibleRes();
        VedantuErrorCode errorCode = null;
        for (OrgContentVisibleOption option : request.options) {
            SrcEntity orgEntity = option.orgEntity;
            ContentGroup contentGroup = null;
            long sizeToBeAdded = 0;
            if (CollectionUtils.isNotEmpty(request.contents)) {
                contentGroup = null;
                OrgProgramSectionBasicInfo sectionBasicInfo = OrgSectionDAO.INSTANCE
                        .getBasicInfo(orgEntity.id);
                for (SrcEntity src : request.contents) {
                    // get Link from CMDSContentLink

                    // TODO: we can make it more generic instead of module
                    if (src.type != EntityType.CMDSMODULE) {
                        option.downloadableEntities = null;
                    }
                    LOGGER.debug(" Content " + src + " orgOption" + option);
                    Scope scope = Scope.UNKNOWN;
                    if (option.visible != null) {
                        if (option.visible) {
                            scope = Scope.ORG;
                            // only in case of visible we create content group
                            contentGroup = new ContentGroup();
                            contentGroup.target = option.orgEntity;
                        } else {
                            scope = Scope.PRIVATE;
                        }
                    }

                    //Set/Delete schedules inside a module
                    if(src.type == EntityType.CMDSMODULE && option.visible != null && option.visible){
                        List<ModuleSchedules> schedules = ModuleSchedulesDAO.INSTANCE.getSchedule(orgEntity, src);
                        CMDSModule module = CMDSModuleDAO.INSTANCE.getById(src.id);
                        SrcEntity globalSource = new SrcEntity(EntityType.MODULE, module.globalModuleId);
                        for(ModuleSchedules schedule: schedules){
                            LOGGER.error("Module Schedule: "+schedule.globalEntity);
                            CMDSTest cmdsTest = CMDSTestDAO.INSTANCE.getById(schedule.entity.id);
                            SrcEntity globalEntity = new SrcEntity(EntityType.TEST, cmdsTest.globalTestId);
                            schedule.globalEntity = globalEntity;
                            schedule.globalSource = globalSource;
                            ModuleSchedulesDAO.INSTANCE.save(schedule);
                        }
                    }

                    ContentWiseVisibilityReport row = new ContentWiseVisibilityReport();

                    row.sectionInfo = sectionBasicInfo;
                    row.content = src;

                    try {

                        if (sectionBasicInfo.sdOnly && option.downloadble != null
                                && option.downloadble) {
                            throw new VedantuException(VedantuErrorCode.NOT_ALLOWED,
                                    "Can not make downloadable");
                        }

                        Logger.debug("...........Inside try function...............");
                        MutableLong totalHits = new MutableLong(0L);
                        List<CMDSContentLink> cmdsContentLinks = CmdsContentLinkDAO.INSTANCE
                                .getCmdsContentLinks(src, orgEntity, CmdsContentLinkType.ADDED,
                                        null, 0, 1, VedantuRecordState.ACTIVE, totalHits);
                        if (totalHits.longValue() != 1 && cmdsContentLinks.size() != 1) {
                            LOGGER.debug("There is problem while adding to library");
                            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
                        }
                        CMDSContentLink cmdsContentLink = cmdsContentLinks.get(0);

                        if (option.visible != null) {
                            if(option.visible){
                                if(option.schedule == null){
                                    LOGGER.debug("No Schedule Input From Client");
                                    option.schedule = null;
                                }else {
                                    LOGGER.debug("Client provided schedule input");
                                    if(option.schedule.startTime == null){
                                        LOGGER.debug("Client provided schedule input startTime null"+option.schedule.toString());
                                        option.schedule.startTime = new Date();
                                        LOGGER.debug("Client provided schedule input startTime"+option.schedule.toString());
                                    }
                                }
                                cmdsContentLink.setSchedule(option.schedule);
                            }else{
                                if(cmdsContentLink.getSchedule() != null){
                                    LOGGER.debug("Setting schedule to null");
                                    option.schedule = null;
                                }
                                cmdsContentLink.setSchedule(option.schedule);
                            }
                        }
                        CmdsContentLinkDAO.INSTANCE.save(cmdsContentLink);


                        LibraryContentLink contentLink = null;
                        @SuppressWarnings("rawtypes")
                        VedantuBasicDAO vedantuBasicDAO = EntityTypeDAOFactory.INSTANCE
                                .get(src.type);
                        if (vedantuBasicDAO instanceof CmdsContentDAO) {
                            Logger.debug("...........Inside if statement...............");
                            @SuppressWarnings("rawtypes")
                            CmdsContentDAO cmdsDAO = (CmdsContentDAO) vedantuBasicDAO;

                            ICMDSModel cmdsModel = (ICMDSModel) cmdsDAO.getById(src.id);

                            // create Link in LibraryContentLink with globalQId
                            if (cmdsModel.getGlobalId() != null) {
                                SrcEntity globalEntity = new SrcEntity(
                                        src.type._getPublishedType(), cmdsModel.getGlobalId());
                                LOGGER.debug(" Adding global entity "
                                        + src.type._getPublishedType() + " to respective library "
                                        + orgEntity);
                                Logger.debug("...........Before add to library...............");
                                contentLink = LibraryManager.addToLibrary(globalEntity, orgEntity,
                                        UserActionType.ADDED, request.userId, scope,
                                        option.schedule, null, option.downloadble, option.encLevel,
                                        option.downloadableEntities, cmdsContentLink.position);

                                sizeToBeAdded += ((AbstractContentModel) cmdsModel)
                                        .getExportableSize();
                                // TODO: if required send notification all
                                // members belonging to the target
                                if (option.visible != null && option.visible) {
                                    if (src.type._getPublishedType() != EntityType.QUESTION) {
                                        MadeVisibleDetails details = new MadeVisibleDetails();
                                        details.entity = globalEntity;
                                        details.orgEntity = orgEntity;
                                        details.enableNotifcation(true);
                                        details.userAction = UserActionType.MADE_VISIBLE;
                                        details.userId = request.userId;
                                        generateEventAysc(details.userId, details,
                                                EventType.MADE_VISIBLE);
                                    } else {
                                        contentGroup.contents.add(globalEntity);
                                    }
                                }
                            } else {

                                throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED);
                            }
                        } else {
                            throw new VedantuException(VedantuErrorCode.NOT_VALID_CMDS_CONTENT);

                        }

                        cmdsContentLink.globalLinkId = contentLink._getStringId();
                        CmdsContentLinkType linkedType = null;
                        cmdsContentLink.setScope(contentLink.getScope());
                        if (option.visible != null) {
                            linkedType = option.visible ? CmdsContentLinkType.VISIBLED
                                    : CmdsContentLinkType.INVISIBLED;
                            CmdsContentLinkDAO.INSTANCE.addLink(src, orgEntity, linkedType,
                                    request.userId, true);
                        }

                        cmdsContentLink.setDownloadable(contentLink.isDownloadable());
                        if (option.downloadble != null) {
                            linkedType = contentLink.isDownloadable() ? CmdsContentLinkType.DOWNLOAD_ENABLED
                                    : CmdsContentLinkType.DOWNLOAD_DISABLED;
                            CmdsContentLinkDAO.INSTANCE.addLink(src, orgEntity, linkedType,
                                    request.userId, true);
                        }
                        if (src.type == EntityType.CMDSMODULE) {
                            cmdsContentLink.setDownloadableEntities(contentLink
                                    .getDownloadableEntities());

                        }

                        CmdsContentLinkDAO.INSTANCE.save(cmdsContentLink);

                        row.visibility = cmdsContentLink.getScope();
                        row.errorCode = StringUtils.EMPTY;
                        row.downloadable = cmdsContentLink.isDownloadable();
                        row.downloadableEntities = cmdsContentLink.getDownloadableEntities();
                        response.list.add(row);

                    } catch (VedantuException exception) {
                        LOGGER.error(" Making visible failed", exception);

                        row.errorCode = exception.errorCode.name();

                        response.list.add(row);
                        errorCode = exception.errorCode;
                    }
                    response.totalHits++;

                    if (contentGroup != null && CollectionUtils.isNotEmpty(contentGroup.contents)) {

                        ContentGroupDAO.INSTANCE.save(contentGroup);
                        MadeVisibleDetails details = new MadeVisibleDetails();
                        details.entity = new SrcEntity(EntityType.CONTENTGROUP,
                                contentGroup._getStringId());
                        details.orgEntity = orgEntity;
                        details.enableNotifcation(true);
                        details.userAction = UserActionType.MADE_VISIBLE;
                        details.userId = request.userId;
                        generateEventAysc(details.userId, details, EventType.MADE_VISIBLE);
                    }

                    // TODO can not optimize further as option.visible is a Boolean value

                }// iterated over content
                if (option.visible != null) {
                    if (option.visible) {
                        OrgSectionDAO.INSTANCE.addSize(Arrays.asList(orgEntity.id), false,
                                sizeToBeAdded);
                    } else {
                        OrgSectionDAO.INSTANCE.addSize(Arrays.asList(orgEntity.id), true,
                                sizeToBeAdded);
                    }
                }
            }// iterate over sections
             // TODO update it using event

        }// contents not null


        if (CollectionUtils.isNotEmpty(request.contents)) {
            LOGGER.debug("Contents are not empty");
            for (SrcEntity entity : request.contents) {
                if (entity.type != EntityType.CMDSTEST) {
                    LOGGER.debug("Content Type is NOT CMDSTEST");
                    continue;
                }
                LOGGER.debug("Content Type is CMDSTEST");
                String cmdsTestId = entity.id;
                LOGGER.debug("CMDSTEST Id is "+cmdsTestId);
                Test test = TestDAO.INSTANCE.getByCMDSTestId(cmdsTestId);
                if(test == null){
                    LOGGER.debug("No TEST Object found for CMDSTEST Id"+cmdsTestId);
                    continue;
                }
                LOGGER.debug("Retrieved a TEST Object");
//                test = addSimplifiedBoardNames(test,cmdsTestId);
//                test = setPartialMarkingAndSectionLocking(test,cmdsTestId);
                if (CollectionUtils.isEmpty(test.metadata)) {
                    LOGGER.debug("Cannot find TEST metadata");
                    continue;
                }
                for (TestMetadata metaData : test.metadata) {
                    if (CollectionUtils.isEmpty(metaData.details)) {
                        LOGGER.debug("TEST has metadata but it is empty");
                        continue;
                    }
                    LOGGER.debug("TEST has metadata");
                    for (TestDetails details : metaData.details) {
                        if (details.type != QuestionType.PARA) {
                            LOGGER.debug("TEST metadata details is not of type PARA");
                            continue;
                        }
                        LOGGER.debug("TEST metadata details is of type PARA");
                        List<String> qIds = details.qIds;
                        if(CollectionUtils.isEmpty(qIds)) {
                            continue;
                        }
                        LOGGER.debug("Question Ids of type PARA"+qIds.toString());
                        for(String qId : qIds){
                            LOGGER.debug("Implementing custom logic for adding custom fields of question id "+qId);
                            Question question = QuestionDAO.INSTANCE.getById(qId);
                            Answer answer = AnswerDAO.INSTANCE.getByQuestionId(qId);
                            CMDSQuestion cmdsQuestion = CMDSQuestionDAO.INSTANCE.getById(question.cmdsQId);

                            if(question == null || answer == null || cmdsQuestion == null){
                                LOGGER.debug("One of the Question || Answer || CMDSQuestion Object is NULL");
                                continue;
                            }
                            // Change hasTrue field to TRUE
                            // Add field ParagraphId by getting it from CMDSQuestion Table with the help of field cmdsQId
                            MCQsolutionInfo solInfo = (MCQsolutionInfo) cmdsQuestion.solutionInfo;
                            if(CollectionUtils.isNotEmpty(solInfo.answer)){
                                LOGGER.debug("CMDSQuestion Object has Solutions");
                                List<String> answers = solInfo.answer;
                                if(answers != null && !answers.isEmpty()){
                                    LOGGER.debug("Setting Answer.answer && Question.hasAns && Question.paragraphId");
                                    answer.answer = answers;
                                    question.hasAns = true;
                                    question.paragraphId = cmdsQuestion.paragraphId;
                                }
                            }
                            LOGGER.debug("Saving Answer and Question Objects");
                            AnswerDAO.INSTANCE.save(answer);
                            QuestionDAO.INSTANCE.save(question);
                            LOGGER.debug("Saved Answer and Question Object");
                        }
                    }

                }

            }
        }

        if (errorCode != null) {
            response.cumulativeErrorCode = errorCode;
        }
        return response;

    }

    public static GetVisibilityChartRes getContentVisibilityReport(GetVisibilityChartReq request)
            throws VedantuException {

        MutableLong totalHits = new MutableLong(0L);

        GetVisibilityChartRes response = new GetVisibilityChartRes();
        // TODO restrict types
        ModelBasicInfo info = EntityTypeDAOFactory.INSTANCE.get(request.content.type).getBasicInfo(
                request.content.id);
        if (info == null) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }

        OrgMember orgMember = OrgMemberDAO.INSTANCE
                .getMemberByUserId(request.orgId, request.userId);

        if (orgMember == null) {
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        List<String> sectionIds = new ArrayList<String>();
        if (CollectionUtils.isNotEmpty(orgMember.mappings)) {
            for (OrgMemberMappingInfo mapping : orgMember.mappings) {
                sectionIds.add(mapping.sectionId);
            }
            LOGGER.debug("Collected section ids" + sectionIds);
        }
        List<VisibilityReport> report = getContentVisibilityReport(sectionIds, new SrcEntity(
                request.content.type, request.content.id), CmdsContentLinkType.ADDED,
                request.start, request.size, totalHits);

        Collections.sort(report, VisibilityReportComparator.INSTANCE);
        response.list.addAll(report);
        response.totalHits = totalHits.longValue();

        return response;

    }

    public static List<VisibilityReport> getContentVisibilityReport(List<String> sectionIds,
            SrcEntity content, CmdsContentLinkType linkType, int start, int size,
            MutableLong totalHits) {

        // TODO update it use only sectionIds;

        List<VisibilityReport> visibilityReports = new ArrayList<VisibilityReport>();
        List<SrcEntity> targets = new ArrayList<SrcEntity>();
        if (CollectionUtils.isEmpty(sectionIds)) {
            return visibilityReports;
        }

        for (String sectionId : sectionIds) {
            targets.add(new SrcEntity(EntityType.SECTION, sectionId));
        }
        List<CMDSContentLink> links = CmdsContentLinkDAO.INSTANCE
                .getCmdsContentLinksForTargets(content, targets, linkType, null, start, size,
                        VedantuRecordState.ACTIVE, totalHits);

        if (CollectionUtils.isNotEmpty(links)) {
            for (CMDSContentLink link : links) {
                LOGGER.debug(link.target.id + " type " + link.target.type);

                VisibilityReport row = null;
                row = createVisibilityReport(link.target.id);
                row.visibility = link.getScope();
                row.downloadable = link.isDownloadable();
                row.downloadableEntities = link.getDownloadableEntities();
                visibilityReports.add(row);
            }
        }

        return visibilityReports;
    }

    private static VisibilityReport createVisibilityReport(String sectionId) {

        OrgSection section = OrgSectionDAO.INSTANCE.getById(sectionId);
        VisibilityReport row = new VisibilityReport();

        LOGGER.debug("id " + section._getStringId() + " centerId " + section.centerId);

        row.programInfo = OrgProgramDAO.INSTANCE.getBasicInfo(section.programId);
        row.centerInfo = OrgCenterDAO.INSTANCE.getBasicInfo(section.centerId);

        row.sectionInfo = new OrgStructureBasicInfo(section._getStringId(), section.recordState,
                section.getName(), section.code, EntityType.SECTION);

        return row;
    }

    public static int getAllProgramsAddedTo(SrcEntity content, CmdsContentLinkType linkType) {

        // TODO update it use only sectionIds to optimize using aggregation
        LOGGER.info(" Getting programs added to information ");
        MutableLong totalHits = new MutableLong();
        List<CMDSContentLink> links = CmdsContentLinkDAO.INSTANCE.getCmdsContentLinks(content,
                new SrcEntity(EntityType.SECTION, null), linkType, null, 0, Integer.MAX_VALUE,
                VedantuRecordState.ACTIVE, totalHits);

        Set<String> programIds = new HashSet<String>();
        if (CollectionUtils.isNotEmpty(links)) {
            for (CMDSContentLink link : links) {
                OrgSection section = OrgSectionDAO.INSTANCE.getById(link.target.id);
                if (section != null && !programIds.contains(section.programId)) {
                    programIds.add(section.programId);
                }

            }
        }

        return programIds.size();
    }

    private static Set<SrcEntity> getSections(String orgId, List<OrganizationEntity> orgEntities)
            throws VedantuException {

        LOGGER.debug("getting section for : " + orgId + ", orgEntities" + orgEntities);
        Set<SrcEntity> collectedSections = new HashSet<SrcEntity>();
        for (OrganizationEntity srcEntity : orgEntities) {
            if (!ObjectIdUtils.hasInvalidId(srcEntity.id)) {
                if (srcEntity.type == EntityType.SECTION) {
                    // TODO check if its valid section
                    collectedSections.add(new SrcEntity(EntityType.SECTION, srcEntity.id));
                } else if (srcEntity.type == EntityType.PROGRAM) {

                    collectedSections = getProgramSections(orgId, srcEntity, collectedSections);

                } else if (srcEntity.type == EntityType.ORGANIZATION) {
                    // TODO to do later
                    // TODO check if its valid organization
                    throw new VedantuException(VedantuErrorCode.INCORRECT_ORGANIZATION_ENTITY);
                }
            } else {
                throw new VedantuException(VedantuErrorCode.INCORRECT_ORGANIZATION_ENTITY);
            }
            //
        }
        return collectedSections;
    }

    private static boolean hasSharedProgramAccess(String programId) throws VedantuException {
        // TODO Auto-generated method stub
        OrgProgram prog = OrgProgramDAO.INSTANCE.getById(programId);
        return prog.sharedProgramAccess;
    }

    /**
     * Convert sections to SrcEntities
     *
     * @param orgId
     * @param srcEntity
     * @param entities
     * @return
     * @throws VedantuException
     */
    private static Set<SrcEntity> getProgramSections(String orgId, OrganizationEntity srcEntity,
            Set<SrcEntity> entities) throws VedantuException {

        Set<SrcEntity> newCollectedEntities = new HashSet<SrcEntity>();
        List<String> centerIds = new ArrayList<String>();
        if (CollectionUtils.isNotEmpty(srcEntity.centers)) {
            for (SrcEntity center : srcEntity.centers) {
                centerIds.add(center.id);
            }
        }
        Set<String> sectionIds = OrgProgramManager.getProgramSections(orgId, srcEntity.id,
                centerIds);
        for (String sectionId : sectionIds) {
            newCollectedEntities.add(new SrcEntity(EntityType.SECTION, sectionId));
        }
        entities.addAll(newCollectedEntities);
        return entities;
    }

    @SuppressWarnings("unchecked")
    public static PublishRes publish(final PublishReq request) throws VedantuException {

        LOGGER.debug("......in publish function ........");
        PublishRes response = new PublishRes();
        for (final SrcEntity publishableEntity : request.entities) {
            try {

                LOGGER.debug("Publishing entity : " + publishableEntity);

                if(checkIfPublishInProgress(publishableEntity)){
                    response.addStatus(publishableEntity.id, VedantuErrorCode.PUBLISH_IN_PROGRESS.toString());
                    continue;
                }

                final IPublisher publisher = EntityTypePublisherFactory.INSTANCE
                        .get(publishableEntity.type);

                VedantuBasicDAO<VedantuBaseMongoModel, ObjectId> basicEntityTypeDAO = EntityTypeDAOFactory.INSTANCE
                        .get(publishableEntity.type);
                if (basicEntityTypeDAO instanceof IPublishable) {

                    IPublishable publishableDAO = (IPublishable) basicEntityTypeDAO;
                    if (!publishableDAO.isReadyToPublished(publishableEntity.id)) {
                        throw new VedantuException(VedantuErrorCode.INCOMPLETE_PUBLISHABLE_STATE);
                    }

                    LOGGER.debug("Entity need to be published using publisher " + publisher);

                    final EntityOperationStatus jobStatus = new EntityOperationStatus();
                    jobStatus.type = publishableEntity.type;
                    jobStatus.id = publishableEntity.id;
                    jobStatus.numOfSteps++;
                    jobStatus.numOfSteps++; // for entity size calculations
                    EntityOperationStatusDAO.INSTANCE.save(jobStatus);
                    LOGGER.debug("Entity will be  published in jobId " + jobStatus._getStringId());
                    EntityPublishingDetails entityPublishingDetails = new EntityPublishingDetails(
                            request.userId, request.orgId, publishableEntity,
                            jobStatus._getStringId());
                    generateEventAysc(request.userId, entityPublishingDetails,
                            EventType.PUBLISH_ENTITY);
                    addPublishEntityProgress(publishableEntity);
                    response.addStatus(publishableEntity.id, jobStatus._getStringId());
                } else {
                    throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_PUBLISHED);
                }
            } catch (VedantuException exception) {
                response.addStatus(publishableEntity.id, null, exception.errorCode);
            }

        }
        return response;
    }

    private static void addPublishEntityProgress(SrcEntity publishableEntity) {
        // TODO Auto-generated method stub
        if(publishableEntity.type == EntityType.CMDSVIDEO){
            CMDSVideo entity = CMDSVideoDAO.INSTANCE.getById(publishableEntity.id);
            entity.publishingInProgress = true;
            CMDSVideoDAO.INSTANCE.save(entity);
        }else if(publishableEntity.type == EntityType.CMDSDOCUMENT){
            CMDSDocument entity = CMDSDocumentDAO.INSTANCE.getById(publishableEntity.id);
            entity.publishingInProgress = true;
            CMDSDocumentDAO.INSTANCE.save(entity);
        }else if(publishableEntity.type == EntityType.CMDSMODULE){
            CMDSModule entity = CMDSModuleDAO.INSTANCE.getById(publishableEntity.id);
            entity.publishingInProgress = true;
            CMDSModuleDAO.INSTANCE.save(entity);
        }else if(publishableEntity.type == EntityType.CMDSTEST){
            CMDSTest entity = CMDSTestDAO.INSTANCE.getById(publishableEntity.id);
            entity.publishingInProgress = true;
            CMDSTestDAO.INSTANCE.save(entity);
        }else if(publishableEntity.type == EntityType.CMDSASSIGNMENT){
            CMDSAssignment entity = CMDSAssignmentDAO.INSTANCE.getById(publishableEntity.id);
            entity.publishingInProgress = true;
            CMDSAssignmentDAO.INSTANCE.save(entity);
        }
    }

    private static boolean checkIfPublishInProgress(SrcEntity publishableEntity) {
        // TODO Auto-generated method stub
        if(publishableEntity.type == EntityType.CMDSVIDEO){
            return CMDSVideoDAO.INSTANCE.getById(publishableEntity.id).publishingInProgress;
        }else if(publishableEntity.type == EntityType.CMDSDOCUMENT){
            return CMDSDocumentDAO.INSTANCE.getById(publishableEntity.id).publishingInProgress;
        }else if(publishableEntity.type == EntityType.CMDSMODULE){
            return CMDSModuleDAO.INSTANCE.getById(publishableEntity.id).publishingInProgress;
        }else if(publishableEntity.type == EntityType.CMDSTEST){
            return CMDSTestDAO.INSTANCE.getById(publishableEntity.id).publishingInProgress;
        }else if(publishableEntity.type == EntityType.CMDSASSIGNMENT){
            return CMDSAssignmentDAO.INSTANCE.getById(publishableEntity.id).publishingInProgress;
        }
        return false;
    }

    public static GetStatus getStatus(GetEntityPublishingStatusReq request) throws VedantuException {

        List<EntityOperationStatus> statuses = EntityOperationStatusDAO.INSTANCE
                .getByIds(ObjectIdUtils.toObjectIds(request.jobIds));
        if (CollectionUtils.isEmpty(statuses)) {
            throw new VedantuException(VedantuErrorCode.INVALID_JOB_ID);
        }

        GetStatus response = new GetStatus();

        for (EntityOperationStatus status : statuses) {
            EntityOperationStatusRes individualResponse = new EntityOperationStatusRes();
            LOGGER.debug("individual reponse is" + individualResponse);
            individualResponse.jobId = status._getStringId();
            individualResponse.id = status.id;
            individualResponse.type = status.type;
            individualResponse.numCompletedSteps = status.numOfStepsCompleted;
            individualResponse.numOfSteps = status.numOfSteps;
            individualResponse.errorCode = status.errorCode;
            //individualResponse.message = status.message;
            if (StringUtils.isEmpty(status.errorCode)) {
                individualResponse.errorCode = StringUtils.EMPTY;
            }

            response.list.add(individualResponse);

        }
        response.totalHits = statuses.size();
        return response;
    }

    /**
     * This feature is needed for adding position of content link
     *
     * @param request
     * @return
     * @throws VedantuException
     */
    public static ActionTakenRes updateLocation(UpdateRankReq request) throws VedantuException {

        // TODO start with zookeeper lock or simple mongo local to handle or check on section
        // library version updates

        ActionTakenRes response = new ActionTakenRes();
        response.done = false;

        MutableLong totalHits = new MutableLong();
        List<CMDSContentLink> links = CmdsContentLinkDAO.INSTANCE.getCmdsContentLinks(
                request.entity, request.target, CmdsContentLinkType.ADDED, null, Scope.UNKNOWN, 0,
                1, VedantuRecordState.ACTIVE, totalHits, null);

        if (CollectionUtils.isEmpty(links)) {
            throw new VedantuException(VedantuErrorCode.NOT_ALLOWED);

        }

        CMDSContentLink link = links.get(0);
        if (link.position != request.moveFrom) {
            throw new VedantuException(VedantuErrorCode.NOT_ALLOWED);
        }

        Query<CMDSContentLink> findQuery = CmdsContentLinkDAO.INSTANCE.createQuery();
        if (request.moveFrom == request.moveTo) {
            return response;
        }

        // move ahead moveFrom <= moveTo
        // decrement all between greater than moveFrom and equal to moveTo
        // update current link to moveTo

        boolean moveDown = false;
        if (request.moveFrom < request.moveTo) {

            findQuery.and(findQuery.criteria(CMDSContentLink.POSITION)
                    .greaterThan(request.moveFrom), findQuery.criteria(CMDSContentLink.POSITION)
                    .lessThanOrEq(request.moveTo));
        } else {
            moveDown = true;
            findQuery.and(
                    findQuery.criteria(CMDSContentLink.POSITION).greaterThanOrEq(request.moveTo),
                    findQuery.criteria(CMDSContentLink.POSITION).lessThan(request.moveFrom));
        }
        // move back moveFrom >= moveTo
        // increment all between greater than equal to moveTo equal To moveFrom

        String order = new SortOrderInfo(SortOrder.DESC, CMDSContentLink.POSITION).toString();
        LOGGER.debug(" Order by " + order + " query " + findQuery.toString());
        findQuery.order(order);

        List<CMDSContentLink> updatableLinks = findQuery.asList();
        List<String> positionFieldUpdateArray = Arrays.asList(CMDSContentLink.POSITION);
        LibraryContentLink dummyLink = new LibraryContentLink();
        List<String> libraryListCollectors = new ArrayList<String>();
        for (CMDSContentLink updatableLink : updatableLinks) {

            if (moveDown) {
                updatableLink.position += updatableLink.position > 0 ? 1 : 0;
            } else {
                updatableLink.position -= 1;
            }

            CmdsContentLinkDAO.INSTANCE.updateModel(updatableLink, positionFieldUpdateArray);
            CMDSContentLinkManager.INSTANCE.reIndex(updatableLink);
            LOGGER.debug("Check globalLink " + updatableLink.globalLinkId);
            if (StringUtils.isNotEmpty(updatableLink.globalLinkId)) {
                dummyLink.position = updatableLink.position;
                dummyLink.id = new ObjectId(updatableLink.globalLinkId);
                LibraryContentLinksDAO.INSTANCE.updateModel(dummyLink, positionFieldUpdateArray);
                libraryListCollectors.add(updatableLink.globalLinkId);
            }
        }

        // UpdateOperations<CMDSContentLink> rankUpdater = CmdsContentLinkDAO.INSTANCE
        // .createUpdateOperations();
        //
        // rankUpdater.inc(CMDSContentLink.POSITION);
        // UpdateResults<CMDSContentLink> updateResults = CmdsContentLinkDAO.INSTANCE.update(
        // findQuery, rankUpdater);
        link.position = request.moveTo;
        CmdsContentLinkDAO.INSTANCE.updateModel(link, Arrays.asList(CMDSContentLink.POSITION));
        CMDSContentLinkManager.INSTANCE.reIndex(link);
        if (StringUtils.isNotEmpty(link.globalLinkId)) {
            dummyLink.position = link.position;
            dummyLink.id = new ObjectId(link.globalLinkId);
            LOGGER.debug("Check globalLink " + link.globalLinkId);
            LibraryContentLinksDAO.INSTANCE.updateModel(dummyLink, positionFieldUpdateArray);
            libraryListCollectors.add(link.globalLinkId);
        }
        // TODO As of now this index updating is not being used any where
        // ReIndexLibraryContentReq libraryRequestUpdateReq = new ReIndexLibraryContentReq();
        // libraryRequestUpdateReq.linkIds= libraryListCollectors;
        // IndexingManager.INSTANCE.reIndexLibraryContentLinks(libraryRequestUpdateReq);
        response.done = true;
        return response;
    }

    public static ActionTakenRes calculate(String sectionId) throws VedantuException {

        // TODO start with zookeeper lock or simple mongo local to handle or check on section
        // library version updates

        ActionTakenRes response = new ActionTakenRes();
        long accumulatedSize = 0;
        List<CMDSContentLink> links = CmdsContentLinkDAO.INSTANCE.getCmdsContentLinks(null,
                new SrcEntity(EntityType.SECTION, sectionId), CmdsContentLinkType.ADDED, null,
                Scope.ORG, MongoManager.NO_START, MongoManager.NO_LIMIT, VedantuRecordState.ACTIVE,
                new MutableLong());

        for (CMDSContentLink link : links) {

            VedantuBasicDAO<?, ?> dao = EntityTypeDAOFactory.INSTANCE.get(link.source.type);
            if (dao != null) {
                AbstractContentModel contentModel = (AbstractContentModel) dao
                        .getById(link.source.id);
                accumulatedSize += contentModel.getExportableSize();
            }
        }

        OrgSectionDAO.INSTANCE.resetSize(Arrays.asList(sectionId), accumulatedSize);
        response.done = true;
        return response;
    }

}