package com.lms.board.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractFileUploadReq;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
public class UploadGlobalBoardReq extends AbstractFileUploadReq {

    public UploadGlobalBoardReq(MultipartFile body) {
        super(body);
    }

}
