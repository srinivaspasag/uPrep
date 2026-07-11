package com.vedantu.content.pojos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StudentSubjectWiseResult {
    public String userName;
    public String memberId;
    public List<UserAnalyticsResult> results;
    public long  endTime;
    public long  startTime;
    public String phoneNumber;

    public StudentSubjectWiseResult(String userName, String memberId) {
        super();
        this.userName = userName;
        this.memberId = memberId;
        this.results = new ArrayList<UserAnalyticsResult>();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StudentSubjectWiseResult other = (StudentSubjectWiseResult) obj;
        if (memberId == null) {
            if (other.memberId != null)
                return false;
        } else if (!memberId.equals(other.memberId))
            return false;
        if (userName == null) {
            if (other.userName != null)
                return false;
        } else if (!userName.equals(other.userName))
            return false;
        return true;
    }

    public static class SubjectComparator implements Comparator<StudentSubjectWiseResult>{
        private int index;

        public SubjectComparator(int index) {
            super();
            this.index = index;
        }

        @Override
        public int compare(StudentSubjectWiseResult o1, StudentSubjectWiseResult o2) {
            if(o1 == o2) return 0;
            if(o2 == null) return -1;
            if(o1 == null) return 1;
            return Double.compare(o2.results.get(index).subjectMarks, o1.results.get(index).subjectMarks);
        }


    }
}
