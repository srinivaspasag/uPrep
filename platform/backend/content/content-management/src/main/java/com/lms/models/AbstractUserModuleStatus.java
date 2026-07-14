package com.lms.models;

import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
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
