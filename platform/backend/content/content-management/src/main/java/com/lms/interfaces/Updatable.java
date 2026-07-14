package com.lms.interfaces;

import com.lms.common.exception.VedantuException;
import com.lms.pojos.requests.EditContentReq;

public interface Updatable {

    public boolean update(EditContentReq request) throws VedantuException;
}
