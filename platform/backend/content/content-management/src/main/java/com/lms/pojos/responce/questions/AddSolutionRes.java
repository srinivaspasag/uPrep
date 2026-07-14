package com.lms.pojos.responce.questions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddSolutionRes {

    public String qid;
    public String id;

    public AddSolutionRes(String qid, String id) {
        this.qid = qid;
        this.id = id;
    }

}
