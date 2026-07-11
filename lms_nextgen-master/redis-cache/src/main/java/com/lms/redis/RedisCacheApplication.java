package com.lms.redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Properties;

@SpringBootApplication
public class RedisCacheApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RedisCacheApplication.class);
        Properties properties = new Properties();
        properties.setProperty("spring.cache.type", "none");
        application.setDefaultProperties(properties);
        application.run(args);
//        SpringApplication.run(RedisCacheApplication.class, args);
    }

}
