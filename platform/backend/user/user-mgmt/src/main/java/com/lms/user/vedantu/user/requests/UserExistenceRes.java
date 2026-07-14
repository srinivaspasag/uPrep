package com.lms.user.vedantu.user.requests;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserExistenceRes {

    public boolean doesEmailExists;
    public boolean doesContactNumberExists;
    public boolean doesReferralCodeExists;

}
