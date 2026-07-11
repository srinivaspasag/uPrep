package com.lms.user.vedantu.user.requests;

import com.lms.user.vedantu.user.enums.Gender;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class UpdateUserReq extends AbstractAppCheckReq{
    @NotBlank(message = "Target user ID is Required")
    public String  targetUserId;
    @NotBlank(message = "First name is required")
    public String  firstName;
    public String  lastName;
    public String  dob;
    @NotBlank(message = "Gender is required")
    public Gender gender;
    private String email;

    public String orgId;
    public String orgName;
    public String password;

    public String getEmail() {

        return email;
    }

    public void setEmail(String email) {

        this.email = email.toLowerCase();
    }
}
