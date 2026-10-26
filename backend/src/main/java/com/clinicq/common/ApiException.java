package com.clinicq.common;

import org.springframework.http.HttpStatus;

/** An error the client should see, rendered as ProblemDetail with {@code code}. */
public class ApiException extends RuntimeException {

	private final HttpStatus status;
	private final String code;

	public ApiException(HttpStatus status, String code, String detail) {
		super(detail);
		this.status = status;
		this.code = code;
	}

	public HttpStatus status() {
		return status;
	}

	public String code() {
		return code;
	}

	public static ApiException notFound() {
		return new ApiException(HttpStatus.NOT_FOUND, "not_found", "Not found");
	}
}
