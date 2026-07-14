package com.lms.pojos.responce.questions;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class GetQuestionsSolutionRes implements IListResponseObj {

    public Map<String, List<GetSolutionRes>> solutions = new HashMap<String, List<GetSolutionRes>>();
}
