package com.lms.pojos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAnalyticsResult {
    public String subjectName;
    public double subjectMarks;
    public int rankOfStudent;

    public UserAnalyticsResult(String subjectName, double subjectMarks) {
        super();
        this.subjectName = subjectName;
        this.subjectMarks = subjectMarks;
    }
}
