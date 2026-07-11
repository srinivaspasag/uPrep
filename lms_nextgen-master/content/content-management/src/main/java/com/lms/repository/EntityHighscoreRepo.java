package com.lms.repository;

import com.lms.models.EntityHighscore;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntityHighscoreRepo extends MongoRepository<EntityHighscore, String> {
}
