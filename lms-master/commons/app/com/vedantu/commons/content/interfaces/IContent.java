package com.vedantu.commons.content.interfaces;

import java.util.List;

import com.vedantu.commons.pojos.SrcEntity;

public interface IContent {

    public List<SrcEntity> getChildren(String id);
}
