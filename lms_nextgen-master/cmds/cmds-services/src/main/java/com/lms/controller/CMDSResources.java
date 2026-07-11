package com.lms.controller;

import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.*;
import com.lms.services.CMDSResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/cmdsResources")
public class CMDSResources {
    @Autowired
    private CMDSResourceService cmdsResourceService;

	@PostMapping("/getResources")
	public ResponseEntity<VedantuResponse> getResources(GetResourcesReq getResourcesReq) {
		return ResponseEntity.ok(cmdsResourceService.getResources(getResourcesReq));
	}
	@PostMapping("/getQuestionsCount")
	public ResponseEntity<VedantuResponse> getQuestionsCount(GetResourcesReq getResourcesReq) {
		return ResponseEntity.ok(cmdsResourceService.getQuestionsCount(getResourcesReq));
	}
	@PostMapping("/getQuestions")
	public ResponseEntity<VedantuResponse> getQuestions(GetResourcesReq getResourcesReq) {
		return ResponseEntity.ok(cmdsResourceService.getQuestions(getResourcesReq));
	}
	@PostMapping("/createFolder")
	public ResponseEntity<VedantuResponse> createFolder(CreateFolderReq createFolderReq) {
		return ResponseEntity.ok(cmdsResourceService.createFolder(createFolderReq));
	}
	@PostMapping("/move")
	public ResponseEntity<VedantuResponse> move(MoveContentReq moveContentReq) {
		return ResponseEntity.ok(cmdsResourceService.move(moveContentReq));
	}

	@PostMapping("/removeResources")
	public ResponseEntity<VedantuResponse> removeResources(DeleteContentReq deleteContentReq) {
		return ResponseEntity.ok(cmdsResourceService.removeResources(deleteContentReq));
	}

	@PostMapping("/getFolders")
	public ResponseEntity<VedantuResponse> getFolders(GetFoldersReq getFoldersReq) {
		return ResponseEntity.ok(cmdsResourceService.getFolders(getFoldersReq));
	}

	@PostMapping("/upload")
	public ResponseEntity<VedantuResponse> upload(@RequestParam("file") MultipartFile file,
												  UploadCMDSContentFileReq uploadCMDSContentFileReq) throws VedantuException {
		return ResponseEntity.ok(cmdsResourceService.upload(file, uploadCMDSContentFileReq));
	}

	@PostMapping("/getSignedRequest")
	public ResponseEntity<VedantuResponse> getSignedRequest(SignUploadFileReq signUploadFileReq)
			throws VedantuException {
		return ResponseEntity.ok(cmdsResourceService.getSignedRequest(signUploadFileReq));
	}

	@PostMapping("/update")
	public ResponseEntity<VedantuResponse> update(EditContentReq editContentReq) {
		return ResponseEntity.ok(cmdsResourceService.update(editContentReq));
	}

	@PostMapping("/publish")
	public ResponseEntity<VedantuResponse> publish(PublishReq publishReq) {
		return ResponseEntity.ok(cmdsResourceService.publish(publishReq));
	}

	@PostMapping("/getStatus")
	public ResponseEntity<VedantuResponse> getStatus(GetEntityPublishingStatusReq getEntityPublishingStatusReq) {
		return ResponseEntity.ok(cmdsResourceService.getStatus(getEntityPublishingStatusReq));
	}

	@PostMapping("/getQuestionSharingBasicInfo")
	public ResponseEntity<VedantuResponse> getQuestionSharingBasicInfo(GetSharedQuestionsBasicInfoReq getSharedQuestionsBasicInfoReq) {
		return ResponseEntity.ok(cmdsResourceService.getQuestionSharingBasicInfo(getSharedQuestionsBasicInfoReq));
	}

	@PostMapping("/addMappings")
	public ResponseEntity<VedantuResponse> addMappings(AddMappingsReq addMappingsReq) {
		return ResponseEntity.ok(cmdsResourceService.addMappings(addMappingsReq));
	}

	@PostMapping("/saveMapping")
	public ResponseEntity<VedantuResponse> saveMapping(SaveMappingsReq saveMappingsReq) {
		return ResponseEntity.ok(cmdsResourceService.saveMapping(saveMappingsReq));
	}

	@PostMapping("/deleteMapping")
	public ResponseEntity<VedantuResponse> deleteMapping(DeleteMappingReq deleteMappingReq) {
		return ResponseEntity.ok(cmdsResourceService.deleteMapping(deleteMappingReq));
	}

	@PostMapping("/visibleMapping")
	public ResponseEntity<VedantuResponse> visibleMapping(VisibleMappingReq visibleMappingReq) {
		return ResponseEntity.ok(cmdsResourceService.visibleMapping(visibleMappingReq));
	}

	@PostMapping("/shareMapping")
	public ResponseEntity<VedantuResponse> shareMapping(DeleteMappingReq deleteMappingReq) {
		return ResponseEntity.ok(cmdsResourceService.shareMapping(deleteMappingReq));
	}
}
