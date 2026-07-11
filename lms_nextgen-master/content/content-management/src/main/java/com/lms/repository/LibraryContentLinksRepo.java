package com.lms.repository;

import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.UserActionType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.models.LibraryContentLink;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LibraryContentLinksRepo extends MongoRepository<LibraryContentLink, String> {

    List<LibraryContentLink> findBySourceIdInAndTargetIdAndRecordState(Object[] toArray, String secId, String name);

    List<LibraryContentLink> findByLinkTypeAndRecordStateAndSourceIdAndTargetType(UserActionType added, VedantuRecordState active, String id, EntityType section);
}
