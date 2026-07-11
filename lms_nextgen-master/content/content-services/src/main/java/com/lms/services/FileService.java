package com.lms.services;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.GetSimilarEntities;
import com.lms.pojos.requests.file.GetFileReq;
import com.lms.pojos.requests.file.GetFilesReq;

public interface FileService {

    VedantuResponse getFiles(GetFilesReq getFilesReq);

    VedantuResponse getFile(GetFileReq getFileReq);

    VedantuResponse getSimilarFiles(GetSimilarEntities getSimilarEntities);

}
