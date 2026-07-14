package com.vedantu.organization.managers;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.mutable.MutableLong;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.pojos.responses.ActionTakenRes;
import com.vedantu.mongo.MongoManager;
import com.vedantu.organization.daos.LicensingPlanDAO;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.organization.enums.PlanState;
import com.vedantu.organization.models.licensing.LicensingPlan;
import com.vedantu.organization.pojos.LicensingInfo;
import com.vedantu.organization.pojos.requests.licensing.AddLicensingPlanReq;
import com.vedantu.organization.pojos.requests.licensing.UpdateLicensingPlanReq;
import com.vedantu.organization.pojos.responses.licensing.AvailablePlansRes;
import com.vedantu.organization.pojos.responses.licensing.SupportedFeaturesRes;
import com.vedantu.user.managers.AbstractVedantuEventManager;

public class LicensingManager extends AbstractVedantuEventManager {

    private static List<String>  allFeatures = Play.application().configuration()
                                                     .getStringList("features.set");
    private static final ALogger LOGGER      = Logger.of(LicensingManager.class);

    public static SupportedFeaturesRes getAllSupportedFeatures() {

        SupportedFeaturesRes response = new SupportedFeaturesRes();
        response.features.addAll(allFeatures);
        LOGGER.debug("Features " + allFeatures);
        return response;
    }

    public static AvailablePlansRes createPlan(AddLicensingPlanReq request) throws VedantuException {

        LicensingPlan plan = LicensingPlanDAO.INSTANCE.addPlan(request.name, request.desc,
                request.peruser, request.cost, request.additionalCost, request.users,
                request.features, request.rank);

        AvailablePlansRes response = new AvailablePlansRes();
        response.list.add((LicensingInfo) plan.toBasicInfo());

        return response;
    }

    public static AvailablePlansRes update(UpdateLicensingPlanReq request) throws VedantuException {

        LicensingPlan plan = LicensingPlanDAO.INSTANCE.getById(request.planId);
        if (plan.state != PlanState.DRAFT) {
            throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_DELIVERED);
        }
        plan.name = request.name;
        plan.desc = request.desc;
        plan.peruser = request.peruser;
        plan.cost = request.cost;
        plan.users = request.users;
        plan.features = request.features;
        plan.additionalCost = request.additionalCost;
        plan.rank = request.rank;
        LicensingPlanDAO.INSTANCE.save(plan);

        AvailablePlansRes response = new AvailablePlansRes();
        response.list.add((LicensingInfo) plan.toBasicInfo());

        return response;
    }

    /**
     * we wont need offsetd
     * 
     * @param ids
     * @return
     * @throws VedantuException
     */
    public static AvailablePlansRes getPlans(List<String> ids, PlanState state)
            throws VedantuException {

        List<LicensingPlan> plans = LicensingPlanDAO.INSTANCE.getAllPlans(ids, state);

        AvailablePlansRes response = new AvailablePlansRes();

        if (CollectionUtils.isNotEmpty(plans)) {
            for (LicensingPlan plan : plans) {
                response.list.add((LicensingInfo) plan.toBasicInfo());

            }
            response.totalHits = plans.size();
        }

        return response;
    }

    public static ActionTakenRes delelePlan(String id) throws VedantuException {

        // TODO do not delete if plan is being used.
        // enhance it if organization services
        LicensingPlanDAO.INSTANCE.deletePlan(id);
        ActionTakenRes response = new ActionTakenRes();
        response.done = true;
        return response;
    }

    public static ActionTakenRes mark(String planId, PlanState state) throws VedantuException {

        if (state == PlanState.INVALID) {
            throw new VedantuException(VedantuErrorCode.INVALID_LICENSING_STATE);
        }
        ActionTakenRes response = new ActionTakenRes();
        MutableLong totalHits = new MutableLong();
        OrganizationDAO.INSTANCE.getByPlanId(planId, MongoManager.NO_START, MongoManager.NO_LIMIT,
                totalHits);

        LicensingPlan plan = LicensingPlanDAO.INSTANCE.getById(planId);

        switch (plan.state) {
        case ACTIVE: {
            if (state == PlanState.ACTIVE || state == PlanState.DRAFT) {
                throw new VedantuException(VedantuErrorCode.LICENSING_PLAN_ALREADY_ACTIVE);
            }

        }
            break;
        case OBSOLETE: {

            if (state == PlanState.DRAFT) {
                throw new VedantuException(VedantuErrorCode.LICENSE_INVALIDATED);

            }

        }
            break;
        default:
            break;

        }
        plan.state = state;
        LicensingPlanDAO.INSTANCE.updateModel(plan, Arrays.asList(LicensingPlan.STATE));
        response.done = true;
        return response;
    }
}
