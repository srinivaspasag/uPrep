package com.lms.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntityMeasures extends QuestionMeasures {

    public double score;
    public double maxScore; // maxScore by any user

    public EntityMeasures() {

        super();
    }

    public EntityMeasures(int attempts, int correct, int partial, int incorrect, int left, long timeTaken,
                          double score) {

        super(attempts, correct, partial, incorrect, left, timeTaken);
        this.score = score;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{score:").append(score).append(", maxScore:").append(maxScore)
                .append(", attempts:").append(attempts).append(", correct:").append(correct)
                .append(", incorrect:").append(incorrect).append(", left:").append(left)
                .append(", timeTaken:").append(timeTaken).append("}");
        return builder.toString();
    }

}
