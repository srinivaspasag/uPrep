package com.vedantu.eventbus.processors.challenges;

import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.enums.Status;

public class ChallengeCreditProcessor extends AbstractChallengeRewardProcessor {

    @Override
    public Status process(IConsumable consumable) {
        return Status.SUCCESS;
    }

}
