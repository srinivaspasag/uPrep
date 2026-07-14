package com.vedantu.cmds.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.daos.CMDSModuleDAO;
import com.vedantu.cmds.daos.CmdsContentLinkDAO;
import com.vedantu.cmds.enums.CmdsContentLinkType;
import com.vedantu.cmds.models.CMDSContentLink;
import com.vedantu.cmds.models.CMDSFile;
import com.vedantu.cmds.models.CMDSModule;
import com.vedantu.cmds.pojos.requests.slpmodules.AddModuleEntryReq;
import com.vedantu.cmds.pojos.requests.slpmodules.CMDSModuleInfo;
import com.vedantu.cmds.pojos.requests.slpmodules.CreateModuleReq;
import com.vedantu.cmds.pojos.requests.slpmodules.DeleteModuleEntryReq;
import com.vedantu.cmds.pojos.requests.slpmodules.DeleteModuleReq;
import com.vedantu.cmds.pojos.requests.slpmodules.GetCMDSModulesReq;
import com.vedantu.cmds.pojos.requests.slpmodules.GetModuleInfoReq;
import com.vedantu.cmds.pojos.requests.slpmodules.MoveModuleEntryReq;
import com.vedantu.cmds.pojos.responses.AddModuleRes;
import com.vedantu.cmds.pojos.responses.slpmodules.CMDSModuleNameInfo;
import com.vedantu.cmds.pojos.responses.slpmodules.CreateModuleRes;
import com.vedantu.cmds.pojos.responses.slpmodules.DeleteModuleEntryRes;
import com.vedantu.cmds.pojos.responses.slpmodules.DeleteModuleRes;
import com.vedantu.cmds.pojos.responses.slpmodules.GetCMDSModulesRes;
import com.vedantu.cmds.pojos.responses.slpmodules.GetModuleInfoRes;
import com.vedantu.cmds.pojos.responses.slpmodules.MoveModuleEntryRes;
import com.vedantu.cmds.pojos.responses.slpmodules.ScheduleRes;
import com.vedantu.cmds.pojos.responses.slpmodules.UpdateModuleEntryRes;
import com.vedantu.cmds.pojos.responses.slpmodules.UpdateModuleRes;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.content.interfaces.IContentManager;
import com.vedantu.commons.entity.factory.EntityTypeContentManagerFactory;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.pojos.ScheduleInfo;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.daos.ModuleSchedulesDAO;
import com.vedantu.content.managers.ModuleManager;
import com.vedantu.content.models.AbstractContentModel;
import com.vedantu.content.models.ModuleEntry;
import com.vedantu.content.models.ModuleSchedules;
import com.vedantu.content.pojos.requests.ModuleScheduleReq;
import com.vedantu.content.pojos.requests.SyncModuleReq;
import com.vedantu.content.pojos.requests.UpdateModuleEntryReq;
import com.vedantu.content.pojos.requests.UpdateModuleReq;
import com.vedantu.content.pojos.responses.SyncModuleRes;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.user.pojos.EntityUserActionDAO;

public class CMDSModuleManager extends AbstractCMDSContentManager {

    static final ALogger            LOGGER   = Logger.of(CMDSModuleManager.class);
    public static CMDSModuleManager INSTANCE = new CMDSModuleManager();

