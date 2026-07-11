package com.lms.repository;

import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.PlanState;
import com.lms.models.LicensingPlan;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LicensingPlanRepo extends MongoRepository<LicensingPlan, String> {

    // List<LicensingPlan> findAllByIdAndState(String id, PlanState active);

     List<LicensingPlan> findByIdAndState(String id, PlanState active);

    List<LicensingPlan> findAllByIdInAndStateAndRecordState(List<String> planIds, PlanState state, VedantuRecordState active);

    List<LicensingPlan> findAllByRecordState(VedantuRecordState active);

    List<LicensingPlan> findAllByIdInAndRecordState(List<String> planIds, VedantuRecordState active);

    List<LicensingPlan> findByIdInAndRecordState(List<String> planIds, VedantuRecordState active);
}
