package com.lms.services.serviceImpl;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.component.CMDSModuleManager;
import com.lms.pojos.requests.*;
import com.lms.pojos.requests.splModules.GetModuleInfoReq;
import com.lms.pojos.responce.*;
import com.lms.services.CMDSModulesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CMDSModulesServiceImpl implements CMDSModulesService {
    @Autowired
    private CMDSModuleManager cmdsModuleManager;

    @Override
    public VedantuResponse createmodule(CreateModuleReq createModuleReq) {
        CreateModuleRes createModuleResponse = cmdsModuleManager.createModule(createModuleReq);
        return new VedantuResponse(createModuleResponse);
    }

    @Override
    public VedantuResponse addmoduleEntries(AddModuleEntryReq addModuleEntryReq) {
        AddModuleRes addModuleResponse = cmdsModuleManager.addModuleEntries(addModuleEntryReq);
        return new VedantuResponse(addModuleResponse);

    }

    @Override
    public VedantuResponse updatemodule(UpdateModuleReq updateModuleReq) {
        UpdateModuleRes updateModuleResponse = cmdsModuleManager.updateModule(updateModuleReq);
        return new VedantuResponse(updateModuleResponse);
    }

    @Override
    public VedantuResponse deleteModule(DeleteModuleReq deleteModuleReq) {
        DeleteModuleRes deleteModuleResponse = cmdsModuleManager.deleteModule(deleteModuleReq);
        return new VedantuResponse(deleteModuleResponse);

    }

    @Override
    public VedantuResponse moveModuleEntry(MoveModuleEntryReq moveModuleEntryReq) {
        MoveModuleEntryRes moveModuleEntryResponse = cmdsModuleManager.moveModuleEntry(moveModuleEntryReq);
        return new VedantuResponse(moveModuleEntryResponse);
    }

    @Override
    public VedantuResponse deleteModuleEntry(DeleteModuleEntryReq deleteModuleEntryReq) {
        DeleteModuleEntryRes deleteModuleEntryResponse = cmdsModuleManager.deleteModuleEntry(deleteModuleEntryReq);
        return new VedantuResponse(deleteModuleEntryResponse);

    }

    @Override
    public VedantuResponse getCMDSModuleInfo(GetModuleInfoReq getModuleInfoReq) {
        GetModuleInfoRes getModuleInfoResponse = cmdsModuleManager.getModuleInfo(getModuleInfoReq);
        return new VedantuResponse(getModuleInfoResponse);

    }

    @Override
    public VedantuResponse updatemoduleEntries(UpdateModuleEntryReq updateModuleEntryReq) {
        UpdateModuleEntryRes updateModuleEntryResponse = cmdsModuleManager.updateModuleEntry(updateModuleEntryReq);
        return new VedantuResponse(updateModuleEntryResponse);

    }

    @Override
    public VedantuResponse getCMDSModules(GetCMDSModulesReq getCMDSModulesReq) {
        GetCMDSModulesRes getCMDSModulesResponse = cmdsModuleManager.getCMDSModules(getCMDSModulesReq);
        return new VedantuResponse(getCMDSModulesResponse);

    }
}
