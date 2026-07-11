package com.lms.pojos.responce.analytics;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.models.QuestionMeasures;
import com.lms.user.vedantu.user.pojo.UserInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetUserEntityMeasuresRes implements IListResponseObj {
    public QuestionMeasures measures;
    public UserInfo user;
    public long lastAttempted;

    public void _finalizeMeasures() {

        measures.left -= (measures.correct + measures.incorrect);
        measures.attempts = measures.correct + measures.incorrect;
    }
}
