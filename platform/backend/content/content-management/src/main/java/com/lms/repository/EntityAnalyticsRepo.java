package com.lms.repository;

import com.lms.common.vedantu.enums.EntityType;
import com.lms.enums.AcademicDimensionType;
import com.lms.models.analytics.EntityAnalytics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntityAnalyticsRepo extends MongoRepository<EntityAnalytics, String> {

    @Query(value = "{'entity.type': ?0, 'entity.id' : ?1 ,'acadDim.type':?2,'acadDim.id':?3}")
    EntityAnalytics findByEntityTypeAndEntityIdAndAcadDimTypeAndAcadDimId(EntityType type, String id, AcademicDimensionType acadDimType, String acadDimId);

    EntityAnalytics findByEntityId(Object s);

    @Query("{'entity.id' : ?0}")
    List<EntityAnalytics> findByEntityId(String s);
}
