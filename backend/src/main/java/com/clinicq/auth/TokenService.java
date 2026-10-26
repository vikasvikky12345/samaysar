package com.clinicq.auth;

import java.time.Duration;
import java.time.Instant;

import javax.crypto.SecretKey;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import com.clinicq.common.ApiException;
import com.clinicq.staff.AppUser;

/** Issues access/signup JWTs. Access tokens are verified by the resource server (see JwtConfig). */
@Service
class TokenService {

	static final Duration ACCESS_TTL = Duration.ofMinutes(15);
	private static final Duration SIGNUP_TTL = Duration.ofMinutes(15);

	private final JwtEncoder encoder;
	private final NimbusJwtDecoder signupDecoder;

	TokenService(JwtEncoder encoder, SecretKey jwtKey) {
		this.encoder = encoder;
		this.signupDecoder = NimbusJwtDecoder.withSecretKey(jwtKey).macAlgorithm(MacAlgorithm.HS256).build();
		this.signupDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefault(),
				new JwtClaimValidator<String>("purpose", "signup"::equals)));
	}

	String accessToken(AppUser user) {
		return encode(JwtClaimsSet.builder()
			.subject(user.getId().toString())
			.claim("clinic_id", user.getClinicId().toString())
			.claim("role", user.getRole().name()), ACCESS_TTL);
	}

	String signupToken(String mobile) {
		return encode(JwtClaimsSet.builder().subject(mobile).claim("purpose", "signup"), SIGNUP_TTL);
	}

	/** @return the E.164 mobile the signup token was issued for */
	String signupMobile(String token) {
		try {
			return signupDecoder.decode(token).getSubject();
		}
		catch (JwtException | IllegalArgumentException e) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "signup_token_invalid", "Signup session expired, verify OTP again");
		}
	}

	private String encode(JwtClaimsSet.Builder claims, Duration ttl) {
		Instant now = Instant.now();
		JwtClaimsSet set = claims.issuedAt(now).expiresAt(now.plus(ttl)).build();
		return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), set)).getTokenValue();
	}
}
