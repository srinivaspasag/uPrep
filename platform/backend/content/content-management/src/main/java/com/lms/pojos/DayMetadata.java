package com.lms.pojos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class DayMetadata {
    public String id;
    public String name;
    public List<EntityCount> details;
}
