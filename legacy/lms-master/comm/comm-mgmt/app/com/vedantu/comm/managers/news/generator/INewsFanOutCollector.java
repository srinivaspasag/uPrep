package com.vedantu.comm.managers.news.generator;

import java.util.Map;
import java.util.Set;

import com.vedantu.comm.enums.NotificationReason;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;

public interface INewsFanOutCollector {

	void getNewsSubscribers(NewsActivity activity,
			Map<NotificationReason, Set<SrcEntity>> subscribers,
			Set<SrcEntity> newsUpdateToDeduplicate);
}
