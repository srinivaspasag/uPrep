package com.lms.controller;


import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.*;
import com.lms.services.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/modules")
public class Modules {
    @Autowired
    ModuleService moduleServiceimpl;

    @PostMapping("/updateUserModuleStatus")
    public ResponseEntity<VedantuResponse> updateUserModuleStatus(UpdateUserModuleReq updateUserModuleReq) {
        return ResponseEntity.ok(moduleServiceimpl.updateUserModuleStatus(updateUserModuleReq));
    }

    @PostMapping("/getUserModuleStatus")
    public ResponseEntity<VedantuResponse> getUserModuleStatus(GetUserModuleReq getUserModuleReq) {
        return ResponseEntity.ok(moduleServiceimpl.getUserModuleStatus(getUserModuleReq));
    }

    @PostMapping("/syncModule")
    public ResponseEntity<VedantuResponse> syncModule(SyncModuleReq syncModuleReq) {
        return ResponseEntity.ok(moduleServiceimpl.syncModule(syncModuleReq));
    }

    @PostMapping("/getModule")
    public ResponseEntity<VedantuResponse> getModule(GetModuleReq getModuleReq) {
        return ResponseEntity.ok(moduleServiceimpl.getModule(getModuleReq));
    }

    @PostMapping("/getModules")
    public ResponseEntity<VedantuResponse> getModules(GetModulesReq getModulesReq) {
        return ResponseEntity.ok(moduleServiceimpl.getModules(getModulesReq));
    }
}

