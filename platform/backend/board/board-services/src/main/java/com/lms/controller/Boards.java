package com.lms.controller;

import com.lms.board.pojos.requests.DeleteBoardReq;
import com.lms.board.pojos.requests.GetTreesOfBoardsReq;
import com.lms.board.pojos.requests.GetChildrenReq;
import com.lms.board.pojos.requests.UploadConsumerBoardReq;
import com.lms.board.pojos.requests.UploadGlobalBoardReq;
import com.lms.board.pojos.requests.UploadOrgBoardReq;
import com.lms.board.pojos.test.requests.GetTargetsReq;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.services.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@RestController
@RequestMapping("/boards")
public class Boards {
    @Autowired
    private BoardService boardServiceImpl;

    @PostMapping("/uploadConsumerBoards")
    public ResponseEntity<VedantuResponse> uploadConsumerBoards(@RequestParam("file") MultipartFile file,
                                                                UploadConsumerBoardReq uploadConsumerBoardReq) throws VedantuException {
        return ResponseEntity.ok(boardServiceImpl.uploadConsumerBoards(file, uploadConsumerBoardReq));
    }

    @PostMapping("/uploadOrgBoards")

    public ResponseEntity<VedantuResponse> uploadOrgBoards(@RequestParam("file") MultipartFile file,
                                                           UploadOrgBoardReq uploadOrgBoardReq) throws VedantuException {
        return ResponseEntity.ok(boardServiceImpl.uploadOrgBoards(file, uploadOrgBoardReq));
    }

    @PostMapping("/getTargets")
    public ResponseEntity<VedantuResponse> getTargets(GetTargetsReq getTargetsReq) {
        return ResponseEntity.ok(boardServiceImpl.getTargets(getTargetsReq));
    }

    @PostMapping("/uploadGlobalBoards")
    public ResponseEntity<VedantuResponse> uploadGlobalBoards(@RequestParam("file") MultipartFile file, UploadGlobalBoardReq uploadGlobalBoardReq) throws VedantuException {
        return ResponseEntity.ok(boardServiceImpl.uploadglobalBoards(file, uploadGlobalBoardReq));
    }

    @PostMapping("/delete")
    public ResponseEntity<VedantuResponse> delete(DeleteBoardReq deleteBoardReq)
    {
        return ResponseEntity.ok(boardServiceImpl.delete(deleteBoardReq));
    }

    @PostMapping("/getTreesOfBoards")
    public ResponseEntity<VedantuResponse> getTreesOfBoards(GetTreesOfBoardsReq getTreesOfBoardsReq) {
        return ResponseEntity.ok(boardServiceImpl.getTreesOfBoards(getTreesOfBoardsReq));
    }
    @PostMapping("/getChildren")
    public ResponseEntity<VedantuResponse> getChildren(@Valid GetChildrenReq getChildrenReq) throws VedantuException {
        return ResponseEntity.ok(boardServiceImpl.getchildren(getChildrenReq));
    }
}
