package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class AddScheduleReq extends GetScheduleReq {
    public long day;
    public List<SrcEntity> entityList = new ArrayList<SrcEntity>();
    @NotBlank(message = "boardId should not be null")
    public String boardId;
    @NotBlank(message = "boardName should not be null")
    public String boardName;
}
