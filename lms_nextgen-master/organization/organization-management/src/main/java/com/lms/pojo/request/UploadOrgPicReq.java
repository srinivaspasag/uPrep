package com.lms.pojo.request;

import com.lms.common.vedantu.commons.pojos.requests.AbstractFileUploadReq;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.util.Map;

@Setter
@Getter
public class UploadOrgPicReq extends AbstractFileUploadReq {
@NotBlank(message = "orgId should be mandatory")
        public String orgId;
@NotBlank(message = "orgMemberId should not be null")
        public String orgMemberId;

    public UploadOrgPicReq(MultipartFile body) {
        super(body);
    }





}
