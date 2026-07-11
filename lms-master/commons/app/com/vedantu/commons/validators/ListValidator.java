package com.vedantu.commons.validators;

import com.vedantu.commons.pojos.InputFieldInfo;
import com.vedantu.ei.utils.CollectionUtils;

public class ListValidator implements IValidator {

    private InputFieldInfo info;

    public ListValidator(InputFieldInfo info) {

        this.info = info;
    }

    @Override
    public boolean validate(String value) {

        if (value == null || CollectionUtils.isEmpty(info.valueSet)) {
            return false;
        }
        for (String matchInfo : info.valueSet) {
            if (value.equalsIgnoreCase(matchInfo)) {
                return true;
            }
        }
        return false;
    }
}
