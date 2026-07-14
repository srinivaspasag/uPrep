package com.lms.billing.repository;


import com.lms.billing.model.CouponCode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponCodeRepo extends MongoRepository<CouponCode, String> {
    CouponCode findByCode(String couponCode);
}
