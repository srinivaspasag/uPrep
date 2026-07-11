package com.vedantu.comm.news.details;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.FollowType;
import com.vedantu.commons.utils.ISocialEntity;

public class TestNewsEntityDetails extends AbstractSocialEntity implements ISocialEntity {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public String             name;


    public TestNewsEntityDetails() {

        type = EntityType.TEST;

    }

    public TestNewsEntityDetails(String id, EntityType type) {

        this.type = type;
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
