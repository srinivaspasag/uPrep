package com.vedantu.cmds.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.daos.CMDSDocumentDAO;
import com.vedantu.cmds.daos.CMDSFolderDAO;
import com.vedantu.cmds.daos.CMDSTestDAO;
import com.vedantu.cmds.enums.CmdsContentLinkType;
import com.vedantu.cmds.models.CMDSDocument;
import com.vedantu.cmds.models.CMDSFolder;
import com.vedantu.cmds.models.CMDSTest;
import com.vedantu.cmds.models.event.details.DocumentEncodingDetails;
import com.vedantu.cmds.models.event.search.details.CMDSDocumentSearchIndexDetails;
import com.vedantu.cmds.pojos.requests.documents.ConfirmDocumentUploadReq;
import com.vedantu.cmds.pojos.requests.documents.GetCMDSDocumentReq;
import com.vedantu.cmds.pojos.responses.documents.GetCMDSDocumentRes;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.daos.EntityOperationStatusDAO;
import com.vedantu.commons.entity.factory.EntityStorageFactory;
import com.vedantu.commons.entity.storage.CMDSDocumentFileStorage;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.entity.storage.IEntityFileStorage;
import com.vedantu.commons.entity.storage.ImageSize;
import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.OperationType;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.fs.exception.FileHandlerFactory;
import com.vedantu.commons.fs.exception.FileHandlerFactory.HandlerType;
import com.vedantu.commons.fs.exception.FileStoreException;
import com.vedantu.commons.fs.handlers.FileSystemFactory;
import com.vedantu.commons.fs.handlers.LocalFileSystemHandler;
import com.vedantu.commons.fs.managers.DownloadImageManager;
import com.vedantu.commons.models.mongo.EntityOperationStatus;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.commons.utils.ImageDisplayURLUtil;
import com.vedantu.content.daos.LibraryContentLinksDAO;
import com.vedantu.content.managers.DocumentManager;
import com.vedantu.content.pojos.SrcType.LinkType;
import com.vedantu.content.pojos.requests.EditContentReq;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuRecordState;

public class CMDSDocumentManager extends AbstractCMDSContentManager {

    public static CMDSDocumentManager INSTANCE = new CMDSDocumentManager();

    private final static ALogger      LOGGER   = Logger.of(CMDSDocumentManager.class);

    public CMDSDocumentManager() {

    }

