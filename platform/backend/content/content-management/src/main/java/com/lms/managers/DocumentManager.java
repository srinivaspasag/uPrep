package com.lms.managers;

import com.lms.common.exception.VedantuException;
import com.lms.common.pojos.DownloadableFileInfo;
import com.lms.common.utils.FileUtils;
import com.lms.common.vedantu.commons.pojos.requests.FileData;
import com.lms.common.vedantu.entity.storage.*;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.ImageSize;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.enums.SrcType.LinkType;
import com.lms.models.AbstractFileModel;
import com.lms.models.Documents;
import com.lms.pojos.requests.EditContentReq;
import com.lms.repository.DocumentsRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class DocumentManager extends AbstractContentManager {

	 private static final Logger logger = LoggerFactory.getLogger(DocumentManager.class);
     @Autowired
     private DocumentsRepo documentsRepo;
     @Autowired
     private DocumentEntityFileStorage documentEntityFileStorage;

    @Override
    public boolean update(EditContentReq request) throws VedantuException {

       /* Document content = DocumentDAO.INSTANCE.getById(request.entity.id);

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
                EventType.INDEX_DOCUMENT, UserActionType.UPDATED, false);*/

        return true;

    }

    @Override
    public boolean calculate(String id, boolean recalculate, VedantuBaseMongoModel... contents)
            throws VedantuException {

       /* List<Document> docs = new ArrayList<Document>();

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

        }*/
        return true;
    }

    @Override
    public List<DownloadableFileInfo> getFiles(EntityType entityType, String entityId)
            throws VedantuException {
    	VedantuBaseMongoModel model = null;
        List<DownloadableFileInfo> fileInfos = new ArrayList<DownloadableFileInfo>();
      Optional<Documents> documents =  documentsRepo.findById(entityId);
         model = documents.get();
         documentEntityFileStorage.AbstractEntityFileStorageEntity(entityType);
        IEntityFileStorage defs = documentEntityFileStorage;

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
        if (data != null) {
            thumbInfo.size = data.getContentLength();
            thumbInfo.downloadUrl = data.getSecuredURL();
            thumbInfo.mediaType = MediaType.IMAGE;
            fileInfos.add(thumbInfo);
        }

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
            if (data != null) {
                contentInfo.size = data.getContentLength();
                contentInfo.downloadUrl = data.getSecuredURL();
                contentInfo.mediaType = MediaType.DOC;
                fileInfos.add(contentInfo);
            }
        }

        return fileInfos;
    }

}
