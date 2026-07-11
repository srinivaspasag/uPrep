package com.lms.user.vedantu.user.pojo.responce;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestUserDataRes {
    public boolean userAlreadyExists;
    public boolean userAdded;
    public boolean mappingAdded;
    public String userId;
}
