package com.lms.pojos.responce.analytics;

import com.lms.pojos.StudentSubjectWiseResult;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class GetUserEntityAnalyticsBySubjectRes {
    public String testName;
    public String orgName;
    public double totalMarks;
    public double highestMarks;
    public double averageMarks;
    public List<StudentSubjectWiseResult> results;
    public Map<String, Integer> subjectMaxMarksMap = new HashMap<String, Integer>();
}
