package com.vedantu.content.pojos.responses.questions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vedantu.commons.pojos.responses.IListResponseObj;

public class GetQuestionsSolutionRes implements IListResponseObj {

    public Map<String, List<GetSolutionRes>> solutions = new HashMap<String, List<GetSolutionRes>>();
}
