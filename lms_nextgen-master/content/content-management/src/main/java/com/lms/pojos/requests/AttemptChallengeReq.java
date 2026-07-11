package com.lms.pojos.requests;

import javax.validation.constraints.NotBlank;
import java.util.List;

public class AttemptChallengeReq extends GetChallengeHintReq {

    @NotBlank(message = "answer should not be null")
    public List<String> answer;
}
