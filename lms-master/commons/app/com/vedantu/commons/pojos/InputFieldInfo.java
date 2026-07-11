package com.vedantu.commons.pojos;

import java.util.List;

import com.google.code.morphia.annotations.Embedded;
import com.vedantu.commons.enums.FieldType;
import com.vedantu.commons.enums.ValidationType;

@Embedded
public class InputFieldInfo extends FieldInfo {

    public boolean        required;

    public FieldType      fieldType;
    public ValidationType validationType = ValidationType.VALUE;

    public List<String>   valueSet;
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
