package com.lms.pojos;

import com.lms.user.vedantu.user.pojo.UserInfo;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EntityTopper {

    public UserInfo user;
    public float percentage;

    public EntityTopper() {
    }

    public EntityTopper(UserInfo user, float percentage) {
        this.user = user;
        this.percentage = percentage;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{user:");
        builder.append(user);
        builder.append(", percentage:");
        builder.append(percentage);
        builder.append("}");
        return builder.toString();
    }

}
