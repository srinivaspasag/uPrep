package com.vedantu.eventbus.news.generators;

import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.vedantu.comm.enums.NotificationReason;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.daos.SolutionDAO;
import com.vedantu.content.models.Solution;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuDBResult;

public class AddSolutionNewsFanOutCollector extends AbstractSocialNewsFanOutCollector {

    public static AddSolutionNewsFanOutCollector INSTANCE = new AddSolutionNewsFanOutCollector();

    private AddSolutionNewsFanOutCollector() {

    }

    @Override
    public void getNewsSubscribers(NewsActivity activity,
            Map<NotificationReason, Set<SrcEntity>> subscribers,
            Set<SrcEntity> newsUpdateToDeduplicate) {

        super.getNewsSubscribers(activity, subscribers, newsUpdateToDeduplicate);
        populateSolutionProviders( activity, subscribers, newsUpdateToDeduplicate);

    }

    protected void populateSolutionProviders( NewsActivity activity,
            Map<NotificationReason, Set<SrcEntity>> subscribers,
            Set<SrcEntity> newsUpdateToDeduplicate) {

       
        VedantuDBResult<Solution> solutionResult = SolutionDAO.INSTANCE.getSolutions(
                activity.src.id, null, MongoManager.NO_START, MongoManager.NO_LIMIT);

        if (CollectionUtils.isNotEmpty(solutionResult.results)) {
            for (Solution solution : solutionResult.results) {
                accumulateFanOutEntity(new SrcEntity(EntityType.USER, solution.userId),
                        NotificationReason.ADDED_SOLUTION, subscribers, newsUpdateToDeduplicate,
                        activity.actor);
            }
        }
    }
}
