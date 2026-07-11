package com.lms.models;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.enums.ShareType;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Document(value = "entitysharemapping")
@CompoundIndexes(@CompoundIndex(name = "userId, entity.id, entity.type"))
public class EntityShare extends VedantuBaseMongoModel {

    public String userId;
    public SrcEntity entity;
    public Set<SrcEntity> with;
    public String content;
    public ShareType type;

    // public ActivityPermission permission;
    public EntityShare() {

    }

    public EntityShare(String userId, SrcEntity entity, Collection<? extends SrcEntity> with, String content,
                       ShareType type) {
        super();
        this.userId = userId;
        this.entity = entity;
        this.with = new HashSet<SrcEntity>();
        for (SrcEntity wth : with) {
            if (wth != null) {
                this.with.add(wth);
            }
        }
        this.content = content != null ? content : "";
        this.type = type;
        // this.permission = ActivityPermission.VIEW;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EntityShare [userId:").append(userId).append(", entity:").append(entity).append(", with:")
                .append(with).append(", content:").append(content).append(", type:").append(type)
                // .append(", permission:").append(permission)
                .append(", ").append(super.toString()).append("]");
        return builder.toString();
    }

}
