package com.lms.models.events.searchdetails;

import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.pojos.search.details.AbstractBoardSearchEntityTagDetails;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

@Setter
@Getter
public class CMDSResourceDetails extends AbstractBoardSearchEntityTagDetails implements
        IListResponseObj {

    public SrcEntity content;
    public String queryContext;

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

        return StringUtils.isEmpty(name) || StringUtils.isEmpty(queryContext);
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
