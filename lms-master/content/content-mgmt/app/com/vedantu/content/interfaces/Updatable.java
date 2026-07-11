package com.vedantu.content.interfaces;

import com.vedantu.commons.VedantuException;
import com.vedantu.content.pojos.requests.EditContentReq;

public interface Updatable {

    public boolean update(EditContentReq request) throws VedantuException;
}
