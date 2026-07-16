package com.manifest.concurrency.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author Erdem Yusuf
 * <p>Created a ConResponse class to represent responses in the Concurrency API.</p>
 */
@Getter
@Builder
public class ConResponse<T> {

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Builder.Default
    private String code = UUID.randomUUID().toString();

    private Boolean isSuccess;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T response;

    // A success response with no data
    public static <T> ConResponse<T> success(T response) {
        return ConResponse.<T>builder()
                .isSuccess(true)
                .response(response)
                .build();
    }

    // Creates a success response with the specified response object
    public static <T> ConResponse<T> successOf(final T response) {
        return ConResponse.<T>builder()
                .isSuccess(true)
                .response(response)
                .build();
    }

}
