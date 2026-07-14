package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthRequest {
    private String username;
    private String password;
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
}
