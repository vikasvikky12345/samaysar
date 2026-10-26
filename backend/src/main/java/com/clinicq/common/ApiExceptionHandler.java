package com.clinicq.common;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	static ProblemDetail problem(HttpStatusCode status, String code, String detail) {
		ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
		pd.setType(URI.create("/problems/" + code));
		pd.setProperty("code", code);
		return pd;
	}

	@ExceptionHandler(ApiException.class)
	ProblemDetail api(ApiException e) {
		return problem(e.status(), e.code(), e.getMessage());
	}

	// Concurrent signup/staff-add racing on a unique column (slug, mobile).
	@ExceptionHandler(DataIntegrityViolationException.class)
	ProblemDetail conflict(DataIntegrityViolationException e) {
		return problem(HttpStatus.CONFLICT, "conflict", "Conflicting data, please retry");
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "validation_failed", "Validation failed");
		Map<String, String> errors = new LinkedHashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(f -> errors.putIfAbsent(f.getField(), f.getDefaultMessage()));
		pd.setProperty("errors", errors);
		return ResponseEntity.badRequest().body(pd);
	}

	// Give Spring's built-in errors (bad JSON, 404, 405...) a code too.
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
			HttpStatusCode statusCode, WebRequest request) {
		if (body instanceof ProblemDetail pd && (pd.getProperties() == null || !pd.getProperties().containsKey("code"))) {
			String code = statusCode.value() == 400 ? "validation_failed"
					: HttpStatus.valueOf(statusCode.value()).name().toLowerCase();
			pd.setProperty("code", code);
		}
		return super.handleExceptionInternal(ex, body, headers, statusCode, request);
	}
}
