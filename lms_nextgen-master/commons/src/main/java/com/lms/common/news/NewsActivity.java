package com.lms.common.news;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.lms.common.ShareWithEntity;
import com.lms.common.hbase.AbstractHbaseModels;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EventType;
import com.lms.common.vedantu.enums.Scope;

import java.util.List;

public class NewsActivity  extends AbstractHbaseModels {

    public EventType eType;
    public long                  time;
    public SrcEntity src;
    // @JsonManagedReference
    public SrcEntity             srcOwner;
    // @JsonManagedReference
    public SrcEntity             actor;
    @JsonManagedReference
    public List<ShareWithEntity> sharedWith;
    public String                comments;
    @JsonManagedReference
    public List<SrcEntity>       involved;
    public EntityNewsInfo        info;
    public boolean               sendNewsFeed;
    public Scope scope;
    public Scope                 orgId;

    public NewsActivity() {

        this.time = System.currentTimeMillis();
        this.sendNewsFeed = true;
        this.scope = Scope.UNKNOWN;
    }

    public NewsActivity(NewsActivity n) {

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

        if (o == null || !(o instanceof NewsActivity)) {
            return false;
        }
        NewsActivity testNewsActivity = (NewsActivity) o;
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

    public List<SrcEntity> getInvolved() {

        return involved;
    }

    public void setSharedWith(List<ShareWithEntity> sharedWith) {

        this.sharedWith = sharedWith;
    }

    public void setInvolved(List<SrcEntity> involved) {

        this.involved = involved;
    }

    public boolean isPrivate() {

        if (scope == Scope.PRIVATE) {
            return true;
        }
        return false;
    }

    @Override
    public String getKey() {

        // TODO Auto-generated method stub
        return null;
    }

}
