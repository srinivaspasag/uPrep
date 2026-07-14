package com.lms.component;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.EventType;
import com.lms.common.vedantu.enums.UserActionType;
import com.lms.enums.ModuleRun;
import com.lms.managers.AbstractContentManager;
import com.lms.models.LibraryContentLink;
import com.lms.models.Module;
import com.lms.pojos.ModuleEntryCompletionRule;
import com.lms.pojos.ModuleSearchIndexDetails;
import com.lms.pojos.requests.UpdateModuleEntryReq;
import com.lms.pojos.requests.UpdateModuleReq;
import com.lms.repository.LibraryContentLinksRepo;
import com.lms.repository.ModuleRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;


@Component
public class ModuleManager extends AbstractContentManager {
    private static final Logger logger = LoggerFactory.getLogger(ModuleManager.class);
    @Autowired
    private ModuleRepo moduleRepo;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private LibraryContentLinksRepo libraryContentLinksRepo;

    public boolean updateModule(UpdateModuleReq request) throws VedantuException {


        try {
            logger.debug("......Inside create module........");

            Module module = updateModule(request.id, request.name,
                    request.moduleRun, request.tags, request.brdIds, request.targetIds,
                    request.prerequsiteModuleId, request.updateList);
            SrcEntity entity = new SrcEntity(EntityType.MODULE, module._getStringId());

            updateLastUpdated(entity);
            generateEventAysc(request.userId, module, UserActionType.EventActionType.UPDATE,
                    EventType.INDEX_MODULE, UserActionType.UPDATED, false);
            return true;

        } catch (VedantuException exception) {
            throw exception;
        } catch (Exception exception) {
            logger.error(" Error", exception);

            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, exception);
        }
    }

    public Module updateModule(String id, String name, ModuleRun moduleRun, List<String> tags,
                               List<String> brdIds, List<String> targetIds, String prerequsiteModuleId,
                               List<String> updateList) throws VedantuException {

        logger.debug("..... Inside update Module DAO function.......");
        Optional<Module> module1 = moduleRepo.findById(id);
        Module module = module1.get();

        if (CollectionUtils.isEmpty(updateList)) {
            throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_UPDATED, "empty update list");
        }

        if (name != null && updateList.contains(ConstantsGlobal.NAME)) {
            logger.debug(".... setting module name.....");
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
        logger.debug(".... about to save the module.....");
        moduleRepo.save(module);
        logger.debug(".... module is saved.....");
        return module;
    }

    public boolean updateLastUpdated(SrcEntity content) throws VedantuException {

        long lastUpdated = System.currentTimeMillis();
        logger.debug("............" + content.id + ".....Entering updateLastUpdated.....");
        Query query = new Query();
        Criteria criteria = new Criteria();


        // Query<LibraryContentLink> getContentQuery = getDS().createQuery(LibraryContentLink.class);
        addSourceFilter(criteria, content);
        query.addCriteria(criteria);
        List<LibraryContentLink> updateResults = mongoTemplate.find(query, LibraryContentLink.class);
        for (LibraryContentLink libraryContentLink : updateResults) {
            libraryContentLink.setLastUpdated(lastUpdated);
            libraryContentLinksRepo.save(libraryContentLink);
        }
        logger.debug("...Exiting updateLastUpdated...");
        return true;
    }

    public Criteria addSourceFilter(Criteria criteria, SrcEntity source) {

        if (source != null) {
            if (source.type != null) {
                criteria.and("source.type").is(source.type);
                if (source.id != null) {
                    criteria.and("source.id").is(source.id);

                }
            }

        }
        return criteria;
    }

    public boolean updateModuleEntry(UpdateModuleEntryReq request) throws VedantuException {

        try {
            logger.debug("......Inside update moduleEntry........");// +
            // request.children.indexOf(0).);

            Module module = updateModuleEntry(request.moduleId, request.pos,
                    request.name, request.completionRule);

            SrcEntity entity = new SrcEntity(EntityType.MODULE, module._getStringId());

            updateLastUpdated(entity);
            generateEventAysc(request.userId, module, UserActionType.EventActionType.UPDATE,
                    EventType.INDEX_MODULE, UserActionType.UPDATED, false);
            return true;

        } catch (VedantuException exception) {
            throw exception;
        } catch (Exception exception) {
            logger.error(" Error", exception);

            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, exception);
        }

    }

    public Module updateModuleEntry(String moduleId, int pos, String name,
                                    ModuleEntryCompletionRule completionRule) throws VedantuException {

        logger.debug("..... Inside update Module DAO function.......");
        Optional<Module> module1 = moduleRepo.findById(moduleId);
        Module module = module1.get();

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
        moduleRepo.save(module);
        logger.debug(".... module is saved.....");

        return module;
    }
}
