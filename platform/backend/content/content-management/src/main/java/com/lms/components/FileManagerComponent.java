package com.lms.components;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;
import com.lms.common.vedantu.entity.storage.FileCategory;
import com.lms.common.vedantu.entity.storage.MediaType;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.SrcType.LinkType;
import com.lms.managers.AbstractContentManager;
import com.lms.models.Files;
import com.lms.pojos.requests.GetSimilarEntities;
import com.lms.pojos.requests.file.GetFileReq;
import com.lms.pojos.requests.file.GetFilesReq;
import com.lms.pojos.responce.SearchListResponse;
import com.lms.pojos.responce.file.GetFileRes;
import com.lms.pojos.search.details.FileSearchIndexDetails;
import com.lms.repository.FilesRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class FileManagerComponent extends AbstractContentManager {
    @Autowired
    private FilesRepo filesRepo;

    public SearchListResponse<GetFileRes> getFiles(GetFilesReq request) {
        SearchListResponse<GetFileRes> results = getEntityInfos(request, EntityType.FILE,
                GetFileRes.class, null);
        annotateExtraInfo(request.userId, request.orgId, EntityType.FILE, results.list);
        annotateFileURLInfo(results.list);
        return results;
    }

    private void annotateFileURLInfo(List<? extends FileSearchIndexDetails> files) {

        for (FileSearchIndexDetails file : files) {
            annotateFileURLInfo(file);
        }
    }

    private void annotateFileURLInfo(FileSearchIndexDetails file) {

        annotateLinkInfo(file);
        if (file.linkType == LinkType.UPLOADED) {

            file.url = getEntityDownloadURL(EntityType.FILE, file.uuid,
                    file.extension, MediaType.FILE, FileCategory.ORIGINAL, file.id);
        }

        file.thumbnail = "";

    }

    public GetFileRes getFile(GetFileReq request) {
        Optional<Files> fileOptional = filesRepo.findById(request.id);
        if (!fileOptional.isPresent()) {
            throw new VedantuException(VedantuErrorCode.FILE_NOT_FOUND);
        }
        Files file = fileOptional.get();
        if (file.recordState == VedantuRecordState.TEMPORARY) {
            throw new VedantuException(VedantuErrorCode.CONTENT_IN_TEMPORARY_STAGE);
        }
        GetFileRes fileResponse = new GetFileRes();
        fileResponse.fromMongoModel(file);
        // TODO decorate video thumnail url
        // TODO update streaming video url
        fileResponse = (GetFileRes) annotateExtraInfo(request.userId, file.contentSrc != null
                        && file.contentSrc.type == EntityType.ORGANIZATION ? file.contentSrc.id : null,
                EntityType.FILE, fileResponse);

        annotateFileURLInfo(fileResponse);
        return fileResponse;
    }

    public ListResponse<GetFileRes> getSimilarFiles(GetSimilarEntities request) {
        ListResponse<GetFileRes> results = getSimilarEntityInfos(request, GetFileRes.class, null);
        annotateExtraInfo(request.userId, request.orgId, EntityType.FILE, results.list);
        annotateFileURLInfo(results.list);
        return results;
    }

}
