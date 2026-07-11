package com.lms.pojo.responce;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class GetDigitalLibraryFieldsRes {

    // update fields return to client
    public Set<String> fields;
}
