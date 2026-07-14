package com.lms.service;

import com.lms.common.vedantu.dto.response.VedantuResponse;

public interface EventBusProcessorsService {

    VedantuResponse startAll();

    VedantuResponse stopAll();

    VedantuResponse restartAll();

    VedantuResponse start(String eventType);

    VedantuResponse stop(String eventType);

    VedantuResponse getStatus(String eventType);

    VedantuResponse getStatusAll();

    VedantuResponse stopEnqueeFailedEvents();

    VedantuResponse enqueeFailedEvents(int size);

}
