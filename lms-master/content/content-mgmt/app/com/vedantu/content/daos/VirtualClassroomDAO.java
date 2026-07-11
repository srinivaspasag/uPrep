package com.vedantu.content.daos;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.content.models.VirtualClassroom;
import com.vedantu.mongo.VedantuBasicDAO;

public class VirtualClassroomDAO extends VedantuBasicDAO<VirtualClassroom, ObjectId> {
    private static final ALogger  LOGGER   = Logger.of(VirtualClassroomDAO.class);
    public static final VirtualClassroomDAO INSTANCE = new VirtualClassroomDAO();
    private VirtualClassroomDAO() {
        super(VirtualClassroom.class);
    }
    public VirtualClassroom createClassroom(String description, long startTime, long endTime,
            boolean recordClass, boolean cancelled, boolean audioOnly, String userId, String orgId){
        VirtualClassroom room = new VirtualClassroom(description, startTime, endTime, recordClass, cancelled, audioOnly, userId, orgId);
        save(room);
        return room;
    }
}
