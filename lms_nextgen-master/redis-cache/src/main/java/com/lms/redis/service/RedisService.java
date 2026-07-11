package com.lms.redis.service;

import com.lms.redis.dto.TaskDTO;

public interface RedisService {
    TaskDTO getLongRunningTaskResult(long seconds);
}
