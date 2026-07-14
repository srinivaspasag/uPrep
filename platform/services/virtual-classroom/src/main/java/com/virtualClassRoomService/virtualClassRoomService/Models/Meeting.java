package com.virtualClassRoomService.virtualClassRoomService.Models;

import com.mongodb.lang.NonNull;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(value = "meetings")
public class Meeting {
    @NonNull
    public String meetingId;
    @NonNull
    public String attendePW;
    @NonNull
    public String moderatorPW;
    @NonNull
    public String internalmeetingId;
    public String className;
    public String subject;
    public String topic;
    public long startTime;
    public long duration;
    public List<String> sectionIds;
    public String orgId;
    public String userId;
    public long timeCreated;

    public Meeting(@NonNull String meetingId, @NonNull String attendePW, @NonNull String moderatorPW,
                   @NonNull String internalmeetingId, String className, String subject, String topic,
                   long startTime, long duration, List<String> sectionIds, String orgId,
                   String userId, long timeCreated) {
        this.meetingId = meetingId;
        this.attendePW = attendePW;
        this.moderatorPW = moderatorPW;
        this.internalmeetingId = internalmeetingId;
        this.className = className;
        this.subject = subject;
        this.topic = topic;
        this.startTime = startTime;
        this.duration = duration;
        this.sectionIds = sectionIds;
        this.orgId = orgId;
        this.userId = userId;
        this.timeCreated = timeCreated;
    }

    @Override
    public String toString() {
        return "Meeting{" +
                "meetingId='" + meetingId + '\'' +
                ", attendePW='" + attendePW + '\'' +
                ", moderatorPW='" + moderatorPW + '\'' +
                ", internalmeetingId='" + internalmeetingId + '\'' +
                ", className='" + className + '\'' +
                ", subject='" + subject + '\'' +
                ", topic='" + topic + '\'' +
                ", startTime=" + startTime +
                ", duration=" + duration +
                ", sectionIds=" + sectionIds +
                ", orgId='" + orgId + '\'' +
                ", userId='" + userId + '\'' +
                ", timeCreated=" + timeCreated +
                '}';
    }
}
