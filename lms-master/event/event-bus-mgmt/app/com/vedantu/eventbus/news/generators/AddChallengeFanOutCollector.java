package com.vedantu.eventbus.news.generators;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableLong;

import com.vedantu.comm.enums.NotificationReason;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.daos.challenges.ChallengeDAO;
import com.vedantu.content.enums.challenges.ChallengeStatus;
import com.vedantu.content.models.challenges.Challenge;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.organization.daos.OrgMemberDAO;
import com.vedantu.organization.models.OrgMember;

/**
 * This will take care of letting all users ( who are intended to know about published challenge)
 * 
 * @author vikram
 * 
 */
public class AddChallengeFanOutCollector extends AbstractNewsFanOutCollector {

    public static AddChallengeFanOutCollector INSTANCE = new AddChallengeFanOutCollector();

    private AddChallengeFanOutCollector() {

    }

    @Override
    final public void getNewsSubscribers(NewsActivity activity,
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

        if (challenge.status == ChallengeStatus.ACTIVE) {

            super.getNewsSubscribers(activity, subscribers, newsUpdateToDeduplicate);
            populateOrganizationMembers(activity, subscribers, newsUpdateToDeduplicate,
                    (Challenge) challengeModel);
        }
    }

    private void populateOrganizationMembers(NewsActivity activity,
            Map<NotificationReason, Set<SrcEntity>> subscribers,
            Set<SrcEntity> newsUpdateToDeduplicate, Challenge challenge) {

        MutableLong totalHits = new MutableLong();

        List<OrgMember> orgMembers = OrgMemberDAO.INSTANCE.getOrgMembers(challenge.contentSrc.id,
                null, null, null, null, null, null, MongoManager.NO_START, MongoManager.NO_LIMIT,
                totalHits);
        for (OrgMember orgMember : orgMembers) {

            accumulateFanOutEntity(new SrcEntity(EntityType.USER, orgMember.userId),
                    NotificationReason.SHARED_WITH, subscribers, newsUpdateToDeduplicate,
                    activity.actor);

        }
    }
}
