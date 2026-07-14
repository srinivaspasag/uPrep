package com.vedantu.comm.news.details;

import com.vedantu.comm.managers.news.populators.EntityNewsDetailsFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;

public class CommentNewsEntityDetails extends AbstractSocialEntity {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public String             content;
    public boolean            isReply;
    public int                pageNumber;
    public SrcEntity          parentDetails;
    public SrcEntity          rootDetails;

    static {
        EntityNewsDetailsFactory.INSTANCE.register(EntityType.CENTER,
                CommentNewsEntityDetails.class);
    }

    public CommentNewsEntityDetails(EntityType type, String id) {

        super(type, id);

    }
}
