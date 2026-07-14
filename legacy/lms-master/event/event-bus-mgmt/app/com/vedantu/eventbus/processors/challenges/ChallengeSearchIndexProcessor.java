package com.vedantu.eventbus.processors.challenges;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.relationships.ContentLinkRelationshipDetails;
import com.vedantu.content.managers.ChannelManager;
import com.vedantu.content.search.details.ChallengeSearchIndexDetails;
import com.vedantu.eventbus.processors.AbstractSearchIndexProcessor;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.enums.Status;

public class ChallengeSearchIndexProcessor extends AbstractSearchIndexProcessor {

    private boolean updateUserActionMapping = true;

    public ChallengeSearchIndexProcessor() {

        this(true);
    }

    public ChallengeSearchIndexProcessor(boolean updateUserActionMapping) {

        super(EntityType.CHALLENGE.getIndexType(), EntityType.CHALLENGE.getIndexName());
        this.updateUserActionMapping = updateUserActionMapping;
    }

    @Override
    public Status process(IConsumable consumable) {

        ChallengeSearchIndexDetails details = new ChallengeSearchIndexDetails();
        Status status = super.process(consumable, details);
        if (status == Status.SUCCESS && updateUserActionMapping) {
            ContentLinkRelationshipDetails channelEntityDetails = new ContentLinkRelationshipDetails(
                    details.userId, new SrcEntity(EntityType.CHALLENGE, details.id), new SrcEntity(
                            EntityType.CHANNEL, details.channelId), details.scope);
            ChannelManager.updateUserActionMappintToEs(channelEntityDetails, new SrcEntity(
                    EntityType.CHALLENGE, details.id), UserActionType.ADDED, EventActionType.ADD,
                    null);
        }
        return status;
    }
}
