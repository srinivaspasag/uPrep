package com.lms.pojos.requests;

import com.lms.interfaces.IReverseImageMapperProcessor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.io.IOException;

@Setter
@Getter
public class AddDiscussionReq extends AbstractAddContentBoardReq implements
        IReverseImageMapperProcessor {

    @NotBlank
    public String name;

    @NotBlank
    public String content;

    @Override
    public void addImageSrcUrl() {

    }

    @Override
    public void removeImageSrc(boolean moveImages) throws IOException {

    }


}
