package com.lms.user.vedantu.user.repository;

import com.lms.user.vedantu.user.model.EmailBlacklist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailBlacklistRepo extends MongoRepository<EmailBlacklist ,String> {
    Optional<EmailBlacklist> findByEmail(String email);

//  Optional<EmailBlacklist> findAllByEmail(String query);

    Page<EmailBlacklist> findAllSortBy(Pageable pageWithElements, String sortOrder);
}
