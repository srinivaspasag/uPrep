package com.vedantu.content.daos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.content.interfaces.BoardUpdatable;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.models.Module;
import com.vedantu.content.models.ModuleEntry;
import com.vedantu.content.models.ModuleRun;
import com.vedantu.content.pojos.ModuleEntryCompletionRule;
import com.vedantu.content.pojos.ModuleEntryInfo;
import com.vedantu.content.pojos.requests.ModuleInfo;
import com.vedantu.content.search.details.ModuleSearchIndexDetails;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.socials.apis.ICommentable;
import com.vedantu.socials.apis.IFollowable;
import com.vedantu.socials.apis.IUpVotable;
import com.vedantu.socials.apis.IViewable;
import com.vedantu.user.daos.AbstractUserActionDAO;

public class ModuleDAO extends AbstractUserActionDAO<Module, ObjectId> implements ICommentable,
        IUpVotable, IFollowable, IViewable, BoardUpdatable {

    public static final ModuleDAO INSTANCE = new ModuleDAO();
    private static final ALogger  LOGGER   = Logger.of(ModuleDAO.class);

    public ModuleDAO() {

        super(Module.class);
        // TODO Auto-generated constructor stub
    }

    public Module updateModule(String id, String name, ModuleRun moduleRun, List<String> tags,
            List<String> brdIds, List<String> targetIds, String prerequsiteModuleId,
            List<String> updateList) throws VedantuException {

        LOGGER.debug("..... Inside update Module DAO function.......");
        Module module = getModuleById(id);

        if (CollectionUtils.isEmpty(updateList)) {
            throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_UPDATED, "empty update list");
        }

        if (name != null && updateList.contains(ConstantsGlobal.NAME)) {
            LOGGER.debug(".... setting module name.....");
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
        LOGGER.debug(".... about to save the module.....");
        save(module);
        LOGGER.debug(".... module is saved.....");
        return module;
    }

    public ModuleInfo getModuleInfo(String id) throws VedantuException {

        LOGGER.debug("..... Inside get Module DAO function......." + id);
        Module module = getModuleById(id);
        LOGGER.debug("..... Module is InitialisedgetModuleById.......");
        String orgId = (module.contentSrc != null) ? module.contentSrc.id : StringUtils.EMPTY;
        ModuleInfo moduleInfo = new ModuleInfo(module._getStringId(), module.name,
                EntityType.MODULE, orgId, module.timeCreated, module.lastUpdated, module.userId, 0,
                module.published, module.completed, true, module.cmdsModuleId, module.moduleRun,
                module.recordState, new ArrayList<ModuleEntryInfo>(), module.prerequsiteModuleId);

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
                }
                moduleInfo.children.add(moduleEntryInfo);
            }
            LOGGER.debug("..... Outside the loop.......");
        }

        return moduleInfo;
        // return null;
    }

    public Module updateModuleEntry(String moduleId, int pos, String name,
            ModuleEntryCompletionRule completionRule) throws VedantuException {

        LOGGER.debug("..... Inside update Module DAO function.......");
        Module module = getModuleById(moduleId);

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
        save(module);
        LOGGER.debug(".... module is saved.....");

        return module;
    }

    public List<ModuleEntry> get(String id) throws VedantuException {

        LOGGER.debug(".......Inside ModuleDAO get function.......");
        Module module = getById(id);
        if (module == null) {
            throw new VedantuException(VedantuErrorCode.MODULE_DOES_NOT_EXISTS);
        }
        return module.children;
    }

    public Module getModuleById(String moduleId) throws VedantuException {

        LOGGER.debug("inside getModuleById function" + moduleId);
        Module module = getById(moduleId);
        if (module == null) {
            LOGGER.debug("module is null");
            throw new VedantuException(VedantuErrorCode.MODULE_DOES_NOT_EXISTS,
                    "no module found with id : " + moduleId);
        }

        return module;
    }

    @Override
    public List<SrcEntity> getChildren(String id) {

        Module module = getById(id);
        if(module == null ){
            LOGGER.debug("Content not found");
            return null;
        }
        List<SrcEntity> children = new ArrayList<SrcEntity>();
        if (CollectionUtils.isNotEmpty(module.children)) {
            for (ModuleEntry entry : module.children) {
                if (entry.entity == null) {
                    continue;
                }
                children.add(entry.entity);
            }
        }

        return children;
    }

    public Module getByCMDSModuleId(String cmdsModuleId) {
        Module module = getQuery().filter("cmdsModuleId", cmdsModuleId).get();
        if (module == null) {
            LOGGER.error("Cannot find module with the cmds module id :" + cmdsModuleId);
        }
        return module;
    }

}
