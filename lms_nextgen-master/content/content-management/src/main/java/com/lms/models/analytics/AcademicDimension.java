package com.lms.models.analytics;

import com.lms.enums.AcademicDimensionType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

@Setter
@Getter
public class AcademicDimension {
    public AcademicDimensionType type;
    @Field("id")
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
        return type == t.type && (id.equalsIgnoreCase(t.id));
    }

    @Override
    public int hashCode() {

        return ((null == type ? AcademicDimensionType.UNKNOWN : type).name() +
                defaultIfEmpty(id, "")).hashCode();
    }

    public  String defaultIfEmpty(String str, String defaultStr) {
        return isEmpty(str) ? defaultStr : str;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{type:").append(type).append(", id:").append(id).append("}");
        return builder.toString();
    }
}
