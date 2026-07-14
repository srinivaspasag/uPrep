package com.vedantu.organization.managers;

import com.vedantu.organization.daos.UserTokenDAO;
import com.vedantu.organization.models.UserToken;
import com.vedantu.organization.pojos.requests.organizations.AddUserTokenReq;
import com.vedantu.organization.pojos.responses.organizations.AddUserTokenRes;
import com.vedantu.user.daos.UserDAO;
import com.vedantu.user.models.User;

public class UserTokenManager {

    public static AddUserTokenRes addUserToken(AddUserTokenReq addUserTokenReq) {
        User user = UserDAO.INSTANCE.getById(addUserTokenReq.userId);
        if (user != null) {
            UserToken userTokenCheck = UserTokenDAO.INSTANCE.getUserTokenByUserId(addUserTokenReq.userId);
            if(userTokenCheck != null){
                userTokenCheck.tokenId = addUserTokenReq.tokenId;
                UserTokenDAO.INSTANCE.save(userTokenCheck);
            }else{
                UserToken userToken = new UserToken(addUserTokenReq.userId, addUserTokenReq.tokenId);
                UserTokenDAO.INSTANCE.save(userToken);
            }
        }
        AddUserTokenRes response = new AddUserTokenRes();
        response.success = true;
        return response;
    }

}
