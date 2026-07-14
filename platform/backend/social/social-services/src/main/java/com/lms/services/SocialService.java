package com.lms.services;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.enums.UserActionType;
import com.lms.requests.AddEntityUserActionReq;
import com.lms.requests.GetEntityUserActionUsersReq;
import com.lms.requests.RemoveEntityUserActionReq;
import com.lms.requests.SendEmailReq;

public interface SocialService {

    VedantuResponse view(AddEntityUserActionReq addEntityUserActionReq);

    VedantuResponse upVote(AddEntityUserActionReq addEntityUserActionReq);

    VedantuResponse follow(AddEntityUserActionReq addEntityUserActionReq);

    VedantuResponse unfollow(RemoveEntityUserActionReq removeEntityUserActionReq);

    VedantuResponse getfollowers(GetEntityUserActionUsersReq getEntityUserActionUsersReq);

    VedantuResponse getViewers(GetEntityUserActionUsersReq getEntityUserActionUsersReq, UserActionType actionType);

    VedantuResponse completed(AddEntityUserActionReq addEntityUserActionReq, UserActionType userActionType, boolean allowDuplicates);

    VedantuResponse getvoters(GetEntityUserActionUsersReq getEntityUserActionUsersReq);

    VedantuResponse sendemail(SendEmailReq sendEmailReq);
}
