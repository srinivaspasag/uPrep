package com.lms.user.vedantu.user.model;

import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "usersalts")
@Setter
@Getter
public class UserSalt extends VedantuBaseMongoModel {
    @Indexed(unique = true)
    public String username;
    public String salt;

    public UserSalt() {

    }

    public UserSalt(String username, String salt) {
        this.username = username;
        this.salt = salt;
    }

}
