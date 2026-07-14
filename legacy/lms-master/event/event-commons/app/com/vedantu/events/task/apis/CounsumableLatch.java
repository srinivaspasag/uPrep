package com.vedantu.events.task.apis;

import java.util.concurrent.CountDownLatch;

import play.Logger;
import play.Logger.ALogger;

public class CounsumableLatch extends CountDownLatch {

    private static final ALogger LOGGER          = Logger.of(CounsumableLatch.class);

    private int                  successfullyConsumedCount = 0;

    public CounsumableLatch(int count) {

        super(count);
        LOGGER.debug("created signal with count : " + count);
    }

    public void consumed(boolean success) {

        successfullyConsumedCount += success ? 1 : 0;
        super.countDown();
    }

    public int getSuccessfullyConsumedCount() {

        return successfullyConsumedCount;
    }
    
    
}
