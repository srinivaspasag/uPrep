package com.lms.pojos.responce;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetEntityReviews {

    public String userName;
    public String userProfilePic;
    public String userId;
    public String feedback;
    public long lastUpdated;
    public boolean approved;

    public void clear() {
        // TODO Auto-generated method stub
        this.userId = "";
        this.userName = "";
        this.userProfilePic = "";
        this.feedback = "";
        this.lastUpdated = 0;
        this.approved = false;
    }

}
