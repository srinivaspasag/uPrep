package com.lms.services;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.*;
import org.springframework.web.multipart.MultipartFile;

public interface CMDSResourceService {

    VedantuResponse getResources(GetResourcesReq getResourcesReq);

    VedantuResponse getQuestionsCount(GetResourcesReq getResourcesReq);

    VedantuResponse getQuestions(GetResourcesReq getResourcesReq);

    VedantuResponse createFolder(CreateFolderReq createFolderReq);

    VedantuResponse move(MoveContentReq moveContentReq);

    VedantuResponse removeResources(DeleteContentReq deleteContentReq);

    VedantuResponse getFolders(GetFoldersReq getFoldersReq);

    VedantuResponse upload(MultipartFile file, UploadCMDSContentFileReq uploadCMDSContentFileReq);

    VedantuResponse getSignedRequest(SignUploadFileReq signUploadFileReq);

    VedantuResponse update(EditContentReq editContentReq);

    VedantuResponse publish(PublishReq publishReq);

    VedantuResponse getStatus(GetEntityPublishingStatusReq getEntityPublishingStatusReq);

    VedantuResponse getQuestionSharingBasicInfo(GetSharedQuestionsBasicInfoReq getSharedQuestionsBasicInfoReq);

    VedantuResponse addMappings(AddMappingsReq addMappingsReq);

    VedantuResponse saveMapping(SaveMappingsReq saveMappingsReq);

    VedantuResponse deleteMapping(DeleteMappingReq deleteMappingReq);

    VedantuResponse visibleMapping(VisibleMappingReq visibleMappingReq);

    VedantuResponse shareMapping(DeleteMappingReq deleteMappingReq);

}
