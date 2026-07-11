package com.lms.controller;


import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojo.request.AddUserTokenReq;
import com.lms.service.UserTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usertokens")
public class UserTokens {
    @Autowired
    UserTokenService userTokenServiceimpl;

    @PostMapping("/addUserToken")
    public ResponseEntity<VedantuResponse> addUserToken(AddUserTokenReq addUserTokenReq) {
        return ResponseEntity.ok(userTokenServiceimpl.addUserToken(addUserTokenReq));
    }

}
