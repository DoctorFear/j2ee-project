package com.futabus.transport.common.handler;

import com.futabus.transport.common.exception.BusinessException;
import com.futabus.transport.common.exception.CommonErrorCode;
import com.futabus.transport.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusiness(BusinessException exception, WebRequest request) {
        ErrorCode code = exception.getErrorCode();
        ProblemDetail problem = problem(code.getStatus(), code, exception.getMessage());
        return respond(exception, problem, HttpHeaders.EMPTY, request);
    }

    @ExceptionHandler({ DataIntegrityViolationException.class, ConcurrencyFailureException.class })
    public ResponseEntity<Object> handleDataIntegrity(Exception exception, WebRequest request) {
        ErrorCode code = CommonErrorCode.COMMON_DATA_CONFLICT;
        return respond(exception, problem(code.getStatus(), code), HttpHeaders.EMPTY, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception exception, WebRequest request) {
        ErrorCode code = CommonErrorCode.COMMON_INTERNAL_ERROR;
        return handleExceptionInternal(exception, null, HttpHeaders.EMPTY, code.getStatus(), request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception,
            Object body,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ProblemDetail response;
        if (status.is5xxServerError()) {
            log.error("Unhandled transport request failure (HTTP {})", status.value(), exception);
            response = problem(status, CommonErrorCode.COMMON_INTERNAL_ERROR);
        } else {
            response = switch (exception) {
                case MethodArgumentNotValidException validation -> validationProblem(status, validation);
                case HttpMessageNotReadableException ignored -> problem(status, CommonErrorCode.COMMON_INVALID_JSON);
                case MissingServletRequestParameterException missing ->
                    parameterProblem(status, CommonErrorCode.COMMON_MISSING_PARAMETER, missing.getParameterName());
                case MethodArgumentTypeMismatchException mismatch ->
                    parameterProblem(status, CommonErrorCode.COMMON_INVALID_PARAMETER, mismatch.getName());
                default -> problem(status, frameworkErrorCode(status));
            };
        }
        return respond(exception, response, headers, request);
    }

    private ResponseEntity<Object> respond(
            Exception exception,
            ProblemDetail problem,
            HttpHeaders headers,
            WebRequest request
    ) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.putAll(headers);
        responseHeaders.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        HttpStatusCode status = HttpStatusCode.valueOf(problem.getStatus());
        return super.handleExceptionInternal(exception, problem, responseHeaders, status, request);
    }

    private ProblemDetail validationProblem(HttpStatusCode status, MethodArgumentNotValidException exception) {
        ErrorCode code = CommonErrorCode.COMMON_VALIDATION_FAILED;
        Map<String, List<String>> errors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            String message = fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage()
                    : code.getMessage();
            errors.computeIfAbsent(fieldError.getField(), field -> new ArrayList<>()).add(message);
        }
        String detail = errors.entrySet()
                .stream()
                .findFirst()
                .map(field -> field.getKey() + ": " + field.getValue().getFirst())
                .orElse(code.getMessage());
        ProblemDetail problem = problem(status, code, detail);
        if (!errors.isEmpty()) {
            problem.setProperty("errors", errors);
        }
        return problem;
    }

    private ProblemDetail parameterProblem(HttpStatusCode status, ErrorCode code, String parameter) {
        ProblemDetail problem = problem(status, code, parameter + ": " + code.getMessage());
        problem.setProperty("errors", Map.of(parameter, List.of(code.getMessage())));
        return problem;
    }

    private ErrorCode frameworkErrorCode(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> CommonErrorCode.COMMON_BAD_REQUEST;
            case 404 -> CommonErrorCode.COMMON_NOT_FOUND;
            case 405 -> CommonErrorCode.COMMON_METHOD_NOT_ALLOWED;
            case 406 -> CommonErrorCode.COMMON_NOT_ACCEPTABLE;
            case 415 -> CommonErrorCode.COMMON_UNSUPPORTED_MEDIA_TYPE;
            default -> CommonErrorCode.COMMON_REQUEST_FAILED;
        };
    }

    private ProblemDetail problem(HttpStatusCode status, ErrorCode code) {
        return problem(status, code, code.getMessage());
    }

    private ProblemDetail problem(HttpStatusCode status, ErrorCode code, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setProperty("errorCode", code.name());
        return problem;
    }
}
