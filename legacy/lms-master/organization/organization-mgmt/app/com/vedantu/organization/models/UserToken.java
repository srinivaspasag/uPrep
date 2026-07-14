package com.vedantu.organization.models;

import com.google.code.morphia.annotations.Entity;
import com.vedantu.mongo.VedantuBaseMongoModel;

// This model is for firebase tokens

@Entity(value = "usertokens", noClassnameStored = true)
public class UserToken extends VedantuBaseMongoModel {
    public String userId;
    public String tokenId;

    public UserToken() {
        super();
    }

    public UserToken(String userId) {
        super();
        this.userId = userId;
    }

    public UserToken(String userId, String tokenId) {
        super();
        this.userId = userId;
        this.tokenId = tokenId;
    }

    @Override
    public String toString() {
        return "UserToken [userId=" + userId + ", tokenId=" + tokenId + "]";
    }

}
