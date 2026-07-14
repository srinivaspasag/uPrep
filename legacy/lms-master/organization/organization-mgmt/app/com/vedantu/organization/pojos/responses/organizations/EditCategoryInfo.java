package com.vedantu.organization.pojos.responses.organizations;

import java.util.List;

import com.vedantu.commons.pojos.responses.IListResponseObj;

public class EditCategoryInfo implements IListResponseObj {

    public String       id;
    public String       name;
    public List<String> addedSectionIds;
    public List<String> removedSectionIds;

    public EditCategoryInfo() {

        super();
    }

    public EditCategoryInfo(String id, String name, List<String> addedSectionIds,
            List<String> removedSectionIds) {

        this.id = id;
        this.name = name;
        this.addedSectionIds = addedSectionIds;
        this.removedSectionIds = removedSectionIds;
    }

    @Override
    public String toString() {

        return "EditCategoryInfo [id=" + id + ", name=" + name + ", addedSectionIds="
                + addedSectionIds + ", removedSectionIds=" + removedSectionIds + "]";
    }
    
    
    
}
