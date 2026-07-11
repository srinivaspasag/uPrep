package com.lms.news.generators;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lms.common.vedantu.enums.EventType;
import com.lms.managers.NewsActivityGenerator;
import com.lms.managers.news.generator.INewsFanOutCollector;

public class NewsFanOutCollectorFactory {

	private static final Logger logger = LoggerFactory.getLogger(NewsFanOutCollectorFactory.class);
	public static NewsFanOutCollectorFactory INSTANCE = new NewsFanOutCollectorFactory();

	private HashMap<EventType, INewsFanOutCollector> eventNewsGeneratorMap = new HashMap<EventType, INewsFanOutCollector>();

	private NewsFanOutCollectorFactory() {

		eventNewsGeneratorMap.put(EventType.VOTE_ENTITY, VoteEntityNewsFanOutCollector.INSTANCE);
		//eventNewsGeneratorMap.put(EventType.ATTEMPT_ENTITY, AttemptEntityNewsFanOutCollector.INSTANCE);
		//eventNewsGeneratorMap.put(EventType.FOLLOW_ENTITY, FollowEntityNewsFanOutCollector.INSTANCE);
		//eventNewsGeneratorMap.put(EventType.ADD_COMMENT, CommentNewsFanOutCollector.INSTANCE);
		//eventNewsGeneratorMap.put(EventType.END_CHALLENGE, EndChallengeNewsFanOutCollector.INSTANCE);
		//eventNewsGeneratorMap.put(EventType.SHARE_ENTITY, ShareEntityNewsFanOutCollector.INSTANCE);
		//eventNewsGeneratorMap.put(EventType.POST_REMARK, PostRemarkNewsFanOutCollector.INSTANCE);
		//eventNewsGeneratorMap.put(EventType.MADE_VISIBLE, MadeVisibleNewsFanOutCollector.INSTANCE);
		//eventNewsGeneratorMap.put(EventType.ADD_SOLUTION, AddSolutionNewsFanOutCollector.INSTANCE);
		//eventNewsGeneratorMap.put(EventType.INDEX_CHALLENGE, AddChallengeFanOutCollector.INSTANCE);

	}

	public INewsFanOutCollector get(EventType eventType) {

		logger.debug("Getting NewsGenerator information for eventType : " + eventType.name());
		INewsFanOutCollector newsGenerator = eventNewsGeneratorMap.get(eventType);
		if (newsGenerator == null) {
			logger.debug("No NewsGenerator found for eventType : " + eventType);
		}
		return newsGenerator;
	}

}
