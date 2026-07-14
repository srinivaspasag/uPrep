package com.lms.repository;

import com.lms.models.Answer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;


@Repository
public interface AnswerRepo extends MongoRepository<Answer, String> {

    Answer findByqId(String id);

    List<Answer> findByqIdIn(Collection<String> qIds);


}
