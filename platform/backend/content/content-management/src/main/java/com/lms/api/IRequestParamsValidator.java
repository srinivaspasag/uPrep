package com.lms.api;

import com.lms.common.exception.VedantuException;

public interface IRequestParamsValidator {

    public boolean validateRequestParams() throws VedantuException;
}
