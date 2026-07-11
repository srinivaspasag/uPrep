package com.vedantu.board.pojos;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.vedantu.commons.enums.boards.BoardType;
import com.vedantu.commons.enums.boards.GradeType;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.mongo.VedantuRecordState;

public class BoardBasicInfo extends ModelBasicInfo {

    public static final BoardComparator COMPARATOR  = new BoardComparator();

    public String                       name;
    public String                       code;
    public BoardType                    type;
    public String                       treeName;
    public Set<GradeType>               grades;
    public List<String>                 parentIds;
    public String                       year;

    public static final int             physics     = 1;
    public static final int             chemistry   = 2;
    public static final int             mathematics = 3;
    public static final int             biology     = 4;
    public static final int             botany      = 5;
    public static final int             zoology     = 6;

    public BoardBasicInfo(String id, VedantuRecordState recordState, String name, String code,
            BoardType type, String treeName, Set<GradeType> grades, List<String> parentIds,
            String year) {
        super(id, recordState);
        this.name = name;
        this.code = code;
        this.type = type;
        this.treeName = treeName;
        this.grades = grades;
        this.parentIds = parentIds;
        this.year = year;
    }

    public int getPriority() {
        String subjectLowerCase = this.name.toLowerCase();
        if (subjectLowerCase.contains("physics")) {
            return physics;
        }
        if (subjectLowerCase.contains("chemistry")) {
            return chemistry;
        }
        if (subjectLowerCase.contains("biology")) {
            return biology;
        }
        if (subjectLowerCase.contains("botany")) {
            return botany;
        }
        if (subjectLowerCase.contains("mathematics") || subjectLowerCase.contains("maths")) {
            return mathematics;
        }
        if (subjectLowerCase.contains("zoology")) {
            return zoology;
        }

        return 9;
    }

    @Override
    public String toString() {
        return "BoardBasicInfo [name=" + name + ", code=" + code + ", type=" + type + ", treeName="
                + treeName + ", grades=" + grades + ", parentIds=" + parentIds + ", id=" + id
                + ", recordState=" + recordState + "]";
    }

    public static class BoardComparator implements Comparator<BoardBasicInfo> {

        @Override
        public int compare(BoardBasicInfo x, BoardBasicInfo y) {
            return (x.getPriority() < y.getPriority()) ? -1
                    : ((x.getPriority() == y.getPriority()) ? x.name.compareTo(y.name) : 1);
        }

    }

}
