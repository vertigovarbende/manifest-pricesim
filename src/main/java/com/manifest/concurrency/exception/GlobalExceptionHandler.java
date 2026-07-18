package com.manifest.concurrency.exception;


import com.manifest.concurrency.api.response.ConErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Locale;
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
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("validation.failed", null, "Validation failed", locale);
        return error(HttpStatus.BAD_REQUEST, message, details, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ConErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.error(ex.getMessage(), ex);
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("validation.missing-parameter", new Object[]{ex.getParameterName()}, "Missing required parameter: " + ex.getParameterName(), locale);
        return error(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(SimulationAlreadyRunningException.class)
    public ResponseEntity<ConErrorResponse> handleSimulationAlreadyRunningException(SimulationAlreadyRunningException ex, HttpServletRequest request) {
        log.error(ex.getMessage(), ex);
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("simulation.error.already-running", null, ex.getMessage(), locale);
        return error(HttpStatus.CONFLICT, message, request);
    }

    @ExceptionHandler(SimulationNotFoundException.class)
    public ResponseEntity<ConErrorResponse> handleSimulationNotFoundException(SimulationNotFoundException ex, HttpServletRequest request) {
        log.error(ex.getMessage(), ex);
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("simulation.error.not-found", null, ex.getMessage(), locale);
        return error(HttpStatus.NOT_FOUND, message, request);
    }

    @ExceptionHandler(SimulationExecutionException.class)
    public ResponseEntity<ConErrorResponse> handleSimulationExecutionException(SimulationExecutionException ex, HttpServletRequest request) {
        log.error(ex.getMessage(), ex);
        Locale locale = LocaleContextHolder.getLocale();
        String key = ex.getMessage() != null && ex.getMessage().contains("interrupted") ? "simulation.error.interrupted" : "simulation.error.failed";
        String message = messageSource.getMessage(key, null, ex.getMessage(), locale);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, message, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ConErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.error(ex.getMessage(), ex);
        String message = "Invalid request parameter: " + ex.getName();
        if ("threadMode".equals(ex.getName())) {
            message = "Invalid threadMode. Allowed values: PLATFORM, VIRTUAL";
        }
        return error(HttpStatus.BAD_REQUEST, message, request);
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
