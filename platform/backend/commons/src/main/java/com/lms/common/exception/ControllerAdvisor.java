package com.lms.common.exception;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class ControllerAdvisor {

    Logger logger =  LogManager.getLogger(ControllerAdvisor.class);

    @ExceptionHandler(VedantuException.class)
    public ResponseEntity<VedantuResponse> handleVedantuException(VedantuException ex, WebRequest request) {
        logger.error(ex);
        VedantuResponse body = new VedantuResponse(null, ex.getMessage(), ex.errorCode.name());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<VedantuResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request) {
        BindingResult bindingResult = ex.getBindingResult();
        String defaultMessage = bindingResult.getFieldError().getDefaultMessage();
        VedantuResponse body = new VedantuResponse(null, defaultMessage, VedantuErrorCode.MISSING_PARAMETERS.name());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
