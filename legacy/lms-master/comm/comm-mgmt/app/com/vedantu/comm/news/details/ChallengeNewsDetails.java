package com.vedantu.comm.news.details;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.FollowType;
import com.vedantu.commons.utils.ISocialEntity;

public class ChallengeNewsDetails extends AbstractSocialEntity implements ISocialEntity {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public String             name;
    public boolean			  succeded;
    public String             qId;

    public ChallengeNewsDetails() {

        super();
        type = EntityType.CHALLENGE;
    }

    public ChallengeNewsDetails(EntityType type, String id) {

        super(type, id);
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
