package com.lms.controller;

import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojo.request.UploadImageReq;
import com.lms.services.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/uploads")
public class Uploads {
    @Autowired
    private UploadService uploadService;

    @PostMapping("/uploadImage")
    public ResponseEntity<VedantuResponse> uploadImage(@RequestParam("file") MultipartFile file, UploadImageReq uploadImageReq) throws VedantuException {
        return ResponseEntity.ok(uploadService.uploadImage(file, uploadImageReq));
    }
}
