package com.lms.board.pojos.test.responses;

import com.lms.board.pojos.test.BoardBasicInfo;
import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetTargetsRes extends ListResponse<BoardBasicInfo> {
}
