package com.vedantu.cmds.daos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.vedantu.cmds.enums.PublishedStatus;
import com.vedantu.cmds.mgmt.interfaces.ICMDSResource;
import com.vedantu.cmds.models.CMDSContentLink;
import com.vedantu.cmds.models.CMDSModule;
import com.vedantu.cmds.models.CMDSVideo;
import com.vedantu.cmds.models.event.search.details.CMDSResourceDetails;
import com.vedantu.cmds.pojos.requests.slpmodules.CMDSModuleInfo;
import com.vedantu.cmds.pojos.responses.slpmodules.CMDSModuleNameInfo;
import com.vedantu.cmds.pojos.responses.slpmodules.ModuleEntryInfo;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.content.interfaces.IPublishable;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.daos.AssignmentDAO;
import com.vedantu.content.daos.DocumentDAO;
import com.vedantu.content.daos.FileDAO;
import com.vedantu.content.daos.ModuleDAO;
import com.vedantu.content.daos.TestDAO;
import com.vedantu.content.daos.VideoDAO;
import com.vedantu.content.models.ModuleEntry;
import com.vedantu.content.models.ModuleRun;
import com.vedantu.content.pojos.ModuleEntryCompletionRule;
import com.vedantu.content.search.details.ModuleSearchIndexDetails;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;

