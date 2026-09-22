package com.mfgops.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandardResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private List<String> errors;

    public static <T> StandardResponse<T> success(T data, String message) {
        return StandardResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> StandardResponse<T> failure(List<String> errors, String message) {
        return StandardResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .build();
    }
}
