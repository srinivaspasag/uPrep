package com.lms.board.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class DeleteBoardReq extends AbstractAuthCheckReq
{
    @NotBlank(message = "boardid cannot be empty")
    public String brdId;
    public String parentBrdId;
}
