package com.lms.pojos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SharedBoardInfo {
    public String parentBoardId;
    public String parentBoardName;
    public String sharedBoardId;
    public String sharedBoardName;
    public boolean status;
}
