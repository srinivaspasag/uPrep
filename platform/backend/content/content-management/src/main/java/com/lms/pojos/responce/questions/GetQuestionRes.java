package com.lms.pojos.responce.questions;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.enums.FollowType;
import com.lms.pojos.responce.analytics.IQuestionAnswer;
import com.lms.pojos.search.details.QuestionSearchIndexDetails;
import com.lms.utils.IAttemptableEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetQuestionRes extends QuestionSearchIndexDetails implements
        IListResponseObj, IAttemptableEntity {

    public boolean attempted;
    public boolean voted;
    public boolean hasSeenAnswer;
    public FollowType followType;
    public IQuestionAnswer answer;

    @Override
    public void _setVoted(boolean voted) {
        this.voted = voted;
    }

    @Override
    public void _setFollowType(FollowType followType) {
        this.followType = followType;
    }

    @Override
    public void _setAttempted(boolean attempted) {
        this.attempted = attempted;
        this.hasSeenAnswer = this.attempted;
    }

    @Override
    public String _getEntityId() {
        return id;
    }

}
