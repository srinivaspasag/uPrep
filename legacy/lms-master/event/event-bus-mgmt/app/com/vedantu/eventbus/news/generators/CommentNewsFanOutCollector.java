package com.vedantu.eventbus.news.generators;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableLong;

import com.vedantu.comm.enums.NotificationReason;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.daos.QuestionDAO;
import com.vedantu.content.daos.SolutionDAO;
import com.vedantu.content.models.Question;
import com.vedantu.content.models.Solution;

public class CommentNewsFanOutCollector extends AbstractSocialNewsFanOutCollector {

    public static CommentNewsFanOutCollector INSTANCE = new CommentNewsFanOutCollector();

    private CommentNewsFanOutCollector() {

    }

    @Override
    public void getNewsSubscribers(NewsActivity activity,
            Map<NotificationReason, Set<SrcEntity>> subscribers,
            Set<SrcEntity> newsUpdateToDeduplicate) {

        super.getNewsSubscribers(activity, subscribers, newsUpdateToDeduplicate);
        populateForNonCommentSrc(activity, subscribers, newsUpdateToDeduplicate);
    }

    private void populateForNonCommentSrc(NewsActivity activity,
            Map<NotificationReason, Set<SrcEntity>> subscribers,
            Set<SrcEntity> newsUpdateToDeduplicate) {

        MutableLong totalHits = new MutableLong();

        switch (activity.src.type) {
        case SOLUTION:
            Solution solution = SolutionDAO.INSTANCE.getById(activity.src.id);
            Question question = QuestionDAO.INSTANCE.getById(solution.qId);
            accumulateFanOutEntity(new SrcEntity(EntityType.USER, question.userId),
                    NotificationReason.ROOT_OWNER, subscribers, newsUpdateToDeduplicate,
                    activity.actor);
            break;
        default:
            break;
        }
    }
}
