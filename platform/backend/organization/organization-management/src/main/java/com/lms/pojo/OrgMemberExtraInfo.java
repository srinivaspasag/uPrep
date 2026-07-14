package com.lms.pojo;

import com.lms.common.vedantu.commons.pojos.requests.FieldInfo;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrgMemberExtraInfo extends FieldInfo {

    public String value;

    public OrgMemberExtraInfo() {

        super();
    }

    public OrgMemberExtraInfo(String name, String value) {

        super(name);
        this.value = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{value:").append(value).append(", name:").append(name).append("}");
        return builder.toString();
    }

}
