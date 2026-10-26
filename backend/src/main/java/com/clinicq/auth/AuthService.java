package com.clinicq.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinicq.clinic.Clinic;
import com.clinicq.clinic.ClinicService;
import com.clinicq.common.ApiException;
import com.clinicq.common.CurrentUser;
import com.clinicq.common.Mobiles;
import com.clinicq.common.Role;
import com.clinicq.staff.AppUser;
import com.clinicq.staff.AppUserRepository;

@Service
class AuthService {

	static final Duration REFRESH_TTL = Duration.ofDays(30);

	/** A logged-in result: access token for the body, refresh token for the cookie. */
	record Session(String accessToken, String refreshToken, Me user) {
	}

	/** Exactly one of session / signupToken is set. */
	record VerifyResult(Session session, String signupToken) {
	}

	private final SecureRandom random = new SecureRandom();
	private final OtpService otp;
	private final TokenService tokens;
	private final AppUserRepository users;
	private final ClinicService clinics;
	private final RefreshTokenRepository refreshTokens;

	AuthService(OtpService otp, TokenService tokens, AppUserRepository users, ClinicService clinics,
			RefreshTokenRepository refreshTokens) {
		this.otp = otp;
		this.tokens = tokens;
		this.users = users;
		this.clinics = clinics;
		this.refreshTokens = refreshTokens;
	}

	void sendOtp(String tenDigits) {
		otp.send(Mobiles.toE164(tenDigits));
	}

	// Not transactional: a failed OTP check must still persist its attempt count.
	VerifyResult verifyOtp(String tenDigits, String code) {
		String mobile = Mobiles.toE164(tenDigits);
		otp.verify(mobile, code);
		return users.findByMobile(mobile)
			.map(user -> new VerifyResult(newSession(requireActive(user), UUID.randomUUID()), null))
			.orElseGet(() -> new VerifyResult(null, tokens.signupToken(mobile)));
	}

	@Transactional
	Session signup(String signupToken, String ownerName, String clinicName, String city) {
		String mobile = tokens.signupMobile(signupToken);
		if (users.existsByMobile(mobile)) {
			throw new ApiException(HttpStatus.CONFLICT, "mobile_already_registered", "This mobile is already registered");
		}
		Clinic clinic = clinics.create(clinicName.strip(), city.strip());
		AppUser owner = users.save(new AppUser(clinic.getId(), mobile, ownerName.strip(), Role.OWNER));
		return newSession(owner, UUID.randomUUID());
	}

	// noRollbackFor: a detected reuse must keep the family revocation even though we answer 401.
	@Transactional(noRollbackFor = ApiException.class)
	Session refresh(String rawToken) {
		RefreshToken token = findToken(rawToken);
		Instant now = Instant.now();
		if (token.getRevokedAt() != null || refreshTokens.revoke(token.getId(), now) == 0) {
			refreshTokens.revokeFamily(token.getFamilyId(), now);
			throw refreshInvalid();
		}
		if (token.getExpiresAt().isBefore(now)) {
			throw refreshInvalid();
		}
		AppUser user = users.findById(token.getUserId()).filter(AppUser::isActive).orElseThrow(AuthService::refreshInvalid);
		return newSession(user, token.getFamilyId());
	}

	@Transactional
	void logout(String rawToken) {
		if (rawToken != null) {
			refreshTokens.findByTokenHash(sha256(rawToken)).ifPresent(t -> refreshTokens.revoke(t.getId(), Instant.now()));
		}
	}

	Me me(CurrentUser current) {
		AppUser user = users.findByIdAndClinicId(current.userId(), current.clinicId())
			.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "unauthorized", "Login required"));
		return Me.of(requireActive(user), clinics.get(user.getClinicId()));
	}

	private Session newSession(AppUser user, UUID familyId) {
		byte[] bytes = new byte[32];
		random.nextBytes(bytes);
		String refresh = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		refreshTokens.save(new RefreshToken(user.getId(), sha256(refresh), familyId, Instant.now().plus(REFRESH_TTL)));
		return new Session(tokens.accessToken(user), refresh, Me.of(user, clinics.get(user.getClinicId())));
	}

	private RefreshToken findToken(String rawToken) {
		if (rawToken == null || rawToken.isBlank()) {
			throw refreshInvalid();
		}
		return refreshTokens.findByTokenHash(sha256(rawToken)).orElseThrow(AuthService::refreshInvalid);
	}

	private static AppUser requireActive(AppUser user) {
		if (!user.isActive()) {
			throw new ApiException(HttpStatus.FORBIDDEN, "user_inactive", "Your account has been deactivated");
		}
		return user;
	}

	private static ApiException refreshInvalid() {
		return new ApiException(HttpStatus.UNAUTHORIZED, "refresh_invalid", "Session expired, please log in again");
	}

	private static String sha256(String s) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}
}
