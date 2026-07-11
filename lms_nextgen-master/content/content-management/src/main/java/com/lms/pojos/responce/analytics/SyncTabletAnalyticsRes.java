package com.lms.pojos.responce.analytics;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SyncTabletAnalyticsRes extends EndAttemptRes {

    public boolean processed;
    public boolean overrideAnalytics;
}
