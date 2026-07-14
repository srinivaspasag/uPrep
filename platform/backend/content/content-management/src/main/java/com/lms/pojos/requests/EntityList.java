package com.lms.pojos.requests;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EntityList {
    public String id;
    public String cmdsId;
    public String name;
    public boolean attempted;
}
