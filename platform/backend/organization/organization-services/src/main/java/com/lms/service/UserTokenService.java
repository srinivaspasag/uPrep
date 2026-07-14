package com.lms.service;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojo.request.AddUserTokenReq;

public interface UserTokenService {

    VedantuResponse addUserToken(AddUserTokenReq addUserTokenReq);
}
