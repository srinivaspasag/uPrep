package com.vedantu.commons.enums;

import org.apache.commons.validator.routines.RegexValidator;

import com.vedantu.commons.pojos.InputFieldInfo;
import com.vedantu.commons.validators.ListValidator;
import com.vedantu.ei.utils.CollectionUtils;

public enum ValidationType {
    RANGE {

        @Override
        public boolean validate(InputFieldInfo fieldInfo, String value) {

            String min = fieldInfo.valueSet.get(0);
            String max = fieldInfo.valueSet.get(1);
            int minCheck = fieldInfo.fieldType.compare(min, value);
            int maxCheck = fieldInfo.fieldType.compare(max, value);

            if (minCheck <= 0 && maxCheck >= 0) {
                return true;
            }
            return false;
        }

        @Override
        public boolean validate(InputFieldInfo fieldInfo) {

            if (CollectionUtils.isEmpty(fieldInfo.valueSet)) {

                return false;
            }

            if (fieldInfo.valueSet.size() < 2) {
                return false;
            }
            for (String value : fieldInfo.valueSet) {
                if (!fieldInfo.fieldType.validate(value)) {
                    return false;
                }

            }

            return true;
        }

    },
    LIST {

        @Override
        public boolean validate(InputFieldInfo fieldInfo, String value) {

            ListValidator validator = new ListValidator(fieldInfo);
            return validator.validate(value);

        }

        @Override
        public boolean validate(InputFieldInfo fieldInfo) {

            if (CollectionUtils.isEmpty(fieldInfo.valueSet)) {

                return false;
            }
            for (String value : fieldInfo.valueSet) {
                if (!fieldInfo.fieldType.validate(value)) {
                    return false;
                }

            }
            return true;
        }

    },
    VALUE {

        @Override
        public boolean validate(InputFieldInfo fieldInfo, String value) {

            return fieldInfo.fieldType.validate(value);

        }

        @Override
        public boolean validate(InputFieldInfo fieldInfo) {

            return true;
        }

    },
    REGEX {

        @Override
        public boolean validate(InputFieldInfo fieldInfo, String value) {

            RegexValidator validator = new RegexValidator(fieldInfo.valueSet.get(0));
            return validator.isValid(value);

        }

        @Override
        public boolean validate(InputFieldInfo fieldInfo) {

            if (CollectionUtils.isEmpty(fieldInfo.valueSet) ||  fieldInfo.valueSet.size() > 1) {

                return false;
            }
            
            return true;
        }

    };

    public abstract boolean validate(InputFieldInfo fieldInfo, String value);

    public abstract boolean validate(InputFieldInfo fieldInfo);

}
