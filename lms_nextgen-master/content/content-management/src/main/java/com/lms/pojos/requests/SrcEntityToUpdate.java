package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SrcEntityToUpdate {
    public SrcEntity entity;

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        SrcEntityToUpdate other = (SrcEntityToUpdate) obj;
        if (entity == null) {
            return other.entity == null;
        } else return entity.equals(other.entity);
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((entity == null) ? 0 : entity.hashCode());
        return result;
    }

    @Override
    public String toString() {

        return "SrcEntityToUpdate [entity=" + entity + "]";
    }


}