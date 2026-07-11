package com.vedantu.cmds.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.daos.CMDSFileDAO;
import com.vedantu.cmds.daos.CMDSFolderDAO;
import com.vedantu.cmds.daos.CMDSModuleDAO;
import com.vedantu.cmds.enums.CmdsContentLinkType;
import com.vedantu.cmds.models.CMDSFile;
import com.vedantu.cmds.models.CMDSFolder;
import com.vedantu.cmds.models.event.details.FileProcessingDetails;
import com.vedantu.cmds.models.event.search.details.CMDSFileSearchIndexDetails;
import com.vedantu.cmds.pojos.requests.files.ConfirmFileUploadReq;
import com.vedantu.cmds.pojos.requests.files.GetCMDSFileReq;
import com.vedantu.cmds.pojos.responses.files.GetCMDSFileRes;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.daos.EntityOperationStatusDAO;
import com.vedantu.commons.entity.factory.EntityStorageFactory;
import com.vedantu.commons.entity.storage.AbstractEntityFileStorage;
import com.vedantu.commons.entity.storage.CMDSFileStorage;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.entity.storage.IEntityFileStorage;
import com.vedantu.commons.entity.storage.ImageSize;
import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.OperationType;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.fs.exception.FileStoreException;
import com.vedantu.commons.fs.handlers.FileSystemFactory;
import com.vedantu.commons.models.mongo.EntityOperationStatus;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.commons.utils.ImageDisplayURLUtil;
import com.vedantu.content.daos.LibraryContentLinksDAO;
import com.vedantu.content.managers.FileManager;
import com.vedantu.content.models.AbstractBoardEntityTagModel;
import com.vedantu.content.models.AbstractContentModel;
import com.vedantu.content.models.AbstractFileModel;
import com.vedantu.content.pojos.SrcType.LinkType;
import com.vedantu.content.pojos.requests.EditContentReq;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuRecordState;

public class CMDSFileManager extends AbstractCMDSContentManager {

    public static CMDSFileManager INSTANCE = new CMDSFileManager();

    private final static ALogger  LOGGER   = Logger.of(CMDSFileManager.class);

    public CMDSFileManager() {

    }

    public GetCMDSFileRes confirm(ConfirmFileUploadReq request) throws VedantuException {

        CMDSFile cmdsFile = null;
        if (StringUtils.isEmpty(request.orgId)) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
        }

        if (StringUtils.isEmpty(request.userId)) {
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }

        if (request.type == null
                || !(request.type == LinkType.UPLOADED || request.type == LinkType.ADDED)) {
            throw new VedantuException(VedantuErrorCode.INVALID_TYPE);
        }

        if (request.type != LinkType.UPLOADED) {
            throw new VedantuException(VedantuErrorCode.INVALID_TYPE);
        }

        cmdsFile = CMDSFileDAO.INSTANCE.getById(request.fileId, VedantuRecordState.TEMPORARY);

        if (StringUtils.isEmpty(request.originalFileName) || StringUtils.isEmpty(request.uuid)) {
            throw new VedantuException(VedantuErrorCode.FILE_NOT_FOUND);
        }

        if (cmdsFile == null || !cmdsFile.uuid.equals(request.uuid)
                || !cmdsFile.originalFileName.equals(request.originalFileName)) {
            throw new VedantuException(VedantuErrorCode.FILE_NOT_FOUND);
        }
        CMDSFileStorage ds = new CMDSFileStorage();

        String computedFileId = AbstractEntityFileStorage.computeFileId(cmdsFile.uuid,
                EntityType.CMDSFILE, FileUtils.getExtensionWithoutDOT(cmdsFile.extension),
                MediaType.FILE, FileCategory.ORIGINAL, null);

        boolean fileFoundInStorage = false;
        try {
            fileFoundInStorage = FileSystemFactory.INSTANCE.getFS().exists(ds.getStorageId(),
                    request.uploadedFileName);
        } catch (FileStoreException e) {
            throw new VedantuException(VedantuErrorCode.DOCUMENT_NOT_FOUND);
        }

        if (!computedFileId.equals(request.uploadedFileName) || !fileFoundInStorage) {
            throw new VedantuException(VedantuErrorCode.DOCUMENT_NOT_FOUND);
        }
        cmdsFile.stored = true;
        cmdsFile.extension = FileUtils.getExtensionWithoutDOT(request.originalFileName);

