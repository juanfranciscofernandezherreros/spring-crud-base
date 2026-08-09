package com.example.crudbase.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Standard API error response")
public class ErrorResponse {

    @Schema(description = "Time the error occurred", example = "2026-01-01T12:00:00Z")
    private final Instant timestamp;

    @Schema(description = "HTTP status code", example = "404")
    private final int status;

    @Schema(description = "HTTP status reason phrase", example = "Not Found")
    private final String error;

    @Schema(description = "Human-readable error message", example = "Result not found with id: 999")
    private final String message;

    @Schema(description = "Request path that produced the error", example = "/api/results/999")
    private final String path;

    public ErrorResponse(int status, String error, String message, String path) {
        this.timestamp = Instant.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }
}
