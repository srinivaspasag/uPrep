package com.lms.pojo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BoardMappings {
    public String parentBoardId;
    public String sharedToBoardId;
    public String boardType;
    public boolean status;

    @Override
    public String toString() {
        return "BoardMappings [parentBoardId=" + parentBoardId + ", sharedToBoardId="
                + sharedToBoardId + ", status=" + status + "]";
    }
}
