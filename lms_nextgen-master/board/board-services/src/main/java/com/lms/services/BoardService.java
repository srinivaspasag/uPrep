package com.lms.services;

import com.lms.board.pojos.requests.DeleteBoardReq;
import com.lms.board.pojos.requests.GetTreesOfBoardsReq;
import com.lms.board.pojos.requests.GetChildrenReq;
import com.lms.board.pojos.requests.UploadConsumerBoardReq;
import com.lms.board.pojos.requests.UploadGlobalBoardReq;
import com.lms.board.pojos.requests.UploadOrgBoardReq;
import com.lms.board.pojos.test.requests.GetTargetsReq;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import org.springframework.web.multipart.MultipartFile;

public interface BoardService {
	VedantuResponse getTargets(GetTargetsReq getTargetsReq);


    VedantuResponse delete(DeleteBoardReq deleteBoardReq);

    VedantuResponse getTreesOfBoards(GetTreesOfBoardsReq getTreesOfBoardsReq);
	VedantuResponse getchildren(GetChildrenReq getChildrenReq);

	VedantuResponse uploadConsumerBoards(MultipartFile file, UploadConsumerBoardReq uploadConsumerBoardReq);

	VedantuResponse uploadOrgBoards(MultipartFile file, UploadOrgBoardReq uploadOrgBoardReq);

	VedantuResponse uploadglobalBoards(MultipartFile file, UploadGlobalBoardReq uploadGlobalBoardReq);
}
