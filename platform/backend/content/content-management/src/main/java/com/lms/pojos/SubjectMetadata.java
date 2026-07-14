package com.lms.pojos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class SubjectMetadata {
    public String id;
    public String name;
    public List<EntityDetails> details;
}
