package com.lms.pojos.responce.analytics;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetEntityAttemptAnalyticsRes {
    public long totalhits;
    public List<GetEntityAttemptsStudentsListRes> list;
    public long testProgressCount;
    public long testCompletedCount;
    public long testPausedCount;
    public long testResumedCount;
}
