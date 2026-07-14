package com.lms.enums;


import com.lms.common.vedantu.enums.BoardType;

public enum AcademicDimensionType
{
    UNKNOWN, OVERALL, COURSE, TOPIC, SUBTOPIC;

    public static AcademicDimensionType getType(BoardType boardType) {
        try {
            return valueOf(boardType.name());
        } catch (Throwable t) {
            // swallow
        }
        return UNKNOWN;
    }
}
