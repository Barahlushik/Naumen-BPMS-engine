package ru.naumen.bpms.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.naumen.bpms.service.exception.BpmsException;

import java.util.Collection;
import java.util.Optional;

@Aspect
@Component
@Slf4j
public class MethodLoggingAspect {

    @Around("""
            within(ru.naumen.bpms.controller..*) ||
            within(ru.naumen.bpms.service..*) ||
            within(ru.naumen.bpms.repository.criteria..*)
            """)
    public Object logApplicationMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String operation = joinPoint.getSignature().toShortString();
        long startedAt = System.currentTimeMillis();

        log.debug("Operation started. operation={}", operation);

        try {
            Object result = joinPoint.proceed();
            long elapsedMs = System.currentTimeMillis() - startedAt;

            log.debug("Operation completed. operation={}, elapsedMs={}, result={}",
                    operation, elapsedMs, summarizeResult(result));

            return result;
        } catch (BpmsException ex) {
            long elapsedMs = System.currentTimeMillis() - startedAt;

            log.warn("Operation rejected by business rule. operation={}, elapsedMs={}, errorCode={}, message={}",
                    operation, elapsedMs, ex.getErrorCode(), ex.getMessage());

            throw ex;
        } catch (Throwable ex) {
            long elapsedMs = System.currentTimeMillis() - startedAt;

            log.error("Operation failed unexpectedly. operation={}, elapsedMs={}", operation, elapsedMs, ex);

            throw ex;
        }
    }

    private String summarizeResult(Object result) {
        if (result == null) {
            return "null";
        }
        if (result instanceof Collection<?> collection) {
            return "Collection(size=" + collection.size() + ")";
        }
        if (result instanceof Optional<?> optional) {
            return optional.isPresent() ? "Optional(present)" : "Optional(empty)";
        }
        if (result instanceof Page<?> page) {
            return "Page(number=" + page.getNumber() + ", size=" + page.getSize()
                    + ", totalElements=" + page.getTotalElements() + ")";
        }
        if (result instanceof ResponseEntity<?> responseEntity) {
            return "ResponseEntity(status=" + responseEntity.getStatusCode().value() + ")";
        }
        return result.getClass().getSimpleName();
    }
}
