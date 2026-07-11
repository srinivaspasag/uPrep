package com.lms.common.vedantu.commons.pojos.requests;

import com.lms.common.vedantu.enums.FieldType;
import com.lms.common.vedantu.enums.ValidationType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class InputFieldInfo extends FieldInfo {

    public boolean        required;

    public FieldType fieldType;
    public ValidationType validationType = ValidationType.VALUE;

    public List<String> valueSet;
    public String         help;
    public String         placeHolder;
    public String         defaultValue;

    // TODO: add field type and valid range/list if applicable

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{required:").append(required).append(", name:").append(name).append("}");
        return builder.toString();
    }
    public boolean validate() {

        return this.validationType.validate(this);
    }

}
