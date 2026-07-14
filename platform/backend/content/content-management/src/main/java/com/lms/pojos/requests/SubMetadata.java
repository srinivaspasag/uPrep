package com.lms.pojos.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class SubMetadata {
    public String id;
    public String subName;
    public List<EntityTypeData> details = new ArrayList<EntityTypeData>();
}
