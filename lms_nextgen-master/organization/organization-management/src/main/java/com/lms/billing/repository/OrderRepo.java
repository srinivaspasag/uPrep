package com.lms.billing.repository;


import com.lms.billing.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepo extends MongoRepository<Order, String> {

    Order findByOrderId(long orderId);
}
