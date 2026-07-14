package com.lms.pojo;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EditCategoryInfo implements IListResponseObj {

    public String id;
    public String name;
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
