package com.vedantu.content.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import com.vedantu.content.daos.FileDAO;
import com.vedantu.content.models.AbstractBoardEntityTagModel;
import com.vedantu.content.models.AbstractContentModel;
import com.vedantu.content.models.AbstractFileModel;
import com.vedantu.content.models.File;
import com.vedantu.content.pojos.SrcType.LinkType;
import com.vedantu.content.pojos.requests.EditContentReq;
import com.vedantu.content.pojos.requests.GetSimilarEntities;
import com.vedantu.content.pojos.requests.file.GetFileReq;
import com.vedantu.content.pojos.requests.file.GetFilesReq;
import com.vedantu.content.pojos.responses.files.GetFileRes;
import com.vedantu.content.search.details.FileSearchIndexDetails;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;

public class FileManager extends AbstractContentManager {

    private static final ALogger LOGGER   = Logger.of(FileManager.class);
    public static FileManager    INSTANCE = new FileManager();
    static {

        EntityTypeContentManagerFactory.INSTANCE.register(EntityType.FILE, FileManager.class);
    }

    public FileManager() {

    }

    public GetFileRes get(GetFileReq request) throws VedantuException {

        File file = FileDAO.INSTANCE.getById(request.id);
        if (file == null) {
            throw new VedantuException(VedantuErrorCode.FILE_NOT_FOUND);
        }
        if (file.recordState == VedantuRecordState.TEMPORARY) {
            throw new VedantuException(VedantuErrorCode.CONTENT_IN_TEMPORARY_STAGE);
        }
        GetFileRes fileResponse = new GetFileRes();
        fileResponse.fromMongoModel(file);
        // TODO decorate video thumnail url
        // TODO update streaming video url
        LOGGER.info("fileInfo: " + fileResponse);
        fileResponse = (GetFileRes) annotateExtraInfo(request.userId, file.contentSrc != null
                && file.contentSrc.type == EntityType.ORGANIZATION ? file.contentSrc.id : null,
                EntityType.FILE, fileResponse);

        annotateFileURLInfo(fileResponse);
        return fileResponse;
    }

    public SearchListResponse<GetFileRes> gets(GetFilesReq request) throws VedantuException {

        SearchListResponse<GetFileRes> results = getEntityInfos(request, EntityType.FILE,
                GetFileRes.class, null);
        annotateExtraInfo(request.userId, request.orgId, EntityType.FILE, results.list);
        annotateFileURLInfo(results.list);
        return results;
    }

    private void annotateFileURLInfo(FileSearchIndexDetails file) {

        annotateLinkInfo(file);
        if (file.linkType == LinkType.UPLOADED) {

            file.url = ImageDisplayURLUtil.getEntityDownloadURL(EntityType.FILE, file.uuid,
                    file.extension, MediaType.FILE, FileCategory.ORIGINAL, file.id);
        }

        file.thumbnail = StringUtils.EMPTY;

    }

    private void annotateFileURLInfo(List<? extends FileSearchIndexDetails> files) {

        for (FileSearchIndexDetails file : files) {
            annotateFileURLInfo(file);
        }
    }

    public ListResponse<GetFileRes> getSimilarFiles(GetSimilarEntities request) {

        ListResponse<GetFileRes> results = getSimilarEntityInfos(request, GetFileRes.class, null);
        annotateExtraInfo(request.userId, request.orgId, EntityType.FILE, results.list);
        annotateFileURLInfo(results.list);
        return results;
    }

    @Override
    public boolean update(EditContentReq request) throws VedantuException {

        File content = FileDAO.INSTANCE.getById(request.entity.id);

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

        FileDAO.INSTANCE.updateModel(content, updateList);
        generateEventAysc(request.userId, content, EventActionType.UPDATE, EventType.INDEX_FILE,
                UserActionType.UPDATED, false);

        return true;

    }

    @Override
    public boolean calculate(String id, boolean recalculate,VedantuBaseMongoModel... contents) throws VedantuException {

        List<File> files = new ArrayList<File>();

        if (StringUtils.isNotEmpty(id)) {
            File file = FileDAO.INSTANCE.getById(id);

            if (file == null) {
                return false;
            }
            files.add(file);
        }

        if (contents != null && contents.length != 0) {
            for (VedantuBaseMongoModel content : contents) {
                if (content instanceof File) {
                    files.add((File) content);
                }
            }
        }

        // calculate question image size;
        IEntityFileStorage defs = EntityStorageFactory.INSTANCE.get(EntityType.FILE);
        for (File file : files) {
            if( file.size.isFinalized() && !recalculate){
                continue;
            }
            file.size.reset();

            if (file.linkType == LinkType.UPLOADED) {
                long originalSize = defs.size(file.uuid, EntityType.FILE,
                        FileUtils.getExtensionWithoutDOT(file.originalFileName), MediaType.FILE,
                        FileCategory.ORIGINAL, ImageSize.ORIGINAL);

                long encrypted = originalSize;
                file.size.addOriginal(originalSize);
                file.size.addEncrypted(encrypted);
            }
            file.size.finalize();
            FileDAO.INSTANCE.updateModel(file, Arrays.asList(File.SIZE));

        }
        return true;
    }

    @Override
    public List<DownloadableFileInfo> getFiles(EntityType entityType, String entityId) throws VedantuException, EntityFileStorageException {

        List<DownloadableFileInfo> fileInfos = new ArrayList<DownloadableFileInfo>();
        VedantuBasicDAO<?, ?> dao = EntityTypeDAOFactory.INSTANCE.get(entityType);
        VedantuBaseMongoModel model = dao.getById(entityId);

        IEntityFileStorage defs = EntityStorageFactory.INSTANCE.get(entityType);

        if (((AbstractFileModel) model).linkType == LinkType.UPLOADED) {
            DownloadableFileInfo contentInfo = new DownloadableFileInfo();
            contentInfo.entityId = entityId;
            contentInfo.entityType = entityType;
            contentInfo.name = AbstractEntityFileStorage.computeFileId(
                    ((AbstractFileModel) model).uuid, entityType,
                    FileUtils.getExtensionWithoutDOT(((AbstractFileModel) model).originalFileName),
                    MediaType.FILE, FileCategory.ENCRYPTED, ImageSize.MEDIUM);
            FileData data =null;
            data= defs.getSecuredURL(((AbstractFileModel) model).uuid,
                    entityType,
                    FileUtils.getExtensionWithoutDOT(((AbstractFileModel) model).originalFileName),
                    MediaType.FILE, FileCategory.ENCRYPTED, ImageSize.MEDIUM);
            contentInfo.size =data.getContentLength();
            contentInfo.downloadUrl=data.getSecuredURL();
            contentInfo.mediaType= MediaType.FILE;
            fileInfos.add(contentInfo);

        }
        return fileInfos;
    }
}
