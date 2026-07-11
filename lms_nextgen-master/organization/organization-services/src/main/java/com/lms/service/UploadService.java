package com.lms.service;

import org.springframework.web.multipart.MultipartFile;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojo.request.UploadImageReq;

public interface UploadService {

	VedantuResponse uploadImage(MultipartFile file, UploadImageReq uploadImageReq);

}
