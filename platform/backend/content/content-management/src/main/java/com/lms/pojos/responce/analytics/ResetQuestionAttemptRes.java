package com.lms.pojos.responce.analytics;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetQuestionAttemptRes {

    public boolean success;
    public int attempts;


    public ResetQuestionAttemptRes(boolean success, int attempts) {

        super();
        this.success = success;
        this.attempts = attempts;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{success:").append(success).append(", attempts:").append(attempts)
                .append("}");
        return builder.toString();
    }


}
