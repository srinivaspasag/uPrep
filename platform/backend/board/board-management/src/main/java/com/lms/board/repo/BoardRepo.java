package com.lms.board.repo;

import com.lms.board.model.Board;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface BoardRepo extends MongoRepository<Board, String> {
    List<Board> findByIdIn(List<ObjectId> toObjectIds);

    List<Board> findAllByIdIn(Set<String> ids);
}
