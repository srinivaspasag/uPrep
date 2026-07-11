package com.lms.enums;

import com.lms.common.vedantu.constants.HardCodedConstants;

public class EnumBasket {

    public enum Correctness {
        UNKNOWN, CORRECT, INCORRECT, UNJUDGEABLE;

        public static Correctness valueOfKey(String val) {
            Correctness correctness = Correctness.UNKNOWN;
            try {
                correctness = valueOf(val.isEmpty() ? HardCodedConstants.emptyString : (UNKNOWN.name()).toUpperCase());
            } catch (Exception e) {
            }

            return correctness;
        }
    }

    public enum Judgement {
        UNKNOWN, JUDGE, DONT_JUDGE, DONT_CARE;

        public static Judgement valueOfKey(String val) {
            Judgement judgement = Judgement.UNKNOWN;
            judgement = valueOf(val.isEmpty() ? HardCodedConstants.emptyString : (UNKNOWN.name()).toUpperCase());
            // if(judgement==UNKNOWN){
            // throw new Exception("judgement type not recognised!");
            // }

            return judgement;
        }
    }

    public enum Status {
        COMPLETE, SKIP, REVIEW, UNKNOWN;

        public static Status valueOfKey(String val) {
            Status status = Status.COMPLETE;
            status = valueOf(val.isEmpty() ? HardCodedConstants.emptyString : (COMPLETE.name()).toUpperCase());
            return status;
        }
    }

    public enum TestType {
        UNKNOWN, TEST, ASSIGNMENT, QUESTION_LIST, CHALLENGE, ONLINE, OFFLINE;

        public static TestType valueOfKey(String type) {
            TestType testType = UNKNOWN;

            testType = valueOf(type.isEmpty() ? HardCodedConstants.emptyString : (TEST.name()).toUpperCase());

            if (testType == UNKNOWN) {
                // throw new Exception("Type is not recognised");
            }

            return testType;
        }
    }

    public enum MarksType {
        AVERAGE, LOWEST, HIGHEST;

        public static MarksType valueOfKey(String type) {
            MarksType marksType = AVERAGE;
            try {
                marksType = valueOf(type.isEmpty() ? HardCodedConstants.emptyString : (AVERAGE.name()).toUpperCase());

            } catch (Exception e) {

            }
            return marksType;
        }
    }

}