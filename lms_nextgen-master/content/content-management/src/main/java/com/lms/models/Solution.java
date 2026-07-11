package com.lms.models;

import com.lms.enums.SolutionType;
import com.lms.pojos.Attachment;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;


@Document(value = "solutions")
public class Solution  extends AbstractContentStatsModel
{
    transient public Map<String, Set<String>> imageTypeMap;

    @Indexed
    public String                             qId;

    public String content;
    public List<String> answers;


    public Set<String>                        imgUuids;
    public Map<String, List<String>>          gridAnswer;

    public SolutionType                       type;
    public boolean                            verified;
    public List<Attachment>                   attachments;

    public Solution() {

        super();
    }

    public Solution(String qId, String userId, String content, List<String> answers,

                    SolutionType type, List<Attachment> attachments) {

        super();
        this.qId = qId;
        this.userId = userId;
        this.content = content;
        this.imgUuids = new HashSet<String>();
        this.answers = answers;
        this.type = type;
        this.gridAnswer = new HashMap<String, List<String>>();
        this.attachments = attachments;

    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("Solution [qid:").append(qId).append(", userId:").append(userId)
                .append(", content:").append(content).append(", imgUuids:").append(imgUuids)
                .append(", answers:").append(answers).append(", type:").append(type).append("]");
        return builder.toString();
    }

}
