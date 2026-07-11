package com.vedantu.eventbus.news.generators;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.vedantu.comm.enums.NotificationReason;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.daos.analytics.UserEntityAttemptDAO;
import com.vedantu.content.daos.challenges.ChallengeDAO;
import com.vedantu.content.enums.challenges.ChallengeStatus;
import com.vedantu.content.models.analytics.UserEntityAttempt;
import com.vedantu.content.models.challenges.Challenge;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;

public class EndChallengeNewsFanOutCollector extends AbstractNewsFanOutCollector {

    public static EndChallengeNewsFanOutCollector INSTANCE = new EndChallengeNewsFanOutCollector();

    private EndChallengeNewsFanOutCollector() {

    }

    @Override
    public void getNewsSubscribers(NewsActivity activity,
            Map<NotificationReason, Set<SrcEntity>> subscribers,
            Set<SrcEntity> newsUpdateToDeduplicate) {

        VedantuBasicDAO<?, ?> basicDAO = EntityTypeDAOFactory.INSTANCE.get(activity.src.type);
        if (!(basicDAO instanceof ChallengeDAO)) {
            return;
        }

        VedantuBaseMongoModel challengeModel = (VedantuBaseMongoModel) basicDAO
                .getById(activity.src.id);
        if (!(challengeModel instanceof Challenge)) {
            return;
        }
        Challenge challenge = (Challenge) challengeModel;

        if (challenge.status == ChallengeStatus.ENDED) {
            super.getNewsSubscribers(activity, subscribers, newsUpdateToDeduplicate);

            SrcEntity challengeEntity = new SrcEntity(EntityType.CHALLENGE, activity.src.id);

            List<UserEntityAttempt> attempts = UserEntityAttemptDAO.INSTANCE.getUserAttempts(
                    EntityType.CHALLENGE, activity.src.id);

            if (CollectionUtils.isNotEmpty(attempts)) {
                Set<SrcEntity> userEntities = new HashSet<SrcEntity>();
                for (UserEntityAttempt attempt : attempts) {
                    accumulateFanOutEntity(new SrcEntity(EntityType.USER, attempt.userId),
                            NotificationReason.ATTEMPTED, subscribers, newsUpdateToDeduplicate,
                            activity.actor);
                }
            }

        }

    }
}
