package com.vedantu.comm.news.details;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.FollowType;
import com.vedantu.commons.utils.ISocialEntity;

public class SolutionNewsDetails extends AbstractSocialEntity implements ISocialEntity {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public String             content;
    
    public String             qId;

    public SolutionNewsDetails() {

        type = EntityType.SOLUTION;
    }

    public SolutionNewsDetails(String id) {

        type = EntityType.SOLUTION;
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
