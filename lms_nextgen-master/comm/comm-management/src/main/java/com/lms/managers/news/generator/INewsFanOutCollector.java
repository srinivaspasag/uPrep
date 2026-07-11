package com.lms.managers.news.generator;

import java.util.Map;
import java.util.Set;

import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.enums.NotificationReason;

public interface INewsFanOutCollector {

	void getNewsSubscribers(NewsActivity activity, Map<NotificationReason, Set<SrcEntity>> subscribers,
			Set<SrcEntity> newsUpdateToDeduplicate);
}
