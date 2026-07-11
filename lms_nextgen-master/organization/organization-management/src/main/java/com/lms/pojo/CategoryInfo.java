package com.lms.pojo;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;

import java.util.Set;


public class CategoryInfo implements IListResponseObj {

    public String id;
    public String name;
    public Set<String> sectionIds;
    public String description;
    public String shortDescription;
    public String thumbnail;
    public String banner;
    public int priority;

    public CategoryInfo() {

        super();
    }

    public CategoryInfo(String id, String name, Set<String> sectionIds, String description,
                        String shortDescription, int priority, String banner, String thumbnail) {
        super();
        this.id = id;
        this.name = name;
        this.sectionIds = sectionIds;
        this.description = description;
        this.shortDescription = shortDescription;
        this.priority = priority;
        this.banner = banner;
        this.thumbnail = thumbnail;
    }
}
