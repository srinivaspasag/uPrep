package com.vedantu.commons.content.interfaces;

import java.util.List;

import com.vedantu.commons.VedantuException;

public interface BoardUpdatable {

    public List<String> update(boolean changeState,boolean remove, List<String> brdIds) throws VedantuException;
}
