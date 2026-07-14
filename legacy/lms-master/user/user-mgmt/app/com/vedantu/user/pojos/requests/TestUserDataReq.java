package com.vedantu.user.pojos.requests;

import com.vedantu.commons.pojos.requests.AbstractAppCheckReq;
import play.data.validation.Constraints.Required;

public class TestUserDataReq extends AbstractAppCheckReq{

    @Required
    public String adminUserId;

    public String surname;

    @Required
    public String name;
    public String dob;
    public String address;
    public String parentsMobile;

    @Required
    public String studentsMobile;
    @Required
    public String memberId;

    @Required
    public String email;

    @Required
    public String orgId;

    @Required
    public String sectionId;

    public String stdCode;
    public String telephoneNo;
    public String caste;
    public String subcaste;
    public String otherReservations;
    public String schoolName;
    public String schoolPlace;
    public String medium;
    public double percentage;
    public String course;
    public int mathMarks;
    public int scienceMarks;
    public int englishMarks;
    public String qualifiedFor;
    public String collegeName;
    public String subject;
    public String motherTongue;
    public String ambition;
    public String coachingOpted;
    public String fatherName;
    public String fatherQualification;
    public String fatherOccupation;
    public String fatherDesignation;
    public String fatherDepartment;
    public String motherName;
    public String motherQualification;
    public String motherOccupation;
    public String motherDesignation;
    public String motherDepartment;
    public String brotherName;
    public String brotherQualification;
    public String sisterName;
    public String sisterQualification;
    public String dateFormSubmit;
}
