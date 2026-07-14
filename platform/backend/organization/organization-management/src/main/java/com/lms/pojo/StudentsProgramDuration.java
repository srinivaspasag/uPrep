package com.lms.pojo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StudentsProgramDuration {
    public long numberOfSixMonths;
    public long numberOfOneYear;
    public long numberOfTwoYears;
    public String programName;

    public StudentsProgramDuration() {
        super();
    }

    public StudentsProgramDuration(String programName) {
        super();
        this.programName = programName;
    }
}
