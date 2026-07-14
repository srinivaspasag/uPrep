package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.Scope;
import com.lms.enums.CommentType;
import com.lms.interfaces.IReverseImageMapperProcessor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.List;

@Setter
@Getter
public class AddCommentReq extends AbstractAddContentReq implements
        IReverseImageMapperProcessor {

  //  @NotBlank(message = "parent should not be null")
    public SrcEntity parent;

    public SrcEntity base;
    public SrcEntity root;

    public Scope scope;

    @NotBlank(message = "content should not be null")
    public String content;
    public List<String> tags;
    public CommentType type;

    @Override
    public void addImageSrcUrl() {

    }

    @Override
    public void removeImageSrc(boolean moveImages) throws IOException {

    }


}
