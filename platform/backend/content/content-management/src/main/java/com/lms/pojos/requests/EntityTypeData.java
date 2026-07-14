package com.lms.pojos.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Setter
@Getter
public class EntityTypeData {
    public String type;
    public List<EntityList> contents = new ArrayList<EntityList>();
}
