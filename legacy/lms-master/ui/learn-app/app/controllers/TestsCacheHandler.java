package controllers;

import java.util.Date;

import play.Logger;
import play.cache.Cache;
import play.data.validation.Required;
import pojos.TestCacheData;
import util.ClientUtil;

public class TestsCacheHandler  extends AbstractUIController {
    private final static int MAX_ATTEMPT_COUNT = 5;
    private final static String PRE_STR_TEST_CACHE_KEY = "TAKE_TEST/USER/";

    public static long _getTimeLeft(TestCacheData data){
        long timeLeft = 0;
        if(data.getStartTime()!=0 && data.getDuration()>0){
            long curTime = new Date().getTime();
            timeLeft = (data.getStartTime()+data.getExtraTime()+data.getDuration()) - curTime;
            timeLeft = timeLeft>data.getDuration()?data.getDuration():timeLeft;
        }
        return timeLeft;
    }
    public static String _getTimeLeftInSec(TestCacheData data){
        long sec = _getTimeLeft(data);
        sec = sec<1000?0:sec/1000;
        String expireTime = sec+"s";
        Logger.log4j.error("CURRENT EXPIREY TIME ============== === "+expireTime);
        return expireTime;
    }
    private static String _getKey(@Required String keyId){
        return PRE_STR_TEST_CACHE_KEY+keyId;
    }
    public static String _verifyTestTime(@Required String userId, @Required String testId,
            @Required String attemptId,String sessionId){
        String key = _getKey(userId);
        TestCacheData data = Cache.get(key,TestCacheData.class);
        if(data!=null){
            long timeLeft = _getTimeLeft(data);
            Logger.log4j.info("TEST TIME VERIFICATION ==================== "+timeLeft+", for Test Attempt Id == "+attemptId);
            String resp;
            if(!testId.equals(data.getTestId())){
                resp = "TEST_SUBMIT_ANSWER_ERR_TESTID";
            }
            else if(attemptId!=null && !attemptId.equals(data.getAttemptId())){
                resp = "TEST_SUBMIT_ANSWER_ERR_ATTEMPTID";
            }
            else if(!sessionId.equals(data.getSessionId())){
                resp = "TEST_SUBMIT_ANSWER_ERR_SESSION";
            }else if(timeLeft<1 && data.getDuration()>0){
                resp = "TEST_TIME_OVER";
            }else{
                resp = "";
            }
            return resp;
        }else{
            Logger.log4j.error("TEST CACHE DATA NOT FOUND IN CACHE ============== for key === "+key);
            return "TEST_TIME_OVER";
        }
    }
    public static TestCacheData _getCurrentCache(@Required String userId){
        String key = _getKey(userId);
        return Cache.get(key,TestCacheData.class);
    }
    public static String _verifyBeforeTest(@Required String userId,@Required String testId,
            String sessionId){
        String key = _getKey(userId);
        TestCacheData data = Cache.get(key,TestCacheData.class);
        int reattemptCount=0;
        if(data!=null){
            if(testId!=null &&testId.equals(data.getTestId())){
                if(!sessionId.equals(data.getSessionId())){
                    reattemptCount = data.getReattemptCount()+1;
                    if(reattemptCount > MAX_ATTEMPT_COUNT){
                        return "TEST_MAX_ATTEMPT";
                    }
                }
            }else{
                return "TEST_INPROGRESS";
            }
        }else{
            return "";
        }
        return "";
    }
    public static String _setTestCache(@Required String userId,
            @Required long startTime,long duration,@Required String attemptId,
            @Required String testId,String testName,long extraTime,String sessionId){
        if(attemptId==null || attemptId.isEmpty()){
            return null;
        }
        int reattemptCount = 0;
        String key = _getKey(userId);
        TestCacheData data = Cache.get(key,TestCacheData.class);
        if(data!=null){
            if(testId!=null && attemptId!=null && testId.equals(data.getTestId())
                    && attemptId.equals(data.getAttemptId())){

                if(!sessionId.equals(data.getSessionId())){
                    reattemptCount = data.getReattemptCount()+1;
                    if(reattemptCount > MAX_ATTEMPT_COUNT){
                        return "TEST_MAX_ATTEMPT";
                    }
                }
                reattemptCount = data.getReattemptCount();
                startTime = data.getStartTime();
            }else{
                return "TEST_INPROGRESS";
            }
        }
        data = new TestCacheData(startTime, duration, attemptId, testId, testName,
                    extraTime, userId, sessionId, reattemptCount);
        String expireTime = ClientUtil.DEFAULT_TEST_CACHE_EXPIRY_TIME_HR;
        if(duration>0 || extraTime>0){
            expireTime = _getTimeLeftInSec(data);
        }
        boolean chSet = Cache.safeSet(key, data, expireTime);
        Logger.log4j.info("TEST Cache Set ======================== "+chSet+", for data ========= "+data+", expireTime ======== "+expireTime);
        return "";
    }

    public static boolean _clearCache(String attemptId){
        String key = _getKey(attemptId);
        Logger.log4j.info("Test Cache deleted >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> for key == "+key);
        return Cache.safeDelete(key);
    }
}
