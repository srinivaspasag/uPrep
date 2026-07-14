package com.vedantu.comm.news.details;

import com.vedantu.comm.managers.news.populators.EntityNewsDetailsFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;

public class RemarkNewsEntityDetails extends SrcEntity {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    public String             content;
    public SrcEntity          contentSrc;

    static {
        EntityNewsDetailsFactory.INSTANCE
                .register(EntityType.REMARK, RemarkNewsEntityDetails.class);
    }

    public RemarkNewsEntityDetails(EntityType type, String id) {

        super(type, id);
        // TODO Auto-generated constructor stub
    }
}
