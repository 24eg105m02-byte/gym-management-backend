package com.gym.exception;

import java.time.LocalDateTime;

public class ApiErrorResponse {
    private boolean success = false;
    private String message;
    private LocalDateTime timestamp = LocalDateTime.now();

    public ApiErrorResponse(String message) {
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
