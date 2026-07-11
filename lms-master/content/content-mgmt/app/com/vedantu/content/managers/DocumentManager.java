package com.vedantu.content.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.entity.factory.EntityStorageFactory;
import com.vedantu.commons.entity.factory.EntityTypeContentManagerFactory;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.entity.storage.AbstractEntityFileStorage;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.entity.storage.IEntityFileStorage;
import com.vedantu.commons.entity.storage.ImageSize;
import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.pojos.DownloadableFileInfo;
import com.vedantu.commons.pojos.FileData;
import com.vedantu.commons.pojos.responses.ListResponse;
import com.vedantu.commons.pojos.responses.SearchListResponse;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.commons.utils.ImageDisplayURLUtil;
import com.vedantu.content.daos.DocumentDAO;
import com.vedantu.content.models.AbstractFileModel;
import com.vedantu.content.models.Document;
import com.vedantu.content.pojos.SrcType.LinkType;
import com.vedantu.content.pojos.requests.EditContentReq;
import com.vedantu.content.pojos.requests.GetSimilarEntities;
import com.vedantu.content.pojos.requests.documents.GetDocumentReq;
import com.vedantu.content.pojos.requests.documents.GetDocumentsReq;
import com.vedantu.content.pojos.responses.documents.GetDocumentRes;
import com.vedantu.content.search.details.DocumentSearchIndexDetails;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;

public class DocumentManager extends AbstractContentManager {

    private static final ALogger  LOGGER   = Logger.of(DocumentManager.class);
    public static DocumentManager INSTANCE = new DocumentManager();
    static {

        EntityTypeContentManagerFactory.INSTANCE.register(EntityType.DOCUMENT,
                DocumentManager.class);
    }

    public DocumentManager() {

    }

    public GetDocumentRes get(GetDocumentReq request) throws VedantuException {

        Document document = DocumentDAO.INSTANCE.getById(request.id);
        if (document == null) {
            throw new VedantuException(VedantuErrorCode.DOCUMENT_NOT_FOUND);
        }
        if (document.recordState == VedantuRecordState.TEMPORARY) {
            throw new VedantuException(VedantuErrorCode.CONTENT_IN_TEMPORARY_STAGE);
        }
        GetDocumentRes documentRes = new GetDocumentRes();
        documentRes.fromMongoModel(document);
        LOGGER.info("document Info: " + documentRes);
        documentRes = (GetDocumentRes) annotateExtraInfo(
                request.userId,
                document.contentSrc != null && document.contentSrc.type == EntityType.ORGANIZATION ? document.contentSrc.id
                        : null, EntityType.DOCUMENT, documentRes);

        annotateDocumentURLInfo(documentRes, request.isWebReq(), request.__getSessionParams(), request.orgId);
        return documentRes;
    }

    public SearchListResponse<GetDocumentRes> gets(GetDocumentsReq request) throws VedantuException {

        SearchListResponse<GetDocumentRes> results = getEntityInfos(request, EntityType.DOCUMENT,
                GetDocumentRes.class, null);
        annotateExtraInfo(request.userId, request.orgId, EntityType.DOCUMENT, results.list);
        annotateDocumentURLInfo(results.list, request.isWebReq(),request.orgId);
        return results;
    }

    private void annotateDocumentURLInfo(DocumentSearchIndexDetails document, boolean isWebReq, String orgId) {

        annotateDocumentURLInfo(document, isWebReq, null, orgId);
    }

    private void annotateDocumentURLInfo(DocumentSearchIndexDetails document, boolean isWebReq,
            Map<String, String> sessionParams, String orgId) {

        annotateLinkInfo(document);
        if (document.linkType == LinkType.UPLOADED) {
            //For UPrep Organisation.
            if(StringUtils.isNotEmpty(orgId) && orgId.equals("5df8a0d0e4b0897459b25d86")){
              document.url = ImageDisplayURLUtil.getEntityDocumentURL(EntityType.DOCUMENT,
                              document.uuid, document.extension, FileCategory.CONVERTED);
            }
            else{
                document.url = (document.converted) ? ImageDisplayURLUtil.getEntityDocumentSecureURL(
                        EntityType.DOCUMENT, document.uuid, sessionParams, isWebReq)
                        : ImageDisplayURLUtil.getEntityDocumentSecureURL(EntityType.DOCUMENT,
                                document.uuid, document.extension, FileCategory.CONVERTED,
                                sessionParams, isWebReq);
            }
        }

        if (StringUtils.isNotEmpty(document.thumbnail)) {
            document.thumbnail = ImageDisplayURLUtil.getEntityThumbnail(EntityType.DOCUMENT,
                    document.thumbnail);
        } else {
            document.thumbnail = StringUtils.EMPTY;
        }
    }

    private void annotateDocumentURLInfo(List<? extends DocumentSearchIndexDetails> documentsList,
            boolean isWebReq, String orgId) {

        for (DocumentSearchIndexDetails document : documentsList) {
            annotateDocumentURLInfo(document, isWebReq ,orgId);
        }
    }

    public ListResponse<GetDocumentRes> getSimilarDocuments(GetSimilarEntities request) {

        ListResponse<GetDocumentRes> results = getSimilarEntityInfos(request, GetDocumentRes.class,
                null);
        annotateExtraInfo(request.userId, request.orgId, EntityType.DOCUMENT, results.list);
        annotateDocumentURLInfo(results.list, request.isWebReq(), request.orgId);
        return results;
    }

