package com.lms.user.vedantu.controllers;

import com.lms.user.vedantu.request.Guest;
import com.lms.user.vedantu.service.DefaultSevice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class DefaultController {

    @Autowired
    DefaultSevice defaultSevice;

    public static final String server = "1";

    @GetMapping("/sayHello")
    public ResponseEntity<String> sayHello(String name) {
        return ResponseEntity.ok("Hello " + name + "!");
    }

    @PostMapping("/sayHelloFormally")
    public ResponseEntity<String> sayHelloFormally(Guest guest) {
        return ResponseEntity.ok(defaultSevice.sayHelloFormally(guest));
    }



}
