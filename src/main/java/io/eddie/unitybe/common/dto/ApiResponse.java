package io.eddie.unitybe.common.dto;

public record ApiResponse<T>(boolean success, String message, T data) {

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<T>(true, message, data);
    }
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<T>(true, message, null);
    }
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<T>(true, null, data);
    }

    public static <T> ApiResponse<T> failure(String message, T data) {
        return new ApiResponse<T>(false, message, data);
    }
    public static <T> ApiResponse<Void> failure(String message) {
        return new ApiResponse<Void>(false, message, null);
    }
}
