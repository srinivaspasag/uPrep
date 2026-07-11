package com.virtualClassRoomService.virtualClassRoomService.controllers;


import com.virtualClassRoomService.virtualClassRoomService.Models.Meeting;
import com.virtualClassRoomService.virtualClassRoomService.Pojos.Requests.CreateMeetingRoomRequest;
import com.virtualClassRoomService.virtualClassRoomService.Pojos.Response.CreateMeetingResponse;
import com.virtualClassRoomService.virtualClassRoomService.Service.VirtualClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/virtualClass")
public class VirtualClassController {

    @Autowired
    VirtualClassService virtualClassService;

    @PostMapping("/createMeeting")
    public CreateMeetingResponse createMeeting(@RequestBody CreateMeetingRoomRequest request) throws IOException {
        return virtualClassService.createMeeting(request);
    }

    @GetMapping("/getMeetingBySectionId")
    public List<Meeting> getMeetingBySectionId(@RequestParam String sectionId){
        return virtualClassService.getMeetingList(sectionId);
    }

}
