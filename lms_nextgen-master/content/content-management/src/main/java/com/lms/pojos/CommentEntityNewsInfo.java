package com.lms.pojos;

import com.lms.common.news.EntityNewsInfo;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CommentEntityNewsInfo extends EntityNewsInfo {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public SrcEntity comment;
    public String commentText;

}
