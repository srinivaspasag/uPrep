package com.lms.pojos.responce;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.user.vedantu.user.pojo.UserInfo;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class AbstractContentRes implements IListResponseObj {
    public String id;
    public UserInfo user;
    public long timeCreated;
    public long lastUpdated;

    public AbstractContentRes() {
    }

    public AbstractContentRes(String id, long timeCreated, long lastUpdated) {
        super();
        this.id = id;
        this.timeCreated = timeCreated;
        this.lastUpdated = lastUpdated;
    }



    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{id:");
        builder.append(id);
        builder.append(", user:");
        builder.append(user);
        builder.append(", timeCreated:");
        builder.append(timeCreated);
        builder.append(", lastUpdated:");
        builder.append(lastUpdated);
        builder.append("}");
        return builder.toString();
    }

}
