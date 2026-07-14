package com.lms.services;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.*;

public interface ModuleService {


    VedantuResponse updateUserModuleStatus(UpdateUserModuleReq updateUserModuleReq);

    VedantuResponse getUserModuleStatus(GetUserModuleReq GetUserModuleReq);

    VedantuResponse syncModule(SyncModuleReq syncModuleReq);

    VedantuResponse getModule(GetModuleReq getModuleReq);

    VedantuResponse getModules(GetModulesReq getModulesReq);


}
