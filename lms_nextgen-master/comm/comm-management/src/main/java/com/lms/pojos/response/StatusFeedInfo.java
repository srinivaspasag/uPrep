package com.lms.pojos.response;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.models.StatusFeed;
import com.lms.pojo.OrgInfo;
import com.lms.pojos.Source;
import com.lms.user.vedantu.user.pojo.UserInfo;

public class StatusFeedInfo extends ModelBasicInfo {

    public Source sourceContent;
    public String statusMessage;

    public int upVotes;
    public int views;
    public int followers;
    public int comments;
    public int shares;
    public boolean voted;
    public UserInfo srcOwner;
    public OrgInfo contentSrcInfo;
    public long time;

    public StatusFeedInfo(StatusFeed feedInfo) {
        super(feedInfo._getStringId(), feedInfo.recordState);

        upVotes = feedInfo.upVotes;
        views = feedInfo.views;
        followers = feedInfo.followers;
        shares = feedInfo.shares;
        comments = feedInfo.comments;
        // srcOwner = UserDAO.INSTANCE.getBasicInfo(feedInfo.userId);
        sourceContent = feedInfo.sourceContent;
        statusMessage = feedInfo.statusMessage;
        /*
         * if (sourceContent != null && StringUtils.isNotEmpty(sourceContent.image)) {
         * LOGGER.info("Source image" + sourceContent.image); sourceContent.image =
         * ImageDisplayURLUtil.getStatuFeedOrginalImageURL(sourceContent.image); if
         * (sourceContent.linkType == LinkType.UPLOADED) { sourceContent.url =
         * sourceContent.image; }
         *
         * }
         */

        time = feedInfo.timeCreated;

    }

}
