package com.vedantu.eventbus.processors.challenges;

import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;

public abstract class AbstractChallengeRewardProcessor implements IProcessor {

	@Override
	public Status process(IConsumable consumable) {

		return Status.SUCCESS;
	}

}
