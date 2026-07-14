package com.lms.controller;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.*;
import com.lms.services.VideosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/videos")
public class Videos {
	@Autowired
	private VideosService videosService;

	@PostMapping("/getVideoInfo")
	public ResponseEntity<VedantuResponse> getVideoInfo(GetVideoInfoReq getVideoInfoReq) {
		return ResponseEntity.ok(videosService.getVideoInfo(getVideoInfoReq));
	}

	@PostMapping("/getVideo")
	public ResponseEntity<VedantuResponse> getVideo(GetVideoReq getVideoReq) {
		return ResponseEntity.ok(videosService.getVideo(getVideoReq));
	}

	@PostMapping("/getPlaylistVideos")
	public ResponseEntity<VedantuResponse> getPlaylistVideos(GetPlaylistVideosReq getPlaylistVideosReq) {
		return ResponseEntity.ok(videosService.getPlaylistVideos(getPlaylistVideosReq));
	}

	@PostMapping("/getVideos")
	public ResponseEntity<VedantuResponse> getVideos(GetVideosReq getVideosReq) {
		return ResponseEntity.ok(videosService.getVideos(getVideosReq));
	}

	@PostMapping("/getSimilarVideos")
	public ResponseEntity<VedantuResponse> getSimilarVideos(GetSimilarEntities getSimilarEntities) {
		return ResponseEntity.ok(videosService.getSimilarVideos(getSimilarEntities));
	}

}
