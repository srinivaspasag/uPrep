package com.lms.repository;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.UserActionType;
import com.lms.models.EntityUserActionMapping;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface EntityUserActionMappingRepo extends MongoRepository<EntityUserActionMapping,String>
{

    List<EntityUserActionMapping> findByUserIdAndTargetIn(String userId, Set<String> ids);

    List<EntityUserActionMapping> findAllByUserIdAndActionTypeAndTargetIn(String userId, String name, Set<String> ids);

    List<EntityUserActionMapping> findAllByUserIdAndTargetIn(String userId, String name, Set<String> ids);

    List<EntityUserActionMapping> findAllByUserIdAndActionTypeAndTargetIdIn(String userId, String name, Set<String> ids);

    List<EntityUserActionMapping> findAllByUserIdInAndTargetId(Set<String> ids, String name, String userId);

    EntityUserActionMapping findByUserIdAndActionTypeAndTarget(String userId, UserActionType actionType, SrcEntity target);

    EntityUserActionMapping findByUserIdAndActionTypeAndTargetIdAndTargetType(String userId, UserActionType actionType, String id, EntityType type);

    List<EntityUserActionMapping> findByTargetIdAndTargetType(String entityId, EntityType entityType);
}
