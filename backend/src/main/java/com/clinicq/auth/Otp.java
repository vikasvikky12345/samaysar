package com.clinicq.auth;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
class Otp {

	@Id
	@GeneratedValue
	private UUID id;
	private String mobile;
	private String codeHash;
	private String purpose;
	private Instant expiresAt;
	private int attempts;
	private Instant usedAt;
	private Instant createdAt = Instant.now();

	protected Otp() {
	}

	Otp(String mobile, String codeHash, String purpose, Instant expiresAt) {
		this.mobile = mobile;
		this.codeHash = codeHash;
		this.purpose = purpose;
		this.expiresAt = expiresAt;
	}

	UUID getId() { return id; }
	String getCodeHash() { return codeHash; }
	Instant getExpiresAt() { return expiresAt; }
	Instant getUsedAt() { return usedAt; }
}
