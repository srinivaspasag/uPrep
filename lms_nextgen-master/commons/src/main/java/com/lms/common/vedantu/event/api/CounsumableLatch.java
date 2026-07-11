package com.lms.common.vedantu.event.api;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CounsumableLatch extends CountDownLatch {

	private static final Logger logger = LoggerFactory.getLogger(CounsumableLatch.class);

	private int successfullyConsumedCount = 0;

	public CounsumableLatch(int count) {

		super(count);
		logger.debug("created signal with count : " + count);
	}

	public void consumed(boolean success) {

		successfullyConsumedCount += success ? 1 : 0;
		super.countDown();
	}

	public int getSuccessfullyConsumedCount() {

		return successfullyConsumedCount;
	}

}
