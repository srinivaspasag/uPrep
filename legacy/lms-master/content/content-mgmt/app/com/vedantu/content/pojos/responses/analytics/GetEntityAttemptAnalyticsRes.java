package com.vedantu.content.pojos.responses.analytics;

import java.util.List;

public class GetEntityAttemptAnalyticsRes {
    public long totalhits;
    public List<GetEntityAttemptsStudentsListRes> list;
    public long testProgressCount;
    public long testCompletedCount;
    public long testPausedCount;
    public long testResumedCount;
}
