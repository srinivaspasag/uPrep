package com.virtualClassRoomService.virtualClassRoomService.Service;

import com.virtualClassRoomService.virtualClassRoomService.Helpers.URLCreationHelper;
import com.virtualClassRoomService.virtualClassRoomService.Models.Meeting;
import com.virtualClassRoomService.virtualClassRoomService.Pojos.Requests.CreateMeetingRoomRequest;
import com.virtualClassRoomService.virtualClassRoomService.Pojos.Response.CreateMeetingResponse;
import com.virtualClassRoomService.virtualClassRoomService.Repositories.MeetingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class VirtualClassService {

    private static Logger log = LoggerFactory.getLogger(VirtualClassService.class);

    @Autowired
    MeetingRepository meetingRepository;
    @Autowired
    URLCreationHelper urlCreationHelper;

    public CreateMeetingResponse createMeeting(CreateMeetingRoomRequest request) throws IOException {
        String url=urlCreationHelper.createUrl(request.className);
        RestTemplate restTemplate = new RestTemplate();
        CreateMeetingResponse response = restTemplate.getForObject(url, CreateMeetingResponse.class);
        Meeting meeting=new Meeting(response.getMeetingID(),response.getAttendeePW(),response.getModeratorPW(),
                response.getInternalMeetingID(),request.className,request.subject,request.topic,
                request.startTime,request.duration,request.sectionId,request.orgId,
                request.userId,response.getCreateTime());
        meetingRepository.save(meeting);
        return response;
    }


    public List<Meeting> getMeetingList(String SectionId){
        List<String> secs=new ArrayList<>();
        secs.add(SectionId);
        List<Meeting> meetings=meetingRepository.findBySectionIds(secs);
        System.out.println(meetings);
        return meetingRepository.findBySectionId(SectionId);
    }
}
