package com.lms.pojos.news;

public interface IClusterable {
    int hashCode();

    @Override
    boolean equals(Object o);

    String toString();
}
