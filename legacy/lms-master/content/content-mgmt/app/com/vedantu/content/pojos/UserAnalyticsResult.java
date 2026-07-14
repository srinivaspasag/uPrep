package com.vedantu.content.pojos;

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
