package com.vedantu.comm.news.details;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.FollowType;
import com.vedantu.commons.utils.ISocialEntity;

public class DocumentNewsEntityDetails extends AbstractSocialEntity implements ISocialEntity {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public String             name;

    public DocumentNewsEntityDetails() {

        type = EntityType.FILE;
    }

    public DocumentNewsEntityDetails(String id) {

        type = EntityType.DOCUMENT;
        this.id = id;
        // TODO Auto-generated constructor stub
    }

    @Override
    public String _getEntityId() {

        return id;
    }

    @Override
    public void _setVoted(boolean voted) {

        // TODO Auto-generated method stub

    }

    @Override
    public void _setFollowType(FollowType followType) {

        // TODO Auto-generated method stub

    }
}
