package com.lms.user.vedantu.user.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserSaltDto {
    public String username;
    public String salt;
}
