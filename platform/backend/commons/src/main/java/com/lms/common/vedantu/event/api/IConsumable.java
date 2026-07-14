package com.lms.common.vedantu.event.api;

import java.util.Set;

public interface IConsumable {
    String _getConsumableId();

    Set<String> _getProcessedBy();

    void addProcessedBy(String processor);
}
