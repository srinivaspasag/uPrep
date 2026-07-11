package com.lms.common.service;

import org.springframework.stereotype.Service;
import java.io.Serializable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
@Service
public class SpringDataPageable implements Serializable, Pageable {

    // current page
    private Integer pagenumber = 1;
    // current page number
    private Integer pagesize = 10;
         // Sorting conditions
    private Sort sort;

    public void setSort(Sort sort) {
        this.sort = sort;
    }
    // The current page
    @Override
    public int getPageNumber() {
        return getPagenumber();
    }
    // The number of bars displayed per page
    @Override
    public int getPageSize() {
        return getPagesize();
    }
    // The number of pages needed to increase
    @Override
    public int getOffset() {
        return (getPagenumber() - 1) * getPagesize();
    }
    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return null;
    }

    @Override
    public Pageable previousOrFirst() {
        return null;
    }

    @Override
    public Pageable first() {
        return null;
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    public Integer getPagenumber() {
        return pagenumber;
    }
    public void setPagenumber(Integer pagenumber) {
        this.pagenumber = pagenumber;
    }
    public Integer getPagesize() {
        return pagesize;
    }
    public void setPagesize(Integer pagesize) {
        this.pagesize = pagesize;
    }
}
