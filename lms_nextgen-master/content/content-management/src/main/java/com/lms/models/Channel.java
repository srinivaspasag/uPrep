package com.lms.models;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.Scope;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(value = "channels")
public class Channel extends VedantuBaseMongoModel
{
    public String    userId;
    public String    name;
    public Scope scope;
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
