package com.vedantu.content.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.content.interfaces.IContentManager;
import com.vedantu.commons.entity.factory.EntityTypeContentManagerFactory;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.pojos.DownloadableFileInfo;
import com.vedantu.commons.pojos.ScheduleInfo;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.SearchListResponse;
import com.vedantu.content.daos.LibraryContentLinksDAO;
import com.vedantu.content.daos.ModuleDAO;
import com.vedantu.content.daos.ModuleSchedulesDAO;
import com.vedantu.content.daos.UserModuleStatusDAO;
import com.vedantu.content.daos.analytics.UserEntityAttemptDAO;
import com.vedantu.content.models.AbstractContentModel;
import com.vedantu.content.models.Module;
import com.vedantu.content.models.ModuleEntry;
import com.vedantu.content.models.ModuleScheduleInfo;
import com.vedantu.content.models.ModuleSchedules;
import com.vedantu.content.models.analytics.UserEntityAttempt;
import com.vedantu.content.pojos.ModuleEntryInfo;
import com.vedantu.content.pojos.requests.GetModuleReq;
import com.vedantu.content.pojos.requests.GetModuleScheduleReq;
import com.vedantu.content.pojos.requests.GetModulesReq;
import com.vedantu.content.pojos.requests.GetModulesReq1;
import com.vedantu.content.pojos.requests.GetUserModuleReq;
import com.vedantu.content.pojos.requests.GetUserModulesReq;
import com.vedantu.content.pojos.requests.SyncModuleReq;
import com.vedantu.content.pojos.requests.UpdateModuleEntryReq;
import com.vedantu.content.pojos.requests.UpdateModuleReq;
import com.vedantu.content.pojos.requests.UpdateUserModuleReq;
import com.vedantu.content.pojos.responses.GetModuleRes;
import com.vedantu.content.pojos.responses.GetModuleScheduleRes;
import com.vedantu.content.pojos.responses.GetModulesRes;
import com.vedantu.content.pojos.responses.GetUserModuleRes;
import com.vedantu.content.pojos.responses.GetUserModulesRes;
import com.vedantu.content.pojos.responses.ModuleAccessStatus;
import com.vedantu.content.pojos.responses.ModuleContentAccessStatus;
import com.vedantu.content.pojos.responses.ModuleInfo;
import com.vedantu.content.pojos.responses.SyncModuleRes;
import com.vedantu.content.pojos.responses.UpdateUserModuleRes;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.user.pojos.EntityUserActionDAO;

public class ModuleManager extends AbstractContentManager {

    private static final ALogger LOGGER   = Logger.of(ModuleManager.class);
    public static ModuleManager  INSTANCE = new ModuleManager();

