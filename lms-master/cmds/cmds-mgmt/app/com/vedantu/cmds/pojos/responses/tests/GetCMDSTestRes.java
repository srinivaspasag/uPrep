package com.vedantu.cmds.pojos.responses.tests;

import java.util.List;

import com.vedantu.cmds.models.CMDSDocument;
import com.vedantu.cmds.models.event.search.details.CMDSTestSearchIndexDetails;

public class GetCMDSTestRes extends CMDSTestSearchIndexDetails {
    public CMDSDocument pdf;
    public String       pdfId;
    public String       password;
    public String       resultPassword;
    public boolean      enablePartialMarks;
    public List<String> partialMarksQTypes;
    public List<String> oneOrMoreMarksQTypes;
    public boolean      enableSectionLocking;
    public boolean      enableAutoResumeTest;
    public boolean      subjectiveTest;
    public boolean      isNTAPattern;
}
