package com.vedantu.content.models;

import java.util.ArrayList;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;
import com.vedantu.commons.enums.EntityType;

@Entity(value = "modules", noClassnameStored = true)
public class Module extends AbstractModuleModel {

    @Indexed
    public String cmdsModuleId;

    public Module() {

        super();
        this.contentType = EntityType.MODULE;
    }

    public Module(String name, String orgId, ModuleRun moduleRun, String cmdsModuleId) {

        super(name, new ArrayList<ModuleEntry>(), moduleRun);
        this.cmdsModuleId = cmdsModuleId;
    }

}
