package com.vedantu.user.models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.annotations.Transient;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.user.pojos.requests.TestUserDataReq;

@Entity(value = "testusers", noClassnameStored = true)
public class TestUser extends VedantuBaseMongoModel {
    @Transient
    public static final String FIELD_TELEPHONE = "studentsMobile";
    @Transient
    public static final String FIELD_INSTITUTE_ID = "memberId";

    public String surname;
    public String name;
    public String dob;
    public String address;
    public String parentsMobile;

    @Indexed(unique = true)
    public String studentsMobile;
    public String memberId;

    @Indexed(unique = true)
    public String email;

    //References userId in Users table
    @Indexed(unique = true)
    public String userId;

    public String orgId;

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

    public void createFromReq(TestUserDataReq request) {
        this.surname = request.surname;
        this.name = request.name;
        this.email = request.email;
        this.dob = request.dob;
        this.address = request.address;
        this.parentsMobile = request.parentsMobile;
        this.studentsMobile = request.studentsMobile;
        this.memberId = request.memberId;
        this.stdCode = request.stdCode;
        this.telephoneNo = request.telephoneNo;
        this.caste = request.caste;
        this.subcaste = request.subcaste;
        this.otherReservations = request.otherReservations;
        this.schoolName = request.schoolName;
        this.schoolPlace = request.schoolPlace;
        this.medium = request.medium;
        this.percentage = request.percentage;
        this.course = request.course;
        this.mathMarks = request.mathMarks;
        this.scienceMarks = request.scienceMarks;
        this.englishMarks = request.englishMarks;
        this.qualifiedFor = request.qualifiedFor;
        this.collegeName = request.collegeName;
        this.subject = request.subject;
        this.motherTongue = request.motherTongue;
        this.ambition = request.ambition;
        this.coachingOpted = request.coachingOpted;
        this.fatherName = request.fatherName;
        this.fatherQualification = request.fatherQualification;
        this.fatherOccupation = request.fatherOccupation;
        this.fatherDesignation = request.fatherDesignation;
        this.fatherDepartment = request.fatherDepartment;
        this.motherName = request.motherName;
        this.motherQualification = request.motherQualification;
        this.motherOccupation = request.motherOccupation;
        this.motherDesignation = request.motherDesignation;
        this.motherDepartment = request.motherDepartment;
        this.brotherName = request.brotherName;
        this.brotherQualification = request.brotherQualification;
        this.sisterName = request.sisterName;
        this.sisterQualification = request.sisterQualification;
        this.dateFormSubmit = request.dateFormSubmit;
        this.orgId = request.orgId;
    }
}
