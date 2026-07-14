package com.lms.models;

import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.enums.DoubtState;
import com.lms.pojos.DoubtAssignmentDetails;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(value = "doubttransactions")
@Setter
@Getter
public class DoubtTransaction extends VedantuBaseMongoModel {
    public String                       discussionId;
    public boolean                      completed;
    public long                         completedAt;
    public String                       completedBy;
    public DoubtState state;
    public List<DoubtAssignmentDetails> assignedTo = new ArrayList<DoubtAssignmentDetails>();
    public List<String> acceptedBy = new ArrayList<String>();
    public List<String>                 rejectedBy = new ArrayList<String>();
    public List<String>                 mayBeLater = new ArrayList<String>();

    public DoubtTransaction() {
        super();
    }

    public DoubtTransaction(String discussionId) {
        super();
        this.discussionId = discussionId;
        this.state = DoubtState.UNASSIGNED;
        this.completed = false;
    }

    public DoubtTransaction(String discussionId, boolean completed, long completedAt,
                            String completedBy, DoubtState state, List<DoubtAssignmentDetails> assignedTo,
                            List<String> acceptedBy, List<String> rejectedBy, List<String> mayBeLater) {
        super();
        this.discussionId = discussionId;
        this.completed = completed;
        this.completedAt = completedAt;
        this.completedBy = completedBy;
        this.state = state;
        this.assignedTo = assignedTo;
        this.acceptedBy = acceptedBy;
        this.rejectedBy = rejectedBy;
        this.mayBeLater = mayBeLater;
    }



    public DoubtAssignmentDetails getDoubtAssignmentDetailsForTeacherId(String teacherId){
        for(DoubtAssignmentDetails doubtDetail : this.assignedTo){
            if(doubtDetail.teacherId.equals(teacherId)){
                return doubtDetail;
            }
        }
        return null;
    }
    @Override
    public String toString() {
        return "DoubtTransaction [discussionId=" + discussionId + ", completed=" + completed
                + ", completedAt=" + completedAt + ", completedBy=" + completedBy + ", assignedTo="
                + assignedTo + ", state=" + state + "]";
    }

}
