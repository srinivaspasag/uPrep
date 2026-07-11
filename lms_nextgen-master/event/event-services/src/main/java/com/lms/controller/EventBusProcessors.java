package com.lms.controller;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.service.EventBusProcessorsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/EventBusProcessors")
public class EventBusProcessors {
	@Autowired
    private EventBusProcessorsService eventBusProcessorsService; 
	
	@GetMapping("/startAll")
    public ResponseEntity<VedantuResponse> startAll() {
        return ResponseEntity.ok(eventBusProcessorsService.startAll());
    }
	@GetMapping("/stopAll")
    public ResponseEntity<VedantuResponse> stopAll() {
        return ResponseEntity.ok(eventBusProcessorsService.stopAll());
    }

    @GetMapping("/restartAll")
    public ResponseEntity<VedantuResponse> restartAll() {
        return ResponseEntity.ok(eventBusProcessorsService.restartAll());
    }

    @GetMapping("/start")
    public ResponseEntity<VedantuResponse> start(@RequestParam("eventType") String eventType) {
        return ResponseEntity.ok(eventBusProcessorsService.start(eventType));
    }

    @GetMapping("/stop")
    public ResponseEntity<VedantuResponse> stop(@RequestParam("eventType") String eventType) {
        return ResponseEntity.ok(eventBusProcessorsService.stop(eventType));
    }

    @GetMapping("/getStatus")
    public ResponseEntity<VedantuResponse> getStatus(@RequestParam("eventType") String eventType) {
        return ResponseEntity.ok(eventBusProcessorsService.getStatus(eventType));
    }

    @GetMapping("/getStatusAll")
    public ResponseEntity<VedantuResponse> getStatusAll() {
        return ResponseEntity.ok(eventBusProcessorsService.getStatusAll());
    }

    @GetMapping("/enqueeFailedEvents")
    public ResponseEntity<VedantuResponse> enqueeFailedEvents(@RequestParam("size") int size) {
        return ResponseEntity.ok(eventBusProcessorsService.enqueeFailedEvents(size));
    }

    @GetMapping("/stopEnqueeFailedEvents")
    public ResponseEntity<VedantuResponse> stopEnqueeFailedEvents() {
        return ResponseEntity.ok(eventBusProcessorsService.stopEnqueeFailedEvents());
    }
}