    public boolean updateModule(UpdateModuleReq request) throws VedantuException {

        try {
            LOGGER.debug("......Inside create module........");

            Module module = ModuleDAO.INSTANCE.updateModule(request.id, request.name,
                    request.moduleRun, request.tags, request.brdIds, request.targetIds,
                    request.prerequsiteModuleId, request.updateList);
            SrcEntity entity = new SrcEntity(EntityType.MODULE, module._getStringId());

            LibraryContentLinksDAO.INSTANCE.updateLastUpdated(entity);
            generateEventAysc(request.userId, module, EventActionType.UPDATE,
                    EventType.INDEX_MODULE, UserActionType.UPDATED, false);
            return true;

        } catch (VedantuException exception) {
            throw exception;
        } catch (Exception exception) {
            LOGGER.error(" Error", exception);

            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, exception);
        }
    }

    public boolean updateModuleEntry(UpdateModuleEntryReq request) throws VedantuException {

        try {
            LOGGER.debug("......Inside update moduleEntry........");// +
                                                                    // request.children.indexOf(0).);

            Module module = ModuleDAO.INSTANCE.updateModuleEntry(request.moduleId, request.pos,
                    request.name, request.completionRule);

            SrcEntity entity = new SrcEntity(EntityType.MODULE, module._getStringId());

            LibraryContentLinksDAO.INSTANCE.updateLastUpdated(entity);
            generateEventAysc(request.userId, module, EventActionType.UPDATE,
                    EventType.INDEX_MODULE, UserActionType.UPDATED, false);
            return true;

        } catch (VedantuException exception) {
            throw exception;
        } catch (Exception exception) {
            LOGGER.error(" Error", exception);

            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, exception);
        }

    }

    public UpdateUserModuleRes updateUserModuleStatus(UpdateUserModuleReq updateUserModuleReq)
            throws VedantuException {

        boolean isSuccessful = UserModuleStatusDAO.INSTANCE.update(updateUserModuleReq.userId,
                updateUserModuleReq.moduleId, updateUserModuleReq.moduleEntry);

        UpdateUserModuleRes response = new UpdateUserModuleRes();
        response.isSuccessful = isSuccessful;
        return response;
    }

    public GetUserModuleRes getUserModuleStatus(GetUserModuleReq getUserModuleReq)
            throws VedantuException {

        List<ModuleContentAccessStatus> moduleContentsAccessStatus = UserModuleStatusDAO.INSTANCE
                .get(getUserModuleReq.userId, getUserModuleReq.moduleId);

        GetUserModuleRes response = new GetUserModuleRes();
        response.contentAcccessStatus = moduleContentsAccessStatus;
        return response;
    }

    public GetUserModulesRes getUserModulesStatus(GetUserModulesReq getUserModulesReq)
            throws VedantuException {

        List<ModuleAccessStatus> modulesAccessStatus = UserModuleStatusDAO.INSTANCE
                .getModulesStatus(getUserModulesReq.userId, getUserModulesReq.moduleIds);

        GetUserModulesRes response = new GetUserModulesRes();
        response.modulesAccessStatus = modulesAccessStatus;
        return response;
    }

    public GetModulesRes getModules(GetModulesReq1 getModulesReq) throws VedantuException {

        List<ModuleInfo> modules = UserModuleStatusDAO.INSTANCE.getModules(getModulesReq.orgId);
        LOGGER.debug(".......Inside getModules manager function......." + modules.size());
        GetModulesRes response = new GetModulesRes();
        response.modules = modules;
        return response;
    }

    public GetModuleRes getModule(GetModuleReq request) throws VedantuException {

        Module module = ModuleDAO.INSTANCE.getById(request.id);
        if (module == null) {
            throw new VedantuException(VedantuErrorCode.MODULE_NOT_FOUND);
        }
        if (module.recordState == VedantuRecordState.TEMPORARY) {
            throw new VedantuException(VedantuErrorCode.CONTENT_IN_TEMPORARY_STAGE);
        }
        GetModuleRes moduleRes = new GetModuleRes();
        moduleRes.fromMongoModel(module);
        LOGGER.info("videoInfo: " + moduleRes);
        moduleRes = (GetModuleRes) annotateExtraInfo(request.userId, module.contentSrc != null
                && module.contentSrc.type == EntityType.ORGANIZATION ? module.contentSrc.id : null,
                EntityType.VIDEO, moduleRes);

        List<ModuleEntryInfo> moduleEntryInfos = new ArrayList<ModuleEntryInfo>();
        if (module.children != null) {
            for (ModuleEntry child : module.children) {
                LOGGER.debug("..... Inside the loop.......");
                ModuleEntryInfo moduleEntryInfo = new ModuleEntryInfo();
                moduleEntryInfo.name = child.name;
                moduleEntryInfo.entity = child.entity;

                if (child.entity != null) {
                    LOGGER.debug("..... Entity is not null.......");
                    moduleEntryInfo.completionRule = child.completionRule;
                    moduleEntryInfo.completed = EntityUserActionDAO.INSTANCE
                            .getUserModuleEntryStatus(request.userId, child.entity, new SrcEntity(
                                    EntityType.MODULE, module._getStringId()));
                    @SuppressWarnings("rawtypes")
                    VedantuBasicDAO dao = EntityTypeDAOFactory.INSTANCE.get(child.entity.type);
                    moduleEntryInfo.info = dao.getBasicInfo(child.entity.id);
                    if (child.entity.type == EntityType.ASSIGNMENT
                            || child.entity.type == EntityType.TEST) {

                        UserEntityAttempt entityAttempt = UserEntityAttemptDAO.INSTANCE.getAttempt(
                                request.userId, child.entity.type, child.entity.id);
                        moduleEntryInfo.attempted = (entityAttempt != null);
                    }
                    LOGGER.debug(".....Name....." + moduleEntryInfo.name);
                }
                moduleEntryInfos.add(moduleEntryInfo);
            }
            LOGGER.debug("..... Outside the loop.......");
        }
        moduleRes.moduleEntryInfos = moduleEntryInfos;
        return moduleRes;
    }

    public static SyncModuleRes syncModule(SyncModuleReq syncModuleReq) throws VedantuException {

        LOGGER.debug("..........Inside syncModule fumction in ModuleManager........");
        List<SrcEntity> entities = EntityUserActionDAO.INSTANCE.sync(syncModuleReq.userId,
                syncModuleReq.moduleId, syncModuleReq.entities);

        SyncModuleRes response = new SyncModuleRes();
        response.userId = syncModuleReq.userId;
        response.moduleId = syncModuleReq.moduleId;
        response.entities = entities;
        return response;
    }

    public static GetModuleScheduleRes getModuleSchedules(GetModuleScheduleReq request) throws VedantuException{
        GetModuleScheduleRes response = new GetModuleScheduleRes();
        Map<String,ModuleScheduleInfo> schedules = new HashMap<String, ModuleScheduleInfo>();
        SrcEntity globalSource = new SrcEntity(EntityType.MODULE, request.moduleId);
        SrcEntity target = new SrcEntity(EntityType.SECTION, request.sectionId);
        List<ModuleSchedules> moduleScheduleInfos = ModuleSchedulesDAO.INSTANCE.getGlobalSchedule(target, globalSource);
        for(ModuleSchedules moduleSchedule : moduleScheduleInfos){
            schedules.put(moduleSchedule.globalEntity.id, getModuleScheduleData(moduleSchedule.schedule));
        }
        response.schedules = schedules;
        return response;
    }

    private static ModuleScheduleInfo getModuleScheduleData(ScheduleInfo schedule) {
       ModuleScheduleInfo moduleSchedule = new ModuleScheduleInfo();
       if (schedule.startTime != null) {
           moduleSchedule.startTime = schedule.startTime;
           moduleSchedule.startsIn = moduleSchedule.startTime.getTime() - System.currentTimeMillis();
       }
       if (schedule.endTime != null) {
           moduleSchedule.endTime = schedule.endTime;
           moduleSchedule.endsIn = moduleSchedule.endTime.getTime() - System.currentTimeMillis();
       }
       if (schedule.closeTime != null) {
           moduleSchedule.closeTime = schedule.closeTime;
           moduleSchedule.closesIn = moduleSchedule.closeTime.getTime() - System.currentTimeMillis();
       }
       return moduleSchedule;
    }

    public SearchListResponse<GetModuleRes> getModules(GetModulesReq request)
            throws VedantuException {

        SearchListResponse<GetModuleRes> results = getEntityInfos(request, EntityType.MODULE,
                GetModuleRes.class, null);
        annotateExtraInfo(request.userId, request.orgId, EntityType.MODULE, results.list);
        return results;
    }

    @Override
    public boolean calculate(String id,boolean recalculate, VedantuBaseMongoModel... contents) throws VedantuException {

        List<Module> modules = new ArrayList<Module>();

        if (StringUtils.isNotEmpty(id)) {
            Module module = ModuleDAO.INSTANCE.getById(id);

            if (module == null) {
                return false;
            }
            modules.add(module);
        }

        if (contents != null && contents.length != 0) {
            for (VedantuBaseMongoModel content : contents) {
                if (content instanceof Module) {
                    modules.add((Module) content);
                }
            }
        }

        // calculate question image size;

        for (Module module : modules) {
            if( module.size.isFinalized() && !recalculate){
                continue;
            }

            module.size.reset();

            if (CollectionUtils.isNotEmpty(module.children)) {
                for (ModuleEntry entry : module.children) {
                    if (entry.entity != null) {
                        SrcEntity moduleEntity = entry.entity;
                        LOGGER.debug("Module entry " + moduleEntity);

                        VedantuBasicDAO<?, ?> dao = EntityTypeDAOFactory.INSTANCE
                                .get(moduleEntity.type);
                        if (dao == null) {
                            continue;
                        }

                        VedantuBaseMongoModel model = dao.getById(moduleEntity.id);
                        IContentManager manager = EntityTypeContentManagerFactory.INSTANCE
                                .get(moduleEntity.type);

                        if (manager != null && model instanceof AbstractContentModel) {

                            AbstractContentModel contentModel = (AbstractContentModel) model;

                            if (!contentModel.size.isFinalized()) {
                                manager.calculate(null,false, model);

                            }
                            module.size.addOriginal(contentModel.getExportableSize());
                        }

                    }

                }
            }

            module.size.finalize();
            ModuleDAO.INSTANCE.updateModel(module, Arrays.asList(Module.SIZE));

        }
        return true;
    }

    @Override
    public List<DownloadableFileInfo> getFiles(EntityType entityType, String entityId)
            throws VedantuException {

        List<DownloadableFileInfo> fileInfos = new ArrayList<DownloadableFileInfo>();
        List<SrcEntity> children = ModuleDAO.INSTANCE.getChildren(entityId);

        if (CollectionUtils.isNotEmpty(children)) {
            for (SrcEntity child : children) {
                LOGGER.debug("SrcEntity " + child);
                IContentManager manager = EntityTypeContentManagerFactory.INSTANCE.get(child.type);
                fileInfos.addAll(manager.getFiles(child.type, child.id));
            }
        }
        return fileInfos;
    }
}