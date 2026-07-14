package com.lms.pojos.responce.questions;

import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;
import com.lms.question.SolutionFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetSolutionsRes extends ListResponse<GetSolutionRes> {
    public List<SolutionFormat> solutions;

}
