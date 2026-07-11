package com.lms.common.vedantu.commons.pojos.requests;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FieldInfo  {

    public String name;

    public FieldInfo() {

    }

    public FieldInfo(String name) {

        this.name = name;
    }

}
