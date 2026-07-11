package com.lms.controller;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.GetSimilarEntities;
import com.lms.pojos.requests.file.GetFileReq;
import com.lms.pojos.requests.file.GetFilesReq;
import com.lms.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/files")
public class Files {
	@Autowired
	private FileService fileService;

	@PostMapping("/getFiles")
	public ResponseEntity<VedantuResponse> getFiles(GetFilesReq getFilesReq) {
		return ResponseEntity.ok(fileService.getFiles(getFilesReq));
	}

	@PostMapping("/getFile")
	public ResponseEntity<VedantuResponse> getFile(GetFileReq getFileReq) {
		return ResponseEntity.ok(fileService.getFile(getFileReq));
	}

	@PostMapping("/getSimilarFiles")
	public ResponseEntity<VedantuResponse> getSimilarFiles(GetSimilarEntities getSimilarEntities) {
		return ResponseEntity.ok(fileService.getSimilarFiles(getSimilarEntities));
	}
}
