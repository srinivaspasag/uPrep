package com.lms.services.serviceImpl;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.fs.handlers.responce.SignUploadFileRes;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.entity.storage.UserProfilePicEntityFileStorage;
import com.lms.component.CMDSLibraryManager;
import com.lms.components.CMDSResourcesManager;
import com.lms.pojos.requests.*;
import com.lms.pojos.responce.*;
import com.lms.pojos.responses.GetResourcesRes;
import com.lms.services.CMDSResourceService;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class CMDSResourceServiceImpl implements CMDSResourceService {
    @Autowired
    private CMDSResourcesManager cMDSResourcesManager;
    @Autowired
    private UserProfilePicEntityFileStorage picStorage;
    @Autowired
    private CMDSLibraryManager cMDSLibraryManager;

    @Override
    public VedantuResponse getResources(GetResourcesReq getResourcesReq) {
        GetResourcesRes response = null;
        try {
            response = cMDSResourcesManager.getResources(getResourcesReq);
        } catch (VedantuException e) {

            throw e;
        }

        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse getQuestionsCount(GetResourcesReq getResourcesReq) {
        GetResourcesRes response = null;
        try {
            response = cMDSResourcesManager.getQuestionsCount(getResourcesReq);
        } catch (VedantuException e) {

            throw e;
        }

        return new VedantuResponse(response);

    }

    @Override
    public VedantuResponse getQuestions(GetResourcesReq getResourcesReq) {
        GetResourcesRes response = null;
        try {
            response = cMDSResourcesManager.getQuestions(getResourcesReq);
        } catch (VedantuException e) {

            throw e;
        }

        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse createFolder(CreateFolderReq createFolderReq) {
        CreateFolderRes createFolderResponse = null;

        try {
            createFolderResponse = cMDSResourcesManager.createFolder(createFolderReq);

        } catch (VedantuException e) {

            throw e;
        }
        return new VedantuResponse(createFolderResponse);
    }

    @Override
    public VedantuResponse move(MoveContentReq moveContentReq) {
        MoveContentRes moveContentRes = null;

        try {
            moveContentRes = cMDSResourcesManager.moveFolder(moveContentReq);

        } catch (VedantuException e) {

            throw e;
        }
        return new VedantuResponse(moveContentRes);
    }

    @Override
    public VedantuResponse removeResources(DeleteContentReq deleteRequest) {
        DeleteContentRes deleteResponse = null;

        try {
            deleteResponse = cMDSResourcesManager.removeResources(deleteRequest);

        } catch (VedantuException e) {

            throw e;
        }
        return new VedantuResponse(deleteResponse);

    }

	@Override
	public VedantuResponse getFolders(GetFoldersReq getFoldersReq) {
		 GetFoldersRes getFoldersRes = null;

        try {
            getFoldersRes = cMDSResourcesManager.getFolders(getFoldersReq);

        } catch (VedantuException e) {

            throw e;
        }
        return new VedantuResponse(getFoldersRes);
    }

    @Override
    public VedantuResponse upload(MultipartFile file, UploadCMDSContentFileReq request) {
        UploadContentFileRes response = new UploadContentFileRes();
        response.success = false;
        try {
            request.file = picStorage.convertMultiPartToFile(file);
            response.success = cMDSResourcesManager.upload(request);

        } catch (VedantuException e) {

            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (response.success) {
            return new VedantuResponse(response);
        }

        return null;

    }

    @Override
    public VedantuResponse getSignedRequest(SignUploadFileReq request) {
        SignUploadFileRes response = null;
        try {
            response = cMDSResourcesManager.sign(request);

        } catch (VedantuException e) {

            throw e;
        }
        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse update(EditContentReq request) {
        EditContentRes response = new EditContentRes();
        try {
            response.isUpdated = cMDSResourcesManager.update(request);

        } catch (VedantuException e) {

            throw e;
        }
        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse publish(PublishReq request) {
        PublishRes response = null;
        try {

            response = cMDSLibraryManager.publish(request);

        } catch (VedantuException ex) {

            throw ex;
        }
        return new VedantuResponse(response);

    }

    @Override
    public VedantuResponse getStatus(GetEntityPublishingStatusReq request) {
        GetStatus response = null;
        try {

            response = cMDSLibraryManager.getStatus(request);

        } catch (VedantuException ex) {
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
        }
        return new VedantuResponse(response);

    }

    @Override
    public VedantuResponse getQuestionSharingBasicInfo(GetSharedQuestionsBasicInfoReq request) {
        GetSharedQuestionsBasicInfoRes response = null;
        try {
            response = cMDSResourcesManager.getQuestionSharingBasicInfo(request);
        } catch (VedantuException e) {

            throw e;
        }

        return new VedantuResponse(response);

    }

    @Override
    public VedantuResponse addMappings(AddMappingsReq addMappingsReq) {
        AddMappingsRes response = null;
        try {
            response = cMDSResourcesManager.getBoardsToAddMappings(addMappingsReq);
        } catch (VedantuException e) {

            throw e;
        }

        return new VedantuResponse(response);

    }

    @Override
    public VedantuResponse saveMapping(SaveMappingsReq request) {
        SaveMappingRes response = null;
        try {
            response = cMDSResourcesManager.saveBoardMapping(request);
        } catch (VedantuException e) {

            throw e;
        }

        return new VedantuResponse(response);

    }

    @Override
    public VedantuResponse deleteMapping(DeleteMappingReq request) {
        SaveMappingRes response = null;
        try {
            response = cMDSResourcesManager.deleteBoardMapping(request);
        } catch (VedantuException e) {

            throw e;
        }

        return new VedantuResponse(response);

    }

    @Override
    public VedantuResponse visibleMapping(VisibleMappingReq request) {
        SaveMappingRes response = null;
        try {
            response = cMDSResourcesManager.visibleBoardMapping(request);
        } catch (VedantuException e) {

            throw e;
        }

        return new VedantuResponse(response);

    }

    @Override
    public VedantuResponse shareMapping(DeleteMappingReq request) {
        List<ShareMappingResponse> response = null;
        try {
            response = cMDSResourcesManager.shareBoardMapping(request);
        } catch (VedantuException e) {

            throw e;
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            throw e;
        }

        return new VedantuResponse(response);
    }


}
