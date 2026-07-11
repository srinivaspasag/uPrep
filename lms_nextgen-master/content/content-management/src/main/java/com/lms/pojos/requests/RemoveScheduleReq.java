package com.lms.pojos.requests;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class RemoveScheduleReq extends GetScheduleReq{
    @NotBlank(message = "boardId should not be null")
    public String boardId;
    @NotBlank(message = "entityType should not be null")
    public String entityType;
    @NotBlank(message = "entityType should not be null")
    public String entityId;
    @NotBlank(message = "entityCmdsId should not be null")
    public String entityCmdsId;
}
