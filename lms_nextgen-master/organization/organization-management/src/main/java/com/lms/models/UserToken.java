package com.lms.models;

import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(value = "usertokens")
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
