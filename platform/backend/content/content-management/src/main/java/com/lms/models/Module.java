package com.lms.models;

import com.lms.common.vedantu.enums.EntityType;
import com.lms.enums.ModuleRun;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Getter
@Setter
@Document(value = "modules")
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
