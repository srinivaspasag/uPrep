package com.vedantu.comm.news.details;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.FollowType;
import com.vedantu.commons.utils.ISocialEntity;

public class QuestionNewsEntityDetails extends AbstractSocialEntity implements ISocialEntity {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public String             content;

    public QuestionNewsEntityDetails() {

        type = EntityType.QUESTION;
    }

    public QuestionNewsEntityDetails(String id) {

        type = EntityType.QUESTION;
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
