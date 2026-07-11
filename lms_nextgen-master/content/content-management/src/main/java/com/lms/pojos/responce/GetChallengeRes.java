package com.lms.pojos.responce;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.pojos.ChallengeTakenBasicInfo;
import com.lms.pojos.search.details.ChallengeSearchIndexDetails;
import com.lms.user.vedantu.user.pojo.UserInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class GetChallengeRes extends ChallengeSearchIndexDetails implements
        IListResponseObj {

    public boolean attempted;
    public ChallengeTakenBasicInfo info;
    public List<UserInfo> toppers;

    public void addTopper(UserInfo topper) {
        if (toppers == null) {
            toppers = new ArrayList<UserInfo>();
        }
        if (topper != null) {
            toppers.add(topper);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{attempted:");
        builder.append(attempted);
        builder.append(",info:");
        builder.append(info);
        builder.append(",toppers:");
        builder.append(toppers);
        builder.append(",");
        builder.append(super.toString());
        builder.append("}");
        return builder.toString();
    }

}
