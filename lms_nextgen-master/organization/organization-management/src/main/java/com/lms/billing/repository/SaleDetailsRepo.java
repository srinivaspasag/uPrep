package com.lms.billing.repository;


import com.lms.billing.model.SaleDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleDetailsRepo extends MongoRepository<SaleDetails, String> {
}
