package com.vedantu.eventbus.processors.discussions;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.content.search.details.DiscussionSearchIndexDetails;
import com.vedantu.eventbus.processors.AbstractSearchIndexProcessor;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.enums.Status;

public class DiscussionSearchIndexProcessor extends
		AbstractSearchIndexProcessor {

	public DiscussionSearchIndexProcessor() {
		super(EntityType.DISCUSSION.getIndexType(), EntityType.DISCUSSION
				.getIndexName());
	}

	@Override
	public Status process(IConsumable consumable) {
		return super.process(consumable, new DiscussionSearchIndexDetails());
	}

}
