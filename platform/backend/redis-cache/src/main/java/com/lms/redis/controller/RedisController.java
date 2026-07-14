package com.lms.redis.controller;

import com.lms.redis.dto.TaskDTO;
import com.lms.redis.service.RedisService;
import com.lms.redis.util.ResponseUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class RedisController {

    @Autowired
    RedisService redisService;

    @ApiOperation(value = "Perform the long running task")
    @RequestMapping(value = "/{seconds}", method = RequestMethod.GET)
    public ResponseEntity<TaskDTO> longRunningTask(@PathVariable long seconds) {
        TaskDTO user = redisService.getLongRunningTaskResult(seconds);
        return ResponseEntity.ok(user);
    }
}
