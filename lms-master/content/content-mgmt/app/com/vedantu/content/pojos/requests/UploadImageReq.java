package com.vedantu.content.pojos.requests;

import java.io.File;

import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class UploadImageReq extends AbstractAuthCheckReq {

    public File   imageFile;
    public String imageName;
    public String folder;

    public UploadImageReq() {

        super();
    }

    public UploadImageReq(MultipartFormData body) {

        super(body.asFormUrlEncoded());
        FilePart imageFilePart = body.getFile("imageFile");
        if (null != imageFilePart) {
            this.imageFile = imageFilePart.getFile();
            this.imageName = imageFilePart.getFilename();
        }
    }

}
