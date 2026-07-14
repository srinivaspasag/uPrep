package com.vedantu.cmds.mgmt.publishers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.daos.CMDSModuleDAO;
import com.vedantu.cmds.managers.AbstractCMDSContentManager;
import com.vedantu.cmds.models.CMDSModule;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.content.interfaces.IPublishable;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EncryptionLevel;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.daos.ModuleDAO;
import com.vedantu.content.managers.LibraryManager;
import com.vedantu.content.models.LibraryContentLink;
import com.vedantu.content.models.Module;
import com.vedantu.content.models.ModuleEntry;
import com.vedantu.content.search.details.ModuleSearchIndexDetails;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;

public class ModulePublisher extends AbstractCMDSContentManager {

    private static final ALogger        LOGGER   = Logger.of(ModulePublisher.class);

    public static final ModulePublisher INSTANCE = new ModulePublisher();

    private ModulePublisher() {

        super();
        EntityTypePublisherFactory.INSTANCE.register(EntityType.CMDSMODULE, this);
    }

    @Override
    public void prePublish(SrcEntity content) {

    }

    @Override
    public void postPublish(VedantuBaseMongoModel model) {

    }

    @Override
    protected VedantuBaseMongoModel publish(String userId, String orgId, SrcEntity entity)
            throws VedantuException {

        LOGGER.debug("............Inside ModulePublisher publish function...........");
        CMDSModule cmdsModule = CMDSModuleDAO.INSTANCE.getModuleById(entity.id);

        if (CollectionUtils.isEmpty(cmdsModule.children)) {
            throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_PUBLISHED,
                    "children can not be null or empty");
        }

        // if (cmdsModule.published) {
        //     throw new VedantuException(VedantuErrorCode.ALREADY_PUBLISHED);
        // }

        if (!cmdsModule.completed) {
            throw new VedantuException(VedantuErrorCode.MODULE_IN_DRAFT_STATE);
        }

        Module module;

        if (!StringUtils.isEmpty(cmdsModule.globalModuleId)) {
            module = ModuleDAO.INSTANCE.getModuleById(cmdsModule.globalModuleId);
            LOGGER.debug("siddhardha");
        } else {
            module = new Module(cmdsModule.name, orgId, cmdsModule.moduleRun,
                    cmdsModule._getStringId());
        }
        module.boardIds = cmdsModule.boardIds;
        module.contentSrc = cmdsModule.contentSrc;
        module.scope = Scope.ORG;
        module.tags = cmdsModule.tags;
        module.totalContentCount = cmdsModule.totalContentCount;
        module.targetIds = cmdsModule.targetIds;

        if (!StringUtils.isEmpty(cmdsModule.prerequsiteModuleId)) {
            CMDSModule prerequesiteCMDSModule = CMDSModuleDAO.INSTANCE
                    .getModuleById(cmdsModule.prerequsiteModuleId);
            module.prerequsiteModuleId = prerequesiteCMDSModule.globalModuleId;
        }
        module.published = true;
        module.completed = cmdsModule.completed;
        module.userId = cmdsModule.userId;
        module.size = cmdsModule.size;
        ModuleDAO.INSTANCE.save(module);
        List<ModuleEntry> children = new ArrayList<ModuleEntry>();

        try {
            // TODO: complete this

            SrcEntity moduleEntity = new SrcEntity(EntityType.MODULE, module._getStringId());
            for (ModuleEntry cmdsModuleEntry : cmdsModule.children) {
                ModuleEntry moduleEntry = null;
                if (StringUtils.isNotEmpty(cmdsModuleEntry.name) && cmdsModuleEntry.entity == null) {
                    moduleEntry = new ModuleEntry(cmdsModuleEntry.entity, cmdsModuleEntry.name,
                            cmdsModuleEntry.completionRule);
                    children.add(moduleEntry);
                } else if (cmdsModuleEntry.entity != null) {
                    @SuppressWarnings("rawtypes")
                    VedantuBasicDAO dao = EntityTypeDAOFactory.INSTANCE
                            .get(cmdsModuleEntry.entity.type);
                    if (!(dao instanceof IPublishable)) {
                        throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_PUBLISHED,
                                "module entry " + cmdsModuleEntry.entity + " is not publishable");
                    }

                    SrcEntity globalEntity = ((IPublishable) dao)
                            .getGlobalEntity(cmdsModuleEntry.entity.id);
                    // globalEntity==null case is already handled on AbstractCMDSContentManager
                    moduleEntry = new ModuleEntry(globalEntity, cmdsModuleEntry.name,
                            cmdsModuleEntry.completionRule);
                    LibraryContentLink contentLink = LibraryManager.addToLibrary(globalEntity,
                            moduleEntity, UserActionType.ADDED, cmdsModule.userId, Scope.LIBRARY,
                            null, null, null, EncryptionLevel.NA);
                    LOGGER.debug("added contentLink " + contentLink);
                    children.add(moduleEntry);
                }
            }
        } catch (VedantuException e) {
            ModuleDAO.INSTANCE.delete(module);
            throw e;
        } catch (Throwable e) {
            ModuleDAO.INSTANCE.delete(module);
            throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_PUBLISHED, e.getMessage(), e);
        }

        module.children = children; // //TODO

        ModuleDAO.INSTANCE.save(module);

        cmdsModule.published = true;
        cmdsModule.publishingInProgress = false;
        cmdsModule.globalModuleId = module._getStringId();
        CMDSModuleDAO.INSTANCE.save(cmdsModule);

        LOGGER.debug("............cmdsmodule saved...........");

        ModuleSearchIndexDetails details = new ModuleSearchIndexDetails();
        details.fromMongoModel(module);
        addLiveEntityToSearchIndex(details, EntityType.MODULE, true);

        generateEventAysc(userId, cmdsModule, EventActionType.UPDATE, EventType.INDEX_CMDS_MODULE,
                UserActionType.UPDATED, false);

        return cmdsModule;

    }

}
