package com.vedantu.content.models;

import java.util.List;

// import com.vedantu.cmds.enums.ModuleRun;
// import com.vedantu.cmds.models.ModuleEntry;
import com.vedantu.mongo.VedantuBaseMongoModel;

public abstract class AbstractUserModuleStatus extends VedantuBaseMongoModel {

    public String orgId;
    public String moduleId;
    public List<ModuleEntry> children;


    public AbstractUserModuleStatus() {

        // states = new ArrayList<FileConversionState>();
    }

    public AbstractUserModuleStatus(String name, String orgId, List<ModuleEntry> children) {

        super();
        //this.name = name;
        this.orgId = orgId;
        this.children = children;
        //this.children;
//        this.published = published;
//        this.completed = completed;
    }

}
