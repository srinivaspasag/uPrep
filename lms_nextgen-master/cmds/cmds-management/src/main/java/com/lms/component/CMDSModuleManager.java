package com.lms.component;

import com.amazonaws.services.ecs.model.SortOrder;
import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.pojos.ScheduleInfo;
import com.lms.common.vedantu.Repo.CounterRepo;
import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.constants.HardCodedConstants;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.EventType;
import com.lms.common.vedantu.enums.Scope;
import com.lms.common.vedantu.enums.UserActionType;
import com.lms.common.vedantu.model.Counter;
import com.lms.common.vedantu.mongo.SortOrderInfo;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.components.QuestionComponent;
import com.lms.enums.CmdsContentLinkType;
import com.lms.enums.ModuleRun;
import com.lms.enums.PublishedStatus;
import com.lms.interfaces.IRelationshipSearchDetails;
import com.lms.managers.AbstractTestManager;
import com.lms.models.*;
import com.lms.models.events.searchdetails.CMDSContentLinkDetails;
import com.lms.models.events.searchdetails.CMDSResourceDetails;
import com.lms.pojos.ModuleEntryCompletionRule;
import com.lms.pojos.ModuleEntryInfo;
import com.lms.pojos.ModuleSearchIndexDetails;
import com.lms.pojos.requests.*;
import com.lms.pojos.requests.splModules.CMDSModuleInfo;
import com.lms.pojos.requests.splModules.GetModuleInfoReq;
import com.lms.pojos.responce.*;
import com.lms.pojos.search.details.AbstractSearchDetail;
import com.lms.repo.*;
import com.lms.repository.*;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.lms.common.vedantu.enums.EntityType.CMDSMODULE;
import static com.lms.common.vedantu.enums.EntityType.CMDSQUESTION;

@Component
public class CMDSModuleManager extends AbstractTestManager {
    private static final Logger logger = LoggerFactory.getLogger(CMDSModuleManager.class);
    public static final int NO_START = 0;
    public static final int NO_LIMIT = 0;

    @Autowired
    private CMDSModuleRepo cmdsModuleRepo;
    @Autowired
    private CMDSVideoRepo cmdsVideoRepo;
    @Autowired
    private CMDSTestRepo cmdsTestRepo;
    @Autowired
    private QuestionComponent questionComponent;
    @Autowired
    private CMDSFolderRepo cmdsFolderRepo;
    @Autowired
    private CounterRepo counterRepo;
    @Autowired
    private CMDSContentLinkRepo cmdsContentLinkRepo;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private ModuleManager moduleManager;
    @Autowired
    private VideoRepo videoRepo;
    @Autowired
    private TestRepo testRepo;
    @Autowired
    private AssignmentRepo assignmentRepo;
    @Autowired
    private DocumentsRepo documentsRepo;
    @Autowired
    private FilesRepo filesRepo;
    @Autowired
    private CMDSQuestionRepo cmdsQuestionRepo;

    protected static String getResourceId(SrcEntity content) {

        return (content.type + "_" + content.id).toLowerCase();
    }

    public static String addLiveEntityToSearchIndex(final AbstractSearchDetail details,
                                                    final EntityType entityType, final boolean ensureQueryState) {

    /*    QueryBuilder esQuery = QueryBuilders.termQuery(details._getUniqueId().getName(), details
                ._getUniqueId().getValue());
        SearchHit searchHit = ElasticSearchUtils.findOne(entityType.getIndexName(),
                entityType.getIndexType(), esQuery);
        logger.debug(" searchHit " + searchHit);
        if (searchHit != null) {
            logger.debug("entity already indexed ");
            String id = ElasticSearchManager.getInstance().reIndex(searchHit.getIndex(),
                    ObjectMapperUtils.convertValue(details, Map.class), searchHit.getId(),
                    searchHit.getType());
            if (StringUtils.isNotEmpty(id)) {
                ContentManager.addOrUpdateContentSearchDetails(details);
            }

            return id;
        }
        logger.debug("Indexing entity Live now ");
        String esId = ElasticSearchManager.getInstance().addIndex(entityType.getIndexName(),
                entityType.getIndexType(), ObjectMapperUtils.convertValue(details, Map.class));

        if (StringUtils.isNotEmpty(esId)) {
            ContentManager.addOrUpdateContentSearchDetails(details, ensureQueryState);
        }

        if (StringUtils.isNotEmpty(esId) && ensureQueryState) {
            boolean isQueriable = false;

            int tryCount = 0;
            while (!isQueriable && tryCount < ES_ENSURE_QUERY_STATE_MAX_TRY_COUNT) {
                tryCount++;
                logger.debug("tryCount: " + tryCount + ", query:" + esQuery);
                searchHit = ElasticSearchUtils.findOne(entityType.getIndexName(),
                        entityType.getIndexType(), esQuery);
                if (searchHit != null) {
                    isQueriable = true;
                } else {
                    try {

                        Thread.sleep(ELASTIC_SEARCH_REFRESH_TIME);

                    } catch (InterruptedException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }
        return esId;*/
        return null;
    }

