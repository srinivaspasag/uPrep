package com.virtualClassRoomService.virtualClassRoomService.Pojos.Response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@Data
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class CreateMeetingResponse {

    @XmlElement(name = "returncode")
    private String returncode;
    @XmlElement(name = "meetingID")
    private String meetingID;
    @XmlElement(name = "internalMeetingID")
    private String internalMeetingID;
    @XmlElement(name = "parentMeetingID")
    private String parentMeetingID;
    @XmlElement(name = "attendeePW")
    private String attendeePW;
    @XmlElement(name = "moderatorPW")
    private String moderatorPW;
    @XmlElement(name = "createTime")
    private long createTime;
    @XmlElement(name = "voiceBridge")
    private double voiceBridge;
    @XmlElement(name = "dialNumber")
    private String dialNumber;
    @XmlElement(name = "createDate")
    private Date createDate;
    @XmlElement(name = "hasUserJoined")
    private boolean hasUserJoined;
    @XmlElement(name = "duration")
    private long duration;
    @XmlElement(name = "hasBeenForciblyEnded")
    private boolean hasBeenForciblyEnded;
    @XmlElement(name = "messageKey")
    private String messageKey;
    @XmlElement(name = "message")
    private String message;

    public String getReturncode() {
        return returncode;
    }

    public void setReturncode(String returncode) {
        this.returncode = returncode;
    }

    public String getMeetingID() {
        return meetingID;
    }

    public void setMeetingID(String meetingID) {
        this.meetingID = meetingID;
    }

    public String getInternalMeetingID() {
        return internalMeetingID;
    }

    public void setInternalMeetingID(String internalMeetingID) {
        this.internalMeetingID = internalMeetingID;
    }

    public String getParentMeetingID() {
        return parentMeetingID;
    }

    public void setParentMeetingID(String parentMeetingID) {
        this.parentMeetingID = parentMeetingID;
    }

    public String getAttendeePW() {
        return attendeePW;
    }

    public void setAttendeePW(String attendeePW) {
        this.attendeePW = attendeePW;
    }

    public String getModeratorPW() {
        return moderatorPW;
    }

    public void setModeratorPW(String moderatorPW) {
        this.moderatorPW = moderatorPW;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public double getVoiceBridge() {
        return voiceBridge;
    }

    public void setVoiceBridge(double voiceBridge) {
        this.voiceBridge = voiceBridge;
    }

    public String getDialNumber() {
        return dialNumber;
    }

    public void setDialNumber(String dialNumber) {
        this.dialNumber = dialNumber;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public boolean isHasUserJoined() {
        return hasUserJoined;
    }

    public void setHasUserJoined(boolean hasUserJoined) {
        this.hasUserJoined = hasUserJoined;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean isHasBeenForciblyEnded() {
        return hasBeenForciblyEnded;
    }

    public void setHasBeenForciblyEnded(boolean hasBeenForciblyEnded) {
        this.hasBeenForciblyEnded = hasBeenForciblyEnded;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
