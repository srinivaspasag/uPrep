package com.lms.user.vedantu.user.pojo;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAppCheckReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class TestUserDataReq extends AbstractAppCheckReq {
    @NotBlank(message = "adminUserId should not be null")
    public String adminUserId;

    public String surname;

    @NotBlank(message = "name should not be null")
    public String name;
    public String dob;
    public String address;
    public String parentsMobile;

    @NotBlank(message = "studentsMobile should not be null")
    public String studentsMobile;
    @NotBlank(message = "memberId should not be null")
    public String memberId;

    @NotBlank(message = "email should not be null")
    public String email;

    @NotBlank(message = "orgId should not be null")
    public String orgId;

    @NotBlank(message = "sectionId should not be null")
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
