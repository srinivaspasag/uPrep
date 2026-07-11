package com.lms.models;

import com.lms.enums.ModuleRun;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public abstract class AbstractModuleModel extends AbstractContentStatsModel {

    public List<ModuleEntry> children;
    public ModuleRun moduleRun;
    public boolean published;
    public int totalContentCount;
    public String prerequsiteModuleId;

    public AbstractModuleModel() {

    }

    public AbstractModuleModel(String name, List<ModuleEntry> children, ModuleRun moduleRun) {

        super();
        this.name = name;
        this.children = children;
        this.moduleRun = moduleRun;
    }


    protected void prePersist() {

        super.prePersist();
        if (children != null) {
            int totalContentCount = 0;
            for (ModuleEntry mEntry : children) {
                if (mEntry.entity != null) {
                    totalContentCount++;
                }
            }
            this.totalContentCount = totalContentCount;
        }

    }

}
