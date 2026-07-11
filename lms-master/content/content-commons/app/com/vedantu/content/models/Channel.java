package com.vedantu.content.models;

import com.google.code.morphia.annotations.Entity;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "channels", noClassnameStored = true)
public class Channel extends VedantuBaseMongoModel {

    public String    userId;
    public String    name;
    public Scope     scope;
    public int       contentCount;
    public SrcEntity contentSrc;

    public Channel() {

        super();
    }

    public Channel(String userId, String name, SrcEntity contentSrc, Scope scope) {

        super();
        this.userId = userId;
        this.name = name;
        this.scope = scope;
        this.contentSrc = contentSrc;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{userId:").append(userId).append(", name:").append(name).append(", scope:")
                .append(scope).append(", contentCount:").append(contentCount)
                .append(", contentSrc:").append(contentSrc).append(", id:").append(_getStringId())
                .append(", timeCreated:").append(timeCreated).append(", lastUpdated:")
                .append(lastUpdated).append(", recordState:").append(recordState).append("}");
        return builder.toString();
    }

}
