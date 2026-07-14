package com.vedantu.cmds.pojos.requests.tests;

import java.util.ArrayList;
import java.util.List;

public class SetPasswordForTestReq {
    public String testId;
    public String password;
    public String resultPassword;
    public boolean enablePartialMarks;
    public List<String> qTypes = new ArrayList<String>();
    public List<String> oneOrMoreMarksQTypes = new ArrayList<String>();
    public boolean enableSectionLocking;
    public boolean enableAutoResumeTest;
}
