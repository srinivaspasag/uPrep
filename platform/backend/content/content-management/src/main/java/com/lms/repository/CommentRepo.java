package com.lms.repository;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.models.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CommentRepo extends MongoRepository<Comment, String> {

    List<Comment> findAllByParent(SrcEntity parent);

    VedantuBaseMongoModel findByIdAndRecordState(String entityId, VedantuRecordState active);

    List<VedantuBaseMongoModel> findByParentIdAndRecordState(String entityId, VedantuRecordState active);
}
