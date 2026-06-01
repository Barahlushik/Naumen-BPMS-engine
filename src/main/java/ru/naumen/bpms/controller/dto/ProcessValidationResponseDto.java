package ru.naumen.bpms.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Результат проверки структуры процесса")
public record ProcessValidationResponseDto(
        @Schema(description = "Идентификатор определения процесса")
        Long processDefinitionId,

        @Schema(description = "true, если структура процесса валидна")
        boolean valid,

        @Schema(description = "Ошибки, запрещающие запуск процесса")
        List<String> errors,

        @Schema(description = "Предупреждения, не блокирующие запуск")
        List<String> warnings
) {
}
