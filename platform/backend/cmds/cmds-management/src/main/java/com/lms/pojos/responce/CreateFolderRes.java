package com.lms.pojos.responce;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateFolderRes {
    public String id;
    public String name;
    public String parent;
    public long createdOn;
}
