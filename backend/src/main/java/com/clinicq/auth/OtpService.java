package com.clinicq.auth;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.SecretKey;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.clinicq.common.ApiException;

@Service
class OtpService {

	static final Duration TTL = Duration.ofMinutes(5);
	private static final Duration SEND_WINDOW = Duration.ofMinutes(15);
	private static final int MAX_SENDS = 3;
	private static final int MAX_ATTEMPTS = 5;
	private static final String PURPOSE = "LOGIN";

	private final SecureRandom random = new SecureRandom();
	private final OtpRepository otps;
	private final OtpSender sender;
	private final SecretKey key;

	OtpService(OtpRepository otps, OtpSender sender, SecretKey jwtKey) {
		this.otps = otps;
		this.sender = sender;
		this.key = jwtKey;
	}

	// shortcut: per-mobile limit only, no per-IP limit; and two parallel sends can both pass the count. Add a
	// per-IP limiter (gateway or bucket4j) before public launch.
	void send(String mobile) {
		if (otps.countByMobileAndCreatedAtAfter(mobile, Instant.now().minus(SEND_WINDOW)) >= MAX_SENDS) {
			throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "otp_rate_limited", "Too many OTP requests, try again later");
		}
		String code = "%06d".formatted(random.nextInt(1_000_000));
		otps.save(new Otp(mobile, hash(mobile, code), PURPOSE, Instant.now().plus(TTL)));
		sender.send(mobile, code);
	}

	/** Not transactional on purpose: the attempt counter must persist even when the code is wrong. */
	void verify(String mobile, String code) {
		Otp otp = otps.findFirstByMobileAndPurposeOrderByCreatedAtDesc(mobile, PURPOSE)
			.filter(o -> o.getUsedAt() == null && o.getExpiresAt().isAfter(Instant.now()))
			.orElseThrow(OtpService::invalid);
		if (otps.incrementAttempts(otp.getId(), MAX_ATTEMPTS) == 0) {
			throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "otp_attempts_exceeded", "Too many attempts, request a new OTP");
		}
		byte[] expected = otp.getCodeHash().getBytes(StandardCharsets.UTF_8);
		byte[] actual = hash(mobile, code == null ? "" : code).getBytes(StandardCharsets.UTF_8);
		if (!MessageDigest.isEqual(expected, actual) || otps.markUsed(otp.getId(), Instant.now()) == 0) {
			throw invalid();
		}
	}

	private static ApiException invalid() {
		return new ApiException(HttpStatus.BAD_REQUEST, "otp_invalid", "Wrong or expired OTP");
	}

	// Keyed hash: a plain SHA-256 of a 6-digit code is reversible by brute force from a DB dump.
	private String hash(String mobile, String code) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(key);
			return HexFormat.of().formatHex(mac.doFinal((mobile + ":" + code).getBytes(StandardCharsets.UTF_8)));
		}
		catch (GeneralSecurityException e) {
			throw new IllegalStateException(e);
		}
	}
}
