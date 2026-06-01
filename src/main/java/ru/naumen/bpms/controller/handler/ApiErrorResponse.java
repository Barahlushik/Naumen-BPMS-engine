package ru.naumen.bpms.controller.handler;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Единый формат ошибки API")
public record ApiErrorResponse(
        @Schema(description = "Дата и время ошибки")
        LocalDateTime timestamp,

        @Schema(description = "HTTP status code", example = "400")
        int status,

        @Schema(description = "HTTP status reason", example = "Bad Request")
        String error,

        @Schema(description = "Внутренний код ошибки", example = "VALIDATION_ERROR")
        String errorCode,

        @Schema(description = "Сообщение об ошибке")
        String message,

        @Schema(description = "Путь запроса", example = "/api/process-instances/start")
        String path
) {
}
