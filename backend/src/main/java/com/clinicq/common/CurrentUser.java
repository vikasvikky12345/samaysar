package com.clinicq.common;

import java.util.UUID;

import org.springframework.security.oauth2.jwt.Jwt;

/** Tenant context. The clinic id comes only from the verified access token, never from the request. */
public record CurrentUser(UUID userId, UUID clinicId) {

	public static CurrentUser of(Jwt jwt) {
		return new CurrentUser(UUID.fromString(jwt.getSubject()), UUID.fromString(jwt.getClaimAsString("clinic_id")));
	}
}
