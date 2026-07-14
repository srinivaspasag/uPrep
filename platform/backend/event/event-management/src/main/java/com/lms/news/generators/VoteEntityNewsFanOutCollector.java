package com.lms.news.generators;

import java.util.Map;
import java.util.Set;

import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.enums.NotificationReason;

public class VoteEntityNewsFanOutCollector extends AbstractSocialNewsFanOutCollector {

	public static VoteEntityNewsFanOutCollector INSTANCE = new VoteEntityNewsFanOutCollector();

	private VoteEntityNewsFanOutCollector() {

	}

	
	@Override
	public void getNewsSubscribers(NewsActivity activity, Map<NotificationReason, Set<SrcEntity>> subscribers,
			Set<SrcEntity> newsUpdateToDeduplicate) {

		super.getNewsSubscribers(activity, subscribers, newsUpdateToDeduplicate);
	}

}
