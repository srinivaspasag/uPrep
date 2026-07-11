package com.vedantu.comm.news.details;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.FollowType;
import com.vedantu.commons.utils.ISocialEntity;

public class DiscussionNewsEntityDetails extends AbstractSocialEntity implements ISocialEntity {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public String             name;

    public DiscussionNewsEntityDetails() {

        type = EntityType.DISCUSSION;
    }

    public DiscussionNewsEntityDetails(String id) {

        type = EntityType.DISCUSSION;
        this.id = id;
    }

    @Override
    public String _getEntityId() {

        return id;
    }

    @Override
    public void _setVoted(boolean voted) {

    }

    @Override
    public void _setFollowType(FollowType followType) {

    }

}