    public GetCMDSDocumentRes confirm(ConfirmDocumentUploadReq request) throws VedantuException {

        CMDSDocument cmdsDocument = null;
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

        // TODO check if file exists and then confirm.

        if (request.type == LinkType.UPLOADED) {

            cmdsDocument = CMDSDocumentDAO.INSTANCE.getById(request.docId,
                    VedantuRecordState.TEMPORARY);

            if (cmdsDocument == null || !cmdsDocument.uuid.equals(request.uuid)
                    || !cmdsDocument.originalFileName.equals(request.originalFileName)) {
                throw new VedantuException(VedantuErrorCode.DOCUMENT_NOT_FOUND);
            }

            if (StringUtils.isEmpty(request.originalFileName) || StringUtils.isEmpty(request.uuid)) {
                throw new VedantuException(VedantuErrorCode.FILE_NOT_FOUND);
            }

            cmdsDocument.stored = true;
            cmdsDocument.extension = FileUtils.getExtensionWithoutDOT(request.originalFileName);
            CMDSDocumentFileStorage ds = new CMDSDocumentFileStorage();

            String computedFileId = CMDSDocumentFileStorage.computeFileId(cmdsDocument.uuid,
                    EntityType.CMDSDOCUMENT, cmdsDocument.extension, MediaType.DOC,
                    FileCategory.ORIGINAL, null);

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
            // generate thumnbnail
        } else if (request.type == LinkType.ADDED) {
            cmdsDocument = new CMDSDocument();
            if (StringUtils.isNotEmpty(request.thumbnail)) {
                String uuid = UUID.randomUUID().toString();
                String imageName = uuid + FileUtils.JPG_EXTENTION;
                String imageTempOutputPath = getImageLocalTempPath(imageName);
                LOGGER.info("downloading image to : " + imageTempOutputPath);

                File thumbnailFile = DownloadImageManager.downloadImage(request.thumbnail,
                        imageTempOutputPath, FileUtils.JPG_EXTENTION_WITHOUT_DOT);

                IEntityFileStorage storage = EntityStorageFactory.INSTANCE
                        .get(EntityType.CMDSVIDEO);
                Map<String, String> tags = new HashMap<String, String>();
                tags.put(ConstantsGlobal.ORG_ID, request.orgId);
                tags.put(ConstantsGlobal.ENTITY_TYPE, EntityType.CMDSVIDEO.name());

                try {
                    storage.storeImage(uuid, thumbnailFile, FileCategory.CONVERTED,
                            ImageSize.SMALL, tags);
                } catch (EntityFileStorageException e) {
                    throw new VedantuException(VedantuErrorCode.UPLOAD_ERROR);
                }
                cmdsDocument.thumbnail = uuid;
                cmdsDocument.linkInfo = request.linkInfo;
            }

            cmdsDocument.url = request.url;
        }
        if(!StringUtils.isEmpty(request.folderId)){
            CMDSFolder folder = null;
            if (StringUtils.isEmpty(request.folderId)) {
                folder = CMDSFolderDAO.INSTANCE.getRootFolder(request.orgId);
            } else {
                folder = CMDSFolderDAO.INSTANCE.getById(request.folderId);
            }

            if (folder == null) {
                throw new VedantuException(VedantuErrorCode.INVALID_FOLDER_ID);
            }
        }else if(!StringUtils.isEmpty(request.testId)){
            CMDSTest test = CMDSTestDAO.INSTANCE.getTest(request.testId);
            if (test == null) {
                throw new VedantuException(VedantuErrorCode.INVALID_FOLDER_ID);
            }
        }
        cmdsDocument.linkType = request.type;
        cmdsDocument.difficulty = request.difficulty;
        cmdsDocument.description = request.description;
        cmdsDocument.userId = request.userId;
        cmdsDocument.name = request.name;

        cmdsDocument.orientation = request.orientation;

        // setting tags
        cmdsDocument.tags = new HashSet<String>();
        if (CollectionUtils.isNotEmpty(request.tags)) {
            cmdsDocument.tags.addAll(request.tags);
        }
        // setting boardIds
        cmdsDocument.boardIds = new HashSet<String>();
        if (CollectionUtils.isNotEmpty(request.brdIds)) {
            cmdsDocument.boardIds.addAll(request.brdIds);
        }

        // setting targetIds
        cmdsDocument.targetIds = new HashSet<String>();
        if (CollectionUtils.isNotEmpty(request.targetIds)) {
            cmdsDocument.targetIds.addAll(request.targetIds);
        }
        if (StringUtils.isNotEmpty(request.orgId)) {
            cmdsDocument.contentSrc = new SrcEntity(EntityType.ORGANIZATION, request.orgId);
        }
        cmdsDocument.recordState = VedantuRecordState.ACTIVE;
        /* Added by Shivank */
        cmdsDocument.completed = CMDSDocumentDAO.INSTANCE.isReadyToPublished(cmdsDocument);
        /* Added by Shivank */
        CMDSDocumentDAO.INSTANCE.addDocument(cmdsDocument);
        generateEventAysc(request.userId, cmdsDocument, EventActionType.ADD,
                EventType.INDEX_CMDS_DOCUMENT, UserActionType.ADDED, false);
        SrcEntity cmdsDocEntity = new SrcEntity(EntityType.CMDSDOCUMENT,
                cmdsDocument._getStringId());
        String parentESId = AbstractCMDSContentManager.addAsCMDSResource(cmdsDocEntity,
                EventActionType.ADD, cmdsDocument);
        if(!StringUtils.isEmpty(request.folderId)){
            CMDSResourcesManager.addToFolder(request.orgId, request.userId, cmdsDocEntity,
                request.folderId, CmdsContentLinkType.ADDED, parentESId);
        }else if(!StringUtils.isEmpty(request.testId)){
            CMDSResourcesManager.addToTest(request.orgId, request.userId, cmdsDocEntity,
                request.testId, CmdsContentLinkType.ADDED, parentESId);
            boolean addingPdfIdToTest = CMDSTestManager.INSTANCE.addPdfIdToTest(request.testId, request.docId);
        }
        GetCMDSDocumentRes response = new GetCMDSDocumentRes();
        response.fromMongoModel(cmdsDocument);
        annotateExtraInfo(request.userId, request.orgId, EntityType.CMDSDOCUMENT, response);

        annotateDocumentURLInfo(response, request.isWebReq());

        if (cmdsDocument.linkType == LinkType.UPLOADED) {
            if (response.operationJobIdMap == null) {
                response.operationJobIdMap = new HashMap<OperationType, String>();
            }
            startProcessingUploadDoc(cmdsDocument, response.operationJobIdMap);
        }
        return response;
    }