public class CMDSModuleDAO extends CmdsContentDAO<CMDSModule, ObjectId> implements ICMDSResource,
        IPublishable {

    private static final ALogger      LOGGER   = Logger.of(CMDSModuleDAO.class);
    public static final CMDSModuleDAO INSTANCE = new CMDSModuleDAO();

    public CMDSModuleDAO() {

        super(CMDSModule.class);
    }

    public CMDSModule createModule(String userId, String orgId, ModuleRun moduleRun, String name,
            List<String> tags, List<String> brdIds, List<String> targetIds,
            String prerequsiteModuleId) throws VedantuException {

        LOGGER.debug("..... Inside create Module DAO function.......");
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
        LOGGER.debug(".... about to save the module.....");
        save(module);
        LOGGER.debug(".... module is saved.....");

        return module;
    }

    public CMDSModuleInfo getModuleInfo(CMDSModule module, List<CMDSContentLink> contentLinks) throws VedantuException {

        LOGGER.debug("..... Inside get Module DAO function.......");
        LOGGER.debug("..... Module is InitialisedgetModuleById.......");
        String orgId = (module.contentSrc != null) ? module.contentSrc.id : StringUtils.EMPTY;
        CMDSModuleInfo moduleInfo = new CMDSModuleInfo(module._getStringId(), module.name,
                EntityType.CMDSMODULE, orgId, module.timeCreated, module.lastUpdated,
                module.userId, 0, module.published, module.completed, true, module.globalModuleId,
                module.moduleRun, module.recordState, new ArrayList<ModuleEntryInfo>(),
                module.prerequsiteModuleId);

        Set<String> downloadableEntities = new HashSet<String>();
        for(CMDSContentLink link : contentLinks){
            for(SrcEntity entity : link.getDownloadableEntities()){
                if(entity.type == EntityType.VIDEO){
                    downloadableEntities.add(VideoDAO.INSTANCE.getById(entity.id).getCmdsVideoId());
                }else if(entity.type == EntityType.TEST){
                    downloadableEntities.add(TestDAO.INSTANCE.getById(entity.id).cmdsTestId);
                }else if(entity.type == EntityType.DOCUMENT){
                    downloadableEntities.add(DocumentDAO.INSTANCE.getById(entity.id).getCMDSDocId());
                }else if(entity.type == EntityType.ASSIGNMENT){
                    downloadableEntities.add(AssignmentDAO.INSTANCE.getById(entity.id).cmdsId);
                }else if(entity.type == EntityType.FILE){
                    downloadableEntities.add(FileDAO.INSTANCE.getById(entity.id).getCMDSFileId());
                }
            }
        }

        LOGGER.debug("..... ModuleInfo Initialised.......");

        if (module.children != null) {
            for (ModuleEntry child : module.children) {
                LOGGER.debug("..... Inside the loop.......");
                ModuleEntryInfo moduleEntryInfo = new ModuleEntryInfo();
                moduleEntryInfo.name = child.name;
                moduleEntryInfo.completionRule = child.completionRule;
                moduleEntryInfo.entity = child.entity;
                if (child.entity != null) {
                    LOGGER.debug("..... Entity is not null.......");
                    @SuppressWarnings("rawtypes")
                    VedantuBasicDAO dao = EntityTypeDAOFactory.INSTANCE.get(child.entity.type);
                    moduleEntryInfo.info = dao.getBasicInfo(child.entity.id);
                    moduleEntryInfo.downloadState = downloadableEntities.contains(child.entity.id) ? "ENABLED" : "DISABLED";
                }
                moduleInfo.children.add(moduleEntryInfo);
            }
            LOGGER.debug("..... Outside the loop.......");
        }

        return moduleInfo;
    }

    public CMDSModule updateModule(String id, String name, ModuleRun moduleRun, List<String> tags,
            List<String> brdIds, List<String> targetIds, String prerequsiteModuleId,
            List<String> updateList) throws VedantuException {

        LOGGER.debug("..... Inside update Module DAO function.......");
        CMDSModule module = getModuleById(id);

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
        LOGGER.debug(".... about to save the module.....");
        save(module);
        LOGGER.debug(".... module is saved.....");
        return module;
    }

    public CMDSModule addModuleEntries(String id, List<ModuleEntry> moduleEntries, int pos)
            throws VedantuException {

        LOGGER.debug("..... Inside update Module DAO function.......");
        CMDSModule module = getModuleById(id);

        // //TODO
        // if (module.published == true) {
        //     throw new VedantuException(VedantuErrorCode.ALREADY_PUBLISHED,
        //             "module is published already");
        // }

        if (module.children == null) {
            module.children = new ArrayList<ModuleEntry>();
        }

        if (pos > module.children.size() || pos < -1) {
            throw new VedantuException(VedantuErrorCode.INVALID_POSITION);
        }

        if (moduleEntries != null) {
            if (pos == -1) {
                pos = module.children.size();
            }
            int addedEntryPosition = pos;
            for (ModuleEntry moduleEntry : moduleEntries) {
                if (!module.children.contains(moduleEntry)) {
                    LOGGER.debug(".... inside if statement.....");
                    module.children.add(addedEntryPosition, moduleEntry);
                    addedEntryPosition++;
                }
            }
        }
        module.completed = isReadyToPublished(module);
        LOGGER.debug(".... about to save the module.....");
        save(module);
        LOGGER.debug(".... module is saved.....");
        return module;
    }

    public CMDSModule moveModuleEntry(String id, int oldPos, int pos) throws VedantuException {

        LOGGER.debug("..... Inside update Module DAO function.......");
        CMDSModule module = getModuleById(id);

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
        LOGGER.debug(".... about to save the module.....");
        save(module);
        LOGGER.debug(".... module is saved.....");

        return module;

    }

    public CMDSModule updateModuleEntry(String moduleId, int pos, String name,
            ModuleEntryCompletionRule completionRule) throws VedantuException {

        LOGGER.debug("..... Inside update Module DAO function.......");
        CMDSModule module = getModuleById(moduleId);

        if (module.children == null) {
            throw new VedantuException(VedantuErrorCode.CONTENT_LIST_EMPTY);
        }

        if (pos >= module.children.size() || pos < 0) {
            throw new VedantuException(VedantuErrorCode.INVALID_POSITION);
        }

        LOGGER.debug(".... about to save the module.....");
        if (!StringUtils.isEmpty(name)) {
            module.children.get(pos).name = name;
        }
        if (completionRule != null) {
            module.children.get(pos).completionRule = completionRule;
        }

        module.completed = isReadyToPublished(module);
        save(module);
        LOGGER.debug(".... module is saved.....");

        return module;
    }

    public CMDSModule deleteModule(String id) throws VedantuException {

        LOGGER.debug("..... Inside delete Module DAO function.......");
        CMDSModule module = getById(id);
        if (module == null) {
            throw new VedantuException(VedantuErrorCode.MODULE_DOES_NOT_EXISTS);
        }
        module.recordState = VedantuRecordState.DELETED;
        LOGGER.debug(".... about to save the module.....");
        save(module);
        LOGGER.debug(".... module is saved.....");
        return module;
    }

    public CMDSModule deleteModuleEntry(String moduleId, int pos) throws VedantuException {

        CMDSModule module = getById(moduleId);
        if (module == null) {
            throw new VedantuException(VedantuErrorCode.MODULE_DOES_NOT_EXISTS);
        }

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
        LOGGER.debug(".... about to save the module.....");
        save(module);
        return module;
    }

    public List<CMDSModuleNameInfo> getCMDSModules(String orgId, PublishedStatus publishedStatus,
            int start, int size) throws VedantuException {

        LOGGER.debug(".......Inside getModules function......." + start + size);
        Query<CMDSModule> query = getDS().find(CMDSModule.class)
                .filter("contentSrc.type", EntityType.ORGANIZATION).filter("contentSrc.id", orgId);

        if (publishedStatus != null) {
            if (publishedStatus == PublishedStatus.PUBLISHED) {
                query = query.filter("published", true);
            }
            if (publishedStatus == PublishedStatus.NOT_PUBLISHED) {
                query = query.filter("published", false);
            }
            query = query.offset(start).limit(size);
        }

        List<CMDSModule> modules = query.asList();
        LOGGER.debug(".......Inside getModules function......." + size);
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
        LOGGER.debug(".......Exiting getModules function......." + size);
        return infos;
    }

    public CMDSModule findById(String orgId, String moduleId) {

        // TODO business logic requirement
        CMDSModule module = getDS().find(CMDSModule.class).filter("id", new ObjectId(moduleId))
                .filter("contentSrc.id", orgId).get();

        return module;
    }

    public CMDSModule getModuleById(String moduleId) throws VedantuException {

        LOGGER.debug("inside getModuleById function" + moduleId);
        CMDSModule module = getById(moduleId);
        if (module == null) {
            LOGGER.debug("module is null");
            throw new VedantuException(VedantuErrorCode.MODULE_DOES_NOT_EXISTS,
                    "no module found with id : " + moduleId);
        }

        return module;
    }

    // public CMDSModule getRootModule(String orgId) {
    // LOGGER.debug(".....inside getRootModule dao function......");
    // Query<CMDSModule> query = ds.createQuery(CMDSModule.class);
    //
    // List<Criteria> list = new ArrayList<Criteria>();
    // list.add(query.criteria("orgId").equal(orgId));
    // list.add(query.criteria("parent").doesNotExist());
    // query.and(list.toArray(new Criteria[] {}));
    // return query.get();
    // }

    // @Override
    // public WriteResult deleteByQuery(Query<CMDSModule> arg0) {
    //
    // // TODO Auto-generated method stub
    // return null;
    // }

    @Override
    public CMDSResourceDetails getCMDSResourceDetails(VedantuBaseMongoModel model) {

        CMDSModule cmdsModule = (CMDSModule) model;
        CMDSResourceDetails details = new CMDSResourceDetails();

        details.fromMongoModel(model);
        details.content = new SrcEntity(EntityType.CMDSMODULE, model._getStringId());
        details.queryContext = cmdsModule.name;

        return details;
    }

    @Override
    public VedantuBaseMongoModel getPublishedEntity(String id) throws VedantuException {

        CMDSModule cmdsModule = CMDSModuleDAO.INSTANCE.getById(id);
        if (cmdsModule != null) {
            return ModuleDAO.INSTANCE.getById(cmdsModule.globalModuleId);
        }
        return null;
    }

    @Override
    public SrcEntity getGlobalEntity(String id) {

        CMDSModule module = getById(id);
        return new SrcEntity(EntityType.MODULE, module.globalModuleId);
    }

    @Override
    public boolean isPublished(String id) {

        CMDSModule module = getById(id);
        return module.published;
    }

    @Override
    public boolean isPublished(VedantuBaseMongoModel cmdsModel) {

        if (cmdsModel instanceof CMDSModule) {
            CMDSModule module = (CMDSModule) cmdsModel;
            return module.published;
        }

        return false;
    }

    @Override
    public List<SrcEntity> getChildren(String id) {

        LOGGER.debug("..... inside function getPublishable children.....");
        CMDSModule module = getById(id);
        List<SrcEntity> childEntities = new ArrayList<SrcEntity>();
        if (CollectionUtils.isNotEmpty(module.children)) {
            for (ModuleEntry child : module.children) {
                if (child != null && child.entity != null) {
                    childEntities.add(child.entity);
                }
            }
        }
        return childEntities;
    }

    @Override
    public boolean isReadyToPublished(String id) throws VedantuException {

        CMDSModule module = CMDSModuleDAO.INSTANCE.getById(id);
        return (module.completed);
    }

    public boolean updateModuleStatus(SrcEntity entity) throws VedantuException {

        List<CMDSModule> modules = getDS().find(CMDSModule.class)
                .filter("children.entity.type", entity.type)
                .filter("children.entity.id", entity.id).asList();

        if (!CollectionUtils.isEmpty(modules)) {
            for (CMDSModule module : modules) {
                module.completed = false;
                updateModel(module, Arrays.asList(CMDSModule.COMPLETED));
            }
        }
        return true;

    }

    @Override
    public boolean isReadyToPublished(VedantuBaseMongoModel cmdsModel) throws VedantuException {

        LOGGER.debug("...........Before loop......");
        if (cmdsModel instanceof CMDSModule) {

            CMDSModule module = (CMDSModule) cmdsModel;
            boolean canBePublished = true;
            if (canBePublished && (module.recordState != VedantuRecordState.ACTIVE)) {
                canBePublished &= false;
                LOGGER.debug(" Module is not in active state id:" + module._getStringId());
            }
            if (canBePublished && (CollectionUtils.isEmpty(module.boardIds))) {
                canBePublished &= false;
                LOGGER.debug(" No boards provided for Module id:" + module._getStringId());
            }

            boolean entityPresent = false;
            boolean allContentsComplete = true;
            List<ModuleEntry> children = module.children;

            LOGGER.debug("...........Before loop......");
            if (!CollectionUtils.isEmpty(children)) {
                for (ModuleEntry child : children) {
                    LOGGER.debug(" Inside for loop");
                    if (child.entity != null) {
                        LOGGER.debug("Inside if function");
                        VedantuBasicDAO basicDao = EntityTypeDAOFactory.INSTANCE
                                .get(child.entity.type);
                        if (basicDao instanceof CmdsContentDAO) {
                            LOGGER.debug("Inside inner if function");
                            if (!((CmdsContentDAO) basicDao).isReadyToPublished(child.entity.id)) {
                                LOGGER.debug("...... not ready.......");
                                allContentsComplete = false;
                                break;
                            }

                        }

                        entityPresent = true;
                    }
                }
            }
            LOGGER.debug(".........After loop......");
            canBePublished &= entityPresent;
            canBePublished &= allContentsComplete;
            return canBePublished;
        }
        return false;
    }

    @Override
    public boolean deleteByModel(VedantuBaseMongoModel model) throws VedantuException {

        if (!(model instanceof CMDSModule)) {
            return false;
        }

        CMDSModule module = (CMDSModule) model;

        if (module.published == true || module.globalModuleId != null) {
            throw new VedantuException(VedantuErrorCode.ALREADY_PUBLISHED);
        }
        super.markDeleted(module);
        updateModel(module, Arrays.asList(ConstantsGlobal.RECORD_STATE));

        return true;

    }

}
