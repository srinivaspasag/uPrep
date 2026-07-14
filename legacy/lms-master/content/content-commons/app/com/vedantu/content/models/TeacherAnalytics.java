package com.vedantu.content.models;

import com.google.code.morphia.annotations.Entity;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "teacheranalytics", noClassnameStored = true)
public class TeacherAnalytics extends VedantuBaseMongoModel {
    public String boardId;
    public String currentAssaignedDoubt;
    public long   lastAssaignedTime;
    public long   lastAcceptedTime;
    public int    priorityOrder;
    public String teacherOrgMemberId;

    public TeacherAnalytics() {
        super();
    }

    public TeacherAnalytics(String teacherOrgMemberId) {
        super();
        this.teacherOrgMemberId = teacherOrgMemberId;
    }

    public TeacherAnalytics(String boardId, String teacherOrgMemberId) {
        super();
        this.boardId = boardId;
        this.teacherOrgMemberId = teacherOrgMemberId;
    }

    public TeacherAnalytics(String boardId, String currentAssaignedDoubt, long lastAssaignedTime,
            long lastAcceptedTime, int priorityOrder, String teacherOrgMemberId) {
        super();
        this.boardId = boardId;
        this.currentAssaignedDoubt = currentAssaignedDoubt;
        this.lastAssaignedTime = lastAssaignedTime;
        this.lastAcceptedTime = lastAcceptedTime;
        this.priorityOrder = priorityOrder;
        this.teacherOrgMemberId = teacherOrgMemberId;
    }

    @Override
    public String toString() {
        return "TeacherAnalytics [boardId=" + boardId + ", currentAssaignedDoubt="
                + currentAssaignedDoubt + ", lastAssaignedTime=" + lastAssaignedTime
                + ", lastAcceptedTime=" + lastAcceptedTime + ", priorityOrder=" + priorityOrder
                + ", teacherOrgMemberId=" + teacherOrgMemberId + "]";
    }

}
