package com.lms.pojos.news.details;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.managers.news.populators.EntityNewsDetailsFactory;

public class CommentNewsEntityDetails extends AbstractSocialEntity {

    private static final long serialVersionUID = 1L;

    static {
        EntityNewsDetailsFactory.INSTANCE.register(EntityType.CENTER,
                CommentNewsEntityDetails.class);
    }

    public String content;
    public boolean isReply;
    public int pageNumber;
    public SrcEntity parentDetails;
    public SrcEntity rootDetails;

    public CommentNewsEntityDetails(EntityType type, String id) {

        super(type, id);

    }

}
