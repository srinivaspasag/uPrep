package com.lms.pojos.responce;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddQuestionRes {
    public String id;
    public String paraId;

    public AddQuestionRes(String id) {
        super();
        this.id = id;
    }
}
