package com.lms.pojos;

import com.lms.common.news.EntityNewsInfo;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;

public class MadeVisibleNewsInfo extends EntityNewsInfo {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public SrcEntity target;

    @Override
    public void populate(String userId, String orgId) {


    }
}
