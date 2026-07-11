package com.lms.services;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojo.request.UploadImageReq;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {

	VedantuResponse uploadImage(MultipartFile file, UploadImageReq uploadImageReq);

}
