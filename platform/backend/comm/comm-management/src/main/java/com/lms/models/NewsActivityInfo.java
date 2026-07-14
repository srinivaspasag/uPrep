package com.lms.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.lms.common.ShareWithEntity;
import com.lms.common.news.EntityNewsInfo;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EventType;
import com.lms.common.vedantu.enums.Scope;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("newsactivity")
@Getter
@Setter
public class NewsActivityInfo extends VedantuBaseMongoModel {

    public EventType eType;
    public long time;
    public SrcEntity src;
    // @JsonManagedReference
    public SrcEntity srcOwner;
    // @JsonManagedReference
    public SrcEntity actor;
    @JsonManagedReference
    public List<ShareWithEntity> sharedWith;
    public String comments;
    @JsonManagedReference
    public List<SrcEntity> involved;
    public EntityNewsInfo info;
    public boolean sendNewsFeed;
    public Scope scope;
    public Scope orgId;

    public NewsActivityInfo() {

        this.time = System.currentTimeMillis();
        this.sendNewsFeed = true;
        this.scope = Scope.UNKNOWN;
    }

    public NewsActivityInfo(NewsActivityInfo n) {

        this.eType = n.eType;
        this.time = n.time;
        this.src = n.src;
        this.srcOwner = n.srcOwner;
        this.actor = n.actor;
        this.sharedWith = n.sharedWith;
        this.comments = n.comments;
        this.involved = n.involved;
        this.info = n.info;
        this.sendNewsFeed = n.sendNewsFeed;
        this.scope = n.scope;
        this.orgId = n.orgId;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("NewsActivity [eType:").append(eType).append(", time:").append(time)
                .append(", src:").append(src).append(", srcOwner:").append(srcOwner)
                .append(", actor:").append(actor).append(", sharedWith:").append(sharedWith)
                .append(", comments:").append(comments).append(", involved:").append(involved)
                .append(", info:").append(info).append(", sendNewsFeed:").append(sendNewsFeed)
                .append(", scope:").append(scope).append("]");
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {

        if (o == null || !(o instanceof NewsActivityInfo)) {
            return false;
        }
        NewsActivityInfo testNewsActivity = (NewsActivityInfo) o;
        return (testNewsActivity.eType.equals(this.eType))
                && (testNewsActivity.src.equals(this.src))
                && (testNewsActivity.actor.equals(this.actor));
    }

    @Override
    public int hashCode() {

        return (this.src.id + ":" + this.actor.id + ":" + this.eType).hashCode();
    }

    public List<ShareWithEntity> getSharedWith() {

        return sharedWith;
    }

    public void setSharedWith(List<ShareWithEntity> sharedWith) {

        this.sharedWith = sharedWith;
    }

    public List<SrcEntity> getInvolved() {

        return involved;
    }

    public void setInvolved(List<SrcEntity> involved) {

        this.involved = involved;
    }

    public boolean isPrivate() {

        return scope == Scope.PRIVATE;
    }


}
