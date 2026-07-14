package com.lms.common.vedantu.commons.pojos.requests;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.io.File;
@Setter
@Getter
public abstract class AbstractFileUploadReq extends AbstractAuthCheckReq {
@NotBlank(message = "file name should not be null")
    public String fileName;
    @NotBlank(message = "contentType should not be null")

    public String contentType;
    public File inputFile;

    public AbstractFileUploadReq(MultipartFile body) {
        super();
    }

   /* public AbstractFileUploadReq(MultipartFile body) {
        super(body.asFormUrlEncoded());

        FilePart inputFilePart = body.getInputStream("inputFile");
        if (null != inputFilePart) {
            this.fileName = inputFilePart.getFilename();
            this.contentType = inputFilePart.getContentType();
            this.inputFile = inputFilePart.getFile();
        }
    }*/

    public String validate() {
        if (null == inputFile) {
            return "inputFile missing";
        }
        return null;
    }

}
