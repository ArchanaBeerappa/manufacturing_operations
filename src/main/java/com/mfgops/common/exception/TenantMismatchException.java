package com.mfgops.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TenantMismatchException extends RuntimeException {
    public TenantMismatchException(String message) {
        super(message);
    }
}
