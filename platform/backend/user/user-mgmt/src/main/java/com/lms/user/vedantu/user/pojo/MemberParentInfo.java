package com.lms.user.vedantu.user.pojo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class MemberParentInfo {
    public transient static final String FIELD_NAME = "name";
    public transient static final String FIELD_CONTACTNUMBER = "contactNumber";
    public transient static final String FIELD_EMAIL = "email";
    @NotBlank(message = "name should not be nul")
    public String name;
    public String contactNumber;
    public String email;

    public String validate() {
        if (StringUtils.isEmpty(name)) {
            return "member parent name is null/empty";
        }
        return null;
    }

    @Override
    public String toString() {
        return "MemberParentInfo [name=" + name + ", contactNumber="
                + contactNumber + ", email=" + email + "]";
    }
}