        // generate thumnbnail

        CMDSFolder folder = null;
        if (StringUtils.isEmpty(request.folderId)) {
            folder = CMDSFolderDAO.INSTANCE.getRootFolder(request.orgId);
        } else {
            folder = CMDSFolderDAO.INSTANCE.getById(request.folderId);
        }

        if (folder == null) {
            throw new VedantuException(VedantuErrorCode.INVALID_FOLDER_ID);
        }
        cmdsFile.linkType = request.type;
        cmdsFile.difficulty = request.difficulty;
        cmdsFile.description = request.description;
        cmdsFile.userId = request.userId;
        cmdsFile.name = request.name;

        // setting tags
        cmdsFile.tags = new HashSet<String>();
        if (CollectionUtils.isNotEmpty(request.tags)) {
            cmdsFile.tags.addAll(request.tags);
        }
        // setting boardIds
        cmdsFile.boardIds = new HashSet<String>();
        if (CollectionUtils.isNotEmpty(request.brdIds)) {
            cmdsFile.boardIds.addAll(request.brdIds);
        }

        // setting targetIds
        cmdsFile.targetIds = new HashSet<String>();
        if (CollectionUtils.isNotEmpty(request.targetIds)) {
            cmdsFile.targetIds.addAll(request.targetIds);
        }
        if (StringUtils.isNotEmpty(request.orgId)) {
            cmdsFile.contentSrc = new SrcEntity(EntityType.ORGANIZATION, request.orgId);
        }
        cmdsFile.recordState = VedantuRecordState.ACTIVE;
        /* Added by Shivank */
        cmdsFile.completed = CMDSFileDAO.INSTANCE.isReadyToPublished(cmdsFile);
        /* Added by Shivank */
        CMDSFileDAO.INSTANCE.addFile(cmdsFile);

        generateEventAysc(request.userId, cmdsFile, EventActionType.ADD, EventType.INDEX_CMDS_FILE,
                UserActionType.ADDED, false);
        SrcEntity cmdsDocEntity = new SrcEntity(EntityType.CMDSFILE, cmdsFile._getStringId());
        String parentESId = AbstractCMDSContentManager.addAsCMDSResource(cmdsDocEntity,
                EventActionType.ADD, cmdsFile);
        CMDSResourcesManager.addToFolder(request.orgId, request.userId, cmdsDocEntity,
                request.folderId, CmdsContentLinkType.ADDED, parentESId);

        GetCMDSFileRes response = new GetCMDSFileRes();
        response.fromMongoModel(cmdsFile);
        annotateExtraInfo(request.userId, request.orgId, EntityType.CMDSFILE, response);
        annotateFileURLInfo(response);

