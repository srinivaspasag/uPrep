package com.lms.user.vedantu.user.requests;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class UserExistenceReq {
    @NotBlank(message = "username is required")
    public String email;
  //  @NotBlank(message = "username is required")
    public String contactNumber;
 //   @NotBlank(message = "username is required")
    public String countryCode;
    public String referralCode;
}
