package com.vedantu.content.models.analytics;

import org.apache.commons.lang3.StringUtils;

public class AcademicDimension {

    public AcademicDimensionType type;
    public String                id;

    public AcademicDimension() {

        super();
    }

    public AcademicDimension(AcademicDimensionType type, String id) {

        super();
        this.type = type;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof AcademicDimension)) {
            return false;
        }
        AcademicDimension t = (AcademicDimension) o;
        return type == t.type && StringUtils.equals(id, t.id);
    }

    @Override
    public int hashCode() {

        return ((null == type ? AcademicDimensionType.UNKNOWN : type).name() + StringUtils
                .defaultIfEmpty(id, StringUtils.EMPTY)).hashCode();
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{type:").append(type).append(", id:").append(id).append("}");
        return builder.toString();
    }

}
