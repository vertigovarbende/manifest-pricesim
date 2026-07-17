package com.manifest.concurrency.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Erdem Yusuf
 * <p>I created a ConErrorResponse class to represent error responses in the Concurrency API.</p>
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ConErrorResponse {

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Builder.Default
    private String code = UUID.randomUUID().toString();

    private String header;

    private String message;

    @Builder.Default
    private final Boolean isSuccess = false;

    @Builder.Default
    private Map<String, Object> details = new HashMap<>();

    private int status;

    private String path;

}