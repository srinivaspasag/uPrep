package com.lms.pojo.responce;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AcceptTnCRes {
    public boolean done;

    public AcceptTnCRes(boolean done) {
        super();
        this.done = done;
    }
}