        if (response.operationJobIdMap == null) {
            response.operationJobIdMap = new HashMap<OperationType, String>();
        }
        startProcessingUploadFile(cmdsFile, response.operationJobIdMap);
        return response;
    }

    private void startProcessingUploadFile(CMDSFile file,
            Map<OperationType, String> operationJobIdMap) {

        EntityOperationStatus status = new EntityOperationStatus();
        status.oType = OperationType.ENCRYPTION;
        status.id = file._getStringId();
        status.type = EntityType.CMDSFILE;

        FileProcessingDetails details = new FileProcessingDetails();
        details.fileId = file._getStringId();

        details.encryptIfNeeded = StringUtils.isNotEmpty(file.passphrase);
        status.numOfSteps += details.encryptIfNeeded ? 1 : 0;

        if (status.numOfSteps > 0) {
            EntityOperationStatusDAO.INSTANCE.save(status);
            details.jobId = status._getStringId();
            generateEventAysc(file.userId, details, EventType.PROCESS_FILE);
            operationJobIdMap.put(status.oType, status._getStringId());
        }
    }

    public GetCMDSFileRes get(GetCMDSFileReq request) throws VedantuException {

        CMDSFile file = CMDSFileDAO.INSTANCE.getById(request.id);
        GetCMDSFileRes response = new GetCMDSFileRes();
        response.fromMongoModel(file);

        annotateExtraInfo(request.userId, request.orgId, EntityType.CMDSFILE, response);
        annotateFileURLInfo(response);
        EntityOperationStatus status = EntityOperationStatusDAO.INSTANCE.getByOType(
                EntityType.CMDSFILE, file._getStringId(), OperationType.ENCRYPTION);
        if (status != null && status.recordState == VedantuRecordState.ACTIVE) {
            if (status.numOfSteps != 0 && status.numOfSteps == status.numOfStepsCompleted) {
                EntityOperationStatusDAO.INSTANCE.updateState(status, VedantuRecordState.DELETED);
            } else {

                response.operationJobIdMap.put(OperationType.ENCRYPTION, status._getStringId());
            }
        }
        return response;
    }

    private void annotateFileURLInfo(CMDSFileSearchIndexDetails file) {

        annotateLinkInfo(file);
        if (file.linkType == LinkType.UPLOADED) {
            file.url = ImageDisplayURLUtil.getEntityDownloadURL(EntityType.CMDSFILE, file.uuid,
                    file.extension, MediaType.FILE, FileCategory.ORIGINAL, file.id);
        }

        file.thumbnail = StringUtils.EMPTY;

    }

    @Override
    public boolean update(EditContentReq request) throws VedantuException {

        CMDSFile content = CMDSFileDAO.INSTANCE.getById(request.entity.id);
        if (content == null) {
            throw new VedantuException(VedantuErrorCode.CONTENT_NOT_FOUND);
        }

        List<String> updateList = new ArrayList<String>();
        if (StringUtils.isNotEmpty(request.name)
                && request.updateList.contains(EditContentReq.NAME)) {
            content.name = request.name;
            updateList.add(AbstractContentModel.NAME);
        }

        if (request.updateList.contains(EditContentReq.DESCRIPTION)) {
            content.description = request.description;
            updateList.add(AbstractFileModel.DESCRIPTION);

        }

        if (request.updateList.contains(EditContentReq.BOARD_IDS)) {
            content.boardIds = request.boardIds;
            updateList.add(AbstractBoardEntityTagModel.BOARD_IDS);

        }

        boolean updated = true;
        if (content.globalFileId != null) {
            request.entity = new SrcEntity(EntityType.FILE, content.globalFileId);
            updated = FileManager.INSTANCE.update(request);
        }

        if (updated) {
            CMDSFileDAO.INSTANCE.updateModel(content, updateList);
            CMDSModuleDAO.INSTANCE.updateModuleStatus(request.entity);
            LibraryContentLinksDAO.INSTANCE.updateLastUpdated(request.entity);
            generateEventAysc(request.userId, content, EventActionType.UPDATE,
                    EventType.INDEX_CMDS_FILE, UserActionType.UPDATED, false);

            addAsCMDSResource(request.entity, EventActionType.UPDATE, content);

        }

        return true;
    }

    @Override
    public boolean calculate(String id, boolean recalculate, VedantuBaseMongoModel... contents)
            throws VedantuException {

        List<CMDSFile> files = new ArrayList<CMDSFile>();

        if (StringUtils.isNotEmpty(id)) {
            CMDSFile question = CMDSFileDAO.INSTANCE.getById(id);

            if (question == null) {
                return false;
            }
            files.add(question);
        }

        if (contents != null && contents.length != 0) {
            for (VedantuBaseMongoModel content : contents) {
                if (content instanceof CMDSFile) {
                    files.add((CMDSFile) content);
                }
            }
        }

        // calculate question image size;
        IEntityFileStorage defs = EntityStorageFactory.INSTANCE.get(EntityType.CMDSFILE);
        for (CMDSFile file : files) {
            if (file.size.isFinalized() && !recalculate) {
                continue;
            }
            file.size.reset();
            long originalSize = defs.size(file.uuid, EntityType.CMDSFILE,
                    FileUtils.getExtensionWithoutDOT(file.originalFileName), MediaType.FILE,
                    FileCategory.ORIGINAL, ImageSize.ORIGINAL);

            long encrypted = originalSize;
            file.size.addOriginal(originalSize);
            file.size.addEncrypted(encrypted);
            file.size.finalize();
            CMDSFileDAO.INSTANCE.updateModel(file, Arrays.asList(CMDSFile.SIZE));

            if (file.globalFileId != null) {
                FileManager.INSTANCE.calculate(file.globalFileId,true);
            }
        }
        return true;

    }
}
