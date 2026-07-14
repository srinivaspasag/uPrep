package com.lms.service.serviceImpl;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.utils.ObjectIdUtils;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.models.OrgDepartment;
import com.lms.pojo.request.*;
import com.lms.pojo.responce.*;
import com.lms.repository.OrgDepartmentRepo;
import com.lms.service.DepartmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DepartmentServiceImpl implements DepartmentService {
    private static final Logger logger = LoggerFactory.getLogger(DepartmentServiceImpl.class);
    @Autowired
    private OrgDepartmentRepo orgDepartmentRepo;
    @Override
    public VedantuResponse getDepartments(GetOrgDepartmentsReq getOrgDepartmentsReq) {

        if (getOrgDepartmentsReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        if (ObjectIdUtils.hasInvalidId(getOrgDepartmentsReq.orgId)) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }
        GetOrgDepartmentsRes getOrgDepartmentsRes = getOrgDepartments(getOrgDepartmentsReq);

        return new VedantuResponse(getOrgDepartmentsRes);
    }

    @Override
    public VedantuResponse addDepartment(AddOrgDepartmentReq addOrgDepartmentReq) {
        if (addOrgDepartmentReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        if (ObjectIdUtils.hasInvalidId(addOrgDepartmentReq.getOrgId().trim())) {
           throw new VedantuException(VedantuErrorCode.INVALID_ID);}
        AddOrgDepartmentRes addOrgDepartmentRes = addOrgDepartment(addOrgDepartmentReq);


        return new VedantuResponse(addOrgDepartmentRes);
    }

    @Override
    public VedantuResponse updateDepartment(UpdateOrgDepartmentReq updateOrgDepartmentReq) {
        if (updateOrgDepartmentReq==null) {
           new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        if (ObjectIdUtils.hasInvalidId(updateOrgDepartmentReq.getOrgId().trim(),
                updateOrgDepartmentReq.getDepartmentId().trim())) {
           throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }
        UpdateOrgDepartmentRes updateOrgDepartmentRes =updateOrgDepartment(updateOrgDepartmentReq);


        return new VedantuResponse(updateOrgDepartmentRes);
    }

    @Override
    public VedantuResponse removeDepartment(RemoveOrgDepartmentReq removeOrgDepartmentReq) {

        if (removeOrgDepartmentReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        if (ObjectIdUtils.hasInvalidId(removeOrgDepartmentReq.getOrgId().trim(),
                removeOrgDepartmentReq.getDepartmentId().trim())) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID);

        }
        RemoveOrgDepartmentRes removeOrgDepartmentRes = removeOrgDepartment(removeOrgDepartmentReq);


        return new VedantuResponse(removeOrgDepartmentRes);
    }

    @Override
    public VedantuResponse activateDepartment(ActivateOrgDepartmentReq activateOrgDepartmentReq) {
        if (activateOrgDepartmentReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
       
        if (ObjectIdUtils.hasInvalidId(activateOrgDepartmentReq.getOrgId().trim(),
                activateOrgDepartmentReq.getDepartmentId().trim())) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID);
                   
        }
        ActivateOrgDepartmentRes activateOrgDepartmentRes = activateOrgDepartment(activateOrgDepartmentReq);
        
        return new VedantuResponse(activateOrgDepartmentRes);
    }

    private ActivateOrgDepartmentRes activateOrgDepartment(ActivateOrgDepartmentReq activateOrgDepartmentReq) {



            OrgDepartment orgDepartment = activateOrganizationDepartment(activateOrgDepartmentReq.getOrgId(),
                            activateOrgDepartmentReq.departmentId);

            // TODO: Activate from all corresponding programs ??

            ActivateOrgDepartmentRes activateOrgDepartmentRes = new ActivateOrgDepartmentRes();
            activateOrgDepartmentRes.id = orgDepartment._getStringId();
            activateOrgDepartmentRes.recordState = orgDepartment.recordState;

            return activateOrgDepartmentRes;


    }

    private OrgDepartment activateOrganizationDepartment(String orgId, String departmentId) {

        OrgDepartment orgDepartment = getDepartmentById(orgId, departmentId);
        orgDepartment.setRecordState(VedantuRecordState.ACTIVE);
            orgDepartmentRepo.save(orgDepartment);
            return orgDepartment;

    }

    private RemoveOrgDepartmentRes removeOrgDepartment(RemoveOrgDepartmentReq removeOrgDepartmentReq) {
       
        OrgDepartment orgDepartment = removeOrganizationDepartment(removeOrgDepartmentReq.getOrgId(),
                            removeOrgDepartmentReq.getDepartmentId());

            // TODO: Remove from all corresponding programs

            RemoveOrgDepartmentRes removeOrgDepartmentRes = new RemoveOrgDepartmentRes();
            removeOrgDepartmentRes.id = orgDepartment._getStringId();
            removeOrgDepartmentRes.recordState = orgDepartment.recordState;

            return removeOrgDepartmentRes;
    }

    private OrgDepartment removeOrganizationDepartment(String orgId, String departmentId) {
        OrgDepartment orgDepartment = getDepartmentById(orgId, departmentId);
orgDepartment.setRecordState(VedantuRecordState.DELETED);

        orgDepartmentRepo.save(orgDepartment);

        return orgDepartment;
    }

    private UpdateOrgDepartmentRes updateOrgDepartment(UpdateOrgDepartmentReq updateOrgDepartmentReq) {

        OrgDepartment orgDepartment = getDepartmentById(updateOrgDepartmentReq.getOrgId(), updateOrgDepartmentReq.getDepartmentId());

            orgDepartment.setCode(updateOrgDepartmentReq.getCode());
            orgDepartment.setName(updateOrgDepartmentReq.getName());
            orgDepartmentRepo.save(orgDepartment);

            UpdateOrgDepartmentRes updateOrgDepartmentRes = new UpdateOrgDepartmentRes();
             updateOrgDepartmentRes.setId(orgDepartment._getStringId());
        updateOrgDepartmentRes.setRecordState(orgDepartment.getRecordState());

        return updateOrgDepartmentRes;
    }

    private OrgDepartment getDepartmentById(String orgId, String departmentId) {
        Optional<OrgDepartment> orgDepartment = orgDepartmentRepo.findById(departmentId);
        if (!orgDepartment.isPresent()) {
            logger.error("cannot find orgDepartment for _id: " + departmentId);
            throw new VedantuException(
                    VedantuErrorCode.ORGANIZATION_DEPARTMENT_NOT_FOUND);
        }
        if (!orgDepartment.get().getOrgId().equals(orgId)) {
            logger.error("mismatch in orgId for department _id: "
                    + departmentId + ", expected orgId: " + orgDepartment.get().getOrgId()
                    + ", found orgId: " + orgId);
            throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }
        return orgDepartment.get();
    }

    private AddOrgDepartmentRes addOrgDepartment(AddOrgDepartmentReq addOrgDepartmentReq) {

        OrgDepartment orgDepartment = orgDepartmentRepo.findByOrgIdAndCode(addOrgDepartmentReq.getOrgId(),addOrgDepartmentReq.getCode());

        if ( orgDepartment!=null) {
                if (VedantuRecordState.ACTIVE == orgDepartment.recordState) {
                    logger.error("cannot add orgDepartment as orgDepartment already exists for orgId: "
                            + addOrgDepartmentReq.getOrgId() + ", code: " + addOrgDepartmentReq.getCode());
                    throw new VedantuException(
                            VedantuErrorCode.ORGANIZATION_DEPARTMENT_ALREADY_EXISTS);
                } else {
                    logger.error("changing orgDepartment recordState for orgId: "
                            + addOrgDepartmentReq.getOrgId()
                            + ", code: "
                            + addOrgDepartmentReq.getCode()
                            + ", _id: "
                            + orgDepartment._getStringId()
                            + ", from: "
                            + orgDepartment.recordState
                            + ", to: "
                            + VedantuRecordState.ACTIVE);
                    orgDepartment.setName(addOrgDepartmentReq.getName());
                    orgDepartment.setRecordState(VedantuRecordState.ACTIVE);
                    orgDepartmentRepo.save(orgDepartment);
                }
            }

            orgDepartment = new OrgDepartment(addOrgDepartmentReq.getOrgId(), addOrgDepartmentReq.getCode(), addOrgDepartmentReq.getName());
        orgDepartmentRepo.save(orgDepartment);
        AddOrgDepartmentRes addOrgDepartmentRes = new AddOrgDepartmentRes();
        addOrgDepartmentRes.setId(orgDepartment._getStringId()) ;
        addOrgDepartmentRes.setRecordState(orgDepartment.getRecordState());

        return addOrgDepartmentRes;
    }

    private GetOrgDepartmentsRes getOrgDepartments(GetOrgDepartmentsReq getOrgDepartmentsReq) {


        AtomicLong totalHits = new AtomicLong(0L);
        List<OrgDepartment> departments = getOrganizationDepartments(getOrgDepartmentsReq.orgId, totalHits);
        GetOrgDepartmentsRes getOrgDepartmentsRes = new GetOrgDepartmentsRes();
        if (!departments.isEmpty()) {
            getOrgDepartmentsRes.totalHits = totalHits.get();
            for (OrgDepartment department : departments) {
                if (department==null) {
                    continue;
                }
                OrgDepartmentInfo departmentInfo = new OrgDepartmentInfo(
                        department._getStringId(), department.getName(),
                        department.code, department.recordState);
                getOrgDepartmentsRes.list.add(departmentInfo);
            }
        }
        return getOrgDepartmentsRes;
    }

    private List<OrgDepartment> getOrganizationDepartments(String orgId, AtomicLong totalHits) {
        List<OrgDepartment> orgDepartment=orgDepartmentRepo.findAllByOrgId(orgId);

        totalHits.set(orgDepartment.size());
        return orgDepartment;
    }
}