    private void startProcessingUploadDoc(CMDSDocument document,
            Map<OperationType, String> operationJobIdMap) {

        EntityOperationStatus status = new EntityOperationStatus();
        status.oType = OperationType.DOCUMENT_CONVERSION;
        status.id = document._getStringId();
        status.type = EntityType.CMDSDOCUMENT;

        DocumentEncodingDetails details = new DocumentEncodingDetails();
        details.docId = document._getStringId();
        if (!(FileUtils.getExtensionWithoutDOT(document.originalFileName)
                .equalsIgnoreCase(FileUtils.PDF_EXTENSTION_WITHOUT_DOT))) {
            details.convertToPDF = true;

        }
        details.generateLinearizedPDF = true;
        details.generateThumbnail = true;
        details.encryptIfNeeded = details.generateLinearizedPDF;

        status.numOfSteps += details.encryptIfNeeded ? 1 : 0;
        status.numOfSteps += details.generateLinearizedPDF ? 1 : 0;
        status.numOfSteps += details.generateThumbnail ? 1 : 0;
        status.numOfSteps += details.convertToPDF ? 1 : 0;

        if (status.numOfSteps > 0) {
            EntityOperationStatusDAO.INSTANCE.save(status);
            details.jobId = status._getStringId();
            generateEventAysc(document.userId, details, EventType.CONVERT_DOCUMENT);
            operationJobIdMap.put(status.oType, status._getStringId());
        }
    }

    public GetCMDSDocumentRes reconvert(GetCMDSDocumentReq request) throws VedantuException {

        CMDSDocument cmdsDocument = null;
        if (StringUtils.isEmpty(request.orgId)) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
        }

        if (StringUtils.isEmpty(request.userId)) {
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }

        cmdsDocument = CMDSDocumentDAO.INSTANCE.getById(request.id);

        CMDSDocument document = CMDSDocumentDAO.INSTANCE.getById(request.id);
        if (document == null) {
            throw new VedantuException(VedantuErrorCode.DOCUMENT_NOT_FOUND);
        }

        GetCMDSDocumentRes response = new GetCMDSDocumentRes();
        response.fromMongoModel(cmdsDocument);
        annotateExtraInfo(request.userId, request.orgId, EntityType.CMDSDOCUMENT, response);

        annotateDocumentURLInfo(response, request.isWebReq());

