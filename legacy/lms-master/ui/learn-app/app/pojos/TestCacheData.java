/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pojos;

import java.io.Serializable;

/**
 *
 * @author anirban
 */
public class TestCacheData implements Serializable{
    
    private long startTime;
    private long duration;
    private String attemptId;
    private String testId;
    private long extraTime;
    private String userId;
    private String sessionId;
    private int reattemptCount;
    private String testName;

    public TestCacheData(long startTime, long duration, String attemptId, 
            String testId, String testName, long extraTime, String userId, String sessionId, 
            int reattemptCount) {
        this.startTime = startTime;
        this.duration = duration;
        this.attemptId = attemptId;
        this.testId = testId;
        this.testName = testName;
        this.extraTime = extraTime;
        this.userId = userId;
        this.sessionId = sessionId;
        this.reattemptCount = reattemptCount;
    }

    public String getTestName() {
        return testName;
    }

    public int getReattemptCount() {
        return reattemptCount;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUserId() {
        return userId;
    }
    
    @Override
    public String toString(){
        return "Start Time = "+startTime+",Duration = "+duration+",test Id = "+testId+", attemptId = "+attemptId+", extra time = "+extraTime;
    }
    public String getAttemptId() {
        return attemptId;
    }

    public long getDuration() {
        return duration;
    }

    public long getExtraTime() {
        return extraTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getTestId() {
        return testId;
    }
    
}
