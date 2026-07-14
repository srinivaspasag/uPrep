package com.vedantu.eventbus.news.generators;

import java.util.HashMap;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.comm.managers.news.generator.INewsFanOutCollector;
import com.vedantu.commons.enums.EventType;

public class NewsFanOutCollectorFactory {

    private static final ALogger               LOGGER                = Logger.of(NewsFanOutCollectorFactory.class);
    public static NewsFanOutCollectorFactory         INSTANCE              = new NewsFanOutCollectorFactory();

    private HashMap<EventType, INewsFanOutCollector> eventNewsGeneratorMap = new HashMap<EventType, INewsFanOutCollector>();

    private NewsFanOutCollectorFactory() {

        eventNewsGeneratorMap.put(EventType.VOTE_ENTITY, VoteEntityNewsFanOutCollector.INSTANCE);
        eventNewsGeneratorMap.put(EventType.ATTEMPT_ENTITY, AttemptEntityNewsFanOutCollector.INSTANCE);
        eventNewsGeneratorMap.put(EventType.FOLLOW_ENTITY, FollowEntityNewsFanOutCollector.INSTANCE);
        eventNewsGeneratorMap.put(EventType.ADD_COMMENT, CommentNewsFanOutCollector.INSTANCE);
        eventNewsGeneratorMap.put(EventType.END_CHALLENGE, EndChallengeNewsFanOutCollector.INSTANCE);
        eventNewsGeneratorMap.put(EventType.SHARE_ENTITY, ShareEntityNewsFanOutCollector.INSTANCE);
        eventNewsGeneratorMap.put(EventType.POST_REMARK, PostRemarkNewsFanOutCollector.INSTANCE);
        eventNewsGeneratorMap.put(EventType.MADE_VISIBLE, MadeVisibleNewsFanOutCollector.INSTANCE);
        eventNewsGeneratorMap.put(EventType.ADD_SOLUTION, AddSolutionNewsFanOutCollector.INSTANCE);
        eventNewsGeneratorMap.put(EventType.INDEX_CHALLENGE, AddChallengeFanOutCollector.INSTANCE);

    }

    public INewsFanOutCollector get(EventType eventType) {

        LOGGER.debug("Getting NewsGenerator information for eventType : " + eventType.name());
        INewsFanOutCollector newsGenerator = eventNewsGeneratorMap.get(eventType);
        if (newsGenerator == null) {
            LOGGER.debug("No NewsGenerator found for eventType : " + eventType);
        }
        return newsGenerator;
    }

}