    public static CreateModuleRes createModule(CreateModuleReq request) throws VedantuException {

        try {
            LOGGER.debug("......Inside create module........");
            CMDSModule module = CMDSModuleDAO.INSTANCE.createModule(request.userId, request.orgId,
                    request.moduleRun, request.name, request.tags, request.brdIds,
                    request.targetIds, request.prerequsiteModuleID);
            LOGGER.debug("....Before event start........");
            generateEventAysc(request.userId, module, EventActionType.ADD,
                    EventType.INDEX_CMDS_MODULE, UserActionType.ADDED, false);
            CreateModuleRes response = new CreateModuleRes();
            response.id = module._getStringId();
            response.success = true;
            SrcEntity cmdsEntity = new SrcEntity(EntityType.CMDSMODULE, module._getStringId());
            String parentESId = addAsCMDSResource(cmdsEntity, EventActionType.ADD, module);
            CMDSResourcesManager.addToFolder(request.orgId, request.userId, cmdsEntity,
                    request.folderId, CmdsContentLinkType.ADDED, parentESId);
            return response;

        } catch (VedantuException exception) {
            throw exception;
        } catch (Exception exception) {
            LOGGER.error(" Error", exception);

            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, exception);
        }

    }

    public static SyncModuleRes syncModule(SyncModuleReq updateUserModuleReq)
            throws VedantuException {

        List<SrcEntity> entities = EntityUserActionDAO.INSTANCE.sync(updateUserModuleReq.userId,
                updateUserModuleReq.moduleId, updateUserModuleReq.entities);

        SyncModuleRes response = new SyncModuleRes();
        response.userId = updateUserModuleReq.userId;
        response.moduleId = updateUserModuleReq.moduleId;
        response.entities = entities;
        return response;
    }

    public static AddModuleRes addModuleEntries(AddModuleEntryReq request) throws VedantuException {

        try {
            LOGGER.debug("......Inside add module........");// +
                                                            // request.children.indexOf(0).);

            if (request.children == null) {
                throw new VedantuException(VedantuErrorCode.MODULE_LIST_EMPTY);
            }
            String validateRes = request.validate();
            if (StringUtils.isNotEmpty(validateRes)) {
                throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, validateRes);
            }

            CMDSModule module = CMDSModuleDAO.INSTANCE.addModuleEntries(request.moduleId,
                    request.children, request.pos);

            SrcEntity entity = new SrcEntity();
            entity.type = EntityType.CMDSMODULE;
            entity.id = module._getStringId();

            addAsCMDSResource(entity, EventActionType.UPDATE, module);
            generateEventAysc(request.userId, module, EventActionType.UPDATE,
                    EventType.INDEX_CMDS_MODULE, UserActionType.UPDATED, false);

            AddModuleRes response = new AddModuleRes();
            response.module = module;
            response.success = true;
            return response;

        } catch (VedantuException exception) {
            throw exception;
        } catch (Exception exception) {
            LOGGER.error(" Error", exception);

            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, exception);
        }

    }

    public static GetModuleInfoRes getModuleInfo(GetModuleInfoReq request) throws VedantuException {

        try {
            LOGGER.debug("......Inside delete module........" + request.id);
            CMDSModule module = CMDSModuleDAO.INSTANCE.getModuleById(request.id);
            SrcEntity content = new SrcEntity(EntityType.CMDSMODULE, request.id);
            SrcEntity targetEntity = new SrcEntity(EntityType.SECTION, request.sectionId);
            List<CMDSContentLink> contentLinks = CmdsContentLinkDAO.INSTANCE.getCmdsContentLinks(content,
                    targetEntity, CmdsContentLinkType.ADDED, null, Scope.ORG, MongoManager.NO_START, MongoManager.NO_LIMIT,
                    VedantuRecordState.ACTIVE, null, null);
            CMDSModuleInfo moduleInfo = CMDSModuleDAO.INSTANCE.getModuleInfo(module,contentLinks);
            GetModuleInfoRes response = new GetModuleInfoRes();
            response.fromMongoModel(module);
            Map<String,ScheduleInfo> schedules = new HashMap<String, ScheduleInfo>();
            if(request.target != null && request.target.type == EntityType.SECTION && StringUtils.isNotEmpty(request.target.id)){
                SrcEntity source = new SrcEntity(EntityType.CMDSMODULE, request.id);
                SrcEntity target = new SrcEntity(EntityType.SECTION, request.target.id);
                List<ModuleSchedules> moduleScheduleInfos = ModuleSchedulesDAO.INSTANCE.getSchedule(target, source);
                for(ModuleSchedules moduleSchedule : moduleScheduleInfos){
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
            LOGGER.error(" Error", exception);

            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, exception);
        }
    }

    public static GetCMDSModulesRes getCMDSModules(GetCMDSModulesReq getCMDSModulesReq)
            throws VedantuException {

        List<CMDSModuleNameInfo> modules = CMDSModuleDAO.INSTANCE.getCMDSModules(
                getCMDSModulesReq.orgId, getCMDSModulesReq.publishedStatus,
                getCMDSModulesReq.start, getCMDSModulesReq.size);
        LOGGER.debug(".......Inside getModules manager function......." + getCMDSModulesReq.size);
        GetCMDSModulesRes response = new GetCMDSModulesRes();
        response.modules = modules;
        return response;
    }

    public static DeleteModuleRes deleteModule(DeleteModuleReq request) throws VedantuException {

        try {
            LOGGER.debug("......Inside delete module........");

            CMDSModule module = CMDSModuleDAO.INSTANCE.deleteModule(request.id);
            SrcEntity entity = new SrcEntity();
            entity.type = EntityType.CMDSMODULE;
            entity.id = module._getStringId();

            addAsCMDSResource(entity, EventActionType.UPDATE, module);
            generateEventAysc(request.userId, module, EventActionType.UPDATE,
                    EventType.INDEX_CMDS_MODULE, UserActionType.UPDATED, false);

            DeleteModuleRes response = new DeleteModuleRes();
            response.success = true;
            return response;

        } catch (VedantuException exception) {
            throw exception;
        } catch (Exception exception) {
            LOGGER.error(" Error", exception);

            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, exception);
        }

    }

    public static UpdateModuleRes updateModule(UpdateModuleReq request) throws VedantuException {

        try {
            LOGGER.debug("......Inside create module........");

            CMDSModule module = CMDSModuleDAO.INSTANCE.updateModule(request.id, request.name,
                    request.moduleRun, request.tags, request.brdIds, request.targetIds,
                    request.prerequsiteModuleId, request.updateList);
            SrcEntity entity = new SrcEntity(EntityType.CMDSMODULE, module._getStringId());

            if (module.globalModuleId != null) {
                LOGGER.debug("......global module id is not null........" + module.globalModuleId);
                request.id = module.globalModuleId;
                ModuleManager.INSTANCE.updateModule(request);
            }
            addAsCMDSResource(entity, EventActionType.UPDATE, module);
            generateEventAysc(request.userId, module, EventActionType.UPDATE,
                    EventType.INDEX_CMDS_MODULE, UserActionType.UPDATED, false);
            UpdateModuleRes response = new UpdateModuleRes();
            // response.module = module;
            response.success = true;
            return response;

        } catch (VedantuException exception) {
            throw exception;
        } catch (Exception exception) {
            LOGGER.error(" Error", exception);

            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, exception);
        }

    }

    public static MoveModuleEntryRes moveModuleEntry(MoveModuleEntryReq request)
            throws VedantuException {

        try {
            LOGGER.debug("......Inside add module........");// +
                                                            // request.children.indexOf(0).);

            CMDSModule module = CMDSModuleDAO.INSTANCE.moveModuleEntry(request.moduleId,
                    request.oldPos, request.pos);
            SrcEntity entity = new SrcEntity(EntityType.CMDSMODULE, module._getStringId());

            addAsCMDSResource(entity, EventActionType.UPDATE, module);
            generateEventAysc(request.userId, module, EventActionType.UPDATE,
                    EventType.INDEX_CMDS_MODULE, UserActionType.UPDATED, false);
            MoveModuleEntryRes response = new MoveModuleEntryRes();
            // response.module = module;
            response.success = true;
            return response;

        } catch (VedantuException exception) {
            throw exception;
        } catch (Exception exception) {
            LOGGER.error(" Error", exception);

            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, exception);
        }

    }

    public static UpdateModuleEntryRes updateModuleEntry(UpdateModuleEntryReq request)
            throws VedantuException {

        try {
            LOGGER.debug("......Inside update moduleEntry........");// +
                                                                    // request.children.indexOf(0).);

            CMDSModule module = CMDSModuleDAO.INSTANCE.updateModuleEntry(request.moduleId,
                    request.pos, request.name, request.completionRule);

            if (module.globalModuleId != null) {
                LOGGER.debug("......global module id is not null........" + module.globalModuleId);
                request.moduleId = module.globalModuleId;
                ModuleManager.INSTANCE.updateModuleEntry(request);
            }

            SrcEntity entity = new SrcEntity(EntityType.CMDSMODULE, module._getStringId());

            addAsCMDSResource(entity, EventActionType.UPDATE, module);
            generateEventAysc(request.userId, module, EventActionType.UPDATE,
                    EventType.INDEX_CMDS_MODULE, UserActionType.UPDATED, false);
            UpdateModuleEntryRes response = new UpdateModuleEntryRes();
            // response.module = module;
            response.success = true;
            return response;

        } catch (VedantuException exception) {
            throw exception;
        } catch (Exception exception) {
            LOGGER.error(" Error", exception);

            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, exception);
        }

    }

    public static DeleteModuleEntryRes deleteModuleEntry(DeleteModuleEntryReq request)
            throws VedantuException {

        try {
            LOGGER.debug("......Inside add module........");// +
                                                            // request.children.indexOf(0).);

            CMDSModule module = CMDSModuleDAO.INSTANCE.deleteModuleEntry(request.moduleId,
                    request.pos);
            module.completed = CMDSModuleDAO.INSTANCE.isReadyToPublished(module);
            SrcEntity entity = new SrcEntity();
            entity.type = EntityType.CMDSMODULE;
            entity.id = module._getStringId();

            addAsCMDSResource(entity, EventActionType.UPDATE, module);
            generateEventAysc(request.userId, module, EventActionType.UPDATE,
                    EventType.INDEX_CMDS_MODULE, UserActionType.UPDATED, false);
            DeleteModuleEntryRes response = new DeleteModuleEntryRes();
            // response.module = module;
            response.success = true;
            return response;

        } catch (VedantuException exception) {
            throw exception;
        } catch (Exception exception) {
            LOGGER.error(" Error", exception);

            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, exception);
        }

    }

    @Override
    public boolean calculate(String id,boolean recalculate, VedantuBaseMongoModel... contents) throws VedantuException {

        List<CMDSModule> modules = new ArrayList<CMDSModule>();

        if (StringUtils.isNotEmpty(id)) {
            LOGGER.debug("checking for cmds module id: " + id);
            CMDSModule module = CMDSModuleDAO.INSTANCE.getById(id);

            if (module == null) {
                return false;
            }
            modules.add(module);
        }

        if (contents != null && contents.length != 0) {
            for (VedantuBaseMongoModel content : contents) {
                if (content instanceof CMDSModule) {
                    modules.add((CMDSModule) content);
                }
            }
        }

        // calculate module image size;

        for (CMDSModule module : modules) {
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
                                manager.calculate(null,false, contentModel);
                            }
                            module.size.addOriginal(contentModel.getExportableSize());
                        }

                    }

                }
            }

            module.size.finalize();
            CMDSModuleDAO.INSTANCE.updateModel(module, Arrays.asList(CMDSFile.SIZE));
            if (module.globalModuleId != null) {
                ModuleManager.INSTANCE.calculate(module.globalModuleId,true);
            }
        }
        return true;
    }

    public static ScheduleRes addSchedule(ModuleScheduleReq req) throws VedantuException {
        // TODO Auto-generated method stub
        LOGGER.debug("Inside add module schedulings");
        ScheduleRes res = new ScheduleRes();
        ModuleSchedules testSchedule = ModuleSchedulesDAO.INSTANCE.getSchedule(req);
        if(testSchedule != null){
            LOGGER.debug("Schedule Already Exists, updating schedule....");
            testSchedule.schedule = req.schedule;
        }else{
            if(req.schedule != null){
            testSchedule = new ModuleSchedules();
            testSchedule.entity = req.entity;
            testSchedule.target = req.target;
            testSchedule.source = req.source;
            testSchedule.schedule = req.schedule;
            testSchedule.orgId = req.orgId;
            testSchedule.userId = req.userId;
            }
            else{
                throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,"Missing Schedule Parameters");
            }
        }
        ModuleSchedulesDAO.INSTANCE.save(testSchedule);
        res.success = true;
        return res;
    }

    public static ScheduleRes deleteSchedule(ModuleScheduleReq req) throws VedantuException{
        LOGGER.debug("Inside delete module schedulings");
        ScheduleRes res = new ScheduleRes();
        ModuleSchedules testSchedule = ModuleSchedulesDAO.INSTANCE.getSchedule(req);
        if(testSchedule == null){
            LOGGER.error("Schedule does not exist");
            throw new VedantuException(VedantuErrorCode.DOES_NOT_EXIST);
        }
        testSchedule.recordState = VedantuRecordState.DELETED;
        ModuleSchedulesDAO.INSTANCE.save(testSchedule);
        res.success = true;
        return res;
        }
    }
