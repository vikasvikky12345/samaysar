package com.clinicq.auth;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
class RefreshToken {

	@Id
	@GeneratedValue
	private UUID id;
	private UUID userId;
	private String tokenHash;
	private UUID familyId;
	private Instant expiresAt;
	private Instant revokedAt;
	private Instant createdAt = Instant.now();

	protected RefreshToken() {
	}

	RefreshToken(UUID userId, String tokenHash, UUID familyId, Instant expiresAt) {
		this.userId = userId;
		this.tokenHash = tokenHash;
		this.familyId = familyId;
		this.expiresAt = expiresAt;
	}

	UUID getId() { return id; }
	UUID getUserId() { return userId; }
	UUID getFamilyId() { return familyId; }
	Instant getExpiresAt() { return expiresAt; }
	Instant getRevokedAt() { return revokedAt; }
}
