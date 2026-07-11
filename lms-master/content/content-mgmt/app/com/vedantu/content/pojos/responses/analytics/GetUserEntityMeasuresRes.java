package com.vedantu.content.pojos.responses.analytics;

import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.content.models.analytics.QuestionMeasures;
import com.vedantu.user.pojos.UserInfo;

public class GetUserEntityMeasuresRes implements IListResponseObj {

    public QuestionMeasures measures;
    public UserInfo         user;
    public long             lastAttempted;

    public void _finalizeMeasures() {

        measures.left -= (measures.correct + measures.incorrect);
        measures.attempts = measures.correct + measures.incorrect;
    }
}
