package com.vedantu.events.manager;

import java.util.concurrent.Callable;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.AbstractVedantuManager;
import com.vedantu.commons.AsynExecutorService;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.events.utils.EventUtil;

public class EventScheduler {

    public final static AsynExecutorService asynExecutor = AsynExecutorService.getInstance();
    private static final ALogger            LOGGER       = Logger.of(AbstractVedantuManager.class);

    public static void generateEventAysc(final String userId, final IEventDetails details,
            final EventType eventType) {

        generateEventAysc(userId, details, eventType, 0);
    }

    @SuppressWarnings("unchecked")
    public static void generateEventAysc(final String userId, final IEventDetails details,
            final EventType eventType, final long processTime) {

        // created instance to avoid anonymous instance
        Callable<Boolean> callbackEventExecutor = new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {

                try {
                    synchronized ((userId + eventType.name()).intern()) {
                        LOGGER.info("inside synchronized block for synchronizedKey: "
                                + (userId + eventType.name()).intern());

                        EventUtil.generateEvent(eventType, null, userId, details,
                                details.__getSrcEntity(), EventActionType.ADD, processTime);
                    }
                } catch (Exception ex) {
                    LOGGER.debug("exception", ex);
                    throw ex;
                }
                LOGGER.debug("---------------- returning from executor call method for generateEventAysc ----------------");
                return new Boolean(true);
            }
        };

        asynExecutor.COMPLETION_SERVICE.submit(callbackEventExecutor);

    }

}
