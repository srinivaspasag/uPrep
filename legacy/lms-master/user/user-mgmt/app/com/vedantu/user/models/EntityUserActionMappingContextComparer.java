package com.vedantu.user.models;

import com.vedantu.commons.pojos.SrcEntity;


public class EntityUserActionMappingContextComparer extends EntityUserActionMapping {

@Override
public boolean equals(Object o) {

    if (null == o || !(o instanceof SrcEntity)) {
        return false;
    }
    SrcEntity e = (SrcEntity) o;
    return context != null && context == e;
}

@Override
public int hashCode() {
    return context == null ? 0 : (context).hashCode();
}
}