package com.lms.managers;


import com.lms.common.AsynExecutorService;
import com.lms.common.utils.EventUtil;
import com.lms.common.vedantu.enums.EventType;
import com.lms.common.vedantu.enums.UserActionType;
import com.lms.common.vedantu.event.api.IEventDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.Callable;

public abstract class AbstractVedantuEventManager {

    public final static AsynExecutorService asynExecutor = AsynExecutorService.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(AbstractVedantuEventManager.class);
    @Autowired
    private EventUtil eventUtil;

    public void generateEventAysc(final String userId, final IEventDetails details,
                                  final EventType eventType) {

        generateEventAysc(userId, details, eventType, 0);
    }

    @SuppressWarnings("unchecked")
    public void generateEventAysc(final String userId, final IEventDetails details,
                                  final EventType eventType, final long processTime) {

        // created instance to avoid anonymous instance
        Callable<Boolean> callbackEventExecutor = new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {

                try {
                    synchronized ((userId + eventType.name()).intern()) {
                        logger.info("inside synchronized block for synchronizedKey: "
                                + (userId + eventType.name()).intern());

                        eventUtil.generateEvent(eventType, null, userId, details,
                                details.__getSrcEntity(), UserActionType.EventActionType.ADD, processTime);
                    }
                } catch (Exception ex) {
                    logger.debug("exception", ex);
                    throw ex;
                }
                logger.debug("---------------- returning from executor call method for generateEventAysc ----------------");
                return true;
            }
        };

        asynExecutor.COMPLETION_SERVICE.submit(callbackEventExecutor);

    }
}
