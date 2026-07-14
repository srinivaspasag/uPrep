package com.lms.pojos.responce;

import com.lms.enums.Difficulty;
import com.lms.enums.QuestionType;
import com.lms.pojos.requests.BasicSolutionInfo;
import com.lms.pojos.requests.EditContentReq;
import com.lms.pojos.requests.SrcEntityEdit;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class EditQuestionReq extends EditContentReq {

//    @Required
//    public String                    questionId;
//    @Required
//    public String                    orgId;

    public String content;
    @NotBlank(message = "type should not be null")
    public QuestionType type;
    public List<String> options;

    public List<String> tags;
    public List<String> columnA;
    public List<String> columnB;
    public BasicSolutionInfo solution;
    public List<String> answers;
    public Map<String, List<String>> gridAnswer;

    public Difficulty difficulty;
    public List<String> hints;

    public List<String> brdIds = new ArrayList<String>();
    public List<String> targetIds;


    public String origRefNo;
    public List<SrcEntityEdit> editEntities;
}
