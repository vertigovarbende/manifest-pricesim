package com.manifest.concurrency.exception;


import com.manifest.concurrency.api.response.ConErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Batuhan
 */

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ConErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        log.error(ex.getMessage(), ex);

        Map<String, Object> details = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (firstMessage, secondMessage) -> firstMessage
                ));


        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), details, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ConErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.error(ex.getMessage(), ex);
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(SimulationAlreadyRunningException.class)
    public ResponseEntity<ConErrorResponse> handleSimulationAlreadyRunningException(SimulationAlreadyRunningException ex, HttpServletRequest request) {
        log.error(ex.getMessage(), ex);
        return error(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(SimulationNotFoundException.class)
    public ResponseEntity<ConErrorResponse> handleSimulationNotFoundException(SimulationNotFoundException ex, HttpServletRequest request) {
        log.error(ex.getMessage(), ex);
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(SimulationExecutionException.class)
    public ResponseEntity<ConErrorResponse> handleSimulationExecutionException(SimulationExecutionException ex, HttpServletRequest request) {
        log.error(ex.getMessage(), ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ConErrorResponse> handleException(Exception ex, HttpServletRequest request) {
        log.error(ex.getMessage(), ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    private ResponseEntity<ConErrorResponse> error(HttpStatus status, String message, HttpServletRequest request) {
        return error(status, message, null, request);
    }

    private ResponseEntity<ConErrorResponse> error(HttpStatus status, String message, Map<String, Object> details, HttpServletRequest request) {
        ConErrorResponse error = ConErrorResponse.builder()
                .header(status.name())
                .message(message)
                .details(details)
                .status(status.value())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(error);
    }
}
