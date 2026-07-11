package com.vedantu.content.pojos.responses;

import com.vedantu.ei.utils.StringUtils;

public class GetEntityReviews {

    public String userName;
    public String userProfilePic;
    public String userId;
    public String feedback;
    public long lastUpdated;
    public boolean approved;

    public void clear() {
        // TODO Auto-generated method stub
        this.userId = StringUtils.EMPTY;
        this.userName = StringUtils.EMPTY;
        this.userProfilePic = StringUtils.EMPTY;
        this.feedback = StringUtils.EMPTY;
        this.lastUpdated = 0;
        this.approved = false;
    }

}