    public CreateModuleRes createModule(CreateModuleReq request) throws VedantuException {

        try {
            logger.debug("......Inside create module........");
            CMDSModule module = createModule(request.userId, request.orgId,
                    request.moduleRun, request.name, request.tags, request.brdIds,
                    request.targetIds, request.prerequsiteModuleID);
            logger.debug("....Before event start........");
            questionComponent.generateEventAysc(request.userId, module, UserActionType.EventActionType.ADD,
                    EventType.INDEX_CMDS_MODULE, UserActionType.ADDED, false);
            CreateModuleRes response = new CreateModuleRes();
            response.id = module._getStringId();
            response.success = true;
            SrcEntity cmdsEntity = new SrcEntity(CMDSMODULE, module._getStringId());
            String parentESId = addAsCMDSResource(cmdsEntity, UserActionType.EventActionType.ADD, module);
            addToFolder(request.orgId, request.userId, cmdsEntity,
                    request.folderId, CmdsContentLinkType.ADDED, parentESId);
            return response;

        } catch (VedantuException exception) {
            throw exception;
        } catch (Exception exception) {
            logger.error(" Error", exception);

            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, exception);
        }

    }

    public CMDSModule createModule(String userId, String orgId, ModuleRun moduleRun, String name,
                                   List<String> tags, List<String> brdIds, List<String> targetIds,
                                   String prerequsiteModuleId) throws VedantuException {

        logger.debug("..... Inside create Module DAO function.......");
        CMDSModule module = new CMDSModule();
        module.name = name;
        module.userId = userId;
        module.moduleRun = moduleRun;
        module.boardIds = brdIds != null ? new HashSet<String>(brdIds) : null;
        module.targetIds = targetIds != null ? new HashSet<String>(targetIds) : null;
        module.tags = tags != null ? new HashSet<String>(tags) : null;
        module.contentSrc = new SrcEntity(EntityType.ORGANIZATION, orgId);
        module.prerequsiteModuleId = prerequsiteModuleId;
        module.completed = isReadyToPublished(module);
        logger.debug(".... about to save the module.....");
        cmdsModuleRepo.save(module);
        logger.debug(".... module is saved.....");

        return module;
    }

    public boolean isReadyToPublished(VedantuBaseMongoModel cmdsModel) throws VedantuException {

        logger.debug("...........Before loop......");
        if (cmdsModel instanceof CMDSModule) {

            CMDSModule module = (CMDSModule) cmdsModel;
            boolean canBePublished = true;
            if (canBePublished && (module.recordState != VedantuRecordState.ACTIVE)) {
                canBePublished &= false;
                logger.debug(" Module is not in active state id:" + module._getStringId());
            }
            if (canBePublished && (CollectionUtils.isEmpty(module.boardIds))) {
                canBePublished &= false;
                logger.debug(" No boards provided for Module id:" + module._getStringId());
            }

            boolean entityPresent = false;
            boolean allContentsComplete = true;
            List<ModuleEntry> children = module.children;

            logger.debug("...........Before loop......");
            if (!CollectionUtils.isEmpty(children)) {
                for (ModuleEntry child : children) {
                    logger.debug(" Inside for loop");
                    if (child.entity != null) {
                        logger.debug("Inside if function");
                        if (child.entity.type == EntityType.CMDSTEST) {
                            CMDSTest cmdsTest = cmdsTestRepo.findById(child.entity.id).get();
                            if (!cmdsTest.completed) {
                                logger.debug("...... not ready.......");
                                allContentsComplete = false;
                                break;
                            }
                        } else if (child.entity.type == EntityType.CMDSVIDEO) {
                            CMDSVideo cmdsVideo = cmdsVideoRepo.findById(child.entity.id).get();
                            if (!cmdsVideo.completed) {
                                logger.debug("...... not ready.......");
                                allContentsComplete = false;
                                break;
                            }
                        }

                    }

                    entityPresent = true;
                }
            }

            logger.debug(".........After loop......");
            canBePublished &= entityPresent;
            canBePublished &= allContentsComplete;
            return canBePublished;

        }
        return false;
    }

    public String addAsCMDSResource(SrcEntity modelEntity, UserActionType.EventActionType eventType,
                                    VedantuBaseMongoModel model) {
        String parentESId = null;
        if (modelEntity.type == CMDSMODULE) {
            CMDSResourceDetails resourceDetails = getCMDSResourceDetails(model);
            resourceDetails.id = getResourceId(resourceDetails.content);
            parentESId = addLiveEntityToSearchIndex(resourceDetails, EntityType.CMDSRESOURCE, true);
        } else if (modelEntity.type == CMDSQUESTION) {

        }

        return parentESId;
    }

    public CMDSResourceDetails getCMDSResourceDetails(VedantuBaseMongoModel model) {

        CMDSModule cmdsModule = (CMDSModule) model;
        CMDSResourceDetails details = new CMDSResourceDetails();

        details.fromMongoModel(model);
        details.content = new SrcEntity(EntityType.CMDSMODULE, model._getStringId());
        details.queryContext = cmdsModule.name;

        return details;
    }

    public boolean addToFolder(String orgId, String userId, SrcEntity content,
                               String folderId, CmdsContentLinkType linkType, String parentESId)
            throws VedantuException {

        CMDSFolder qrFolder = cmdsFolderRepo.findByIdAndOrganizationId(folderId, orgId);


        if (qrFolder != null) {
            SrcEntity folderEntity = new SrcEntity(EntityType.FOLDER, qrFolder._getStringId());
            CMDSContentLink linkage = addLink(content, folderEntity,
                    linkType, userId, false);

            logger.debug(" Created linkage : " + linkage);

            CMDSContentLinkDetails libraryContentLinkDetails = new CMDSContentLinkDetails(
                    linkage._getStringId(), userId, content, folderEntity, linkage.getScope(),
                    linkage.timeCreated, linkage.position);

            SrcEntity resource = new SrcEntity(EntityType.CMDSRESOURCE, getResourceId(content));

            updateUserActionMappintToEs(libraryContentLinkDetails, resource, UserActionType.ADDED,
                    UserActionType.EventActionType.ADD, parentESId);

            return true;
        }

        throw new VedantuException(VedantuErrorCode.FOLDER_NOT_FOUND);
    }

    public CMDSContentLink addLink(SrcEntity content, SrcEntity targetEntity,
                                   CmdsContentLinkType linkType, String actorId, boolean allowDuplicates)
            throws VedantuException {

        AtomicLong totalHits = new AtomicLong(0L);
        List<CMDSContentLink> links = getCmdsContentLinks(content, targetEntity, linkType, null, 0,
                1, VedantuRecordState.ACTIVE, totalHits);

        if (totalHits.longValue() > 1 && !allowDuplicates) {
            logger.error("Added multiple times ");
            throw new VedantuException(VedantuErrorCode.ALREADY_ADDED);
        }

        CMDSContentLink contentLinkage = null;
        if (CollectionUtils.isNotEmpty(links) && !allowDuplicates) {
            contentLinkage = links.get(0);
        } else {
            contentLinkage = new CMDSContentLink(targetEntity, content);

            contentLinkage.userId = actorId;
            contentLinkage.linkType = linkType;
            // this is ever increasing number for all positions
            contentLinkage.position = getNextSequence("cmdscontentlinks",
                    getCounterName(contentLinkage, CMDSContentLink.POSITION), 1);

        }
        cmdsContentLinkRepo.save(contentLinkage);
        return contentLinkage;
    }

    public long getNextSequence(String collectionName, String field, int byValue) {

        long count = counterRepo.count() + byValue;
        Counter counter = new Counter();
        counter.setCollection(collectionName);
        counter.setField(field);
        counter.setValue(count);

        Counter counter1 = counterRepo.findByFieldAndCollection(field, collectionName);
        if (counter1 == null) {
            counterRepo.save(counter);
            return counter.value;
        }
        return counter1.value;

    }

    public String getCounterName(CMDSContentLink link, String suffix) {

        logger.debug("Getting counter name for" + link.target + suffix + link.linkType);
        return getCounterName(link.target, link.linkType, suffix);
    }

    protected String getCounterName(SrcEntity target, CmdsContentLinkType linkType,
                                    String suffix) {

        logger.debug("Getting counter name for" + target + suffix + linkType);
        return target.type.name().toLowerCase() + "_" + target.id + "_"
                + linkType.name().toLowerCase() + "_" + suffix.trim().toLowerCase();
    }

    public List<CMDSContentLink> getCmdsContentLinks(SrcEntity content, SrcEntity targetEntity,
                                                     CmdsContentLinkType linkType, String actorId, int start, int size,
                                                     VedantuRecordState recordState, AtomicLong totalHits) {

        return getCmdsContentLinks(content, targetEntity, linkType, actorId, null, start, size,
                recordState, totalHits);
    }

    public List<CMDSContentLink> getCmdsContentLinks(SrcEntity content, SrcEntity targetEntity,
                                                     CmdsContentLinkType linkType, String actorId, Scope scope, int start, int size,
                                                     VedantuRecordState recordState, AtomicLong totalHits) {

        return getCmdsContentLinks(
                content,
                targetEntity,
                linkType,
                actorId,
                scope,
                start,
                size,
                recordState,
                totalHits,
                new HashSet<SortOrderInfo>(Arrays.asList(new SortOrderInfo(SortOrder.DESC,
                        CMDSContentLink.TIME_CREATED))));

    }

    public List<CMDSContentLink> getCmdsContentLinks(SrcEntity content, SrcEntity targetEntity,
                                                     CmdsContentLinkType linkType, String actorId, Scope scope, int start, int size,
                                                     VedantuRecordState recordState, AtomicLong totalHits, Set<SortOrderInfo> orders) {

        Query query = new Query();
        Criteria criteria = new Criteria();
        logger.debug("Querying for " + CMDSContentLink.class);
        addTargetFilter(criteria, targetEntity);
        moduleManager.addSourceFilter(criteria, content);
        addLinkTypeFilter(criteria, linkType);
        addScope(criteria, scope);
        if (recordState != null) {
            criteria.and("recordState").equals(recordState);
        }

        if (CollectionUtils.isNotEmpty(orders)) {
            /*getContentQuery = getContentQuery.order(StringUtils.join(orders, ",")).limit(size)
                    .offset(start);*/
        }
        query.addCriteria(criteria);

        logger.debug("Query: " + query.toString());

        List<CMDSContentLink> cmdsContentLinks = mongoTemplate.find(query, CMDSContentLink.class);

        if (totalHits != null) {
            long totalHts = cmdsContentLinks.stream().count();
            totalHits.set(totalHts);
            logger.debug("Total matched results " + totalHts);
        }

        return cmdsContentLinks;

    }

    private List<String> toStringList(List<? extends Enum<?>> enums) {

        List<String> values = new ArrayList<String>();
        for (Enum<?> e : enums) {
            values.add(e.name());
        }
        return values;
    }

    private Criteria addTargetFilter(Criteria criteria, SrcEntity target) {

        if (target != null) {
            if (target.type != null) {
                criteria.and("target.type").is(target.type);
                if (target.id != null) {
                    criteria.and("target.id").is(target.id);

                }
            }

        }
        return criteria;
    }

    private Criteria addLinkTypeFilter(Criteria criteria,
                                       CmdsContentLinkType linkType) {

        if (linkType == null || linkType == CmdsContentLinkType.UNKNOWN) {
            return criteria;
        }
        return criteria.and("linkType").is(linkType);
    }

    private Criteria addScope(Criteria criteria, Scope scope) {

        if (scope == null || scope == Scope.UNKNOWN) {
            return criteria;
        }
        return criteria.and("scope").is(scope);

    }

    public void updateUserActionMappintToEs(IRelationshipSearchDetails details,
                                            SrcEntity parent, UserActionType actionType, UserActionType.EventActionType eventAction,
                                            String parentEsId) {

        updateUserActionMappintToEs(details, parent, actionType.getSearchIndexType(), eventAction,
                parentEsId);
    }

    public void updateUserActionMappintToEs(IRelationshipSearchDetails details,
                                            SrcEntity parent, String indexType, UserActionType.EventActionType eventAction, String parentEsId) {

        updateUserActionMappintToEs(details, parent, parent.type.getIndexName(),
                parent.type.getIndexType(), indexType, eventAction, parentEsId);

    }

    public void updateUserActionMappintToEs(IRelationshipSearchDetails details,
                                            SrcEntity parent, String indexName, String indexType, String mappingIndexType,
                                            UserActionType.EventActionType eventAction, String parentEsId) {

        if (StringUtils.isEmpty(indexName) || StringUtils.isEmpty(indexType)) {
            return;
        }
        Query esQuery = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ConstantsGlobal.ID).is(parent.id);

        // we already have the es parent _id
       /* SearchHit searchHit = StringUtils.isNotEmpty(parentEsId) ? null : ElasticSearchUtils
                .findOne(indexName, indexType, esQuery);
        if (StringUtils.isNotEmpty(parentEsId) || searchHit != null) {
            if (searchHit != null) {
                parentEsId = searchHit.getId();
            }
            if (eventAction == EventActionType.ADD || eventAction == EventActionType.UPDATE) {
                logger.info("adding details to es mapping : " + details + " with parent type "
                        + parent.type + "  and parent es Id " + parentEsId);
                ElasticSearchUtils.addMappingToES(indexName, mappingIndexType.toLowerCase(),
                        details, parentEsId, true);
            } else if (eventAction == EventActionType.REMOVE) {

                logger.debug("removing details to es mapping : " + details + " with parent type "
                        + parent.type + "  and parent es Id " + parentEsId);

                ElasticSearchManager.getInstance().removeEntry(searchHit.getIndex(),
                        mappingIndexType.toLowerCase(), details._getEsQuery());

            }
        } else {
            logger.debug("no hits found for query:" + esQuery);
        }*/

    }

    public IListResponseObj collectResourceInfo() {

        return null;
    }

    public AddModuleRes addModuleEntries(AddModuleEntryReq request) throws VedantuException {

        try {
            logger.debug("......Inside add module........");// +
            // request.children.indexOf(0).);

            if (request.children == null) {
                throw new VedantuException(VedantuErrorCode.MODULE_LIST_EMPTY);
            }
            String validateRes = request.validate();
            if (!StringUtils.isEmpty(validateRes)) {
                throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, validateRes);
            }

            CMDSModule module = addModuleEntries(request.moduleId,
                    request.children, request.pos);

            SrcEntity entity = new SrcEntity();
            entity.type = CMDSMODULE;
            entity.id = module._getStringId();

            addAsCMDSResource(entity, UserActionType.EventActionType.UPDATE, module);
            questionComponent.generateEventAysc(request.userId, module, UserActionType.EventActionType.UPDATE,
                    EventType.INDEX_CMDS_MODULE, UserActionType.UPDATED, false);

            AddModuleRes response = new AddModuleRes();
            response.module = module;
            response.success = true;
            return response;

        } catch (VedantuException exception) {
            throw exception;
        } catch (Exception exception) {
            logger.error(" Error", exception);

            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, exception);
        }

    }

    public CMDSModule addModuleEntries(String id, List<ModuleEntry> moduleEntries, int pos)
            throws VedantuException {

        logger.debug("..... Inside update Module DAO function.......");
        Optional<CMDSModule> module = cmdsModuleRepo.findById(id);

        // //TODO
        // if (module.published == true) {
        //     throw new VedantuException(VedantuErrorCode.ALREADY_PUBLISHED,
        //             "module is published already");
        // }

        if (module.get().getChildren() == null) {
            module.get().setChildren(new ArrayList<ModuleEntry>());
        }

        if (pos > module.get().getChildren().size() || pos < -1) {
            throw new VedantuException(VedantuErrorCode.INVALID_POSITION);
        }

        if (moduleEntries != null) {
            if (pos == -1) {
                pos = module.get().getChildren().size();
            }
            int addedEntryPosition = pos;
            for (ModuleEntry moduleEntry : moduleEntries) {
                if (!module.get().getChildren().contains(moduleEntry)) {
                    logger.debug(".... inside if statement.....");
                    module.get().getChildren().add(addedEntryPosition, moduleEntry);
                    addedEntryPosition++;
                }
            }
        }
        module.get().setCompleted(isReadyToPublished(module.get()));
        logger.debug(".... about to save the module.....");
        cmdsModuleRepo.save(module.get());
        logger.debug(".... module is saved.....");
        return module.get();
    }

    public UpdateModuleRes updateModule(UpdateModuleReq request) throws VedantuException {

        try {
            logger.debug("......Inside create module........");

            CMDSModule module = updateModule(request.id, request.name,
                    request.moduleRun, request.tags, request.brdIds, request.targetIds,
                    request.prerequsiteModuleId, request.updateList);
            SrcEntity entity = new SrcEntity(CMDSMODULE, module._getStringId());

            if (module.globalModuleId != null) {
                logger.debug("......global module id is not null........" + module.globalModuleId);
                request.id = module.globalModuleId;
                moduleManager.updateModule(request);
            }
            addAsCMDSResource(entity, UserActionType.EventActionType.UPDATE, module);
            questionComponent.generateEventAysc(request.userId, module, UserActionType.EventActionType.UPDATE,
                    EventType.INDEX_CMDS_MODULE, UserActionType.UPDATED, false);
            UpdateModuleRes response = new UpdateModuleRes();
            // response.module = module;
            response.success = true;
            return response;

        } catch (VedantuException exception) {
            throw exception;
        } catch (Exception exception) {
            logger.error(" Error", exception);

            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, exception);
        }

    }

    public CMDSModule updateModule(String id, String name, ModuleRun moduleRun, List<String> tags,
                                   List<String> brdIds, List<String> targetIds, String prerequsiteModuleId,
                                   List<String> updateList) throws VedantuException {

        logger.debug("..... Inside update Module DAO function.......");
        Optional<CMDSModule> module1 = cmdsModuleRepo.findById(id);
        CMDSModule module = module1.get();
        if (CollectionUtils.isEmpty(updateList)) {
            throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_UPDATED, "empty update list");
        }

        if (name != null && updateList.contains(ConstantsGlobal.NAME)) {
            module.name = name;
        }

        if (updateList.contains(ModuleSearchIndexDetails.FIELD_MODULE_RUN)) {
            module.moduleRun = moduleRun;
        }

        if (updateList.contains(ConstantsGlobal.BOARD_IDS)) {
            module.boardIds = brdIds != null ? new HashSet<String>(brdIds) : null;
        }
        if (updateList.contains(ConstantsGlobal.TARGET_IDS)) {
            module.targetIds = targetIds != null ? new HashSet<String>(targetIds) : null;
        }
        if (updateList.contains(ConstantsGlobal.TAGS)) {
            module.tags = tags != null ? new HashSet<String>(tags) : null;
        }
        if (updateList.contains(ConstantsGlobal.PREREQUSITE_MODULE_ID)) {
            module.prerequsiteModuleId = prerequsiteModuleId;
        }
        module.completed = isReadyToPublished(module);
        logger.debug(".... about to save the module.....");
        cmdsModuleRepo.save(module);
        logger.debug(".... module is saved.....");
        return module;
    }

    public DeleteModuleRes deleteModule(DeleteModuleReq request) throws VedantuException {

        try {
            logger.debug("......Inside delete module........");

            Optional<CMDSModule> module1 = cmdsModuleRepo.findById(request.id);
            cmdsModuleRepo.deleteById(request.id);
            CMDSModule module = module1.get();
            SrcEntity entity = new SrcEntity();
            entity.type = CMDSMODULE;
            entity.id = module._getStringId();

            addAsCMDSResource(entity, UserActionType.EventActionType.UPDATE, module);
            questionComponent.generateEventAysc(request.userId, module, UserActionType.EventActionType.UPDATE,
                    EventType.INDEX_CMDS_MODULE, UserActionType.UPDATED, false);

            DeleteModuleRes response = new DeleteModuleRes();
            response.success = true;
            return response;

        } catch (VedantuException exception) {
            throw exception;
        } catch (Exception exception) {
            logger.error(" Error", exception);

            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, exception);
        }

    }

    public MoveModuleEntryRes moveModuleEntry(MoveModuleEntryReq request)
            throws VedantuException {

        try {
            logger.debug("......Inside add module........");// +
            // request.children.indexOf(0).);

            CMDSModule module = moveModuleEntry(request.moduleId,
                    request.oldPos, request.pos);
            SrcEntity entity = new SrcEntity(CMDSMODULE, module._getStringId());

            addAsCMDSResource(entity, UserActionType.EventActionType.UPDATE, module);
            questionComponent.generateEventAysc(request.userId, module, UserActionType.EventActionType.UPDATE,
                    EventType.INDEX_CMDS_MODULE, UserActionType.UPDATED, false);
            MoveModuleEntryRes response = new MoveModuleEntryRes();
            // response.module = module;
            response.success = true;
            return response;

        } catch (VedantuException exception) {
            throw exception;
        } catch (Exception exception) {
            logger.error(" Error", exception);

            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, exception);
        }

    }

    public CMDSModule moveModuleEntry(String id, int oldPos, int pos) throws VedantuException {

        logger.debug("..... Inside update Module DAO function.......");
        Optional<CMDSModule> module1 = cmdsModuleRepo.findById(id);
        CMDSModule module = module1.get();


        // //TODO
        // if (module.published == true) {
        //     throw new VedantuException(VedantuErrorCode.ALREADY_PUBLISHED,
        //             "module is published already");
        // }

        if (module.children == null) {
            throw new VedantuException(VedantuErrorCode.CONTENT_LIST_EMPTY);
        }

        if (oldPos >= module.children.size() || oldPos < 0) {
            throw new VedantuException(VedantuErrorCode.INVALID_POSITION, "invalid oldPos:"
                    + oldPos);
        }

        if (pos >= module.children.size() || pos < -1) {
            throw new VedantuException(VedantuErrorCode.INVALID_POSITION, "invalid pos:" + pos);
        }

        if (oldPos == pos) {
            throw new VedantuException(VedantuErrorCode.INVALID_POSITION, "oldPos[" + oldPos
                    + "] and pos[" + pos + "] can not be same");
        }

        if (pos == -1) {
            pos = module.children.size() - 1;
        }

        ModuleEntry moduleEntry = module.children.remove(oldPos);
        module.children.add(pos, moduleEntry);
        module.completed = isReadyToPublished(module);
        logger.debug(".... about to save the module.....");
        cmdsModuleRepo.save(module);
        logger.debug(".... module is saved.....");

        return module;

    }

    public DeleteModuleEntryRes deleteModuleEntry(DeleteModuleEntryReq request)
            throws VedantuException {

        try {
            logger.debug("......Inside add module........");// +
            // request.children.indexOf(0).);

            CMDSModule module = deleteModuleEntry(request.moduleId,
                    request.pos);
            module.completed = isReadyToPublished(module);
            SrcEntity entity = new SrcEntity();
            entity.type = CMDSMODULE;
            entity.id = module._getStringId();

            addAsCMDSResource(entity, UserActionType.EventActionType.UPDATE, module);
            questionComponent.generateEventAysc(request.userId, module, UserActionType.EventActionType.UPDATE,
                    EventType.INDEX_CMDS_MODULE, UserActionType.UPDATED, false);
            DeleteModuleEntryRes response = new DeleteModuleEntryRes();
            // response.module = module;
            response.success = true;
            return response;

        } catch (VedantuException exception) {
            throw exception;
        } catch (Exception exception) {
            logger.error(" Error", exception);

            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, exception);
        }

    }

    public CMDSModule deleteModuleEntry(String moduleId, int pos) throws VedantuException {

        Optional<CMDSModule> module1 = cmdsModuleRepo.findById(moduleId);
        if (!module1.isPresent()) {
            throw new VedantuException(VedantuErrorCode.MODULE_DOES_NOT_EXISTS);
        }
        CMDSModule module = module1.get();

        // //TODO
        // if (module.published == true) {
        //     throw new VedantuException(VedantuErrorCode.ALREADY_PUBLISHED,
        //             "module is published already");
        // }

        if (module.children == null) {
            throw new VedantuException(VedantuErrorCode.CONTENT_LIST_EMPTY);
        }

        if (pos >= module.children.size() || pos < 0) {
            throw new VedantuException(VedantuErrorCode.INVALID_POSITION);
        }

        module.children.remove(pos);
        module.completed = isReadyToPublished(module);
        logger.debug(".... about to save the module.....");
        cmdsModuleRepo.save(module);
        return module;
    }

    public GetModuleInfoRes getModuleInfo(GetModuleInfoReq request) throws VedantuException {

        try {
            logger.debug("......Inside delete module........" + request.id);
            Optional<CMDSModule> module1 = cmdsModuleRepo.findById(request.getId());
            CMDSModule module = module1.get();
            SrcEntity content = new SrcEntity(CMDSMODULE, request.id);
            SrcEntity targetEntity = new SrcEntity(EntityType.SECTION, request.sectionId);
            List<CMDSContentLink> contentLinks = getCmdsContentLinks(content,
                    targetEntity, CmdsContentLinkType.ADDED, null, Scope.ORG, NO_START, NO_LIMIT,
                    VedantuRecordState.ACTIVE, null, null);
            CMDSModuleInfo moduleInfo = getModuleInfo(module, contentLinks);
            GetModuleInfoRes response = new GetModuleInfoRes();
            response.fromMongoModel(module);
            Map<String, ScheduleInfo> schedules = new HashMap<String, ScheduleInfo>();
            if (request.target != null && request.target.type == EntityType.SECTION && !StringUtils.isEmpty(request.target.id)) {
                SrcEntity source = new SrcEntity(CMDSMODULE, request.id);
                SrcEntity target = new SrcEntity(EntityType.SECTION, request.target.id);
                List<ModuleSchedules> moduleScheduleInfos = getSchedule(target, source);
                for (ModuleSchedules moduleSchedule : moduleScheduleInfos) {
                    schedules.put(moduleSchedule.entity.id, moduleSchedule.schedule);
                }
            }
            response.schedules = schedules;
            annotateExtraInfo(request.userId, request.orgId, EntityType.MODULE, response);
            response.moduleInfo = moduleInfo;
            return response;

        } catch (VedantuException exception) {
            throw exception;
        } catch (Exception exception) {
            logger.error(" Error", exception);

            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, exception);
        }
    }

    public CMDSModuleInfo getModuleInfo(CMDSModule module, List<CMDSContentLink> contentLinks) throws VedantuException {

        logger.debug("..... Inside get Module DAO function.......");
        logger.debug("..... Module is InitialisedgetModuleById.......");
        String orgId = (module.contentSrc != null) ? module.contentSrc.id : HardCodedConstants.emptyString;
        CMDSModuleInfo moduleInfo = new CMDSModuleInfo(module._getStringId(), module.name,
                CMDSMODULE, orgId, module.timeCreated, module.lastUpdated,
                module.userId, 0, module.published, module.completed, true, module.globalModuleId,
                module.moduleRun, module.recordState, new ArrayList<ModuleEntryInfo>(),
                module.prerequsiteModuleId);

        Set<String> downloadableEntities = new HashSet<String>();
        for (CMDSContentLink link : contentLinks) {
            for (SrcEntity entity : link.getDownloadableEntities()) {
                if (entity.type == EntityType.VIDEO) {
                    downloadableEntities.add(videoRepo.findById(entity.id).get().getCmdsVideoId());
                } else if (entity.type == EntityType.TEST) {
                    downloadableEntities.add(testRepo.findById(entity.id).get().cmdsTestId);
                } else if (entity.type == EntityType.DOCUMENT) {
                    downloadableEntities.add(documentsRepo.findById(entity.id).get().getCMDSDocId());
                } else if (entity.type == EntityType.ASSIGNMENT) {
                    downloadableEntities.add(assignmentRepo.findById(entity.id).get().cmdsId);
                } else if (entity.type == EntityType.FILE) {
                    downloadableEntities.add(filesRepo.findById(entity.id).get().getCMDSFileId());
                }
            }
        }

        logger.debug("..... ModuleInfo Initialised.......");

        if (module.children != null) {
            for (ModuleEntry child : module.children) {
                logger.debug("..... Inside the loop.......");
                ModuleEntryInfo moduleEntryInfo = new ModuleEntryInfo();
                moduleEntryInfo.name = child.name;
                moduleEntryInfo.completionRule = child.completionRule;
                moduleEntryInfo.entity = child.entity;
                if (child.entity != null) {
                    logger.debug("..... Entity is not null.......");

                    //  VedantuBasicDAO dao = EntityTypeDAOFactory.INSTANCE.get(child.entity.type);
                    if (child.entity.type == EntityType.CMDSTEST) {
                        moduleEntryInfo.info = getBasicCMDSTestInfo(child.entity.id);

                    } else if (child.entity.type == EntityType.CMDSVIDEO) {

                        moduleEntryInfo.info = getBasicCMDSVideoInfo(child.entity.id);

                    }

                    moduleEntryInfo.downloadState = downloadableEntities.contains(child.entity.id) ? "ENABLED" : "DISABLED";
                }
                moduleInfo.children.add(moduleEntryInfo);
            }
            logger.debug("..... Outside the loop.......");
        }

        return moduleInfo;
    }

    public <B extends ModelBasicInfo> B getBasicCMDSTestInfo(String id) {

        Optional<CMDSTest> result = cmdsTestRepo.findById(id);
        logger.debug("......in getBasicDAO......" + result + id);
        B basicInfo = null != result ? (B) result.get().toBasicInfo() : null;

        return basicInfo;
    }

    public <B extends ModelBasicInfo> B getBasicCMDSVideoInfo(String id) {

        Optional<CMDSVideo> result = cmdsVideoRepo.findById(id);
        logger.debug("......in getBasicDAO......" + result + id);
        B basicInfo = null != result ? (B) result.get().toBasicInfo() : null;

        return basicInfo;
    }

    public <B extends ModelBasicInfo> B getBasicCMDSQuestionInfo(String id) {

        Optional<CMDSQuestion> result = cmdsQuestionRepo.findById(id);
        logger.debug("......in getBasicDAO......" + result + id);
        B basicInfo = null != result ? (B) result.get().toBasicInfo() : null;

        return basicInfo;
    }

    public List<ModuleSchedules> getSchedule(SrcEntity target, SrcEntity source) {

        Query query1 = new Query();
        Criteria criteria = new Criteria();
        criteria.and("target.type").is(target.type);// SECTION
        criteria.and("target.id").is(target.id);
        criteria.and("source.type").is(source.type);// CMDSMODULE
        criteria.and("source.id").is(source.id);
        criteria.and("recordState").is(VedantuRecordState.ACTIVE);
        query1.addCriteria(criteria);
        List<ModuleSchedules> result = mongoTemplate.find(query1, ModuleSchedules.class);
        return result;
    }

    public UpdateModuleEntryRes updateModuleEntry(UpdateModuleEntryReq request)
            throws VedantuException {

        try {
            logger.debug("......Inside update moduleEntry........");// +
            // request.children.indexOf(0).);

            CMDSModule module = updateModuleEntry(request.moduleId,
                    request.pos, request.name, request.completionRule);

            if (module.globalModuleId != null) {
                logger.debug("......global module id is not null........" + module.globalModuleId);
                request.moduleId = module.globalModuleId;
                moduleManager.updateModuleEntry(request);
            }

            SrcEntity entity = new SrcEntity(CMDSMODULE, module._getStringId());

            addAsCMDSResource(entity, UserActionType.EventActionType.UPDATE, module);
            questionComponent.generateEventAysc(request.userId, module, UserActionType.EventActionType.UPDATE,
                    EventType.INDEX_CMDS_MODULE, UserActionType.UPDATED, false);
            UpdateModuleEntryRes response = new UpdateModuleEntryRes();
            // response.module = module;
            response.success = true;
            return response;

        } catch (VedantuException exception) {
            throw exception;
        } catch (Exception exception) {
            logger.error(" Error", exception);

            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, exception);
        }

    }

    public CMDSModule updateModuleEntry(String moduleId, int pos, String name,
                                        ModuleEntryCompletionRule completionRule) throws VedantuException {

        logger.debug("..... Inside update Module DAO function.......");
        Optional<CMDSModule> module1 = cmdsModuleRepo.findById(moduleId);
        CMDSModule module = module1.get();

        if (module.children == null) {
            throw new VedantuException(VedantuErrorCode.CONTENT_LIST_EMPTY);
        }

        if (pos >= module.children.size() || pos < 0) {
            throw new VedantuException(VedantuErrorCode.INVALID_POSITION);
        }

        logger.debug(".... about to save the module.....");
        if (!StringUtils.isEmpty(name)) {
            module.children.get(pos).name = name;
        }
        if (completionRule != null) {
            module.children.get(pos).completionRule = completionRule;
        }

        module.completed = isReadyToPublished(module);
        cmdsModuleRepo.save(module);
        logger.debug(".... module is saved.....");

        return module;
    }

    public GetCMDSModulesRes getCMDSModules(GetCMDSModulesReq getCMDSModulesReq)
            throws VedantuException {

        List<CMDSModuleNameInfo> modules = getCMDSModules(
                getCMDSModulesReq.orgId, getCMDSModulesReq.publishedStatus,
                getCMDSModulesReq.start, getCMDSModulesReq.size);
        logger.debug(".......Inside getModules manager function......." + getCMDSModulesReq.size);
        GetCMDSModulesRes response = new GetCMDSModulesRes();
        response.setModules(modules);
        return response;
    }

    public List<CMDSModuleNameInfo> getCMDSModules(String orgId, PublishedStatus publishedStatus,
                                                   int start, int size) throws VedantuException {

        logger.debug(".......Inside getModules function......." + start + size);
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("contentSrc.type").is(EntityType.ORGANIZATION);
        criteria.and("contentSrc.id").is(orgId);
        if (publishedStatus != null) {
            if (publishedStatus == PublishedStatus.PUBLISHED) {
                criteria.and("published").is(true);
            }
            if (publishedStatus == PublishedStatus.NOT_PUBLISHED) {
                criteria.and("published").is(false);
            }

        }
        query.addCriteria(criteria);

        List<CMDSModule> modules = mongoTemplate.find(query, CMDSModule.class);

        logger.debug(".......Inside getModules function......." + size);
        List<CMDSModuleNameInfo> infos = new ArrayList<CMDSModuleNameInfo>();
        if (!CollectionUtils.isEmpty(modules)) {
            for (CMDSModule module : modules) {
                CMDSModuleNameInfo info = new CMDSModuleNameInfo();
                info.name = module.name;
                info.id = module._getStringId();
                info.published = module.published;
                infos.add(info);
            }
        }
        logger.debug(".......Exiting getModules function......." + size);
        return infos;
    }

}
