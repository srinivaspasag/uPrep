package com.vedantu.content.pojos.responses.analytics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vedantu.content.pojos.StudentSubjectWiseResult;

public class GetUserEntityAnalyticsBySubjectRes {
    public String                         testName;
    public String                         orgName;
    public double                            totalMarks;
    public double                            highestMarks;
    public double                         averageMarks;
    public List<StudentSubjectWiseResult> results;
    public Map<String, Integer>           subjectMaxMarksMap = new HashMap<String, Integer>();
}