    @Override
    public boolean update(EditContentReq request) throws VedantuException {

        Document content = DocumentDAO.INSTANCE.getById(request.entity.id);

        List<String> updateList = new ArrayList<String>();
        if (StringUtils.isNotEmpty(request.name)
                && request.updateList.contains(EditContentReq.NAME)) {
            content.name = request.name;
            updateList.add(Document.NAME);
        }

        if (request.updateList.contains(EditContentReq.DESCRIPTION)) {
            content.description = request.description;
            updateList.add(Document.DESCRIPTION);

        }

        if (request.updateList.contains(EditContentReq.BOARD_IDS)) {
            content.boardIds = request.boardIds;
            updateList.add(Document.BOARD_IDS);

        }

        DocumentDAO.INSTANCE.updateModel(content, updateList);
        generateEventAysc(request.userId, content, EventActionType.UPDATE,
                EventType.INDEX_DOCUMENT, UserActionType.UPDATED, false);

        return true;

    }

    @Override
    public boolean calculate(String id, boolean recalculate, VedantuBaseMongoModel... contents)
            throws VedantuException {

        List<Document> docs = new ArrayList<Document>();

        if (StringUtils.isNotEmpty(id)) {
            Document question = DocumentDAO.INSTANCE.getById(id);

            if (question == null) {
                return false;
            }
            docs.add(question);
        }

        if (contents != null && contents.length != 0) {
            for (VedantuBaseMongoModel content : contents) {
                if (content instanceof Document) {
                    docs.add((Document) content);
                }
            }
        }

        // calculate question image size;
        IEntityFileStorage defs = EntityStorageFactory.INSTANCE.get(EntityType.DOCUMENT);
        for (Document doc : docs) {
            if (doc.size.isFinalized() && !recalculate) {
                continue;
            }

            doc.size.reset();

            long thumbnailSize = defs.size(doc.thumbnail, EntityType.DOCUMENT,
                    FileUtils.JPG_EXTENTION_WITHOUT_DOT, MediaType.IMAGE, FileCategory.CONVERTED,
                    ImageSize.MEDIUM);

            if (doc.linkType == LinkType.UPLOADED) {
                long originalSize = defs.size(doc.uuid, EntityType.DOCUMENT,
                        FileUtils.getExtensionWithoutDOT(doc.originalFileName), MediaType.DOC,
                        FileCategory.ORIGINAL, ImageSize.ORIGINAL);
                long convertedSize = defs.size(doc.uuid, EntityType.DOCUMENT,
                        FileUtils.PDF_EXTENSTION_WITHOUT_DOT, MediaType.DOC,
                        FileCategory.CONVERTED, ImageSize.MEDIUM);

                long encrypted = defs.size(doc.uuid, EntityType.DOCUMENT,
                        FileUtils.PDF_EXTENSTION_WITHOUT_DOT, MediaType.DOC,
                        FileCategory.ENCRYPTED, ImageSize.MEDIUM);
                doc.size.addOriginal(originalSize);
                doc.size.addConverted(convertedSize);
                doc.size.addEncrypted(encrypted);
            }
            doc.size.addThumbnail(thumbnailSize);

            DocumentDAO.INSTANCE.updateModel(doc, Arrays.asList(Document.SIZE));

        }
        return true;
    }

    @Override
    public List<DownloadableFileInfo> getFiles(EntityType entityType, String entityId)
            throws VedantuException, EntityFileStorageException {

        List<DownloadableFileInfo> fileInfos = new ArrayList<DownloadableFileInfo>();
        VedantuBasicDAO<?, ?> dao = EntityTypeDAOFactory.INSTANCE.get(entityType);
        VedantuBaseMongoModel model = dao.getById(entityId);

        IEntityFileStorage defs = EntityStorageFactory.INSTANCE.get(entityType);

        DownloadableFileInfo thumbInfo = new DownloadableFileInfo();
        thumbInfo.entityId = entityId;
        thumbInfo.entityType = entityType;
        thumbInfo.name = AbstractEntityFileStorage.computeFileId(
                ((AbstractFileModel) model).thumbnail, EntityType.DOCUMENT,
                FileUtils.JPG_EXTENTION_WITHOUT_DOT, MediaType.IMAGE, FileCategory.CONVERTED,
                ImageSize.SMALL);
        FileData data = null;
        data = defs.getSecuredURL(((AbstractFileModel) model).thumbnail, entityType,
                FileUtils.JPG_EXTENTION_WITHOUT_DOT, MediaType.IMAGE, FileCategory.CONVERTED,
                ImageSize.SMALL);
        thumbInfo.size = data.getContentLength();
        thumbInfo.downloadUrl = data.getSecuredURL();
        thumbInfo.mediaType = MediaType.IMAGE;
        fileInfos.add(thumbInfo);

        if (((AbstractFileModel) model).linkType == LinkType.UPLOADED) {
            DownloadableFileInfo contentInfo = new DownloadableFileInfo();
            contentInfo.entityId = entityId;
            contentInfo.entityType = entityType;
            contentInfo.name = AbstractEntityFileStorage.computeFileId(
                    ((AbstractFileModel) model).uuid, entityType,
                    FileUtils.PDF_EXTENSTION_WITHOUT_DOT, MediaType.DOC, FileCategory.ENCRYPTED,
                    ImageSize.MEDIUM);
            data = defs.getSecuredURL(((AbstractFileModel) model).uuid, entityType,
                    FileUtils.PDF_EXTENSTION_WITHOUT_DOT, MediaType.DOC, FileCategory.ENCRYPTED,
                    ImageSize.MEDIUM);
            contentInfo.size = data.getContentLength();
            contentInfo.downloadUrl = data.getSecuredURL();
            contentInfo.mediaType = MediaType.DOC;
            fileInfos.add(contentInfo);
        }

        return fileInfos;
    }

}
