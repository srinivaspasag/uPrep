package com.vedantu.cmds.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vedantu.board.daos.BoardDAO;
import com.vedantu.board.daos.BoardMappingsDAO;
import com.vedantu.board.models.Board;
import com.vedantu.board.models.BoardMapping;
import com.vedantu.board.pojos.BoardMappings;
import com.vedantu.cmds.maintenance.managers.ShareQuestionsThread;
import com.vedantu.cmds.models.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.HasChildQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.facet.AbstractFacetBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.vedantu.cmds.daos.CMDSFolderDAO;
import com.vedantu.cmds.daos.CMDSQuestionDAO;
import com.vedantu.cmds.daos.CMDSTestDAO;
import com.vedantu.cmds.daos.CmdsContentDAO;
import com.vedantu.cmds.daos.CmdsContentLinkDAO;
import com.vedantu.cmds.enums.CmdsContentLinkType;
import com.vedantu.cmds.models.event.search.details.CMDSContentLinkDetails;
import com.vedantu.cmds.models.event.search.details.CMDSResourceDetails;
import com.vedantu.cmds.models.event.search.details.ReIndexDetails;
import com.vedantu.cmds.pojos.BoardInfo;
import com.vedantu.cmds.pojos.OrgDetails;
import com.vedantu.cmds.pojos.SharedBoardInfo;
import com.vedantu.cmds.pojos.content.question.CMDSFolderInfo;
import com.vedantu.cmds.pojos.requests.AddMappingsReq;
import com.vedantu.cmds.pojos.requests.CreateFolderReq;
import com.vedantu.cmds.pojos.requests.DeleteContentReq;
import com.vedantu.cmds.pojos.requests.DeleteMappingReq;
import com.vedantu.cmds.pojos.requests.GetFoldersReq;
import com.vedantu.cmds.pojos.requests.GetResourcesReq;
import com.vedantu.cmds.pojos.requests.GetSharedQuestionsBasicInfoReq;
import com.vedantu.cmds.pojos.requests.MoveContentReq;
import com.vedantu.cmds.pojos.requests.SaveMappingsReq;
import com.vedantu.cmds.pojos.requests.VisibleMappingReq;
import com.vedantu.cmds.pojos.requests.videos.SignUploadFileReq;
import com.vedantu.cmds.pojos.requests.videos.UploadCMDSContentFileReq;
import com.vedantu.cmds.pojos.responses.AddMappingsRes;
import com.vedantu.cmds.pojos.responses.CreateFolderRes;
import com.vedantu.cmds.pojos.responses.DeleteContentRes;
import com.vedantu.cmds.pojos.responses.EntityResponse;
import com.vedantu.cmds.pojos.responses.GetFoldersRes;
import com.vedantu.cmds.pojos.responses.GetResourcesRes;
import com.vedantu.cmds.pojos.responses.GetSharedQuestionsBasicInfoRes;
import com.vedantu.cmds.pojos.responses.MoveContentRes;
import com.vedantu.cmds.pojos.responses.SaveMappingRes;
import com.vedantu.cmds.pojos.responses.ShareMappingResponse;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.daos.CounterDAO;
import com.vedantu.commons.daos.EntityOperationStatusDAO;
import com.vedantu.commons.daos.GranteeOrgProgramDAO;
import com.vedantu.commons.entity.factory.EntityStorageFactory;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.entity.storage.AbstractEntityFileStorage;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.OperationType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.fs.exception.FileStoreException;
import com.vedantu.commons.fs.handlers.FileSystemFactory;
import com.vedantu.commons.fs.handlers.IFileSystemHandler;
import com.vedantu.commons.fs.responses.SignUploadFileRes;
import com.vedantu.commons.models.mongo.EntityOperationStatus;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.utils.ContentTypeMapper;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.commons.utils.VedantuStringUtils;
import com.vedantu.content.enums.Difficulty;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.enums.search.SearchResultType;
import com.vedantu.content.pojos.requests.EditContentReq;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.mongo.models.GranteeOrgProgram;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.organization.models.Organization;
import com.vedantu.search.utils.ElasticSearchUtils;

public class CMDSResourcesManager extends AbstractCMDSContentManager {

    static final ALogger               LOGGER   = Logger.of(CMDSResourcesManager.class);
    public static CMDSResourcesManager INSTANCE = new CMDSResourcesManager();

    /**
     *
     * @param userId
     * @param orgId
     * @return
     * @throws VedantuException
     *             {
     */
    public static CMDSFolder createRootFolder(String userId, String orgId) throws VedantuException {

        String rootFolderId = CMDSFolderDAO.INSTANCE.doesRootFolderExists(orgId);
        if (rootFolderId != null) {
            CMDSFolder createdFolder = CMDSFolderDAO.INSTANCE
                    .createFolder(null, userId, orgId, "/");
            return createdFolder;
        }
        return null;
    }

    /**
     *
     * @param userId
     * @param orgId
     * @return {@link CMDSFolder}
     * @throws VedantuException
     *             {
     */
    public static synchronized CMDSFolder getRootFolder(String userId, String orgId)
            throws VedantuException {

        CMDSFolder rootFolder = CMDSFolderDAO.INSTANCE.getRootFolder(orgId);

        if (rootFolder == null) {
            LOGGER.debug("Root Folder" + rootFolder);

            rootFolder = CMDSFolderDAO.INSTANCE.createFolder(null, userId, orgId, "/");
        }

        return rootFolder;
    }

    //
    // public static synchronized CMDSModule getRootModule(String userId, String orgId)
    // throws VedantuException {
    //
    // LOGGER.debug(".......inside getRootModule Function..........");
    //
    // CMDSModule rootModule = CMDSModuleDAO.INSTANCE.getRootModule(orgId);
    //
    // if (rootModule == null) {
    // LOGGER.debug("Root Module" + rootModule);
    //
    // rootModule = CMDSModuleDAO.INSTANCE.createModule( userId, orgId, "/");
    // }
    //
    // // CMDSModule rootModule = CMDSModuleDAO.INSTANCE.createModule(null, userId, orgId, "/");
    //
    // return rootModule;
    // }

    /**
     *
     * @param request
     * @return {@link GetFoldersRes}
     * @throws VedantuException
     */
    public static GetFoldersRes getFolders(GetFoldersReq request) throws VedantuException {

        GetFoldersRes response = new GetFoldersRes();
        MutableLong totalHits = new MutableLong(0);

        if (StringUtils.isEmpty(request.folderId)) {
            CMDSFolder rootFolder = getRootFolder(request.userId, request.orgId);
            totalHits.increment();

            response.list.add(rootFolder.toBasicInfo());
        } else {

            List<CMDSFolder> folders = CMDSFolderDAO.INSTANCE.getChilds(request.folderId,
                    request.start, request.size, totalHits);
            if (CollectionUtils.isNotEmpty(folders)) {
                for (CMDSFolder folder : folders) {
                    response.list.add(folder.toBasicInfo());
                }
            }
        }

        response.totalHits = totalHits.longValue();

        return response;
    }

    public static MoveContentRes moveFolder(MoveContentReq request) throws VedantuException {

        List<EntityResponse> responses = moveFolderTo(request.userId, request.entities,
                request.targetFolderId, request.orgId);
        MoveContentRes response = new MoveContentRes();
        response.list.addAll(responses);
        VedantuErrorCode errorCode = EntityResponse.getCumulativeErrorCode(responses);
        if (errorCode != null) {
            response.cumulativeErrorCode = errorCode;
        }
        return response;
    }

