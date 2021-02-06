package com.fetch.points.api.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPointDeductionException extends RuntimeException {

    public InvalidPointDeductionException() {
        super();
    }

    public InvalidPointDeductionException(String message) {
        super(message);
    }

    public InvalidPointDeductionException(String message, Throwable cause) {
        super(message, cause);
    }
}
