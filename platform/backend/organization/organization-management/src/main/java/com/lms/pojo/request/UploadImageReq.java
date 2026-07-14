package com.lms.pojo.request;

import java.io.File;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
public class UploadImageReq extends AbstractAuthCheckReq {

    public File   imageFile;
    public String imageName;
    public String folder;

    public UploadImageReq() {

        super();
    }

    

}
