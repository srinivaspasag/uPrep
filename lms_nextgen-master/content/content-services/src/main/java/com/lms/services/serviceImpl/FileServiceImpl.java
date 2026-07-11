package com.lms.services.serviceImpl;

import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.components.FileManagerComponent;
import com.lms.pojos.requests.GetSimilarEntities;
import com.lms.pojos.requests.file.GetFileReq;
import com.lms.pojos.requests.file.GetFilesReq;
import com.lms.pojos.responce.SearchListResponse;
import com.lms.pojos.responce.file.GetFileRes;
import com.lms.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileServiceImpl implements FileService {
    @Autowired
    private FileManagerComponent fileManagerComponent;

    @Override
    public VedantuResponse getFiles(GetFilesReq getFilesReq) {
        SearchListResponse<GetFileRes> response = null;
        try {
            response = fileManagerComponent.getFiles(getFilesReq);
        } catch (VedantuException e) {
            throw e;
        }
        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse getFile(GetFileReq request) {
        GetFileRes response = null;
        try {
            response = fileManagerComponent.getFile(request);
        } catch (VedantuException e) {
            throw e;
        }
        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse getSimilarFiles(GetSimilarEntities getSimilarEntities) {
        ListResponse<GetFileRes> response = fileManagerComponent.getSimilarFiles(getSimilarEntities);
        return new VedantuResponse(response);
    }


}
