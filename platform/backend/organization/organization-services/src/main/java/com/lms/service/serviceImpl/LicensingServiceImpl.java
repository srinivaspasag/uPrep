package com.lms.service.serviceImpl;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.PlanState;
import com.lms.models.LicensingPlan;
import com.lms.models.Organization;
import com.lms.pojo.LicensingInfo;
import com.lms.pojo.request.AddLicensingPlanReq;
import com.lms.pojo.request.GetLicensingPlansReq;
import com.lms.pojo.request.MarkStateReq;
import com.lms.pojo.request.campaigns.DeleteLicensingPlanReq;
import com.lms.pojo.responce.ActionTakenRes;
import com.lms.pojo.responce.AvailablePlansRes;
import com.lms.pojo.responce.SupportedFeaturesRes;
import com.lms.repository.LicensingPlanRepo;
import com.lms.repository.OrganizationRepo;
import com.lms.service.LicensingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class LicensingServiceImpl implements LicensingService {
    private static final Logger logger = LoggerFactory.getLogger(LicensingServiceImpl.class);

    @Value("${features.set}")
    public List allFeatures;
    @Autowired
    public LicensingPlanRepo licensingPlanRepo;
    @Autowired
    private OrganizationRepo organizationRepo;
    @Autowired
    private OrganizationsImpl organizationsimpl;
    public  final int      NO_START       = 0;
    public  final int      NO_LIMIT       = 0;
    @Override
    public VedantuResponse getSupportedFeatures() {
        SupportedFeaturesRes response = null;
        response = getAllSupportedFeatures();

        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse getPlans(GetLicensingPlansReq request) {
        AvailablePlansRes response = getPlans(request.planIds, request.state);
        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse create(AddLicensingPlanReq request) {
        if (request==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        AvailablePlansRes response = createPlan(request);
        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse delete(DeleteLicensingPlanReq request) {
        if (request == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        ActionTakenRes response = new ActionTakenRes();
            AtomicLong totalHits = new AtomicLong();
            getByPlanId(request.id, NO_START,
                    NO_LIMIT, totalHits);
            if (totalHits.longValue() != 0) {
                throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_REMOVED);
            }
            response = deletePlan(request.id);

            return new VedantuResponse(response);


    }

    @Override
    public VedantuResponse mark(MarkStateReq request) {
        if (request == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        ActionTakenRes response = mark(request.planId, request.state);

        return new VedantuResponse(response);
    }

    public List<Organization> getByPlanId(String planId, int start, int size, AtomicLong totalHits)
            throws VedantuException {
        List<Organization> organizations = organizationRepo.findAllBySubscriptionPlanId(planId);

        totalHits.set(organizations.size());
        return organizations;

    }

    private ActionTakenRes deletePlan(String id) {
        licensingPlanRepo.deleteById(id);
        ActionTakenRes response = new ActionTakenRes();
        response.done = true;
        return response;
    }

    private AvailablePlansRes createPlan(AddLicensingPlanReq request) {
        LicensingPlan plan = addPlan(request.name, request.desc,
                request.peruser, request.cost, request.additionalCost, request.users,
                request.features, request.rank);

        AvailablePlansRes response = new AvailablePlansRes();
        response.list.add((LicensingInfo) plan.toBasicInfo());

        return response;
    }

    private LicensingPlan addPlan(String name, String desc, boolean isPerUser, float cost, float additionalCost, long users, List<String> features, int rank)
            throws VedantuException {

            try {
                LicensingPlan plan = new LicensingPlan(name, desc, isPerUser, cost, additionalCost,
                        users, features);
                plan.rank = rank;
                licensingPlanRepo.save(plan);
                return plan;
            } catch (Exception exception) {
                throw new VedantuException(VedantuErrorCode.LICENSING_PLAN_ALREADY_EXISTS);
            }
    }

    private AvailablePlansRes getPlans(List<String> planIds, PlanState state) {
        
        List<LicensingPlan> plans=null;
        if(planIds==null){
            plans=licensingPlanRepo.findAllByRecordState(VedantuRecordState.ACTIVE);
        }else {
            plans = licensingPlanRepo.findAllByIdInAndRecordState(planIds, VedantuRecordState.ACTIVE);
        }
        AvailablePlansRes response = new AvailablePlansRes();

        if (!CollectionUtils.isEmpty(plans)) {
            for (LicensingPlan plan : plans) {
                response.list.add((LicensingInfo) plan.toBasicInfo());

            }
            response.totalHits = plans.size();
        }

        return response;
    }

    private SupportedFeaturesRes getAllSupportedFeatures() {
        SupportedFeaturesRes response = new SupportedFeaturesRes();
        response.features.addAll(allFeatures);
        logger.debug("Features " +allFeatures );
        return response;
    }
    public  ActionTakenRes mark(String planId, PlanState state) throws VedantuException {

        if (state == PlanState.INVALID) {
            throw new VedantuException(VedantuErrorCode.INVALID_LICENSING_STATE);
        }
        ActionTakenRes response = new ActionTakenRes();
        AtomicLong totalHits = new AtomicLong();
        getByPlanId(planId, NO_START, NO_LIMIT, totalHits);

        Optional<LicensingPlan> plan1 =licensingPlanRepo.findById(planId);
        if(!plan1.isPresent())
            throw new VedantuException(VedantuErrorCode.INVALID_CODE,"LicensingPlan not found");
        LicensingPlan plan=plan1.get();
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
        licensingPlanRepo.save(plan);
        response.done = true;
        return response;
    }

}
