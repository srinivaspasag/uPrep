package com.lms.service;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojo.request.*;

public interface DepartmentService {
    VedantuResponse getDepartments(GetOrgDepartmentsReq getOrgDepartmentsReq);

    VedantuResponse addDepartment(AddOrgDepartmentReq addOrgDepartmentReq);

    VedantuResponse updateDepartment(UpdateOrgDepartmentReq updateOrgDepartmentReq);

    VedantuResponse removeDepartment(RemoveOrgDepartmentReq removeOrgDepartmentReq);

    VedantuResponse activateDepartment(ActivateOrgDepartmentReq activateOrgDepartmentReq);


}
