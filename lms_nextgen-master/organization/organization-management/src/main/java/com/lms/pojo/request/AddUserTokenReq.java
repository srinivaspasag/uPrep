package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddUserTokenReq {
    public String userId;
    public String tokenId;
}
