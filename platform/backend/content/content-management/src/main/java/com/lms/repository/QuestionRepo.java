package com.lms.repository;

import com.lms.common.vedantu.enums.EntityType;
import com.lms.models.Question;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepo extends MongoRepository<Question, String> {
    List<Question> findAllById(List<ObjectId> ids);

    Question findByContentAndContentTypeIn(String id, List<String> asList);

    List<Question> findByIdInAndContentType(Object[] toArray, EntityType type);
}