        if (cmdsDocument.linkType == LinkType.UPLOADED) {
            if (response.operationJobIdMap == null) {
                response.operationJobIdMap = new HashMap<OperationType, String>();
            }
            startProcessingUploadDoc(cmdsDocument, response.operationJobIdMap);
        }
        return response;
    }

    private String getImageLocalTempPath(String fileName) {

        LocalFileSystemHandler tempLocHandler = (LocalFileSystemHandler) FileHandlerFactory
                .get(HandlerType.TEMP);
        return tempLocHandler.getDirectory() + LocalFileSystemHandler.PATH_SEPARATOR + fileName;

    }

    public GetCMDSDocumentRes get(GetCMDSDocumentReq request) throws VedantuException {

        CMDSDocument document = CMDSDocumentDAO.INSTANCE.getById(request.id);
        if (document == null) {
            throw new VedantuException(VedantuErrorCode.DOCUMENT_NOT_FOUND);
        }

        GetCMDSDocumentRes getDocumentRes = new GetCMDSDocumentRes();
        getDocumentRes.fromMongoModel(document);

        annotateExtraInfo(request.userId, request.orgId, EntityType.CMDSDOCUMENT, getDocumentRes);
        annotateDocumentURLInfo(getDocumentRes, request.isWebReq(), request.__getSessionParams());

        EntityOperationStatus status = EntityOperationStatusDAO.INSTANCE
                .getByOType(EntityType.CMDSDOCUMENT, document._getStringId(),
                        OperationType.DOCUMENT_CONVERSION);
        if (status != null && status.recordState == VedantuRecordState.ACTIVE) {
            if (status.numOfSteps != 0 && status.numOfSteps == status.numOfStepsCompleted) {
                EntityOperationStatusDAO.INSTANCE.updateState(status, VedantuRecordState.DELETED);
            } else {
                getDocumentRes.operationJobIdMap.put(OperationType.DOCUMENT_CONVERSION,
                        status._getStringId());
            }
        }
        return getDocumentRes;
    }

    private void annotateDocumentURLInfo(CMDSDocumentSearchIndexDetails document, boolean isWebReq) {

        annotateDocumentURLInfo(document, isWebReq, null);
    }

    private void annotateDocumentURLInfo(CMDSDocumentSearchIndexDetails document, boolean isWebReq,
            Map<String, String> sessionParams) {

        if (document.linkType == LinkType.UPLOADED) {
            document.url = (document.converted) ? ImageDisplayURLUtil.getEntityDocumentSecureURL(
                    EntityType.CMDSDOCUMENT, document.uuid, sessionParams, isWebReq)
                    : ImageDisplayURLUtil.getEntityDocumentSecureURL(EntityType.CMDSDOCUMENT,
                            document.uuid, document.extension, FileCategory.CONVERTED,
                            sessionParams, isWebReq);
        }

        if (StringUtils.isNotEmpty(document.thumbnail)) {
            document.thumbnail = ImageDisplayURLUtil.getEntityThumbnail(EntityType.CMDSDOCUMENT,
                    document.thumbnail);
        } else {
            document.thumbnail = StringUtils.EMPTY;
        }

    }

    public boolean update(EditContentReq request) throws VedantuException {

        CMDSDocument content = CMDSDocumentDAO.INSTANCE.getById(request.entity.id);

        if (content == null) {
            throw new VedantuException(VedantuErrorCode.CONTENT_NOT_FOUND);
        }

        List<String> updateList = new ArrayList<String>();
        if (StringUtils.isNotEmpty(request.name)
                && request.updateList.contains(EditContentReq.NAME)) {
            content.name = request.name;
            updateList.add(CMDSDocument.NAME);
        }

        if (request.updateList.contains(EditContentReq.DESCRIPTION)) {
            content.description = request.description;
            updateList.add(CMDSDocument.DESCRIPTION);

        }

        if (request.updateList.contains(EditContentReq.BOARD_IDS)) {
            content.boardIds = request.boardIds;
            updateList.add(CMDSDocument.BOARD_IDS);

        }

        boolean updated = true;
        if (content.globalDocId != null) {
            request.entity = new SrcEntity(EntityType.DOCUMENT, content.globalDocId);
            updated = DocumentManager.INSTANCE.update(request);
        }

        if (updated) {
            CMDSDocumentDAO.INSTANCE.updateModel(content, updateList);
            LibraryContentLinksDAO.INSTANCE.updateLastUpdated(request.entity);
            generateEventAysc(request.userId, content, EventActionType.UPDATE,
                    EventType.INDEX_CMDS_DOCUMENT, UserActionType.UPDATED, false);

            addAsCMDSResource(request.entity, EventActionType.UPDATE, content);
        }

        return true;
    }

    @Override
    public boolean calculate(String id, boolean recalculate,VedantuBaseMongoModel... contents) throws VedantuException {

        List<CMDSDocument> docs = new ArrayList<CMDSDocument>();

        if (StringUtils.isNotEmpty(id)) {
            CMDSDocument question = CMDSDocumentDAO.INSTANCE.getById(id);

            if (question == null) {
                return false;
            }
            docs.add(question);
        }

        if (contents != null && contents.length != 0) {
            for (VedantuBaseMongoModel content : contents) {
                if (content instanceof CMDSDocument) {
                    docs.add((CMDSDocument) content);
                }
            }
        }

        // calculate question image size;
        IEntityFileStorage defs = EntityStorageFactory.INSTANCE.get(EntityType.CMDSDOCUMENT);
        for (CMDSDocument doc : docs) {
            if( doc.size.isFinalized() && !recalculate){
                continue;
            }
            
            doc.size.reset();

            long thumbnailSize = defs.size(doc.thumbnail, EntityType.CMDSDOCUMENT,
                    FileUtils.JPG_EXTENTION_WITHOUT_DOT, MediaType.IMAGE, FileCategory.CONVERTED,
                    ImageSize.MEDIUM);

            if (doc.linkType == LinkType.UPLOADED) {
                long originalSize = defs.size(doc.uuid, EntityType.CMDSDOCUMENT,
                        FileUtils.getExtensionWithoutDOT(doc.originalFileName), MediaType.DOC,
                        FileCategory.ORIGINAL, ImageSize.ORIGINAL);
                long convertedSize = defs.size(doc.uuid, EntityType.CMDSDOCUMENT,
                        FileUtils.PDF_EXTENSTION_WITHOUT_DOT, MediaType.DOC,
                        FileCategory.CONVERTED, ImageSize.MEDIUM);

                long encrypted = defs.size(doc.uuid, EntityType.CMDSDOCUMENT,
                        FileUtils.PDF_EXTENSTION_WITHOUT_DOT, MediaType.DOC,
                        FileCategory.ENCRYPTED, ImageSize.MEDIUM);
                doc.size.addOriginal(originalSize);
                doc.size.addConverted(convertedSize);
                doc.size.addEncrypted(encrypted);
            }
            doc.size.addThumbnail(thumbnailSize);

            CMDSDocumentDAO.INSTANCE.updateModel(doc, Arrays.asList(CMDSDocument.SIZE));
            if (doc.globalDocId != null) {
                DocumentManager.INSTANCE.calculate(doc.globalDocId,true);
            }
        }
        return true;

    }
}
