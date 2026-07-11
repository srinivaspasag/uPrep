package com.lms.pojos;

import com.amazonaws.util.StringUtils;
import com.lms.common.vedantu.constants.HardCodedConstants;

public class UniqueId {

    private String idName;
    private String idValue;

    public UniqueId(final String idName, final String idValue) {
        this.idName = idName;
        this.idValue = idValue;
    }

    public String getName() {
        return idName;
    }

    public String getValue() {
        return idValue;
    }

    public void setValue(String value) {
        this.idValue = value;
    }

    @Override
    public boolean equals(Object o) {
        if (null == o || !(o instanceof UniqueId)) {
            return false;
        }
        UniqueId uId = (UniqueId) o;
        return this.hashCode() == uId.hashCode();
    }

    @Override
    public int hashCode() {
        String s = idName.isEmpty() ? HardCodedConstants.emptyString : idName + ":" + (idValue.isEmpty() ? HardCodedConstants.emptyString : idValue);
        return s.hashCode();
    }

    @Override
    public String toString() {
        return "UniqueId [idName=" + idName + ", idValue=" + idValue + "]";

    }
}
