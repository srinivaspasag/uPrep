package com.vedantu.content.pojos;

import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.content.enums.TestMode;

public class TestInfo  extends ModelBasicInfo{

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