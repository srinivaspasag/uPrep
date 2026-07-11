package com.vedantu.content.pojos.responses;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.utils.ImageDisplayURLUtil;
import com.vedantu.content.models.StatusFeed;
import com.vedantu.content.pojos.Source;
import com.vedantu.content.pojos.SrcType.LinkType;
import com.vedantu.organization.pojos.responses.organizations.OrgInfo;
import com.vedantu.user.daos.UserDAO;
import com.vedantu.user.pojos.UserInfo;

public class StatusFeedInfo extends ModelBasicInfo {

	private final ALogger	LOGGER	= Logger.of(StatusFeedInfo.class);

	public Source			sourceContent;
	public String			statusMessage;

	public int				upVotes;
	public int				views;
	public int				followers;
	public int				comments;
	public int				shares;
	public boolean			voted;
	public UserInfo			srcOwner;
	public OrgInfo			contentSrcInfo;
	public long				time;

	public StatusFeedInfo(StatusFeed feedInfo) {
		super(feedInfo._getStringId(), feedInfo.recordState);

		upVotes = feedInfo.upVotes;
		views = feedInfo.views;
		followers = feedInfo.followers;
		shares = feedInfo.shares;
		comments = feedInfo.comments;
		srcOwner = UserDAO.INSTANCE.getBasicInfo(feedInfo.userId);
		sourceContent = feedInfo.sourceContent;
		statusMessage = feedInfo.statusMessage;
		if (sourceContent != null
				&& StringUtils.isNotEmpty(sourceContent.image)) {
			LOGGER.info("Source image" + sourceContent.image);
			sourceContent.image = ImageDisplayURLUtil
					.getStatuFeedOrginalImageURL(sourceContent.image);
			if (sourceContent.linkType == LinkType.UPLOADED) {
				sourceContent.url = sourceContent.image;
			}

		}
		
		time = feedInfo.timeCreated;

	}

}
