package com.fetch.points.api.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class RestApiExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles exception thrown by path variable validation failure. (caused by @Validate)
     * @param response Outgoing validation failure response
     * @throws IOException Possible IO error from the sendErrorMethod
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public void constraintViolationException(HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * Populates outgoing validation error response with cleaner and more actionable details
     * @param ex Exception resulting from failure to validate incoming request body (caused by @Valid)
     * @param headers Headers of outgoing response
     * @param status Status of outgoing response
     * @param request Original web request
     * @return Data enriched error ResponseEntity
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request
    ) {
        Map<String, Object> errorBody = Map.of(
                "status", status.value(),
                "message", ex.getMessage(),
                "details", ex.getBindingResult().getFieldErrors(),
                "timestamp", LocalDateTime.now()
        );
        return new ResponseEntity<>(errorBody, headers, HttpStatus.BAD_REQUEST);
    }

    /**
     Populates outgoing (usually json) parsing error response with cleaner and more actionable details
     * @param ex Exception resulting from failure to parse incoming request
     * @param headers Headers of outgoing response
     * @param status Status of outgoing response
     * @param request Original web request
     * @return Data enriched error ResponseEntity
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request
    ) {
        Map<String, Object> errorBody = Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "errorMessage", (ex.getMessage() == null) ? "Message not provided" : ex.getMessage(),
                "url", request.getContextPath(),
                "timestamp", ZonedDateTime.now(ZoneOffset.UTC)
        );
        return this.handleExceptionInternal(ex, errorBody, headers, status, request);
    }
}