    public static DeleteContentRes delete(DeleteContentReq request) throws VedantuException {

        DeleteContentRes response = new DeleteContentRes();
        // response.info = errorCodeMap;

        // Map<SrcEntity, String> errorCodeMap = new HashMap<SrcEntity, String>();
        List<EntityResponse> responseList = new ArrayList<EntityResponse>();

        for (SrcEntity entity : request.entities) {
            try {
                if (!EntityType.isSupportedCMDSLibraryEntityType(entity.type)) {
                    throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_REMOVED);
                }

                boolean deleteResult = delete(entity);

                LOGGER.debug("Delete result" + entity + "  results " + deleteResult);
                if (!deleteResult) {
                    throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_REMOVED);
                }
                // remove link indiex

                LOGGER.debug("Entity out of cannot be removed");

                CmdsContentLinkDAO.INSTANCE.remove(entity, null, null);

                // drop index and parent child everything
                removeCMDSResource(entity);

                LOGGER.debug("Entity out of remove CMDS Resource");

                // drop all linx
                ReIndexDetails details = new ReIndexDetails();
                details.ids.add(entity.id);
                details.type = entity.type;
                details.userId = request.userId;

                generateEventAysc(details.userId, details, EventType.REINDEX_CMDS_RESOURCE);
                responseList.add(new EntityResponse(entity, null));

                if(entity.type == EntityType.CMDSQUESTION){
                    CMDSQuestion ques = CMDSQuestionDAO.INSTANCE.getById(entity.id, VedantuRecordState.DELETED);
                    // Delete PARA Questions
                    if(ques != null && ques.type == QuestionType.TEXT){
                        deleteParaIds(ques);
                    }
                    // Delete mapping from parent question
                    if(ques.scope == Scope.PRIVATE){
                        CMDSQuestion origQues = CMDSQuestionDAO.INSTANCE.getById(ques.parentQId);
                        origQues.sharedToOrgIds.remove(request.orgId);
                        origQues.sharedCMDSQuesIds.remove(entity.id);
                        CMDSQuestionDAO.INSTANCE.save(origQues);
                    }
                    // Delete all shared questions
                    String learnpediaId = Play.application().configuration().getString("learnpedia.id");
                    if(request.orgId.equals(learnpediaId)){
                        LOGGER.debug("delete : About to delete shared LP Questions");
                        deleteSharedQuestion(entity.id,request.orgId);
                    }
                }

            } catch (VedantuException exception) {
                // errorCodeMap.put(entity, exception.errorCode.name());
                responseList.add(new EntityResponse(entity, exception.errorCode));

            }
        }

        response.list.addAll(responseList);

        VedantuErrorCode errorCode = EntityResponse.getCumulativeErrorCode(response.list);
        if (errorCode != null) {
            response.cumulativeErrorCode = errorCode;
        }
        return response;
    }

    private static void deleteParaIds(CMDSQuestion ques) {
        // TODO Auto-generated method stub
        List<String> paraIds = ques.paraIds;
        for(String qId : paraIds){
            DeleteContentReq req = new DeleteContentReq();
            List<SrcEntity> entities = new ArrayList<SrcEntity>();
            CMDSQuestion que = CMDSQuestionDAO.INSTANCE.getById(qId);
            SrcEntity entity = new SrcEntity();
            entity.id = qId;
            entity.type = EntityType.CMDSQUESTION;
            entities.add(entity);
            req.entities = entities;
            req.orgId = que.contentSrc.id;
            req.userId = que.userId;
            req.callingUserId = que.userId;
            req.callingApp = "cmds-app";
            req.callingAppId = "cmds-app";
            try {
                LOGGER.debug("Deleting PARA question "+qId);
                delete(req);
                LOGGER.debug("Deleted PARA question "+qId);
            } catch (VedantuException e) {
                LOGGER.debug("Error while deleting PARA Question "+qId+" with message "+e.getMessage());
            }
        }
    }

    private static void deleteSharedQuestion(String quesId, String orgId) {
        // TODO Auto-generated method stub
        CMDSQuestion question = CMDSQuestionDAO.INSTANCE.getById(quesId, VedantuRecordState.DELETED);
        Set<String> sharedQIds = question.sharedCMDSQuesIds;
        for(String qId : sharedQIds){
            DeleteContentReq req = new DeleteContentReq();
            List<SrcEntity> entities = new ArrayList<SrcEntity>();
            CMDSQuestion que = CMDSQuestionDAO.INSTANCE.getById(qId);
            SrcEntity entity = new SrcEntity();
            entity.id = qId;
            entity.type = EntityType.CMDSQUESTION;
            entities.add(entity);
            req.entities = entities;
            req.orgId = que.contentSrc.id;
            req.userId = que.userId;
            req.callingUserId = que.userId;
            req.callingApp = "cmds-app";
            req.callingAppId = "cmds-app";
            try {
                LOGGER.debug("Deleting shared LP question "+qId);
                delete(req);
                LOGGER.debug("Deleted shared LP question "+qId);
            } catch (VedantuException e) {
                LOGGER.debug("Error while deleting shared LP Question "+qId+" with message "+e.getMessage());
            }
        }
    }

    /**
     * Move entity from one folder to other but checks if entity is folder calls different calls
     *
     * @param userId
     * @param movingEntities
     * @param targetFolderId
     * @param organizationId
     * @return
     * @throws VedantuException
     */
    public static List<EntityResponse> moveFolderTo(String userId, List<SrcEntity> movingEntities,
            String targetFolderId, String organizationId) throws VedantuException {

        // Map<SrcEntity, String> errorCodeMap = new HashMap<SrcEntity, String>();

        List<EntityResponse> responseList = new ArrayList<EntityResponse>();
        CMDSFolder targetFolder = CMDSFolderDAO.INSTANCE.findById(organizationId, targetFolderId);
        for (SrcEntity movingEntity : movingEntities) {
            try {
                // here moving entity type is folder
                if (movingEntity.type == EntityType.FOLDER) {
                    CMDSFolder movingFolder = CMDSFolderDAO.INSTANCE.findById(organizationId,
                            movingEntity.id);
                    if (movingFolder != null) {
                        move(userId, movingFolder, targetFolder, organizationId);
                    }
                } else {
                    // here individual entity is being moved
                    move(userId, movingEntity, targetFolder, organizationId);
                }
                responseList.add(new EntityResponse(movingEntity, null));
            } catch (VedantuException exception) {
                // errorCodeMap.put(movingEntity, exception.errorCode.name());
                responseList.add(new EntityResponse(movingEntity, exception.errorCode));
            }
        }
        return responseList;
    }

    /**
     * Move one folder to other folder
     *
     * @param userId
     * @param currentFolder
     * @param targetFolder
     * @param organizationId
     * @return
     * @throws VedantuException
     */
    private static boolean move(String userId, CMDSFolder currentFolder, CMDSFolder targetFolder,
            String organizationId) throws VedantuException {

        if (currentFolder == null || targetFolder == null) {
            throw new VedantuException(VedantuErrorCode.FOLDER_NOT_FOUND);
        }

        if (!validateMove(currentFolder, targetFolder)) {
            throw new VedantuException(VedantuErrorCode.FOLDER_CAN_NOT_BE_MOVED,
                    "Can not move folder as its root");
        }
        LOGGER.debug("Moving one folder to other" + currentFolder + " target folder "
                + targetFolder);

        List<String> newParents = new ArrayList<String>();
        if (targetFolder.parentSources == null) {
            targetFolder.parentSources = new ArrayList<String>();

        }

        newParents.addAll(targetFolder.parentSources);
        List<String> oldParents = currentFolder.parentSources;
        CMDSFolderDAO.INSTANCE.updateParent(currentFolder._getStringId(),
                targetFolder._getStringId(), newParents, oldParents);

        SrcEntity content = new SrcEntity(EntityType.FOLDER, currentFolder._getStringId());

        SrcEntity target = new SrcEntity(EntityType.FOLDER, targetFolder._getStringId());

        CmdsContentLinkDAO.INSTANCE.updateTargetEntity(null,
                new SrcEntity(content.type, content.id), new SrcEntity(EntityType.FOLDER, null),
                target, CmdsContentLinkType.ADDED, null);

        List<CMDSContentLink> links = CmdsContentLinkDAO.INSTANCE.getCmdsContentLinks(content,
                target, CmdsContentLinkType.ADDED, null, 0, 1, null);
        CMDSContentLink linkage = links.get(0);
        if (linkage == null) {
            return false;
        }

        updateContentLink(linkage._getStringId(), linkage.userId, content, target,
                linkage.timeCreated, linkage.getScope(), linkage.position);
        return true;
    }

    private static boolean validateMove(CMDSFolder currentFolder, CMDSFolder targetFolder)
            throws VedantuException {

        if (currentFolder.isRoot) {
            throw new VedantuException(VedantuErrorCode.ROOT_FOLDER_CAN_NOT_BE_MOVED,
                    "Can not move folder as its root");
        }

        if (targetFolder._getStringId().equals(currentFolder._getStringId())) {
            throw new VedantuException(VedantuErrorCode.FOLDER_CAN_NOT_BE_CHILD_OF_SELF,
                    "Can not move folder as both folders are same");
        }

        if ((targetFolder.parentSources != null && targetFolder.parentSources
                .contains(currentFolder._getStringId()))) {
            throw new VedantuException(VedantuErrorCode.FOLDER_CAN_NOT_BE_MOVED_TO_CHILD_FOLDER,
                    "Can not move folder as target folder is child of current folder");
        }

        if (CMDSFolderDAO.INSTANCE.checkForSimilarNameDirectory(currentFolder.organizationId,
                targetFolder._getStringId(), currentFolder.name)) {
            throw new VedantuException(VedantuErrorCode.FOLDER_ALREADY_EXISTS,
                    "Can not move folder as  target folder already contains folder with same name");
        }
        return true;
    }

    /**
     * Move content from one folder to other
     *
     * @param userId
     * @param entity
     * @param moveToFolder
     * @param organizationId
     * @return
     * @throws VedantuException
     */
    private static boolean move(String userId, SrcEntity entity, CMDSFolder moveToFolder,
            String organizationId) throws VedantuException {

        MutableLong totalHits = new MutableLong(0L);

        SrcEntity target = new SrcEntity(EntityType.FOLDER, moveToFolder._getStringId());
        CmdsContentLinkDAO.INSTANCE.updateTargetEntity(null, new SrcEntity(entity.type, entity.id),
                new SrcEntity(EntityType.FOLDER, null), target, CmdsContentLinkType.ADDED, null);
        SrcEntity content = new SrcEntity(entity.type, entity.id);

        if (totalHits.longValue() > 1) {
            LOGGER.error("Incorrect number of links for data");
            throw new VedantuException(VedantuErrorCode.CONTENT_CAN_NOT_BE_MOVED);
        }
        VedantuBasicDAO<?, ?> basicDAO = EntityTypeDAOFactory.INSTANCE.get(entity.type);
        if (basicDAO == null) {
            LOGGER.error("No dao found for content :" + entity);
        }
        if (basicDAO instanceof CmdsContentDAO) {
            CmdsContentDAO<?, ?> cmdsContentDAO = (CmdsContentDAO<?, ?>) basicDAO;
            if (!cmdsContentDAO.isMovingAllowed(entity.id)) {
                LOGGER.error("No moving is allowed found for question inside questionsets :"
                        + entity);
                throw new VedantuException(VedantuErrorCode.CONTENT_CAN_NOT_BE_MOVED);
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
        updateContentLink(linkage._getStringId(), linkage.userId, content, target,
                linkage.timeCreated, linkage.getScope(), linkage.position);
        return true;
    }

    public static CreateFolderRes createFolder(CreateFolderReq request) throws VedantuException {

        try {
            CMDSFolderDAO qrFolderDAO = new CMDSFolderDAO();
            if (StringUtils.isEmpty(request.parentFolderId)) {
                CMDSFolder folder = getRootFolder(request.userId, request.orgId);
                request.parentFolderId = folder._getStringId();
            }
            LOGGER.debug("ParentFolder " + request.parentFolderId);
            CMDSFolder createdFolder = qrFolderDAO.createFolder(request.parentFolderId,
                    request.userId, request.orgId, request.name);

            if (createdFolder != null && StringUtils.isNotEmpty(request.parentFolderId)) {

                SrcEntity folderEntity = new SrcEntity(EntityType.FOLDER,
                        createdFolder._getStringId());
                String parentLiveIndexedId = AbstractCMDSContentManager.addAsCMDSResource(
                        folderEntity, EventActionType.ADD, createdFolder);

                CMDSResourcesManager.addToFolder(request.orgId, request.userId, new SrcEntity(
                        EntityType.FOLDER, createdFolder._getStringId()), request.parentFolderId,
                        CmdsContentLinkType.ADDED, parentLiveIndexedId);

            }
            CreateFolderRes response = new CreateFolderRes();
            response.id = createdFolder._getStringId();
            response.parent = createdFolder.parent;
            response.name = createdFolder.name;
            response.createdOn = createdFolder.timeCreated;
            LOGGER.debug(" Added folder " + response.id);
            return response;

        } catch (VedantuException exception) {
            throw exception;
        } catch (Exception exception) {
            LOGGER.error(" Error", exception);

            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, exception);
        }

    }

    @Override
    public boolean update(EditContentReq request) throws VedantuException {

        CMDSFolder content = CMDSFolderDAO.INSTANCE.getById(request.entity.id);
        if (content == null) {
            throw new VedantuException(VedantuErrorCode.CONTENT_NOT_FOUND);
        }

        List<String> updateList = new ArrayList<String>();
        if (StringUtils.isNotEmpty(request.name)
                && request.updateList.contains(EditContentReq.NAME)) {
            content.name = request.name;
            updateList.add(CMDSFolder.NAME);
        }

        try {
            CMDSFolderDAO.INSTANCE.updateModel(content, updateList);
            addAsCMDSResource(request.entity, EventActionType.UPDATE, content);

        } catch (VedantuException exception) {
            if (exception.errorCode == VedantuErrorCode.ALREADY_ADDED) {
                LOGGER.error("Folder with name already exists", exception);
                throw new VedantuException(VedantuErrorCode.FOLDER_ALREADY_EXISTS,
                        "folder with same name already exists");
            }
            throw exception;
        }

        return true;
    }

    public static boolean addToFolder(String orgId, String userId, SrcEntity content,
            String folderId, CmdsContentLinkType linkType) throws VedantuException {

        return addToFolder(orgId, userId, content, folderId, linkType, null);
    }

    public static boolean addToFolder(String orgId, String userId, SrcEntity content,
            String folderId, CmdsContentLinkType linkType, String parentESId)
            throws VedantuException {

        CMDSFolder qrFolder = CMDSFolderDAO.INSTANCE.findById(orgId, folderId);

        if (qrFolder != null) {
            SrcEntity folderEntity = new SrcEntity(EntityType.FOLDER, qrFolder._getStringId());
            CMDSContentLink linkage = CmdsContentLinkDAO.INSTANCE.addLink(content, folderEntity,
                    linkType, userId, false);

            LOGGER.debug(" Created linkage : " + linkage);

            CMDSContentLinkDetails libraryContentLinkDetails = new CMDSContentLinkDetails(
                    linkage._getStringId(), userId, content, folderEntity, linkage.getScope(),
                    linkage.timeCreated, linkage.position);

            SrcEntity resource = new SrcEntity(EntityType.CMDSRESOURCE, getResourceId(content));

            updateUserActionMappintToEs(libraryContentLinkDetails, resource, UserActionType.ADDED,
                    EventActionType.ADD, parentESId);

            return true;
        }

        throw new VedantuException(VedantuErrorCode.FOLDER_NOT_FOUND);
    }

    public static boolean addToTest(String orgId, String userId, SrcEntity content,
            String testId, CmdsContentLinkType linkType, String parentESId)
            throws VedantuException {

        CMDSTest qrTest = CMDSTestDAO.INSTANCE.getTest(testId);

        if (qrTest != null) {
            SrcEntity testEntity = new SrcEntity(EntityType.CMDSTEST, qrTest._getStringId());
            CMDSContentLink linkage = CmdsContentLinkDAO.INSTANCE.addLink(content, testEntity,
                    linkType, userId, false);

            LOGGER.debug(" Created linkage : " + linkage);

            CMDSContentLinkDetails libraryContentLinkDetail = new CMDSContentLinkDetails(
                    linkage._getStringId(), userId, content, testEntity, linkage.getScope(),
                    linkage.timeCreated, linkage.position);

            SrcEntity resource = new SrcEntity(EntityType.CMDSRESOURCE, getResourceId(content));

            updateUserActionMappintToEs(libraryContentLinkDetail, resource, UserActionType.ADDED,
                    EventActionType.ADD, parentESId);

            return true;
        }

        throw new VedantuException(VedantuErrorCode.FOLDER_NOT_FOUND);
    }

    public boolean addToFolder(String orgId, String userId, String folderId,
            CmdsContentLinkType linkType, List<SrcEntity> contents) throws VedantuException {

        // update mapping
        CMDSFolder qrFolder = CMDSFolderDAO.INSTANCE.findById(orgId, folderId);

        if (qrFolder != null) {
            try {
                if (CollectionUtils.isNotEmpty(contents)) {
                    for (SrcEntity content : contents) {
                        CMDSContentLink linkage = CmdsContentLinkDAO.INSTANCE.addLink(content,
                                new SrcEntity(EntityType.FOLDER, qrFolder._getStringId()),
                                linkType, userId);
                        LOGGER.debug(" Created linkage : " + linkage);
                    }
                }
            } catch (Exception exception) {
                throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
            }
            return true;
        }

        throw new VedantuException(VedantuErrorCode.FOLDER_NOT_FOUND);
    }

    public static GetResourcesRes getResources(GetResourcesReq request) throws VedantuException {

        // check in EC

        LOGGER.debug("Getting resources in folder Id " + request.folderId
                + System.currentTimeMillis());
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

            TermQueryBuilder folderIdTermQuery = QueryBuilders.termQuery("target.id",
                    request.folderId);

            folderQuery.must(folderIdTermQuery);

            if (request.diffculty != null && request.diffculty != Difficulty.UNKNOWN) {

                TermQueryBuilder difficultyQueryBuilder = QueryBuilders.termQuery(
                        ConstantsGlobal.DIFFICULTY, request.diffculty.name().toLowerCase());
                resourceQuery.should(difficultyQueryBuilder);
            }

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

            Gson gson = new Gson();
            //LOGGER.debug(":::::: Details : "+gson.toJson(details, (new TypeToken<List<CMDSResourceDetails>>(){}).getType()));

            List<CMDSContentLink> links = new ArrayList<CMDSContentLink>();

            MutableLong totalHits = new MutableLong(0L);
            CMDSContentLink link = null;
            for (CMDSResourceDetails detail : details) {
                SrcEntity folderEntity = new SrcEntity(EntityType.FOLDER, request.folderId);
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

            response.folderInfo = CMDSFolderDAO.INSTANCE.getBasicInfo(request.folderId);
            CMDSFolderDAO.INSTANCE.annotateParentInfo((CMDSFolderInfo) response.folderInfo,
                    request.folderId);

        } catch (Exception exception) {
            LOGGER.debug(" Error", exception);
            Logger.debug(" Error", exception);
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, exception);
        }
        LOGGER.debug("Received resources " + request.folderId + System.currentTimeMillis());

        // check in
        return response;
    }

    public static GetResourcesRes getQuestions(GetResourcesReq request) throws VedantuException {

        LOGGER.debug("Inside getQuestions");

        GetResourcesRes response = new GetResourcesRes();
        String type = StringUtils.EMPTY;

        List<CMDSResourceDetails> details = new ArrayList<CMDSResourceDetails>();
        List<ModelBasicInfo> basicInfos = new ArrayList<ModelBasicInfo>();

        String folderId = CMDSFolderDAO.INSTANCE.getRootFolder(request.orgId)._getStringId();

        if(request.quesType.equals("PARA_QUES")){
            details = getParagraphQuestions(request);
            response.totalHits = details.size();
        }else{
            SrcEntity contentSrc = new SrcEntity(EntityType.ORGANIZATION, request.orgId);

            // Get all the orgIds that gave access to the current organization
//            MutableLong totalProgramHits = new MutableLong(0L);
//            List<GranteeOrgProgram> granteeOrgPrograms = GranteeOrgProgramDAO.INSTANCE
//                    .getGranteeOrgPrograms(request.orgId, null, totalProgramHits);
//            List<String> grantedOrgs = new ArrayList<String>();
//            for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
//                grantedOrgs.add(granteeOrgProgram.providerOrgId);
//            }
//
////            BoolQueryBuilder resourceQuery = buildSearchQuery(SearchResultType.ALL, request.userId,
//                    contentSrc, null, null, null, request.query, null, grantedOrgs);
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            boolQuery.must(QueryBuilders.termQuery(ConstantsGlobal.CONTENT_SRC + "."
                    + ConstantsGlobal.ID, contentSrc.id));
            boolQuery.must(QueryBuilders.termQuery(ConstantsGlobal.CONTENT_SRC + "."
                    + ConstantsGlobal.TYPE, contentSrc.type.name().toLowerCase()));

            if (!StringUtils.isEmpty(request.includeDifficulty)) {
                boolQuery.must(QueryBuilders.termQuery(ConstantsGlobal.DIFFICULTY, request.includeDifficulty.toLowerCase()));
            }

            String learnpediaId = Play.application().configuration().getString("learnpedia.id");
            if(!request.orgId.equals(learnpediaId)){
                if(!includeLearnpediaQuestions(learnpediaId,request.orgId)){
                    boolQuery.must(QueryBuilders.termQuery("scope", "org"));
                }
            }

            BoolFilterBuilder boardFilterBuilder = FilterBuilders.boolFilter();
            // oring all boards assuming boards are of only one level
            ElasticSearchUtils.addBoardAndTargetFilter(request.brdIds, false, boardFilterBuilder);

            TermsQueryBuilder questionTypeBuilder = null;
            if(request.quesType.equals("PARA")){
                questionTypeBuilder = QueryBuilders.inQuery("type", "text");
                type = "NOT_PARA";
            }else if(request.quesType.equals("NOT_PARA")){
                if (!StringUtils.isEmpty(request.includeTypes)) {
                    //boolQuery.must(QueryBuilders.termQuery(ConstantsGlobal.TYPE, request.includeTypes.toLowerCase()));
                    questionTypeBuilder = QueryBuilders.inQuery(ConstantsGlobal.TYPE, request.includeTypes.toLowerCase());
                }else{
                    questionTypeBuilder = QueryBuilders.inQuery("type", "scq","mcq","numeric","matrix","subjective");
                }
                type = "PARA";
            }else{
                questionTypeBuilder = QueryBuilders.inQuery("type", "scq","mcq","numeric","text","matrix","subjective");
            }

            boolQuery.must(questionTypeBuilder);

            QueryBuilder query = QueryBuilders
                    .filteredQuery(
                            boolQuery,
                            CollectionUtils.isEmpty(request.brdIds) ? FilterBuilders
                                    .matchAllFilter() : boardFilterBuilder);

            SearchResponse questionsResponse = ElasticSearchUtils.getSearchResponse(query,
                    request.orderBy, request.sortOrder, request.start, request.size,
                    EntityType.CMDSQUESTION.getIndexName(), EntityType.CMDSQUESTION.getIndexType()
                            .toLowerCase(), null, false, (AbstractFacetBuilder[]) null);

            response.totalHits = AbstractCMDSContentManager.getBasicInfoFromESSearch(questionsResponse,
                    details,"CMDSQUESTION");
        }
        if(request.quesType.equals("PARA") || request.quesType.equals("NOT_PARA")){
            response.otherHits = getQuestionsCount(type,request);
            response.otherType = type;
        }else{
            response.nonParaHits = getQuestionsCount("NOT_PARA",request);
            response.paraHits = getQuestionsCount("PARA",request);
        }


        Gson gson = new Gson();
        //LOGGER.debug(":::::: Details : "+gson.toJson(details, (new TypeToken<List<CMDSResourceDetails>>(){}).getType()));

        List<CMDSContentLink> links = new ArrayList<CMDSContentLink>();

        MutableLong totalHits = new MutableLong(0L);
        CMDSContentLink link = null;
        for (CMDSResourceDetails detail : details) {
//            SrcEntity folderEntity = new SrcEntity(EntityType.FOLDER, request.folderId);

            List<CMDSContentLink> testLinks = CmdsContentLinkDAO.INSTANCE.getCmdsContentLinks(
                    detail.content, null, null, null, 0, 1,
                    totalHits);
            if (CollectionUtils.isNotEmpty(testLinks)) {
                link = testLinks.get(0);
                link.target.type = EntityType.FOLDER;
                link.target.id = folderId;
                links.add(link);
            } else {
                SrcEntity target = new SrcEntity(EntityType.FOLDER, folderId);
                CMDSContentLink contentLinkage = new CMDSContentLink(target, detail.content);

                contentLinkage.userId = detail.userId;
                contentLinkage.linkType = CmdsContentLinkType.ADDED;
                // this is ever increasing number for all positions
                contentLinkage.position = CounterDAO.INSTANCE.getNextSequence(
                        CmdsContentLinkDAO.INSTANCE.getCollection().getName(),
                        CmdsContentLinkDAO.getCounterName(contentLinkage, CMDSContentLink.POSITION), 1);
                LOGGER.debug(" Created linkage : " + contentLinkage);
                CmdsContentLinkDAO.INSTANCE.save(contentLinkage);
                link = contentLinkage;
                link.target = target;
                links.add(link);
            }

        }
        //LOGGER.debug(":::::: Links : "+gson.toJson(links, (new TypeToken<List<CMDSContentLink>>(){}).getType()));
        basicInfos = AbstractCMDSContentManager.getBasicInfoFromLinks(links, basicInfos);
        response.list.addAll(basicInfos);
        //LOGGER.debug(":::::: BasicInfos : "+gson.toJson(basicInfos, (new TypeToken<List<ModelBasicInfo>>(){}).getType()));

        response.folderInfo = CMDSFolderDAO.INSTANCE.getBasicInfo(request.folderId);
        CMDSFolderDAO.INSTANCE.annotateParentInfo((CMDSFolderInfo) response.folderInfo,
                request.folderId);

        return response;

    }

    public static List<CMDSResourceDetails> getParagraphQuestions(GetResourcesReq request) {
        CMDSQuestion paragraphQuestion = new CMDSQuestion();
        List<CMDSResourceDetails> details = new ArrayList<CMDSResourceDetails>();
        try {
            paragraphQuestion = CMDSQuestionDAO.INSTANCE.getQuestionById(request.paraId);
        } catch (VedantuException e) {
            LOGGER.debug("Exception while retreiveing data : "+e.getMessage());
        }
        List<String> paraQuesIds = paragraphQuestion.paraIds;
        if(paraQuesIds.size() > 0){
            return AbstractCMDSContentManager.getBasicInfoOFParaQuestionFromESSearch(paraQuesIds);
        }else{
            return details;
        }
    }

    public static int getQuestionsCount(String type, GetResourcesReq request){
        int hits = 0;

        SrcEntity contentSrc = new SrcEntity(EntityType.ORGANIZATION, request.orgId);

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(QueryBuilders.termQuery(ConstantsGlobal.CONTENT_SRC + "."
                + ConstantsGlobal.ID, contentSrc.id));
        boolQuery.must(QueryBuilders.termQuery(ConstantsGlobal.CONTENT_SRC + "."
                + ConstantsGlobal.TYPE, contentSrc.type.name().toLowerCase()));
        String learnpediaId = Play.application().configuration().getString("learnpedia.id");
        if(!request.orgId.equals(learnpediaId)){
            if(!includeLearnpediaQuestions(learnpediaId,request.orgId)){
                boolQuery.must(QueryBuilders.termQuery("scope", "org"));
            }
        }

        BoolFilterBuilder boardFilterBuilder = FilterBuilders.boolFilter();
        // oring all boards assuming boards are of only one level
        ElasticSearchUtils.addBoardAndTargetFilter(request.brdIds, false, boardFilterBuilder);

        TermsQueryBuilder questionTypeBuilder;
        if(type.equals("PARA")){
            questionTypeBuilder = QueryBuilders.inQuery("type", "text");
        }else if(type.equals("NOT_PARA")){
            questionTypeBuilder = QueryBuilders.inQuery("type", "scq","mcq","numeric","matrix","subjective");
        }else{
            questionTypeBuilder = QueryBuilders.inQuery("type", "scq","mcq","numeric","text","matrix","subjective");
        }

        boolQuery.must(questionTypeBuilder);

        QueryBuilder query = QueryBuilders
                .filteredQuery(
                        boolQuery,
                        CollectionUtils.isEmpty(request.brdIds) ? FilterBuilders
                                .matchAllFilter() : boardFilterBuilder);

        SearchResponse questionsResponse = ElasticSearchUtils.getSearchResponse(
                query, "", "", 0, 0, EntityType.CMDSQUESTION.getIndexName(),
                EntityType.CMDSQUESTION.getIndexType().toLowerCase(), null, false,
                (AbstractFacetBuilder[]) null);
        if (questionsResponse == null || questionsResponse.getHits().getTotalHits() == 0) {
            CMDSResourcesManager.LOGGER.error("empty search response for questions query : "+type);
            return 0;
        }
        hits = (int) questionsResponse.getHits().totalHits();
        return hits;
    }

    public static boolean includeLearnpediaQuestions(String learnpediaId, String orgId) {
        // TODO Auto-generated method stub
        BoardMapping mapping = BoardMappingsDAO.INSTANCE.getBySharedToOrgId(learnpediaId, orgId);
        if(mapping != null){
            return mapping.publish;
        }
        return false;
    }

    public static GetResourcesRes getQuestionsCount(GetResourcesReq request) throws VedantuException{

        LOGGER.info(":::::::::   Inside Get Questions Count   ::::::::::");

        GetResourcesRes resp = new GetResourcesRes();
        resp.totalHits = getQuestionsCount("NOT_PARA",request);
        resp.paraHits = getQuestionsCount("PARA",request);
        return resp;
    }

    @Override
    public void prePublish(SrcEntity content) {

    }

    @Override
    public void postPublish(VedantuBaseMongoModel model) {

    }

    @Override
    protected VedantuBaseMongoModel publish(String userId, String orgId, SrcEntity content)
            throws VedantuException {

        return null;
    }

    @Override
    protected VedantuBaseMongoModel delete(String userId, String orgId, SrcEntity content)
            throws VedantuException {

        return null;
    }

    // public static collectInfo() {
    // BasicDBObject findAllSections = new BasicDBObject();
    //
    // findAllSections.put("target.type", "FOLDER");
    // BasicDBObject matchClause = new BasicDBObject();
    //
    // BasicDBObject match = new BasicDBObject();
    // match.put("$match", findAllSections);
    //
    // BasicDBObject groupResult = new BasicDBObject();
    // groupResult.append("_id", "$source.type");
    // groupResult.append("total", new BasicDBObject("$sum", 1));
    // // groupResult.append( "total",new BasicDBObject("$sum", 1));
    // BasicDBObject grouping = new BasicDBObject();
    // grouping.put("$group", groupResult);
    //
    // AggregationOutput output = table.aggregate(match, grouping);
    // for (DBObject object : output.results())
    // System.out.println(object);
    //
    // }

    /** localfile system simulator for uploading to s3 */

    public static boolean upload(UploadCMDSContentFileReq request) throws VedantuException {

        if (request.entityType == EntityType.UNKNOWN) {
            throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE);
        }
        AbstractEntityFileStorage storage = EntityStorageFactory.INSTANCE.get(request.entityType);

        try {
            storage.storeInFS(request.file, request.key, null);
        } catch (EntityFileStorageException e) {
            throw new VedantuException(VedantuErrorCode.UPLOAD_ERROR);
        }

        return true;
    }

    public static SignUploadFileRes sign(SignUploadFileReq request) throws VedantuException {

        SignUploadFileRes response = null;
        AbstractEntityFileStorage storage = EntityStorageFactory.INSTANCE.get(request.type);
        if (storage == null) {
            throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE);
        }
        @SuppressWarnings("unchecked")
        VedantuBasicDAO<VedantuBaseMongoModel, ObjectId> basicDAO = EntityTypeDAOFactory.INSTANCE
                .get(request.type);
        if (basicDAO == null) {
            throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE);
        }

        VedantuBaseMongoModel basicMongoModel = basicDAO.instantiate();

        if (basicMongoModel == null) {
            throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE);
        }
        basicMongoModel.recordState = VedantuRecordState.TEMPORARY;

        String uuid = UUID.randomUUID().toString();

        IFileSystemHandler fsSystemHandler = FileSystemFactory.INSTANCE.getFS();

        String s3Key = AbstractEntityFileStorage.computeFileId(uuid, request.type,
                FileUtils.getExtensionWithoutDOT(request.fileName), request.mediaType,
                FileCategory.ORIGINAL, null);
        String contentType = ContentTypeMapper.get().getContentType(request.fileName);
        if (org.apache.commons.lang3.StringUtils.isEmpty(contentType)) {
            throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE);
        }
        try {
            response = fsSystemHandler.signContentUpload(request.type, storage.getStorageId(),
                    s3Key, contentType);
        } catch (FileStoreException e) {
            throw new VedantuException(VedantuErrorCode.UPLOAD_ERROR);
        }
        // TODO clean this one usng AbstractFileModel
        if (basicMongoModel instanceof CMDSVideo) {
            CMDSVideo video = (CMDSVideo) basicMongoModel;
            video.originalFileName = request.fileName;
            video.extension = FileUtils.getExtensionWithoutDOT(request.fileName);
            video.uuid = uuid;
        } else if (basicMongoModel instanceof CMDSDocument) {
            CMDSDocument doc = (CMDSDocument) basicMongoModel;
            doc.originalFileName = request.fileName;
            doc.extension = FileUtils.getExtensionWithoutDOT(request.fileName);
            doc.uuid = uuid;
        } else if (basicMongoModel instanceof CMDSFile) {
            CMDSFile doc = (CMDSFile) basicMongoModel;
            doc.originalFileName = request.fileName;
            doc.extension = FileUtils.getExtensionWithoutDOT(request.fileName);
            doc.uuid = uuid;
        }
        // TODO testing
        request.url = null;
        if (StringUtils.isNotEmpty(request.url)) {
            response.url = request.url;
        }
        basicDAO.save(basicMongoModel);
        response.id = basicMongoModel._getStringId();
        response.uuid = uuid;
        return response;
    }

    public static GetSharedQuestionsBasicInfoRes getQuestionSharingBasicInfo(GetSharedQuestionsBasicInfoReq request) throws VedantuException {
        GetSharedQuestionsBasicInfoRes response = new GetSharedQuestionsBasicInfoRes();
        List<ObjectId> orgIds = new ArrayList<ObjectId>();

        MutableLong totalHits = new MutableLong(0L);
        List<Organization> organizations = OrganizationDAO.INSTANCE.getAllOrganizations(null, totalHits);
        Map<String,String> orgNameMap = new HashMap<String,String>();
        for(Organization org: organizations){
            orgNameMap.put(org._getStringId(), org.fullName);
        }
        List<BoardMapping> boardMappings = BoardMappingsDAO.INSTANCE.getByParentOrgId(request.orgId);
        List<OrgDetails> sharedOrgDetails = new ArrayList<OrgDetails>();
        boolean showSharedSubjects = OrganizationDAO.INSTANCE.getById(request.orgId).showSharedSubjects;
        List<Board> boards = BoardDAO.INSTANCE.getAllCourses(request.orgId, showSharedSubjects);
        for(BoardMapping boardMapping : boardMappings){
            List<ObjectId> boardIds = new ArrayList<ObjectId>();
            OrgDetails orgDetail = new OrgDetails();
            orgDetail.orgId = boardMapping.sharedToOrgId;
            orgDetail.orgName = orgNameMap.get(boardMapping.sharedToOrgId);
            orgDetail.publishStatus = boardMapping.publish;
            List<SharedBoardInfo> sharedBoards = new ArrayList<SharedBoardInfo>();
            List<BoardMappings> boardmappings = boardMapping.boardMappings;
            for(BoardMappings board : boardmappings){
                if(board.boardType.equalsIgnoreCase("course")){
                    SharedBoardInfo sbinfo = new SharedBoardInfo();
                    sbinfo.parentBoardId = board.parentBoardId;
                    sbinfo.sharedBoardId = board.sharedToBoardId;
                    sbinfo.parentBoardName = BoardDAO.INSTANCE.getById(board.parentBoardId).name;
                    sbinfo.sharedBoardName = BoardDAO.INSTANCE.getById(board.sharedToBoardId).name;
                    sbinfo.status = board.status;
                    sharedBoards.add(sbinfo);
                    boardIds.add(new ObjectId(board.parentBoardId));
                }
            }
            orgDetail.sharedBoards = sharedBoards;
            if(!boardIds.isEmpty()){
                List<Board> tempBoards = BoardDAO.INSTANCE.getAllCoursesExcept(boardIds,request.orgId);
                for(Board board : tempBoards){
                    orgDetail.unSharedBoards.add(board.name);
                }
            }else{
                for(Board board : boards){
                    orgDetail.unSharedBoards.add(board.name);
                }
            }
            sharedOrgDetails.add(orgDetail);
            orgIds.add(new ObjectId(boardMapping.sharedToOrgId));
        }
        response.sharedOrgDetails = sharedOrgDetails;
        if(!orgIds.isEmpty()){
            organizations.clear();
            organizations = OrganizationDAO.INSTANCE.getAllOrganizationsExcept(orgIds);
        }
        List<OrgDetails> orgDetails = new ArrayList<OrgDetails>();
        for(Organization org: organizations){
            OrgDetails orgDetail = new OrgDetails();
            orgDetail.orgId = org._getStringId();
            orgDetail.orgName = org.name;
            orgDetails.add(orgDetail);
        }
        response.orgDetails = orgDetails;
        return response;
    }

    public static AddMappingsRes getBoardsToAddMappings(AddMappingsReq request) throws VedantuException {
        AddMappingsRes response = new AddMappingsRes();
        BoardMapping boardMapping = BoardMappingsDAO.INSTANCE.getBySharedToOrgId(request.parentOrgId,request.targetOrgId);
        boolean showSharedSubjects = OrganizationDAO.INSTANCE.getById(request.parentOrgId).showSharedSubjects;
        List<Board> parentCourseBoards = BoardDAO.INSTANCE.getAllCourses(request.parentOrgId, showSharedSubjects);
        LOGGER.debug("Size of parentCourseBoards is "+parentCourseBoards.size());
        showSharedSubjects = OrganizationDAO.INSTANCE.getById(request.targetOrgId).showSharedSubjects;
        List<Board> targetCourseBoards = BoardDAO.INSTANCE.getAllCourses(request.targetOrgId, showSharedSubjects);
        LOGGER.debug("Size of targetCourseBoards is "+targetCourseBoards.size());
        Map<String, String> parentBoardMap = new HashMap<String, String>();
        Map<String, String> sharedBoardMap = new HashMap<String, String>();
        if(boardMapping != null){
            List<BoardMappings> boardMappings = boardMapping.boardMappings;
            for(BoardMappings boardMap : boardMappings){
                if(boardMap.boardType.equalsIgnoreCase("COURSE")){
                    parentBoardMap.put(boardMap.parentBoardId, boardMap.sharedToBoardId);
                    sharedBoardMap.put(boardMap.sharedToBoardId, boardMap.parentBoardId);
                }
            }
        }
        LOGGER.debug("Size of parentBoardMap is "+parentBoardMap.size());
        LOGGER.debug("Size of sharedBoardMap is "+sharedBoardMap.size());
        List<BoardInfo> boardDetails = new ArrayList<BoardInfo>();
        for(Board board: parentCourseBoards){
            BoardInfo parentBoardDetail = new BoardInfo();
            if(!parentBoardMap.containsKey(board._getStringId())){
                LOGGER.debug(board.name+" is not mapped");
                List<BoardInfo> childrenBoardDetail = new ArrayList<BoardInfo>();
                List<Board> childBoards = BoardDAO.INSTANCE.getAllChildren(request.parentOrgId,board._getStringId());
                LOGGER.debug("Size of the children of board "+board.name+" is "+childBoards.size());
                parentBoardDetail.boardId = board._getStringId();
                parentBoardDetail.boardName = board.name;
                parentBoardDetail.boardtype = board.type.name();
                for(Board brd : childBoards){
                    BoardInfo brdInfo = new BoardInfo();
                    brdInfo.boardId = brd._getStringId();
                    brdInfo.boardName = brd.name;
                    brdInfo.boardtype = brd.type.toString();
                    childrenBoardDetail.add(brdInfo);
                }
                LOGGER.debug("Size of the children board we created is "+childrenBoardDetail.size());
                parentBoardDetail.children = childrenBoardDetail;
                boardDetails.add(parentBoardDetail);
            }
        }
        response.parentBoardDetails.addAll(boardDetails);
        LOGGER.debug("Size of parentBoardDetails is "+response.parentBoardDetails.size());
        boardDetails.clear();
        for(Board board: targetCourseBoards){
            BoardInfo sharedBoardDetail = new BoardInfo();
            if(!sharedBoardMap.containsKey(board._getStringId())){
                LOGGER.debug(board.name+" is not mapped");
                List<BoardInfo> childrenBoardDetail = new ArrayList<BoardInfo>();
                List<Board> childBoards = BoardDAO.INSTANCE.getAllChildren(request.targetOrgId,board._getStringId());
                LOGGER.debug("Size of the children of board "+board.name+" is "+childBoards.size());
                sharedBoardDetail.boardId = board._getStringId();
                sharedBoardDetail.boardName = board.name;
                sharedBoardDetail.boardtype = board.type.toString();
                for(Board brd : childBoards){
                    BoardInfo brdInfo = new BoardInfo();
                    brdInfo.boardId = brd._getStringId();
                    brdInfo.boardName = brd.name;
                    brdInfo.boardtype = brd.type.toString();
                    childrenBoardDetail.add(brdInfo);
                }
                LOGGER.debug("Size of the children board we created is "+childrenBoardDetail.size());
                sharedBoardDetail.children = childrenBoardDetail;
                boardDetails.add(sharedBoardDetail);
            }
        }
        response.targetBoardDetails.addAll(boardDetails);
        LOGGER.debug("Size of targetBoardDetails is "+response.targetBoardDetails.size());
        return response;
    }

    public static SaveMappingRes saveBoardMapping(SaveMappingsReq request) throws VedantuException {
        SaveMappingRes response = new SaveMappingRes();
        Organization parentOrg = OrganizationDAO.INSTANCE.getById(request.parentOrgId);
        Organization sharedToOrg = OrganizationDAO.INSTANCE.getById(request.sharedToOrgId);
        if(parentOrg == null || sharedToOrg == null){
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND,"Invalid Organisation Info");
        }
        BoardMapping boardMapping = BoardMappingsDAO.INSTANCE.getBySharedToOrgId(request.parentOrgId, request.sharedToOrgId);
        if(boardMapping == null){
            boardMapping = new BoardMapping();
            boardMapping.orgId = request.orgId;
            boardMapping.userId = request.userId;
            boardMapping.parentOrgId = request.parentOrgId;
            boardMapping.sharedToOrgId = request.sharedToOrgId;
        }
        boardMapping.boardMappings.addAll(request.boardMappings);
        response.saved = BoardMappingsDAO.INSTANCE.saveBoardMappings(boardMapping);;
        return response;
    }

    public static SaveMappingRes deleteBoardMapping(DeleteMappingReq request) throws VedantuException {
        SaveMappingRes response = new SaveMappingRes();
        Organization parentOrg = OrganizationDAO.INSTANCE.getById(request.parentOrgId);
        Organization sharedToOrg = OrganizationDAO.INSTANCE.getById(request.sharedToOrgId);
        if(parentOrg == null || sharedToOrg == null){
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND,"Invalid Organisation Info");
        }
        BoardMapping boardMappings = BoardMappingsDAO.INSTANCE.getBySharedToOrgId(request.parentOrgId, request.sharedToOrgId);
        if(boardMappings == null){
            throw new VedantuException(VedantuErrorCode.ORGANISATION_MAPPING_NOT_FOUND,"No Organisation Mapping Found");
        }
        String parentBoardId = request.parentBoardId;
        String sharedToBoardId = request.sharedToBoardId;
        List<Board> childrenBoards = BoardDAO.INSTANCE.getAllChildren(request.parentOrgId,parentBoardId);
        List<String> childBoards = getIdsOfchildBoards(childrenBoards);
        List<BoardMappings> newBoardMappings = new ArrayList<BoardMappings>();
        boolean mappingFound = false;
        for(BoardMappings boardMapping : boardMappings.boardMappings) {
            if(boardMapping.boardType.equalsIgnoreCase("COURSE") && boardMapping.parentBoardId.equals(parentBoardId) && boardMapping.sharedToBoardId.equals(sharedToBoardId)) {
                if(boardMapping.status == false){
                    mappingFound = true;
                    break;
                }else{
                    throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_REMOVED,"Cannot Remove Board Mappings");
                }
            }
        }
        if(!mappingFound){
            throw new VedantuException(VedantuErrorCode.BOARD_MAPPING_NOT_FOUND,"No Board Mapping Found");
        }
        for(BoardMappings boardMapping : boardMappings.boardMappings) {
            if(boardMapping.boardType.equalsIgnoreCase("COURSE") && boardMapping.parentBoardId.equals(parentBoardId) && boardMapping.sharedToBoardId.equals(sharedToBoardId)) {
                // Dont add this mapping(COURSE) to new board
            }
            else if(boardMapping.boardType.equalsIgnoreCase("TOPIC") && childBoards.contains(boardMapping.parentBoardId)){
                // Dont add this mapping(TOPIC) to new board
            }else{
                newBoardMappings.add(boardMapping);
            }
        }
        boardMappings.boardMappings = newBoardMappings;
        response.saved = BoardMappingsDAO.INSTANCE.saveBoardMappings(boardMappings);
        return response;
    }

    public static SaveMappingRes visibleBoardMapping(VisibleMappingReq request) throws VedantuException {
        SaveMappingRes response = new SaveMappingRes();
        Organization parentOrg = OrganizationDAO.INSTANCE.getById(request.parentOrgId);
        Organization sharedToOrg = OrganizationDAO.INSTANCE.getById(request.sharedToOrgId);
        if(!request.isSelfVisible && (parentOrg == null || sharedToOrg == null)){
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND,"Invalid Organisation Info");
        }
        BoardMapping boardMappings = BoardMappingsDAO.INSTANCE.getBySharedToOrgId(request.parentOrgId, request.sharedToOrgId);
        if(boardMappings == null){
            throw new VedantuException(VedantuErrorCode.ORGANISATION_MAPPING_NOT_FOUND,"No Organisation Mapping Found");
        }
        boardMappings.publish = request.visible;
        response.saved = BoardMappingsDAO.INSTANCE.saveBoardMappings(boardMappings);
        return response;
    }

    public static List<ShareMappingResponse> shareBoardMapping(DeleteMappingReq request) throws VedantuException, JSONException {
        List<ShareMappingResponse> response = new ArrayList<ShareMappingResponse>();
        Organization parentOrg = OrganizationDAO.INSTANCE.getById(request.parentOrgId);
        Organization sharedToOrg = OrganizationDAO.INSTANCE.getById(request.sharedToOrgId);
        if(parentOrg == null || sharedToOrg == null){
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND,"Invalid Organisation Info");
        }
        BoardMapping boardMappings = BoardMappingsDAO.INSTANCE.getBySharedToOrgId(request.parentOrgId, request.sharedToOrgId);
        if(boardMappings == null){
            throw new VedantuException(VedantuErrorCode.ORGANISATION_MAPPING_NOT_FOUND,"No Organisation Mapping Found");
        }
        String parentBoardId = request.parentBoardId;
        String sharedToBoardId = request.sharedToBoardId;
        List<Board> childrenBoards = BoardDAO.INSTANCE.getAllChildren(request.parentOrgId,parentBoardId);
        List<Board> sharedChildBoards = BoardDAO.INSTANCE.getAllChildren(request.sharedToOrgId,sharedToBoardId);
        if(sharedChildBoards.size() < childrenBoards.size()){
            throw new VedantuException(VedantuErrorCode.TOPIC_NOT_FOUND);
        }
        List<String> childBoards = getIdsOfchildBoards(childrenBoards);
        boolean shareMapping = false;
        for(BoardMappings boardMapping : boardMappings.boardMappings) {
            if(boardMapping.boardType.equalsIgnoreCase("COURSE") && boardMapping.parentBoardId.equals(parentBoardId) && boardMapping.sharedToBoardId.equals(sharedToBoardId)) {
                if(boardMapping.status == false){
                    shareMapping = true;
                    break;
                }else if(boardMapping.status == true && request.reSync == true){
                    shareMapping = true;
                    break;
                }else{
                    throw new VedantuException(VedantuErrorCode.ALREADY_SHARED,"Board Mapping already shared");
                }
            }
        }
        if(!shareMapping) {
            throw new VedantuException(VedantuErrorCode.BOARD_MAPPING_NOT_FOUND,"Unable To Share Questions");
        }else{
            // Implement logic to share questions
            String sharedToOrgUserId = OrganizationDAO.INSTANCE.getById(request.sharedToOrgId).adminUserId;
            String sharedToOrgId = request.sharedToOrgId;
            Map<String,String> boardIdsMap = new HashMap<String,String>();
            // Inserting course level boards into map
            boardIdsMap.put(parentBoardId, sharedToBoardId);
            // Inserting topic level boards into map
            for(BoardMappings boardMapping : boardMappings.boardMappings){
                if(boardMapping.boardType.equalsIgnoreCase("TOPIC") && childBoards.contains(boardMapping.parentBoardId)) {
                    boardIdsMap.put(boardMapping.parentBoardId, boardMapping.sharedToBoardId);
                }
            }
            if((boardIdsMap.size()-1) != childBoards.size()){
                throw new VedantuException(VedantuErrorCode.INCOMPLETE_BOARD_MAPPING);
            }
            JSONObject info = new JSONObject();
            info.put("parentOrgId", request.parentOrgId);
            info.put("sharedToOrgUserId", sharedToOrgUserId);
            info.put("sharedToOrgId", sharedToOrgId);
            info.put("boardIdsMap", new JSONObject(boardIdsMap));
            info.put("parentBoardId", parentBoardId);
            info.put("sharedToBoardId", sharedToBoardId);
            // Now get list of all learnpedias cmds questions from MONGO and create a new request with target organisation entries and add to cmds questions table
            List<String> boardIds = new ArrayList<String>();
            boardIds.add(parentBoardId);
            // Creating job for SCQ Questions
            List<String> types = new ArrayList<String>();
            types.add(QuestionType.SCQ.toString());
            long totalSCQQuestionsToMap = CMDSQuestionDAO.INSTANCE.countByBoard(request.parentOrgId,request.sharedToOrgId, boardIds,types);
            LOGGER.debug("shareBoardMapping : Total SCQ questions to share is "+totalSCQQuestionsToMap);
            info.put("QType", "SCQ");
            if(totalSCQQuestionsToMap > 0){
                LOGGER.debug("shareBoardMapping : Creating New Job SCQ");
                EntityOperationStatus job = new EntityOperationStatus();
                job.numOfSteps = (int)totalSCQQuestionsToMap;
                job.oType = OperationType.CMDS_QUESTION_SHARING;
                job.message = info.toString();
                EntityOperationStatusDAO.INSTANCE.save(job);
                ShareMappingResponse resp = new ShareMappingResponse();
                resp.jobId = job._getStringId();
                resp.QType = "SCQ";
                response.add(resp);
                ShareQuestionsThread thread = new ShareQuestionsThread(job._getStringId());
                thread.start();
            }
            // Creating Job for MCQ Questions
            types.clear();
            types.add(QuestionType.MCQ.toString());
            long totalMCQQuestionsToMap = CMDSQuestionDAO.INSTANCE.countByBoard(request.parentOrgId,request.sharedToOrgId, boardIds,types);
            LOGGER.debug("shareBoardMapping : Total MCQ questions to share is "+totalMCQQuestionsToMap);
            info.put("QType", "MCQ");
            if(totalMCQQuestionsToMap > 0){
                LOGGER.debug("shareBoardMapping : Creating New Job MCQ");
                EntityOperationStatus job = new EntityOperationStatus();
                job.numOfSteps = (int)totalMCQQuestionsToMap;
                job.oType = OperationType.CMDS_QUESTION_SHARING;
                job.message = info.toString();
                EntityOperationStatusDAO.INSTANCE.save(job);
                ShareMappingResponse resp = new ShareMappingResponse();
                resp.jobId = job._getStringId();
                resp.QType = "MCQ";
                response.add(resp);
                ShareQuestionsThread thread = new ShareQuestionsThread(job._getStringId());
                thread.start();
            }
            // Creating Job for MATRIX Questions
            types.clear();
            types.add(QuestionType.MATRIX.toString());
            long totalMatrixQuestionsToMap = CMDSQuestionDAO.INSTANCE.countByBoard(request.parentOrgId,request.sharedToOrgId, boardIds,types);
            LOGGER.debug("shareBoardMapping : Total Matrix questions to share is "+totalMatrixQuestionsToMap);
            info.put("QType", "MATRIX");
            if(totalMatrixQuestionsToMap > 0){
                LOGGER.debug("shareBoardMapping : Creating New Job MATRIX");
                EntityOperationStatus job = new EntityOperationStatus();
                job.numOfSteps = (int)totalMatrixQuestionsToMap;
                job.oType = OperationType.CMDS_QUESTION_SHARING;
                job.message = info.toString();
                EntityOperationStatusDAO.INSTANCE.save(job);
                ShareMappingResponse resp = new ShareMappingResponse();
                resp.jobId = job._getStringId();
                resp.QType = "MATRIX";
                response.add(resp);
                ShareQuestionsThread thread = new ShareQuestionsThread(job._getStringId());
                thread.start();
            }
            // Creating Job For NUMERIC Questions
            types.clear();
            types.add(QuestionType.NUMERIC.toString());
            long totalNUMERICQuestionsToMap = CMDSQuestionDAO.INSTANCE.countByBoard(request.parentOrgId,request.sharedToOrgId, boardIds,types);
            LOGGER.debug("shareBoardMapping : Total NUMERIC questions to share is "+totalNUMERICQuestionsToMap);
            info.put("QType", "NUMERIC");
            if(totalNUMERICQuestionsToMap > 0){
                LOGGER.debug("shareBoardMapping : Creating New Job NUMERIC");
                EntityOperationStatus job = new EntityOperationStatus();
                job.numOfSteps = (int)totalNUMERICQuestionsToMap;
                job.oType = OperationType.CMDS_QUESTION_SHARING;
                job.message = info.toString();
                EntityOperationStatusDAO.INSTANCE.save(job);
                ShareMappingResponse resp = new ShareMappingResponse();
                resp.jobId = job._getStringId();
                resp.QType = "NUMERIC";
                response.add(resp);
                ShareQuestionsThread thread = new ShareQuestionsThread(job._getStringId());
                thread.start();
            }
            // Creating Job For TEXT Questions
            types.clear();
            types.add(QuestionType.TEXT.toString());
            long totalTEXTQuestionsToMap = CMDSQuestionDAO.INSTANCE.countByBoard(request.parentOrgId,request.sharedToOrgId, boardIds,types);
            LOGGER.debug("shareBoardMapping : Total TEXT questions to share is "+totalTEXTQuestionsToMap);
            info.put("QType", "TEXT");
            if(totalTEXTQuestionsToMap > 0){
                LOGGER.debug("shareBoardMapping : Creating New Job TEXT");
                EntityOperationStatus job = new EntityOperationStatus();
                job.numOfSteps = (int)totalTEXTQuestionsToMap;
                job.oType = OperationType.CMDS_QUESTION_SHARING;
                job.message = info.toString();
                EntityOperationStatusDAO.INSTANCE.save(job);
                ShareMappingResponse resp = new ShareMappingResponse();
                resp.jobId = job._getStringId();
                resp.QType = "TEXT";
                response.add(resp);
                ShareQuestionsThread thread = new ShareQuestionsThread(job._getStringId());
                thread.start();
            }

            if(request.addNewPara){
             // Creating Job For PARA Questions
                types.clear();
                types.add(QuestionType.PARA.toString());
                long totalPARAQuestionsToMap = CMDSQuestionDAO.INSTANCE.countByBoard(request.parentOrgId,request.sharedToOrgId, boardIds,types);
                LOGGER.debug("shareBoardMapping : Total PARA questions to share is "+totalPARAQuestionsToMap);
                info.put("QType", "PARA");
                if(totalPARAQuestionsToMap > 0){
                    LOGGER.debug("shareBoardMapping : Creating New Job PARA");
                    EntityOperationStatus job = new EntityOperationStatus();
                    job.numOfSteps = (int)totalPARAQuestionsToMap;
                    job.oType = OperationType.CMDS_QUESTION_SHARING;
                    job.message = info.toString();
                    EntityOperationStatusDAO.INSTANCE.save(job);
                    ShareMappingResponse resp = new ShareMappingResponse();
                    resp.jobId = job._getStringId();
                    resp.QType = "PARA";
                    response.add(resp);
                    ShareQuestionsThread thread = new ShareQuestionsThread(job._getStringId());
                    thread.start();
                }
            }

            for (BoardMappings boardMapping : boardMappings.boardMappings) {
                if (boardMapping.boardType.equalsIgnoreCase("COURSE")
                        && boardMapping.parentBoardId.equals(info
                                .getString("parentBoardId"))
                        && boardMapping.sharedToBoardId.equals(info
                                .getString("sharedToBoardId"))) {
                    if (boardMapping.status == false) {
                        boardMapping.status = true;
                        break;
                    }
                }
            }
            BoardMappingsDAO.INSTANCE.saveBoardMappings(boardMappings);
        }
        return response;
    }

    private static List<String> getIdsOfchildBoards(List<Board> childBoards) {
        // TODO Auto-generated method stub
        List<String> childrenIds = new ArrayList<String>();
        for(Board board : childBoards){
            childrenIds.add(board._getStringId());
        }
        return childrenIds;
    }
}
