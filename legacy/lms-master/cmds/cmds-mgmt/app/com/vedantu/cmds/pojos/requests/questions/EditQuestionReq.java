package com.vedantu.cmds.pojos.requests.questions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import play.data.validation.Constraints.Required;

import com.vedantu.cmds.pojos.content.question.BasicSolutionInfo;
import com.vedantu.content.enums.Difficulty;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.pojos.SrcEntityEdit;
import com.vedantu.content.pojos.requests.EditContentReq;

public class EditQuestionReq extends EditContentReq {

//    @Required
//    public String                    questionId;
//    @Required
//    public String                    orgId;

    public String                    content;
    @Required
    public QuestionType              type;
    public List<String>              options;

    public List<String>              tags;
    public List<String>              columnA;
    public List<String>              columnB;
    public BasicSolutionInfo         solution;
    public List<String>              answers;
    public Map<String, List<String>> gridAnswer;

    public Difficulty                difficulty;
    public List<String>              hints;

    public List<String>              brdIds = new ArrayList<String>();
    public List<String>              targetIds;


    public String                    origRefNo;
    public List<SrcEntityEdit>       editEntities;
}
