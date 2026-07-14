package com.virtualClassRoomService.virtualClassRoomService.Repositories;


import com.virtualClassRoomService.virtualClassRoomService.Models.Meeting;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingRepository extends MongoRepository<Meeting,String> {

    @Query("{sectionIds : {$in: [?0]}}")
    List<Meeting> findBySectionId(String sectionId);

    @Query("{sectionIds : {$in: ?0}}")
    List<Meeting> findBySectionIds(List<String> sectionIds);
}
