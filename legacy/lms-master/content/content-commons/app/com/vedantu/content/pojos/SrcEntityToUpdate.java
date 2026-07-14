package com.vedantu.content.pojos;

import com.vedantu.commons.pojos.SrcEntity;

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
        if (other.entity != null)
            return false;
    } else if (!entity.equals(other.entity))
        return false;
    return true;
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