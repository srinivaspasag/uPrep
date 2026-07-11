package com.lms.services.serviceImpl;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.entity.storage.FileCategory;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.UserActionType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.SrcType;
import com.lms.managers.AbstractContentManager;
import com.lms.models.Module;
import com.lms.models.*;
import com.lms.pojos.ModuleEntryInfo;
import com.lms.pojos.TestMetadata;
import com.lms.pojos.VideoInfo;
import com.lms.pojos.requests.*;
import com.lms.pojos.responce.*;
import com.lms.repository.*;
import com.lms.services.ModuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ModuleServiceImpl extends AbstractContentManager implements ModuleService {
    private static final Logger logger = LoggerFactory.getLogger(TestMetadata.class);
    private static Map<UserActionType, VedantuErrorCode> alreadyPresentErrorCodeMap;

    static {
        initMappingAlreadyPresentErrorMap();
    }

    @Autowired
    UserModuleStatusRepo userModuleStatusRepo;
    @Autowired
    ModuleRepo moduleRepo;
    @Autowired
    EntityUserActionMappingRepo entityUserActionMappingRepo;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private VideoRepo videoRepo;
    @Autowired
    private DocumentsRepo documentsRepo;

    private static void initMappingAlreadyPresentErrorMap() {

        alreadyPresentErrorCodeMap = new HashMap<UserActionType, VedantuErrorCode>();
        alreadyPresentErrorCodeMap.put(UserActionType.VOTED, VedantuErrorCode.ALREADY_VOTED);
        alreadyPresentErrorCodeMap.put(UserActionType.COMPLETED, VedantuErrorCode.ALREADY_COMPLETED);
        alreadyPresentErrorCodeMap
                .put(UserActionType.FOLLOWING, VedantuErrorCode.ALREADY_FOLLOWING);
    }

    @Override

    public VedantuResponse updateUserModuleStatus(UpdateUserModuleReq updateUserModuleReq) {
        if (updateUserModuleReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        boolean isSuccessful = update(updateUserModuleReq.getUserId(), updateUserModuleReq.getModuleId(), updateUserModuleReq.getModuleEntry());
        UpdateUserModuleRes response = new UpdateUserModuleRes();
        response.isSuccessful = isSuccessful;
        return new VedantuResponse(response);
    }

    public boolean update(String userId, String moduleId, ModuleEntry moduleEntry) {

        logger.debug(".......Inside update function.......");
        UserModuleStatus userModuleStatus = userModuleStatusRepo.findByUserIdAndModuleId(userId, moduleId);
        if (userModuleStatus == null) {
            logger.debug("....... user module status is null.......");
            userModuleStatus = new UserModuleStatus();
            List<ModuleEntry> moduleEnteries = new ArrayList<ModuleEntry>();
            moduleEnteries.add(moduleEntry);
            userModuleStatus.children = moduleEnteries;
            userModuleStatus.moduleId = moduleId;
            userModuleStatus.userId = userId;
        } else if (userModuleStatus.children == null) {
            userModuleStatus = new UserModuleStatus();
            List<ModuleEntry> moduleEnteries = new ArrayList<ModuleEntry>();
            moduleEnteries.add(moduleEntry);
            userModuleStatus.children = moduleEnteries;
        } else {
            userModuleStatus.children.add(moduleEntry);
        }
        userModuleStatusRepo.save(userModuleStatus);
        return true;
    }


    @Override
    public VedantuResponse getUserModuleStatus(GetUserModuleReq getUserModuleReq) {
        List<ModuleContentAccessStatus> moduleContentsAccessStatus = get(getUserModuleReq.getUserId(), getUserModuleReq.getModuleId());
        GetUserModuleRes response = new GetUserModuleRes();
        response.contentAcccessStatus = moduleContentsAccessStatus;
        return new VedantuResponse(response);
    }


    public List<ModuleContentAccessStatus> get(String userId, String moduleId) throws VedantuException {

        logger.debug(".......Inside get function.......");
        UserModuleStatus userModuleStatus = userModuleStatusRepo.findByUserIdAndModuleId(userId, moduleId);
        List<ModuleEntry> children = getChildrens(moduleId);
        logger.debug(".......After ModuleDAO.......");
        List<ModuleContentAccessStatus> contentsAccessStatus = new ArrayList<ModuleContentAccessStatus>();
        if (children == null || !(children).isEmpty()) {
            for (ModuleEntry child : children) {
                logger.debug(".......Inside for loop.......");
                ModuleContentAccessStatus contentAccessStatus = new ModuleContentAccessStatus();
                contentAccessStatus.moduleEntry = child;
                logger.debug(".......Before Accessed is called.......");
                contentAccessStatus.accessed = (userModuleStatus != null && !(userModuleStatus.children).isEmpty()) && userModuleStatus.children.contains(child);
                logger.debug(".......After Accessed is called.......");
                contentsAccessStatus.add(contentAccessStatus);
            }
        }
        return contentsAccessStatus;
    }

    public List<ModuleEntry> getChildrens(String id) throws VedantuException {

        logger.debug(".......Inside ModuleDAO get function.......");
        Module module = moduleRepo.findById(id).get();
        if (module == null) {
            throw new VedantuException(VedantuErrorCode.MODULE_DOES_NOT_EXISTS);
        }
        return module.children;
    }


    @Override
    public VedantuResponse syncModule(SyncModuleReq syncModuleReq) {

        if (syncModuleReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        logger.debug("..........Inside syncModule fumction in ModuleManager........");
        List<SrcEntity> entities = sync(syncModuleReq.userId,
                syncModuleReq.moduleId, syncModuleReq.entities);

        SyncModuleRes response = new SyncModuleRes();
        response.userId = syncModuleReq.userId;
        response.moduleId = syncModuleReq.moduleId;
        response.entities = entities;
        return new VedantuResponse(response);
    }

    public List<SrcEntity> sync(String userId, String moduleId, List<SrcEntity> entities)
            throws VedantuException {

        logger.debug(".......Inside sync function......." + moduleId);

        Criteria criteria = new Criteria();
        Query query = new Query();
        criteria.and("userId").is(userId);
        criteria.and("context.type").is(EntityType.MODULE);
        criteria.and("actionType").is(UserActionType.COMPLETED);
        query.addCriteria(criteria);
        List<EntityUserActionMapping> entitiesUserActionMapping = mongoTemplate.find(query, EntityUserActionMapping.class);

        List<SrcEntity> gobalEntities = new ArrayList<SrcEntity>();

        if (!(entitiesUserActionMapping).isEmpty()) {
            for (EntityUserActionMapping entityUserActionMapping : entitiesUserActionMapping) {
                gobalEntities.add(entityUserActionMapping.target);
            }
        }

        if (!(entities).isEmpty()) {
            logger.debug(".......Entities not null.......");
            for (SrcEntity entity : entities) {
                if ((gobalEntities).isEmpty() || !gobalEntities.contains(entity)) {
                    logger.debug(".......Inside if statement.......");
                    addEntityUserActionMapping(userId, UserActionType.COMPLETED, entity,
                            new SrcEntity(EntityType.MODULE, moduleId), false);
                }
            }
        }
        // if (!CollectionUtils.isEmpty(gobalEntities)) {
        // gobalEntities.removeAll(entities);
        // }
        return gobalEntities;
    }

    public EntityUserActionMapping addEntityUserActionMapping(String userId, UserActionType actionType, SrcEntity target,
                                                              SrcEntity context, boolean allowDuplicates) throws VedantuException {
        Criteria criteria = new Criteria();
        Query query = new Query();
        criteria.and(ConstantsGlobal.USER_ID).is(userId);
        criteria.and(ConstantsGlobal.ACTION_TYPE).is(actionType);
        criteria.and(ConstantsGlobal.TARGET).is(target);
        if (context != null) {
            criteria.and(ConstantsGlobal.CONTEXT).is(context);
        }
        query.addCriteria(criteria);
        //Need to test
        EntityUserActionMapping userActionMapping = mongoTemplate.findOne(query, EntityUserActionMapping.class);

        if (userActionMapping != null && !allowDuplicates) {
            throw new VedantuException(alreadyPresentErrorCodeMap.get(actionType));
        }
        userActionMapping = new EntityUserActionMapping(userId, actionType, target, context);
        logger.debug("saving userActionMapping : " + userActionMapping);

        entityUserActionMappingRepo.save(userActionMapping);
        return userActionMapping;
    }

    @Override
    public VedantuResponse getModule(GetModuleReq getModuleReq) {
        GetModuleRes getModuleRes = null;
        try {
            getModuleRes = getModuleRes(getModuleReq);
        } catch (VedantuException e) {
            throw e;
        }
        return new VedantuResponse(getModuleRes);
    }

    public GetModuleRes getModuleRes(GetModuleReq request) throws VedantuException {

        Optional<Module> moduleOptional = moduleRepo.findById(request.id);
        if (!moduleOptional.isPresent()) {
            throw new VedantuException(VedantuErrorCode.MODULE_NOT_FOUND);
        }
        Module module = moduleOptional.get();
        if (module.recordState == VedantuRecordState.TEMPORARY) {
            throw new VedantuException(VedantuErrorCode.CONTENT_IN_TEMPORARY_STAGE);
        }
        GetModuleRes moduleRes = new GetModuleRes();
        moduleRes.fromMongoModel(module);
        logger.info("videoInfo: " + moduleRes);
        moduleRes = (GetModuleRes) annotateExtraInfo(request.userId, module.contentSrc != null
                        && module.contentSrc.type == EntityType.ORGANIZATION ? module.contentSrc.id : null,
                EntityType.VIDEO, moduleRes);

        List<ModuleEntryInfo> moduleEntryInfos = new ArrayList<ModuleEntryInfo>();
        if (module.children != null) {
            for (ModuleEntry child : module.children) {
                logger.debug("..... Inside the loop.......");
                ModuleEntryInfo moduleEntryInfo = new ModuleEntryInfo();
                moduleEntryInfo.name = child.name;
                moduleEntryInfo.entity = child.entity;

                if (child.entity != null) {
                    logger.debug("..... Entity is not null.......");
                    moduleEntryInfo.completionRule = child.completionRule;
                    moduleEntryInfo.completed =
                            getUserModuleEntryStatus(request.userId, child.entity, new SrcEntity(
                                    EntityType.MODULE, module._getStringId()));
                    moduleEntryInfo.info = getEntity(child.entity.type, child.entity.id);
                    // moduleEntryInfo.info = dao.getBasicInfo(child.entity.id);
                    if (child.entity.type == EntityType.ASSIGNMENT
                            || child.entity.type == EntityType.TEST) {

                        UserEntityAttempt entityAttempt = getAttempt(
                                request.userId, child.entity.type, child.entity.id);
                        moduleEntryInfo.attempted = (entityAttempt != null);
                    }
                    logger.debug(".....Name....." + moduleEntryInfo.name);
                }
                moduleEntryInfos.add(moduleEntryInfo);
            }
            logger.debug("..... Outside the loop.......");
        }
        moduleRes.moduleEntryInfos = moduleEntryInfos;
        return moduleRes;
    }

    public boolean getUserModuleEntryStatus(String userId, SrcEntity target, SrcEntity context)
            throws VedantuException {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ConstantsGlobal.USER_ID).is(userId);
        criteria.and(ConstantsGlobal.ACTION_TYPE).is(UserActionType.COMPLETED);
        criteria.and(ConstantsGlobal.TARGET).is(target);
        criteria.and(ConstantsGlobal.CONTEXT).is(context);
        query.addCriteria(criteria);

        EntityUserActionMapping userActionMapping = mongoTemplate.findOne(query, EntityUserActionMapping.class);
        return (userActionMapping != null);
    }

    public UserEntityAttempt getAttempt(String userId, EntityType entityType, String entityId) {

        logger.debug("getAttempt userId: " + userId + ", entityType: " + entityType + ", entityId"
                + entityId);
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ConstantsGlobal.USER_ID).is(userId);
        criteria.and("entity.id").is(entityId);
        query.addCriteria(criteria);
        UserEntityAttempt userEntityAttempt = mongoTemplate.findOne(query, UserEntityAttempt.class);
        logger.info("getAttempt userEntityAttempt: " + userEntityAttempt);

        return userEntityAttempt;
    }

    private ModelBasicInfo getEntity(EntityType type, String id) {
        if (type == EntityType.VIDEO) {
            Optional<Video> videoOptional = videoRepo.findById(id);
            Video video = videoOptional.get();
            VideoInfo videoInfo = video.toBasicInfo();
            videoInfo.thumbnailURL = getEntityThumbnail(EntityType.VIDEO, video.thumbnail);
            String url = video.linkType == SrcType.LinkType.UPLOADED ? getEntityVideoURL(
                    EntityType.VIDEO, video.uuid, video.extension, video.converted ? FileCategory.CONVERTED
                            : FileCategory.ORIGINAL) : video.url;
            videoInfo.url = url;
            ModelBasicInfo res = videoInfo;
            return res;
        } else if (type == EntityType.DOCUMENT) {
            Optional<Documents> documentsOptional = documentsRepo.findById(id);
            Documents documents = documentsOptional.get();
            return documents.toBasicInfo();
        }
        return null;
    }

    @Override
    public VedantuResponse getModules(GetModulesReq request) {

        SearchListResponse<GetModuleRes> results = getEntityInfos(request, EntityType.MODULE,
                GetModuleRes.class, null);
        annotateExtraInfo(request.userId, request.orgId, EntityType.MODULE, results.list);
        return new VedantuResponse(results);
    }

    public SearchListResponse<GetModuleRes> getModulesForContentResponce(GetModulesReq request) {

        SearchListResponse<GetModuleRes> results = getEntityInfos(request, EntityType.MODULE,
                GetModuleRes.class, null);
        annotateExtraInfo(request.userId, request.orgId, EntityType.MODULE, results.list);
        return results;
    }
}

