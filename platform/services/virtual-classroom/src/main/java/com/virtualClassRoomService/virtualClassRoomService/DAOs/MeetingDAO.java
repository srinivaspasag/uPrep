package com.virtualClassRoomService.virtualClassRoomService.DAOs;

import com.virtualClassRoomService.virtualClassRoomService.Repositories.MeetingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.management.Query;

public class MeetingDAO {

    @Autowired
    MeetingRepository meetingRepository;

    MongoTemplate mongotemplate;

//    public Query getQueryForSectionIds(String sectionId){
//        Query query = new Query();
//        query.
//
//    }
}
