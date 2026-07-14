package com.lms.services;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.*;
import com.lms.pojos.requests.splModules.GetModuleInfoReq;

public interface CMDSModulesService {
    VedantuResponse createmodule(CreateModuleReq createModuleReq);

    VedantuResponse addmoduleEntries(AddModuleEntryReq addModuleEntryReq);

    VedantuResponse updatemodule(UpdateModuleReq addModuleEntryReq);

    VedantuResponse deleteModule(DeleteModuleReq deleteModuleReq);

    VedantuResponse moveModuleEntry(MoveModuleEntryReq moveModuleEntryReq);

    VedantuResponse deleteModuleEntry(DeleteModuleEntryReq deleteModuleEntryReq);

    VedantuResponse getCMDSModuleInfo(GetModuleInfoReq getModuleInfoReq);

    VedantuResponse updatemoduleEntries(UpdateModuleEntryReq updateModuleEntryReq);

    VedantuResponse getCMDSModules(GetCMDSModulesReq getCMDSModulesReq);
}
