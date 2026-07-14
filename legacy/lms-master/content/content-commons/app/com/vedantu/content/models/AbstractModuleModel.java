package com.vedantu.content.models;

import java.util.List;

import com.google.code.morphia.annotations.PrePersist;
import com.vedantu.content.commons.interfaces.IIndexable;

public abstract class AbstractModuleModel extends AbstractContentStatsModel implements IIndexable {

    public List<ModuleEntry> children;
    public ModuleRun         moduleRun;
    public boolean           published;
    public int               totalContentCount;
    public String            prerequsiteModuleId;

    public AbstractModuleModel() {

    }

    public AbstractModuleModel(String name, List<ModuleEntry> children, ModuleRun moduleRun) {

        super();
        this.name = name;
        this.children = children;
        this.moduleRun = moduleRun;
    }

    @PrePersist
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
