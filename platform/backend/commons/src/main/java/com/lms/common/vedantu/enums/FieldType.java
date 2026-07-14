package com.lms.common.vedantu.enums;

import java.util.Date;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.FloatValidator;
import org.apache.commons.validator.routines.LongValidator;
import org.apache.commons.validator.routines.RegexValidator;
import org.springframework.util.StringUtils;

import com.lms.common.utils.VedantuStringUtils;

public enum FieldType {
    DECIMAL {

        public boolean validate(String value) {

            return FloatValidator.getInstance().isValid(value);

        }

        @Override
        public int compare(String value, String otherValue) {

            Float currentValue = Float.valueOf(value);
            return currentValue.compareTo(new Float(otherValue));

        }

    },
    WHOLE_NUMBER {

        public boolean validate(String value) {

            return LongValidator.getInstance().isValid(value);

        }

        @Override
        public int compare(String value, String otherValue) {

            Long currentValue = Long.getLong(value);
            return currentValue.compareTo(new Long(otherValue));

        }

    },
    EMAIL {

        public boolean validate(String value) {

            return EmailValidator.getInstance().isValid(value);

        }

    },
    TEXT {

        public boolean validate(String value) {

            return !StringUtils.isEmpty(value);

        }

    },
    DATE {

        public boolean validate(String value) {

            Date date = VedantuStringUtils.toDate(value);
            return date != null;
        }

    },
    PHONE_NO {

        public boolean validate(String value) {

            RegexValidator validator = new RegexValidator(".*");
            return validator.isValid(value);
        }

    };// , REGEX(false, true);

    public abstract boolean validate(String value);

    public int compare(String value, String otherValue) {

        return value.compareToIgnoreCase(otherValue);

    }
}
