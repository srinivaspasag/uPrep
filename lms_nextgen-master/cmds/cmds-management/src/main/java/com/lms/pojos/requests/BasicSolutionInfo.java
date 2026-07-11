package com.lms.pojos.requests;

import com.lms.pojos.Attachment;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class BasicSolutionInfo {
    public String content;
    public List<Attachment> attachments;
}
