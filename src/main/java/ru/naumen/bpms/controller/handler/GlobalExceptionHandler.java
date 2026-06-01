package ru.naumen.bpms.controller.handler;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.naumen.bpms.service.exception.BpmsException;
import ru.naumen.bpms.service.exception.process.ProcessDefinitionNotFoundException;
import ru.naumen.bpms.service.exception.process.ProcessInstanceNotFoundException;
import ru.naumen.bpms.service.exception.process.StepNotFoundException;
import ru.naumen.bpms.service.exception.process.TransitionNotFoundException;
import ru.naumen.bpms.service.exception.report.ReportNotFoundException;
import ru.naumen.bpms.service.exception.user.UserAlreadyExistException;
import ru.naumen.bpms.service.exception.user.UserNotFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({
            ProcessDefinitionNotFoundException.class,
            ProcessInstanceNotFoundException.class,
            StepNotFoundException.class,
            TransitionNotFoundException.class,
            UserNotFoundException.class,
            ReportNotFoundException.class
    })
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            BpmsException ex,
            HttpServletRequest request
    ) {
        log.info("Resource was not found. path={}, errorCode={}, message={}",
                request.getRequestURI(), ex.getErrorCode(), ex.getMessage());
        return buildResponse(ex, request, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(
            UserAlreadyExistException ex,
            HttpServletRequest request
    ) {
        log.warn("Business conflict. path={}, errorCode={}, message={}",
                request.getRequestURI(), ex.getErrorCode(), ex.getMessage());
        return buildResponse(ex, request, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BpmsException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(
            BpmsException ex,
            HttpServletRequest request
    ) {
        log.warn("Business rule violation. path={}, errorCode={}, message={}",
                request.getRequestURI(), ex.getErrorCode(), ex.getMessage());
        return buildResponse(ex, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ApiErrorResponse response = new ApiErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "VALIDATION_ERROR",
                message.isBlank() ? "Ошибка валидации входных данных." : message,
                request.getRequestURI()
        );

        log.warn("Request body validation failed. path={}, message={}", request.getRequestURI(), response.message());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));

        ApiErrorResponse response = new ApiErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "VALIDATION_ERROR",
                message.isBlank() ? "Ошибка валидации параметров." : message,
                request.getRequestURI()
        );

        log.warn("Request parameter validation failed. path={}, message={}", request.getRequestURI(), response.message());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "ILLEGAL_ARGUMENT",
                ex.getMessage(),
                request.getRequestURI()
        );

        log.warn("Illegal argument. path={}, message={}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "ACCESS_DENIED",
                ex.getMessage(),
                request.getRequestURI()
        );

        log.warn("Access denied. path={}, message={}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception ex,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "INTERNAL_SERVER_ERROR",
                "Внутренняя ошибка сервера.",
                request.getRequestURI()
        );

        log.error("Unexpected server error. path={}", request.getRequestURI(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            BpmsException ex,
            HttpServletRequest request,
            HttpStatus status
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                ex.getErrorCode(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(response);
    }
}
