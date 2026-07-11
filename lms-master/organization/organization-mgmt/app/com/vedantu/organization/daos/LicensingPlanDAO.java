package com.vedantu.organization.daos;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.mongodb.MongoException.DuplicateKey;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.enums.PlanState;
import com.vedantu.organization.models.licensing.LicensingPlan;

public class LicensingPlanDAO extends VedantuBasicDAO<LicensingPlan, ObjectId> {

    private static final ALogger         LOGGER   = Logger.of(LicensingPlanDAO.class);

    public static final LicensingPlanDAO INSTANCE = new LicensingPlanDAO();

    private LicensingPlanDAO() {

        super(LicensingPlan.class);
    }

    public List<LicensingPlan> getAllPlans(List<String> ids, PlanState state) {

        Query<LicensingPlan> findQuery = getQuery();

        if (CollectionUtils.isNotEmpty(ids)) {

            findQuery.field(FIELD_ID).in(ObjectIdUtils.toObjectIds(ids));

        }
        if (state != null) {
            findQuery.field("state").equal(state.name());
        }
        findQuery.field("recordState").equal(VedantuRecordState.ACTIVE);

        return findQuery.order("-rank").asList();
    }

    public LicensingPlan addPlan(String name, String desc, boolean isPerUser, float cost,
            float additionalCost, long users, List<String> features, int rank)
            throws VedantuException {

        try {
            LicensingPlan plan = new LicensingPlan(name, desc, isPerUser, cost, additionalCost,
                    users, features);
            plan.rank = rank;
            LicensingPlanDAO.INSTANCE.save(plan);
            return plan;
        } catch (DuplicateKey exception) {
            throw new VedantuException(VedantuErrorCode.LICENSING_PLAN_ALREADY_EXISTS);
        }
    }

    public void deletePlan(String id) throws VedantuException {

        try {

            LicensingPlanDAO.INSTANCE.deleteById(new ObjectId(id));
        } catch (DuplicateKey exception) {
            throw new VedantuException(VedantuErrorCode.LICENSING_PLAN_ALREADY_EXISTS);
        }
    }

}
