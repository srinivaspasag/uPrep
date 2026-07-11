package com.virtualClassRoomService.virtualClassRoomService.Pojos.Requests;

import java.util.List;

public class CreateMeetingRoomRequest {

    public String className;
    public String subject;
    public String topic;
    public long startTime;
    public long duration;
    public List<String> sectionId;
    public String orgId;
    public String userId;
    public long createdTime;

}
