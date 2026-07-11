package com.lms.service.serviceImpl;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.models.UserToken;
import com.lms.pojo.request.AddUserTokenReq;
import com.lms.pojo.responce.AddUserTokenRes;
import com.lms.repository.UserTokenRepo;
import com.lms.service.UserTokenService;
import com.lms.user.vedantu.user.model.User;
import com.lms.user.vedantu.user.repository.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserTokenServiceimpl implements UserTokenService {
    private static final Logger logger = LoggerFactory.getLogger(CampaignServiceImpl.class);
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private UserTokenRepo userTokenRepo;

    @Override
    public VedantuResponse addUserToken(AddUserTokenReq addUserTokenReq) {

        if (addUserTokenReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        AddUserTokenRes response = new AddUserTokenRes();
        User user = userRepo.findById(addUserTokenReq.getUserId()).get();
        if (user != null) {
            UserToken userTokenCheck = getUserTokenByUserId(addUserTokenReq.userId);
            if (userTokenCheck != null) {
                userTokenCheck.tokenId = addUserTokenReq.tokenId;
                userTokenRepo.save(userTokenCheck);
            } else {
                UserToken userToken = new UserToken(addUserTokenReq.userId, addUserTokenReq.tokenId);
                userTokenRepo.save(userToken);
            }
        }
        response.success = true;
        return new VedantuResponse(response);
    }

    public UserToken getUserTokenByUserId(String userId) {
        UserToken userToken = userTokenRepo.findByUserId(userId);
        if (userToken == null) {
            logger.error("cannot find user token for userId :" + userId);
        }
        return userToken;
    }
}
