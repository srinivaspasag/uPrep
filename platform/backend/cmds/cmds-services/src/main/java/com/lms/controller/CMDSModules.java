package com.lms.controller;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.*;
import com.lms.pojos.requests.splModules.GetModuleInfoReq;
import com.lms.services.CMDSModulesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/cmdsModules")
public class CMDSModules {
    @Autowired
    private CMDSModulesService cmdsModulesServiceImpl;

    @PostMapping("/createModule")
    public ResponseEntity<VedantuResponse> createModule(@Valid CreateModuleReq createModuleReq) {
        return ResponseEntity.ok(cmdsModulesServiceImpl.createmodule(createModuleReq));
    }

    @PostMapping("/addModuleEntries")
    public ResponseEntity<VedantuResponse> addModuleEntries(@Valid AddModuleEntryReq addModuleEntryReq) {
        return ResponseEntity.ok(cmdsModulesServiceImpl.addmoduleEntries(addModuleEntryReq));
    }

    @PostMapping("/updateModule")
    public ResponseEntity<VedantuResponse> updatemodule(@Valid UpdateModuleReq addModuleEntryReq) {
        return ResponseEntity.ok(cmdsModulesServiceImpl.updatemodule(addModuleEntryReq));
    }

    @PostMapping("/deleteModule")
    public ResponseEntity<VedantuResponse> deleteModule(@Valid DeleteModuleReq deleteModuleReq) {
        return ResponseEntity.ok(cmdsModulesServiceImpl.deleteModule(deleteModuleReq));
    }

    @PostMapping("/moveModuleEntry")
    public ResponseEntity<VedantuResponse> moveModuleEntry(@Valid MoveModuleEntryReq moveModuleEntryReq) {
        return ResponseEntity.ok(cmdsModulesServiceImpl.moveModuleEntry(moveModuleEntryReq));
    }

    @PostMapping("/deleteModuleEntry")
    public ResponseEntity<VedantuResponse> deleteModuleEntry(@Valid DeleteModuleEntryReq deleteModuleEntryReq) {
        return ResponseEntity.ok(cmdsModulesServiceImpl.deleteModuleEntry(deleteModuleEntryReq));
    }

    @PostMapping("/getCMDSModuleInfo")
    public ResponseEntity<VedantuResponse> getCMDSModuleInfo(@Valid GetModuleInfoReq getModuleInfoReq) {
        return ResponseEntity.ok(cmdsModulesServiceImpl.getCMDSModuleInfo(getModuleInfoReq));
    }

    @PostMapping("/updateModuleEntries")
    public ResponseEntity<VedantuResponse> updateModuleEntries(@Valid UpdateModuleEntryReq updateModuleEntryReq) {
        return ResponseEntity.ok(cmdsModulesServiceImpl.updatemoduleEntries(updateModuleEntryReq));
    }

    @PostMapping("/getCMDSModules")
    public ResponseEntity<VedantuResponse> getCMDSModules(@Valid GetCMDSModulesReq getCMDSModulesReq) {
        return ResponseEntity.ok(cmdsModulesServiceImpl.getCMDSModules(getCMDSModulesReq));
    }
}
