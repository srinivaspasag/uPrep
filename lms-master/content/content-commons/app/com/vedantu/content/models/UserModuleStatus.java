package com.vedantu.content.models;

import java.util.List;

import com.google.code.morphia.annotations.Entity;
import com.vedantu.commons.enums.Scope;
import com.vedantu.content.enums.EnumBasket.TestType;
import com.vedantu.content.enums.TestMode;
import com.vedantu.content.pojos.tests.TestMetadata;

@Entity(value="usersmodulestatus", noClassnameStored=true)
public class UserModuleStatus extends AbstractUserModuleStatus{
    public String id;
    public String userId;


public UserModuleStatus() {

    super();
}

public UserModuleStatus(String userId, String name, String desc, int qusCount, long duration,
        int totalMarks, List<TestMetadata> metadata, TestType type, TestMode mode, String code,
        Scope scope) {

    //ssuper(userId, name, desc, qusCount, duration, totalMarks, metadata, type, mode, code, scope);
}

}