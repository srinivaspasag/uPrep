package com.lms.repository;

import com.lms.user.vedantu.user.model.LoginStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface LoginStatusRepo extends MongoRepository<LoginStatus, String>
{
        List<LoginStatus> findAllByUserIdAndDeviceIdAndDeviceTypeAndStatus(List<String> userids,String deviceid,String devicetype,String status);
}
