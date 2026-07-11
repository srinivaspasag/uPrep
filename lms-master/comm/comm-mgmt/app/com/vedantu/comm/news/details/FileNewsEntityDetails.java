package com.vedantu.comm.news.details;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.FollowType;
import com.vedantu.commons.utils.ISocialEntity;

public class FileNewsEntityDetails extends AbstractSocialEntity implements ISocialEntity {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public String             name;

    public FileNewsEntityDetails() {

        type = EntityType.FILE;
    }

    public FileNewsEntityDetails(String id) {

        type = EntityType.FILE;
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
