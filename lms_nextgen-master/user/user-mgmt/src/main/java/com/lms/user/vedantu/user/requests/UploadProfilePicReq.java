package com.lms.user.vedantu.user.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractFileUploadReq;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UploadProfilePicReq extends AbstractFileUploadReq {

    public UploadProfilePicReq(MultipartFile body) {

        super(body);
    }
}
