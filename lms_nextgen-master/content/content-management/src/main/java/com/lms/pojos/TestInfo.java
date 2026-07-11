package com.lms.pojos;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.enums.TestMode;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TestInfo extends ModelBasicInfo {

    public String id;
    public String name;
    public long qusCount;
    public TestMode mode;

    public TestInfo(String id, String name, long qusCount, TestMode mode) {

        this.id = id;
        this.name = name;
        this.qusCount = qusCount;
        this.mode = mode;
    }
}