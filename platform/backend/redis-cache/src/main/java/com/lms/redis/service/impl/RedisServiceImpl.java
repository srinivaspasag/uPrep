package com.lms.redis.service.impl;

import com.lms.redis.dto.TaskDTO;
import com.lms.redis.service.RedisService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class RedisServiceImpl implements RedisService {

    @Cacheable(value = "getLongRunningTaskResult", key = "{#seconds}", cacheManager = "cacheManager1Hour")
    public TaskDTO getLongRunningTaskResult(long seconds) {
        try {
            long randomMultiplier = new Random().nextLong();
            long calculatedResult = randomMultiplier * seconds;
            TaskDTO taskDTO = new TaskDTO();
            taskDTO.setCalculatedResult(calculatedResult);
            Thread.sleep(2000); // 2 Second Delay to Simulate Workload
            return taskDTO;
        } catch (InterruptedException e) {
            return null;
        }
    }
}
