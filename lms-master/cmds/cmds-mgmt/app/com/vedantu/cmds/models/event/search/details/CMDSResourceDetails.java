package com.vedantu.cmds.models.event.search.details;

import org.apache.commons.lang3.StringUtils;

import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.content.search.details.AbstractBoardSearchEntityTagDetails;

public class CMDSResourceDetails extends AbstractBoardSearchEntityTagDetails implements
        IListResponseObj {

    public SrcEntity content;
    public String    queryContext;

    @Override
    public SrcEntity __getSrcEntity() {

        return content;
    }

    @Override
    public NewsActivity toNewsActivity() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean _isIndexable() {

        return StringUtils.isNotEmpty(name) || StringUtils.isNotEmpty(queryContext);
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{content:").append(content).append(", queryContext:").append(queryContext)
                .append(", boardTree:").append(boardTree).append(", boards:").append(boards)
                .append(", targets:").append(targets).append(", contentSrc:").append(contentSrc)
                .append(", tags:").append(tags).append(", scope:").append(scope)
                .append(", avgRating:").append(avgRating).append(", views:").append(views)
                .append(", followers:").append(followers).append(", comments:").append(comments)
                .append(", upVotes:").append(upVotes).append(", difficulty:").append(difficulty)
                .append(", name:").append(name).append(", userId:").append(userId).append(", id:")
                .append(id).append(", userAction:").append(userAction).append(", timeCreated:")
                .append(timeCreated).append(", lastUpdated:").append(lastUpdated)
                .append(", lastIndexTime:").append(lastIndexTime)
                .append(", isNotificationEnabled:").append(isNotificationEnabled).append(", user:")
                .append(user).append("}");
        return builder.toString();
    }

}
