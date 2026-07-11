package com.lms.pojos.responce;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.enums.FollowType;
import com.lms.pojos.search.details.DocumentSearchIndexDetails;
import com.lms.utils.ISocialEntity;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetDocumentRes extends DocumentSearchIndexDetails implements
        IListResponseObj, ISocialEntity {

    public boolean		voted;
    public FollowType	followType;

    @Override
    public void _setVoted(boolean voted) {
        this.voted = voted;
    }

    @Override
    public void _setFollowType(FollowType followType) {
        this.followType = followType;
    }

    @Override
    public String _getEntityId() {
        return id;
    }

}
