package com.vedantu.content.pojos;

import com.vedantu.content.enums.AcceptanceState;

public class DoubtAssignmentDetails {
    public String          teacherId;
    public long            timeAssignedAt;
    public AcceptanceState state;

    public DoubtAssignmentDetails() {
        super();
    }

    public DoubtAssignmentDetails(String teacherId, long timeAssignedAt,
            AcceptanceState state) {
        super();
        this.teacherId = teacherId;
        this.timeAssignedAt = timeAssignedAt;
        this.state = state;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DoubtAssignmentDetails other = (DoubtAssignmentDetails) obj;
        if (teacherId == null) {
            if (other.teacherId != null)
                return false;
        } else if (!teacherId.equals(other.teacherId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "DoubtAssignmentDetails [teacherId=" + teacherId + ", timeAssignedAt="
                + timeAssignedAt + ", state=" + state + "]";
    }

}
